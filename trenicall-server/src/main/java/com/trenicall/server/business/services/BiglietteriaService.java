package com.trenicall.server.business.services;

import com.trenicall.server.business.patterns.builder.RicercaBiglietti;
import com.trenicall.server.business.patterns.command.commands.AcquistoBigliettoCommand;
import com.trenicall.server.business.patterns.command.CommandManager;
import com.trenicall.server.business.patterns.command.commands.ModificaBigliettoCommand;
import com.trenicall.server.business.patterns.factory.BigliettoFactory;
import com.trenicall.server.business.patterns.factory.BigliettoFactoryImpl;
import com.trenicall.server.business.patterns.state.states.StatoPrenotato;
import com.trenicall.server.business.patterns.strategy.PricingContext;
import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.valueobjects.TipoBiglietto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BiglietteriaService {
    private final BigliettoFactory factory = new BigliettoFactoryImpl();
    private final CommandManager commandManager = new CommandManager();
    private final PricingContext pricingContext = new PricingContext();
    private final List<Biglietto> archivioBiglietti = new ArrayList<>();

    public List<Biglietto> ricerca(RicercaBiglietti ricerca) {
        List<Biglietto> risultati = new ArrayList<>();
        risultati.add(new Biglietto(
                "TMP-" + System.currentTimeMillis(),
                "CLIENTE_TMP",
                new StatoPrenotato(),
                TipoBiglietto.FRECCIA_ROSSA,
                ricerca.getPartenza(),
                ricerca.getArrivo(),
                ricerca.getDataViaggio(),
                500
        ));
        return risultati;
    }

    public Biglietto acquista(String clienteId, TipoBiglietto tipo, String partenza, String arrivo,
                              LocalDateTime dataViaggio, Integer distanzaKm) {
        Biglietto biglietto = factory.creaBiglietto(tipo, partenza, arrivo, dataViaggio, distanzaKm, clienteId);
        AcquistoBigliettoCommand cmd = new AcquistoBigliettoCommand(biglietto);
        commandManager.executeCommand(cmd);
        double prezzoFinale = pricingContext.calcolaPrezzoFinale(biglietto);
        biglietto.setPrezzo(prezzoFinale);
        archivioBiglietti.add(biglietto);
        return biglietto;
    }

    public Biglietto modifica(Biglietto biglietto, LocalDateTime nuovaData) {
        ModificaBigliettoCommand cmd = new ModificaBigliettoCommand(biglietto, nuovaData);
        commandManager.executeCommand(cmd);
        double prezzoFinale = pricingContext.calcolaPrezzoFinale(biglietto);
        biglietto.setPrezzo(prezzoFinale);
        return biglietto;
    }

    public List<Biglietto> getArchivioBiglietti() {
        return archivioBiglietti;
    }
}
