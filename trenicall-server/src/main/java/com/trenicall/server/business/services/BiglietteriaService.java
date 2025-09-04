package com.trenicall.server.business.services;

import com.trenicall.server.business.patterns.builder.RicercaBiglietti;
import com.trenicall.server.business.patterns.command.CommandManager;
import com.trenicall.server.business.patterns.command.commands.AcquistoBigliettoCommand;
import com.trenicall.server.business.patterns.command.commands.ModificaBigliettoCommand;
import com.trenicall.server.business.patterns.factory.BigliettoFactory;
import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.entities.Cliente;
import com.trenicall.server.domain.entities.DisponibilitaTreno;
import com.trenicall.server.domain.entities.Treno;
import com.trenicall.server.domain.repositories.BigliettoRepository;
import com.trenicall.server.domain.repositories.ClienteRepository;
import com.trenicall.server.domain.repositories.DisponibilitaTrenoRepository;
import com.trenicall.server.domain.repositories.TrenoRepository;
import com.trenicall.server.domain.valueobjects.TipoBiglietto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class BiglietteriaService {

    private final BigliettoFactory factory;
    private final CommandManager commandManager = new CommandManager();
    private final BigliettoRepository bigliettoRepository;
    private final ClienteRepository clienteRepository;
    private final PromozioneService promozioneService;
    private final TrenoRepository trenoRepository;
    private final DisponibilitaTrenoRepository disponibilitaTrenoRepository;

    public BiglietteriaService(BigliettoFactory factory,
                               BigliettoRepository bigliettoRepository,
                               ClienteRepository clienteRepository,
                               PromozioneService promozioneService,
                               TrenoRepository trenoRepository,
                               DisponibilitaTrenoRepository disponibilitaTrenoRepository) {
        this.factory = factory;
        this.bigliettoRepository = bigliettoRepository;
        this.clienteRepository = clienteRepository;
        this.promozioneService = promozioneService;
        this.trenoRepository = trenoRepository;
        this.disponibilitaTrenoRepository = disponibilitaTrenoRepository;
    }

    public List<Biglietto> ricerca(RicercaBiglietti ricerca) {
        return bigliettoRepository.findByPartenzaAndArrivoAndDataViaggio(
                ricerca.getPartenza(),
                ricerca.getArrivo(),
                ricerca.getDataViaggio()
        );
    }

    public Biglietto acquista(String clienteId, TipoBiglietto tipo, String partenza,
                              String arrivo, LocalDateTime dataViaggio, Integer distanzaKm) {

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente non trovato: " + clienteId));

        Biglietto biglietto = factory.creaBiglietto(
                tipo, partenza, arrivo, dataViaggio, distanzaKm, clienteId
        );

        Treno treno = trenoRepository.findById(biglietto.getTrenoAssociato())
                .orElseThrow(() -> new IllegalArgumentException("Treno non trovato: " + biglietto.getTrenoAssociato()));

        DisponibilitaTreno disponibilita = disponibilitaTrenoRepository
                .findByTrenoAndDataViaggio(treno, dataViaggio.toLocalDate())
                .orElseGet(() -> new DisponibilitaTreno(treno, dataViaggio.toLocalDate(), treno.getPostiTotali()));

        disponibilita.prenotaPosti(1);
        disponibilitaTrenoRepository.save(disponibilita);

        commandManager.executeCommand(new AcquistoBigliettoCommand(biglietto));
        double prezzoFinale = promozioneService.applicaPromozioni(biglietto, cliente.isFedelta());
        biglietto.setPrezzo(prezzoFinale);

        return bigliettoRepository.save(biglietto);
    }


    public Biglietto modifica(Biglietto biglietto, LocalDateTime nuovaData) {
        commandManager.executeCommand(new ModificaBigliettoCommand(biglietto, nuovaData));

        Cliente cliente = clienteRepository.findById(biglietto.getClienteId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente non trovato: " + biglietto.getClienteId()));
        double prezzoFinale = promozioneService.applicaPromozioni(biglietto, cliente.isFedelta());
        biglietto.setPrezzo(prezzoFinale);

        return bigliettoRepository.save(biglietto);
    }

    public List<Biglietto> getArchivioBiglietti() {
        return bigliettoRepository.findAll();
    }
}
