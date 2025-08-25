package com.trenicall.server.business.patterns.command;

public interface Command {
    void execute();
    void undo();
}
