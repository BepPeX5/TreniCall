package com.trenicall.server.business.services;

import com.trenicall.server.business.patterns.command.CommandManager;
import com.trenicall.server.business.patterns.command.commands.PrenotazioneCommand;
import com.trenicall.server.business.patterns.factory.BigliettoFactory;
import com.trenicall.server.business.patterns.factory.BigliettoFactoryImpl;
import com.trenicall.server.business.patterns.state.states.StatoScaduto;
import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.valueobjects.TipoBiglietto;

import java.time.LocalDateTime;
import java.util.*;

public class PrenotazioneService {
    private final BigliettoFactory factory = new BigliettoFactoryImpl();
    private final CommandManager commandManager = new CommandManager();
    private final Map<String, Biglietto> prenotazioni = new HashMap<>();
    private final Map<String, LocalDateTime> scadenze = new HashMap<>();

    public Biglietto creaPrenotazione(String clienteId, TipoBiglietto tipo, String partenza, String arrivo,
                                      LocalDateTime dataViaggio, Integer distanzaKm, int minutiValidita) {
        Biglietto biglietto = factory.creaBiglietto(tipo, partenza, arrivo, dataViaggio, distanzaKm, clienteId);
        PrenotazioneCommand cmd = new PrenotazioneCommand(biglietto);
        commandManager.executeCommand(cmd);
        prenotazioni.put(biglietto.getId(), biglietto);
        scadenze.put(biglietto.getId(), LocalDateTime.now().plusMinutes(minutiValidita));
        return biglietto;
    }

    public void verificaScadenze() {
        LocalDateTime now = LocalDateTime.now();
        List<String> scadute = new ArrayList<>();
        for (Map.Entry<String, LocalDateTime> entry : scadenze.entrySet()) {
            if (entry.getValue().isBefore(now)) {
                Biglietto b = prenotazioni.get(entry.getKey());
                if (b != null) {
                    b.setStato(new StatoScaduto());
                    scadute.add(entry.getKey());
                }
            }
        }
        for (String id : scadute) {
            scadenze.remove(id);
            prenotazioni.remove(id);
        }
    }

    public Biglietto confermaAcquisto(String prenotazioneId, BiglietteriaService biglietteriaService) {
        Biglietto prenotato = prenotazioni.get(prenotazioneId);
        if (prenotato == null) {
            throw new IllegalStateException("Prenotazione non trovata o scaduta");
        }
        Biglietto acquistato = biglietteriaService.acquista(
                prenotato.getClienteId(),
                prenotato.getTipo(),
                prenotato.getPartenza(),
                prenotato.getArrivo(),
                prenotato.getDataViaggio(),
                prenotato.getDistanzaKm()
        );
        prenotazioni.remove(prenotazioneId);
        scadenze.remove(prenotazioneId);
        return acquistato;
    }

    public Collection<Biglietto> getPrenotazioniAttive() {
        return prenotazioni.values();
    }
}
