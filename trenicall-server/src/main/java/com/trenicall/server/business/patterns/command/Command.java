package com.trenicall.server.business.patterns.command;

import java.time.LocalDateTime;

public interface Command {

    void execute();

    void undo();

    boolean canUndo();

    String getCommandType();

    String getDescription();

    LocalDateTime getTimestamp();

    String getCommandId();

    Object getTarget();
}