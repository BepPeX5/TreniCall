package com.trenicall.server.business.services;

import com.trenicall.server.business.patterns.builder.RicercaBiglietti;
import com.trenicall.server.business.patterns.command.CommandManager;
import com.trenicall.server.business.patterns.command.commands.AcquistoBigliettoCommand;
import com.trenicall.server.business.patterns.command.commands.ModificaBigliettoCommand;
import com.trenicall.server.business.patterns.factory.BigliettoFactory;
import com.trenicall.server.business.patterns.factory.BigliettoFactoryImpl;
import com.trenicall.server.business.patterns.state.states.StatoPrenotato;
import com.trenicall.server.business.patterns.strategy.PricingContext;
import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.repositories.BigliettoRepository;
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
    private final PricingContext pricingContext = new PricingContext();
    private final BigliettoRepository bigliettoRepository;


    public BiglietteriaService(BigliettoFactory factory,
                               BigliettoRepository bigliettoRepository) {
        this.factory = factory;
        this.bigliettoRepository = bigliettoRepository;
    }

    public List<Biglietto> ricerca(RicercaBiglietti ricerca) {
        return bigliettoRepository.findByPartenzaAndArrivoAndDataViaggio(
                ricerca.getPartenza(), ricerca.getArrivo(), ricerca.getDataViaggio()
        );
    }

    public Biglietto acquista(String clienteId, TipoBiglietto tipo, String partenza, String arrivo,
                              LocalDateTime dataViaggio, Integer distanzaKm) {
        Biglietto biglietto = factory.creaBiglietto(tipo, partenza, arrivo, dataViaggio, distanzaKm, clienteId);
        commandManager.executeCommand(new AcquistoBigliettoCommand(biglietto));
        biglietto.setPrezzo(pricingContext.calcolaPrezzoFinale(biglietto));
        return bigliettoRepository.save(biglietto);
    }

    public Biglietto modifica(Biglietto biglietto, LocalDateTime nuovaData) {
        commandManager.executeCommand(new ModificaBigliettoCommand(biglietto, nuovaData));
        biglietto.setPrezzo(pricingContext.calcolaPrezzoFinale(biglietto));
        return bigliettoRepository.save(biglietto);
    }

    public List<Biglietto> getArchivioBiglietti() {
        return bigliettoRepository.findAll();
    }
}

