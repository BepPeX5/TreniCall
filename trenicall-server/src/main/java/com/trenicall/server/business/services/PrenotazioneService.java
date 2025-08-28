package com.trenicall.server.business.services;

import com.trenicall.server.business.patterns.command.CommandManager;
import com.trenicall.server.business.patterns.command.commands.PrenotazioneCommand;
import com.trenicall.server.business.patterns.factory.BigliettoFactory;
import com.trenicall.server.business.patterns.factory.BigliettoFactoryImpl;
import com.trenicall.server.business.patterns.state.states.StatoScaduto;
import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.entities.Prenotazione;
import com.trenicall.server.domain.repositories.BigliettoRepository;
import com.trenicall.server.domain.repositories.PrenotazioneRepository;
import com.trenicall.server.domain.valueobjects.TipoBiglietto;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
@Transactional
public class PrenotazioneService {

    private final BigliettoFactory factory = new BigliettoFactoryImpl();
    private final CommandManager commandManager = new CommandManager();
    private final PrenotazioneRepository prenotazioneRepository;
    private final BigliettoRepository bigliettoRepository;

    public PrenotazioneService(PrenotazioneRepository prenotazioneRepository, BigliettoRepository bigliettoRepository) {
        this.prenotazioneRepository = prenotazioneRepository;
        this.bigliettoRepository = bigliettoRepository;
    }

    public Prenotazione creaPrenotazione(String clienteId, TipoBiglietto tipo, String partenza, String arrivo,
                                         LocalDateTime dataViaggio, Integer distanzaKm, int minutiValidita) {
        Biglietto biglietto = factory.creaBiglietto(tipo, partenza, arrivo, dataViaggio, distanzaKm, clienteId);
        commandManager.executeCommand(new PrenotazioneCommand(biglietto));
        bigliettoRepository.save(biglietto);
        Prenotazione prenotazione = new Prenotazione(
                biglietto.getId(), null, null, LocalDateTime.now(), minutiValidita, biglietto
        );
        return prenotazioneRepository.save(prenotazione);
    }

    public void verificaScadenze() {
        LocalDateTime now = LocalDateTime.now();
        List<Prenotazione> prenotazioni = prenotazioneRepository.findAll();
        for (Prenotazione p : prenotazioni) {
            if (!p.isAttiva()) {
                p.getBiglietto().setStato(new StatoScaduto());
                prenotazioneRepository.delete(p);
            }
        }
    }

    public Biglietto confermaAcquisto(String prenotazioneId, BiglietteriaService biglietteriaService) {
        Prenotazione prenotazione = prenotazioneRepository.findById(prenotazioneId)
                .orElseThrow(() -> new IllegalStateException("Prenotazione non trovata o scaduta"));

        Biglietto prenotato = prenotazione.getBiglietto();
        Biglietto acquistato = biglietteriaService.acquista(
                prenotato.getClienteId(),
                prenotato.getTipo(),
                prenotato.getPartenza(),
                prenotato.getArrivo(),
                prenotato.getDataViaggio(),
                prenotato.getDistanzaKm()
        );

        prenotazioneRepository.delete(prenotazione);
        return acquistato;
    }

    public Collection<Prenotazione> getPrenotazioniAttive() {
        return prenotazioneRepository.findAll();
    }
}
