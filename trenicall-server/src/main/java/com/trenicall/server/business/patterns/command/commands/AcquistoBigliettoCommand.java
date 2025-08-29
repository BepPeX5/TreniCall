package com.trenicall.server.business.patterns.command.commands;

import com.trenicall.server.business.patterns.command.Command;
import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.business.patterns.state.states.StatoPagato;
import com.trenicall.server.business.patterns.state.states.StatoPrenotato;

public class AcquistoBigliettoCommand implements Command {
    private final Biglietto biglietto;
    private StatoPrenotato statoPrecedente;

    public AcquistoBigliettoCommand(Biglietto biglietto) {
        if (biglietto == null) {
            throw new IllegalArgumentException("Il biglietto non può essere null");
        }
        this.biglietto = biglietto;
    }

    @Override
    public void execute() {
        if (biglietto.getStato() instanceof StatoPrenotato) {
            statoPrecedente = (StatoPrenotato) biglietto.getStato();
            biglietto.setStato(new StatoPagato());
        } else {
            throw new IllegalStateException("Il biglietto deve essere in stato PRENOTATO per poter essere acquistato");
        }
    }

    @Override
    public void undo() {
        if (statoPrecedente != null) {
            biglietto.setStato(statoPrecedente);
        }
    }
}
