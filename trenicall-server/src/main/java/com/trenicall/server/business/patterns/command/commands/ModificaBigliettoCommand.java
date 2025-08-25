package com.trenicall.server.business.patterns.command.commands;

import com.trenicall.server.business.patterns.command.Command;
import com.trenicall.server.domain.entities.Biglietto;

import java.time.LocalDateTime;

public class ModificaBigliettoCommand implements Command {
    private final Biglietto biglietto;
    private LocalDateTime nuovaData;
    private LocalDateTime dataPrecedente;

    public ModificaBigliettoCommand(Biglietto biglietto, LocalDateTime nuovaData) {
        this.biglietto = biglietto;
        this.nuovaData = nuovaData;
    }

    @Override
    public void execute() {
        dataPrecedente = biglietto.getDataViaggio();
        biglietto.setDataViaggio(nuovaData);
    }

    @Override
    public void undo() {
        if (dataPrecedente != null) {
            biglietto.setDataViaggio(dataPrecedente);
        }
    }
}
