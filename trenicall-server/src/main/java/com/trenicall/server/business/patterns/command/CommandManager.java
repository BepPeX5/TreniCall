package com.trenicall.server.business.patterns.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class CommandManager {

    private final Stack<Command> commandHistory;
    private final Stack<Command> undoHistory;
    private final List<CommandListener> listeners;
    private final int maxHistorySize;

    public CommandManager() {
        this.commandHistory = new Stack<>();
        this.undoHistory = new Stack<>();
        this.listeners = new ArrayList<>();
        this.maxHistorySize = 50; // Limite cronologia
    }

    public CommandManager(int maxHistorySize) {
        this.commandHistory = new Stack<>();
        this.undoHistory = new Stack<>();
        this.listeners = new ArrayList<>();
        this.maxHistorySize = maxHistorySize;
    }

    public void execute(Command command) {
        try {
            System.out.println("⚡ Eseguendo comando: " + command.getDescription());

            command.execute();

            commandHistory.push(command);

            undoHistory.clear();

            if (commandHistory.size() > maxHistorySize) {
                commandHistory.removeElementAt(0);
            }

            notifyListeners("EXECUTE", command);

            System.out.println("✅ Comando eseguito con successo: " + command.getCommandId());

        } catch (Exception e) {
            System.err.println("❌ Errore eseguendo comando: " + e.getMessage());

            // Notifica listener dell'errore
            notifyListeners("ERROR", command);

            throw new CommandExecutionException("Errore esecuzione comando: " + command.getDescription(), e);
        }
    }

    public boolean undo() {
        if (commandHistory.isEmpty()) {
            System.out.println("❌ Nessun comando da annullare");
            return false;
        }

        Command lastCommand = commandHistory.pop();

        if (!lastCommand.canUndo()) {
            System.out.println("❌ Comando non può essere annullato: " + lastCommand.getDescription());
            commandHistory.push(lastCommand); // Rimetti nella cronologia
            return false;
        }

        try {
            System.out.println("🔄 Annullando comando: " + lastCommand.getDescription());

            lastCommand.undo();

            undoHistory.push(lastCommand);

            notifyListeners("UNDO", lastCommand);

            System.out.println("✅ Comando annullato: " + lastCommand.getCommandId());
            return true;

        } catch (Exception e) {
            System.err.println("❌ Errore annullando comando: " + e.getMessage());

            commandHistory.push(lastCommand);

            notifyListeners("UNDO_ERROR", lastCommand);

            throw new CommandExecutionException("Errore annullamento comando: " + lastCommand.getDescription(), e);
        }
    }

    public boolean redo() {
        if (undoHistory.isEmpty()) {
            System.out.println("❌ Nessun comando da ripetere");
            return false;
        }

        Command commandToRedo = undoHistory.pop();

        try {
            System.out.println("🔁 Ripetendo comando: " + commandToRedo.getDescription());

            commandToRedo.execute();

            commandHistory.push(commandToRedo);

            notifyListeners("REDO", commandToRedo);

            System.out.println("✅ Comando ripetuto: " + commandToRedo.getCommandId());
            return true;

        } catch (Exception e) {
            System.err.println("❌ Errore ripetendo comando: " + e.getMessage());

            undoHistory.push(commandToRedo);

            throw new CommandExecutionException("Errore ripetizione comando: " + commandToRedo.getDescription(), e);
        }
    }

    public void executeBatch(List<Command> commands) {
        System.out.println("📦 Eseguendo batch di " + commands.size() + " comandi");

        List<Command> executedCommands = new ArrayList<>();

        try {
            for (Command command : commands) {
                command.execute();
                executedCommands.add(command);
                commandHistory.push(command);
            }

            undoHistory.clear();
            System.out.println("✅ Batch completato con successo");

        } catch (Exception e) {
            System.err.println("❌ Errore nel batch, annullando comandi eseguiti...");

            for (int i = executedCommands.size() - 1; i >= 0; i--) {
                Command command = executedCommands.get(i);
                try {
                    command.undo();
                    commandHistory.remove(command);
                } catch (Exception undoError) {
                    System.err.println("❌ Errore annullando comando nel batch: " + undoError.getMessage());
                }
            }

            throw new CommandExecutionException("Errore batch execution", e);
        }
    }

    public void clearHistory() {
        commandHistory.clear();
        undoHistory.clear();
        System.out.println("🧹 Cronologia comandi pulita");
    }

    public List<Command> getCommandHistory() {
        return new ArrayList<>(commandHistory);
    }

    public List<Command> getUndoHistory() {
        return new ArrayList<>(undoHistory);
    }

    public boolean canUndo() {
        return !commandHistory.isEmpty() && commandHistory.peek().canUndo();
    }

    public boolean canRedo() {
        return !undoHistory.isEmpty();
    }

    public Command getLastCommand() {
        return commandHistory.isEmpty() ? null : commandHistory.peek();
    }

    public void addCommandListener(CommandListener listener) {
        listeners.add(listener);
    }

    public void removeCommandListener(CommandListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(String action, Command command) {
        for (CommandListener listener : listeners) {
            try {
                listener.onCommandAction(action, command);
            } catch (Exception e) {
                System.err.println("❌ Errore notificando listener: " + e.getMessage());
            }
        }
    }

    public int getHistorySize() {
        return commandHistory.size();
    }

    public int getUndoHistorySize() {
        return undoHistory.size();
    }

    // Interfaccia per listener
    public interface CommandListener {
        void onCommandAction(String action, Command command);
    }

    // Eccezione custom
    public static class CommandExecutionException extends RuntimeException {
        public CommandExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @Override
    public String toString() {
        return "CommandManager{" +
                "history=" + commandHistory.size() +
                ", undoHistory=" + undoHistory.size() +
                ", canUndo=" + canUndo() +
                ", canRedo=" + canRedo() +
                '}';
    }
}
