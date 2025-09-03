package com.trenicall.server.business.services;

import com.trenicall.server.business.patterns.command.CommandManager;
import com.trenicall.server.business.patterns.command.commands.PrenotazioneCommand;
import com.trenicall.server.business.patterns.factory.BigliettoFactory;
import com.trenicall.server.business.patterns.factory.BigliettoFactoryImpl;
import com.trenicall.server.business.patterns.state.states.StatoScaduto;
import com.trenicall.server.business.patterns.state.states.StatoPagato;
import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.entities.Cliente;
import com.trenicall.server.domain.entities.Prenotazione;
import com.trenicall.server.domain.entities.Treno;
import com.trenicall.server.domain.repositories.BigliettoRepository;
import com.trenicall.server.domain.repositories.PrenotazioneRepository;
import com.trenicall.server.domain.repositories.ClienteRepository;
import com.trenicall.server.domain.repositories.TrenoRepository;
import com.trenicall.server.domain.valueobjects.TipoBiglietto;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PrenotazioneService {

    private final BigliettoFactory factory;
    private final CommandManager commandManager = new CommandManager();
    private final PrenotazioneRepository prenotazioneRepository;
    private final BigliettoRepository bigliettoRepository;
    private final ClienteRepository clienteRepository;
    private final TrenoRepository trenoRepository;

    private static final int MINUTI_SCADENZA = 10;


    public PrenotazioneService(PrenotazioneRepository prenotazioneRepository,
                               BigliettoRepository bigliettoRepository,
                               ClienteRepository clienteRepository,
                               TrenoRepository trenoRepository,
                               BigliettoFactory factory) {
        this.prenotazioneRepository = prenotazioneRepository;
        this.bigliettoRepository = bigliettoRepository;
        this.clienteRepository = clienteRepository;
        this.trenoRepository = trenoRepository;
        this.factory = factory;
    }

    public Prenotazione creaPrenotazione(String clienteId, TipoBiglietto tipo, String partenza, String arrivo,
                                         LocalDateTime dataViaggio, Integer distanzaKm, int minutiValidita) {

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalStateException("Cliente non trovato"));

        Biglietto biglietto = factory.creaBiglietto(tipo, partenza, arrivo, dataViaggio, distanzaKm, clienteId);
        commandManager.executeCommand(new PrenotazioneCommand(biglietto));
        bigliettoRepository.save(biglietto);

        String prenotazioneId = "PR-" + UUID.randomUUID().toString().substring(0, 8);

        Prenotazione prenotazione = new Prenotazione(
                prenotazioneId, cliente, null, LocalDateTime.now(), MINUTI_SCADENZA, biglietto
        );
        return prenotazioneRepository.save(prenotazione);
    }

    public void verificaScadenze() {
        LocalDateTime now = LocalDateTime.now();
        List<Prenotazione> prenotazioni = prenotazioneRepository.findAll();
        for (Prenotazione p : prenotazioni) {
            if (!p.isAttiva()) {
                p.getBiglietto().setStato(new StatoScaduto());
                p.scaduta();
                prenotazioneRepository.delete(p);
                bigliettoRepository.delete(p.getBiglietto());
            }
        }
    }

    public Biglietto confermaAcquisto(String prenotazioneId, BiglietteriaService biglietteriaService) {
        Prenotazione prenotazione = prenotazioneRepository.findById(prenotazioneId)
                .orElseThrow(() -> new IllegalStateException("Prenotazione non trovata o scaduta"));

        if (!prenotazione.isAttiva()) {
            throw new IllegalStateException("Prenotazione scaduta");
        }

        Biglietto prenotato = prenotazione.getBiglietto();
        prenotato.setStato(new StatoPagato());
        bigliettoRepository.save(prenotato);

        decrementaPostiTreno(prenotato.getPartenza(), prenotato.getArrivo());

        prenotazioneRepository.deleteById(prenotazioneId);
        return prenotato;
    }

    public Collection<Prenotazione> getPrenotazioniAttive() {
        verificaScadenze();
        return prenotazioneRepository.findByAttivaTrue();
    }

    public Collection<Prenotazione> getPrenotazioniAttiveByCliente(String clienteId) {
        verificaScadenze();
        List<Prenotazione> prenotazioni = prenotazioneRepository.findByClienteId(clienteId);
        return prenotazioni.stream()
                .filter(p -> p.isAttiva() && prenotazioneRepository.existsById(p.getId()))
                .toList();
    }

    private void decrementaPostiTreno(String partenza, String arrivo) {
        List<Treno> treni = trenoRepository.findAll();
        for (Treno treno : treni) {
            if (treno.getTratta() != null &&
                    treno.getTratta().getStazionePartenza().equals(partenza) &&
                    treno.getTratta().getStazioneArrivo().equals(arrivo)) {
                if (treno.getPostiDisponibili() > 0) {
                    treno.prenotaPosti(1);
                    trenoRepository.save(treno);
                    break;
                }
            }
        }
    }
}