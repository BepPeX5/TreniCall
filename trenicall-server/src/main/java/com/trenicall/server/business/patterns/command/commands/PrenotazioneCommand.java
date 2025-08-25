package com.trenicall.server.business.patterns.command.commands;

import com.trenicall.server.business.patterns.command.Command;
import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.business.patterns.state.states.StatoPrenotato;
import com.trenicall.server.business.patterns.state.states.StatoScaduto;

public class PrenotazioneCommand implements Command {
    private final Biglietto biglietto;

    public PrenotazioneCommand(Biglietto biglietto) {
        this.biglietto = biglietto;
    }

    @Override
    public void execute() {
        biglietto.setStato(new StatoPrenotato());
    }

    @Override
    public void undo() {
        biglietto.setStato(new StatoScaduto());
    }
}
