package com.trenicall.server.business.patterns.command.commands;

import com.trenicall.server.business.patterns.command.Command;
import com.trenicall.server.business.patterns.state.states.StatoPagato;
import com.trenicall.server.business.patterns.state.states.StatoPrenotato;
import com.trenicall.server.domain.entities.Biglietto;
import java.time.LocalDateTime;
import java.util.UUID;

public class AcquistoBigliettoCommand implements Command {

    private final String commandId;
    private final Biglietto biglietto;
    private final Object cliente;
    private final String metodoPagamento;
    private final LocalDateTime timestamp;
    private String transactionId;
    private Object statoOriginaleBiglietto;
    private boolean executed;

    public AcquistoBigliettoCommand(Biglietto biglietto, Object cliente, String metodoPagamento) {
        this.commandId = "ACQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.biglietto = biglietto;
        this.cliente = cliente;
        this.metodoPagamento = metodoPagamento;
        this.timestamp = LocalDateTime.now();
        this.executed = false;
    }

    @Override
    public void execute() {
        if (executed) {
            throw new IllegalStateException("Comando già eseguito: " + commandId);
        }

        System.out.println("💳 Eseguendo acquisto biglietto " + biglietto.getId());

        statoOriginaleBiglietto = biglietto.getStato();

        if (!(biglietto.getStato() instanceof StatoPrenotato)) {
            throw new IllegalStateException("Biglietto deve essere in stato PRENOTATO per acquisto");
        }

        if (!elaboraPagamento()) {
            throw new RuntimeException("Pagamento fallito per biglietto " + biglietto.getId());
        }


        transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();


        biglietto.setStato(new StatoPagato());


        System.out.println("✅ Acquisto completato:");
        System.out.println("   Biglietto: " + biglietto.getId());
        System.out.println("   Cliente: " + cliente);
        System.out.println("   Importo: €" + biglietto.getPrezzo());
        System.out.println("   Pagamento: " + metodoPagamento);
        System.out.println("   Transaction: " + transactionId);

        executed = true;

        inviaConfermaAcquisto();
    }

    @Override
    public void undo() {
        if (!executed) {
            throw new IllegalStateException("Comando non ancora eseguito: " + commandId);
        }

        if (!canUndo()) {
            throw new IllegalStateException("Comando non può essere annullato: " + commandId);
        }

        System.out.println("🔄 Annullando acquisto biglietto " + biglietto.getId());

        // Elabora rimborso
        if (!elaboraRimborso()) {
            throw new RuntimeException("Rimborso fallito per transazione " + transactionId);
        }

        biglietto.setStato((com.trenicall.server.business.patterns.state.StatoBiglietto) statoOriginaleBiglietto);

        System.out.println("✅ Acquisto annullato:");
        System.out.println("   Biglietto: " + biglietto.getId() + " ripristinato a " +
                ((com.trenicall.server.business.patterns.state.StatoBiglietto) statoOriginaleBiglietto).getNomeStato());
        System.out.println("   Rimborso: €" + biglietto.getPrezzo());
        System.out.println("   Transaction annullata: " + transactionId);

        executed = false;

        inviaNotificaAnnullamento();
    }

    private boolean elaboraPagamento() {
        System.out.println("💳 Elaborando pagamento...");
        System.out.println("   Metodo: " + metodoPagamento);
        System.out.println("   Importo: €" + biglietto.getPrezzo());

        try {
            Thread.sleep(100); // Simula latenza
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean successo = Math.random() > 0.05;

        if (successo) {
            System.out.println("✅ Pagamento autorizzato");
        } else {
            System.out.println("❌ Pagamento rifiutato");
        }

        return successo;
    }

    private boolean elaboraRimborso() {
        System.out.println("💸 Elaborando rimborso...");
        System.out.println("   Transaction: " + transactionId);
        System.out.println("   Importo: €" + biglietto.getPrezzo());

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("✅ Rimborso elaborato");
        return true;
    }

    private void inviaConfermaAcquisto() {
        System.out.println("📧 Inviando conferma acquisto...");
        // Qui si integrerebbe con EmailNotifier
    }

    private void inviaNotificaAnnullamento() {
        System.out.println("📧 Inviando notifica annullamento...");
        // Qui si integrerebbe con sistema notifiche
    }

    @Override
    public boolean canUndo() {

        return executed &&
                biglietto.getStato() instanceof StatoPagato &&
                timestamp.isAfter(LocalDateTime.now().minusHours(24));
    }

    @Override
    public String getCommandType() {
        return "ACQUISTO_BIGLIETTO";
    }

    @Override
    public String getDescription() {
        return "Acquisto biglietto " + biglietto.getId() +
                " per €" + biglietto.getPrezzo() +
                " con " + metodoPagamento;
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String getCommandId() {
        return commandId;
    }

    @Override
    public Object getTarget() {
        return biglietto;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public Object getCliente() {
        return cliente;
    }

    public String getMetodoPagamento() {
        return metodoPagamento;
    }

    @Override
    public String toString() {
        return "AcquistoBigliettoCommand{" +
                "id='" + commandId + '\'' +
                ", biglietto=" + biglietto.getId() +
                ", prezzo=" + biglietto.getPrezzo() +
                ", pagamento='" + metodoPagamento + '\'' +
                ", executed=" + executed +
                ", canUndo=" + canUndo() +
                '}';
    }
}