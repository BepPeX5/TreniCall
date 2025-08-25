package com.trenicall.server.business.patterns.command.commands;

import com.trenicall.server.business.patterns.command.Command;
import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.business.patterns.state.states.StatoRimborsato;
import com.trenicall.server.business.patterns.state.states.StatoPagato;

public class AnnullaBigliettoCommand implements Command {
    private final Biglietto biglietto;
    private StatoPagato statoPrecedente;

    public AnnullaBigliettoCommand(Biglietto biglietto) {
        this.biglietto = biglietto;
    }

    @Override
    public void execute() {
        if (biglietto.getStato() instanceof StatoPagato) {
            statoPrecedente = (StatoPagato) biglietto.getStato();
            biglietto.setStato(new StatoRimborsato());
        }
    }

    @Override
    public void undo() {
        if (statoPrecedente != null) {
            biglietto.setStato(statoPrecedente);
        }
    }
}
