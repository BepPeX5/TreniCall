package com.trenicall.server.business.patterns.command.commands;

import com.trenicall.server.business.patterns.command.Command;
import com.trenicall.server.business.patterns.factory.BigliettoFactoryImpl;
import com.trenicall.server.business.patterns.state.states.StatoPrenotato;
import com.trenicall.server.domain.entities.Biglietto;
import java.time.LocalDateTime;
import java.util.UUID;

public class PrenotazioneCommand implements Command {

    private final String commandId;
    private final String tipoBiglietto;
    private final String partenza;
    private final String arrivo;
    private final int distanzaKm;
    private final Object cliente;
    private final LocalDateTime scadenzaPrenotazione;
    private final LocalDateTime timestamp;

    // Oggetti creati dal command
    private Biglietto bigliettoCreato;
    private String numeroPrenotazione;
    private boolean executed;

    public PrenotazioneCommand(String tipoBiglietto, String partenza, String arrivo,
                               int distanzaKm, Object cliente) {
        this.commandId = "PRE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.tipoBiglietto = tipoBiglietto;
        this.partenza = partenza;
        this.arrivo = arrivo;
        this.distanzaKm = distanzaKm;
        this.cliente = cliente;
        this.timestamp = LocalDateTime.now();
        this.scadenzaPrenotazione = timestamp.plusHours(24); // Prenotazione valida 24h
        this.executed = false;
    }

    public PrenotazioneCommand(String tipoBiglietto, String partenza, String arrivo,
                               int distanzaKm, Object cliente, int oreScadenza) {
        this.commandId = "PRE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.tipoBiglietto = tipoBiglietto;
        this.partenza = partenza;
        this.arrivo = arrivo;
        this.distanzaKm = distanzaKm;
        this.cliente = cliente;
        this.timestamp = LocalDateTime.now();
        this.scadenzaPrenotazione = timestamp.plusHours(oreScadenza);
        this.executed = false;
    }

    @Override
    public void execute() {
        if (executed) {
            throw new IllegalStateException("Comando già eseguito: " + commandId);
        }

        System.out.println("📋 Creando prenotazione biglietto");

        // Verifica disponibilità posto (simulato)
        if (!verificaDisponibilità()) {
            throw new RuntimeException("Posto non disponibile per " + partenza + " → " + arrivo);
        }

        // Crea biglietto usando Factory Pattern
        BigliettoFactoryImpl factory = new BigliettoFactoryImpl();
        bigliettoCreato = factory.creaBiglietto(tipoBiglietto, partenza, arrivo, distanzaKm);

        // Genera numero prenotazione
        numeroPrenotazione = "PNR-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

        // Verifica che sia stato creato in stato PRENOTATO
        if (!(bigliettoCreato.getStato() instanceof StatoPrenotato)) {
            throw new IllegalStateException("Biglietto deve essere creato in stato PRENOTATO");
        }

        // Riserva posto
        riservaPosto();

        // Log prenotazione
        System.out.println("✅ Prenotazione creata:");
        System.out.println("   Numero prenotazione: " + numeroPrenotazione);
        System.out.println("   Biglietto: " + bigliettoCreato.getId());
        System.out.println("   Cliente: " + cliente);
        System.out.println("   Tratta: " + partenza + " → " + arrivo);
        System.out.println("   Tipo: " + tipoBiglietto + " (" + distanzaKm + " km)");
        System.out.println("   Prezzo: €" + bigliettoCreato.getPrezzo());
        System.out.println("   Scade il: " + scadenzaPrenotazione);
        System.out.println("   Tempo rimanente: " + calcolaOreRimanenti() + " ore");

        executed = true;

        // Invia notifiche e avvia timer scadenza
        inviaConfermaPrenotazione();
        programmaScadenza();
    }

    @Override
    public void undo() {
        if (!executed) {
            throw new IllegalStateException("Comando non ancora eseguito: " + commandId);
        }

        if (!canUndo()) {
            throw new IllegalStateException("Comando non può essere annullato: " + commandId);
        }

        System.out.println("🔄 Annullando prenotazione " + numeroPrenotazione);

        // Libera posto
        liberaPosto();

        System.out.println("✅ Prenotazione annullata:");
        System.out.println("   Numero: " + numeroPrenotazione);
        System.out.println("   Biglietto: " + bigliettoCreato.getId() + " eliminato");
        System.out.println("   Posto liberato per altri clienti");

        // Resetta oggetti
        bigliettoCreato = null;
        executed = false;

        // Invia notifica annullamento
        inviaNotificaAnnullamentoPrenotazione();
    }

    private boolean verificaDisponibilità() {
        System.out.println("🔍 Verificando disponibilità posto...");
        System.out.println("   Tratta: " + partenza + " → " + arrivo);
        System.out.println("   Tipo: " + tipoBiglietto);

        // Simulazione verifica disponibilità (90% successo)
        boolean disponibile = Math.random() > 0.1;

        if (disponibile) {
            System.out.println("✅ Posto disponibile");
        } else {
            System.out.println("❌ Posto non disponibile");
        }

        return disponibile;
    }

    private void riservaPosto() {
        System.out.println("🪑 Riservando posto...");
        System.out.println("   Prenotazione: " + numeroPrenotazione);
        System.out.println("   Durata riserva: " + calcolaOreRimanenti() + " ore");
        // Qui si integrerebbe con sistema gestione posti
    }

    private void liberaPosto() {
        System.out.println("🪑 Liberando posto riservato...");
        System.out.println("   Prenotazione annullata: " + numeroPrenotazione);
        // Qui si integrerebbe con sistema gestione posti
    }

    private long calcolaOreRimanenti() {
        return java.time.Duration.between(LocalDateTime.now(), scadenzaPrenotazione).toHours();
    }

    private void inviaConfermaPrenotazione() {
        System.out.println("📧 Inviando conferma prenotazione...");
        System.out.println("   Email: Dettagli prenotazione e scadenza");
        System.out.println("   SMS: Codice prenotazione " + numeroPrenotazione);

        // Qui si integrerebbe con Observer Pattern per notifiche
    }

    private void inviaNotificaAnnullamentoPrenotazione() {
        System.out.println("📧 Inviando notifica annullamento prenotazione...");
    }

    private void programmaScadenza() {
        System.out.println("⏰ Programmando scadenza automatica...");
        System.out.println("   Scadenza: " + scadenzaPrenotazione);
        System.out.println("   Timer avviato per liberazione automatica posto");

        // In un sistema reale, qui si programmerebbe un job/scheduler
        // che libera automaticamente il posto alla scadenza
    }

    public boolean isScaduta() {
        return LocalDateTime.now().isAfter(scadenzaPrenotazione);
    }

    public void confermaPrenotazione(String metodoPagamento) {
        if (!executed) {
            throw new IllegalStateException("Prenotazione non ancora creata");
        }

        if (isScaduta()) {
            throw new IllegalStateException("Prenotazione scaduta il " + scadenzaPrenotazione);
        }

        System.out.println("💳 Confermando prenotazione con acquisto...");

        // Qui si createrebbe un AcquistoBigliettoCommand
        AcquistoBigliettoCommand acquisto = new AcquistoBigliettoCommand(bigliettoCreato, cliente, metodoPagamento);
        // E si eseguirebbe tramite CommandManager

        System.out.println("✅ Prenotazione " + numeroPrenotazione + " confermata con acquisto");
    }

    @Override
    public boolean canUndo() {
        // Una prenotazione può essere annullata solo se:
        // 1. È stata eseguita
        // 2. Non è ancora scaduta
        // 3. Non è stata ancora confermata (biglietto ancora in stato PRENOTATO)
        return executed &&
                !isScaduta() &&
                bigliettoCreato != null &&
                bigliettoCreato.getStato() instanceof StatoPrenotato;
    }

    @Override
    public String getCommandType() {
        return "PRENOTAZIONE_BIGLIETTO";
    }

    @Override
    public String getDescription() {
        return "Prenotazione " + (numeroPrenotazione != null ? numeroPrenotazione : "TBD") +
                " per " + tipoBiglietto + " " + partenza + " → " + arrivo +
                " (scade: " + scadenzaPrenotazione.toLocalDate() + " " + scadenzaPrenotazione.toLocalTime() + ")";
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
        return bigliettoCreato;
    }

    public String getNumeroPrenotazione() {
        return numeroPrenotazione;
    }

    public Biglietto getBigliettoCreato() {
        return bigliettoCreato;
    }

    public LocalDateTime getScadenzaPrenotazione() {
        return scadenzaPrenotazione;
    }

    public Object getCliente() {
        return cliente;
    }

    @Override
    public String toString() {
        return "PrenotazioneCommand{" +
                "id='" + commandId + '\'' +
                ", prenotazione='" + numeroPrenotazione + '\'' +
                ", tratta='" + partenza + "→" + arrivo + '\'' +
                ", tipo='" + tipoBiglietto + '\'' +
                ", scadenza=" + scadenzaPrenotazione +
                ", executed=" + executed +
                ", scaduta=" + isScaduta() +
                ", canUndo=" + canUndo() +
                '}';
    }
}