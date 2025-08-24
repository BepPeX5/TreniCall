package com.trenicall.server.business.patterns.command.commands;

import com.trenicall.server.business.patterns.command.Command;
import com.trenicall.server.business.patterns.state.StatoBiglietto;
import com.trenicall.server.business.patterns.state.states.StatoRimborsato;
import com.trenicall.server.domain.entities.Biglietto;
import java.time.LocalDateTime;
import java.util.UUID;

public class AnnullaBigliettoCommand implements Command {

    private final String commandId;
    private final Biglietto biglietto;
    private final String motivoAnnullamento;
    private final boolean rimborsoCompleto;
    private final LocalDateTime timestamp;

    // Backup per undo
    private StatoBiglietto statoOriginale;
    private boolean executed;
    private Double importoRimborsato;
    private String transactionRimborso;

    public AnnullaBigliettoCommand(Biglietto biglietto, String motivoAnnullamento) {
        this(biglietto, motivoAnnullamento, true);
    }

    public AnnullaBigliettoCommand(Biglietto biglietto, String motivoAnnullamento, boolean rimborsoCompleto) {
        this.commandId = "ANN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.biglietto = biglietto;
        this.motivoAnnullamento = motivoAnnullamento;
        this.rimborsoCompleto = rimborsoCompleto;
        this.timestamp = LocalDateTime.now();
        this.executed = false;
    }

    @Override
    public void execute() {
        if (executed) {
            throw new IllegalStateException("Comando già eseguito: " + commandId);
        }

        System.out.println("❌ Annullando biglietto " + biglietto.getId());

        // Verifica che il biglietto possa essere rimborsato
        if (!biglietto.getStato().isRimborsabile()) {
            throw new IllegalStateException("Biglietto in stato " + biglietto.getStato().getNomeStato() +
                    " non può essere annullato");
        }

        // Salva stato originale per undo
        statoOriginale = biglietto.getStato();

        // Calcola importo rimborso
        importoRimborsato = calcolaImportoRimborso();

        // Elabora rimborso
        if (!elaboraRimborso()) {
            throw new RuntimeException("Errore elaborando rimborso per biglietto " + biglietto.getId());
        }

        // Genera transaction rimborso
        transactionRimborso = "REF-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

        // Cambia stato a rimborsato
        biglietto.setStato(new StatoRimborsato());

        // Log annullamento
        System.out.println("✅ Annullamento completato:");
        System.out.println("   Biglietto: " + biglietto.getId());
        System.out.println("   Motivo: " + motivoAnnullamento);
        System.out.println("   Prezzo originale: €" + biglietto.getPrezzo());
        System.out.println("   Importo rimborsato: €" + importoRimborsato);
        System.out.println("   Transaction rimborso: " + transactionRimborso);
        System.out.println("   Rimborso completo: " + (rimborsoCompleto ? "SI" : "NO"));

        executed = true;

        // Invia notifiche
        inviaNotificaAnnullamento();
        liberaPosto();
    }

    @Override
    public void undo() {
        if (!executed) {
            throw new IllegalStateException("Comando non ancora eseguito: " + commandId);
        }

        if (!canUndo()) {
            throw new IllegalStateException("Comando non può essere annullato: " + commandId);
        }

        System.out.println("🔄 Annullando annullamento biglietto " + biglietto.getId());

        // Riaddebitare l'importo (simulato)
        if (!elaboraRiaddebito()) {
            throw new RuntimeException("Errore riaddebito per transazione " + transactionRimborso);
        }

        // Ripristina stato originale
        biglietto.setStato(statoOriginale);

        System.out.println("✅ Annullamento annullato:");
        System.out.println("   Biglietto: " + biglietto.getId() + " ripristinato a " + statoOriginale.getNomeStato());
        System.out.println("   Importo riaddebito: €" + importoRimborsato);
        System.out.println("   Transaction annullata: " + transactionRimborso);

        executed = false;

        // Invia notifica ripristino
        inviaNotificaRipristino();
        riprenotaPosto();
    }

    private Double calcolaImportoRimborso() {
        Double prezzoOriginale = biglietto.getPrezzo();

        if (rimborsoCompleto) {
            return prezzoOriginale;
        }

        // Calcola rimborso parziale basato su policies
        double percentualeRimborso = calcolaPercentualeRimborso();
        Double importo = prezzoOriginale * percentualeRimborso;

        System.out.println("💰 Rimborso calcolato: €" + importo +
                " (" + (percentualeRimborso * 100) + "% di €" + prezzoOriginale + ")");

        return importo;
    }

    private double calcolaPercentualeRimborso() {
        // Calcola percentuale rimborso basata su tempo rimanente
        LocalDateTime ora = LocalDateTime.now();

        // Simuliamo che il treno parta tra alcune ore
        LocalDateTime partenzaTreno = ora.plusHours(6); // Esempio: parte tra 6 ore

        long oreRimanenti = java.time.Duration.between(ora, partenzaTreno).toHours();

        if (oreRimanenti > 24) {
            return 0.90; // 90% rimborso se cancella con >24h anticipo
        } else if (oreRimanenti > 6) {
            return 0.70; // 70% rimborso se cancella con 6-24h anticipo
        } else if (oreRimanenti > 2) {
            return 0.50; // 50% rimborso se cancella con 2-6h anticipo
        } else {
            return 0.20; // 20% rimborso se cancella con <2h anticipo
        }
    }

    private boolean elaboraRimborso() {
        System.out.println("💸 Elaborando rimborso...");
        System.out.println("   Importo: €" + importoRimborsato);
        System.out.println("   Motivo: " + motivoAnnullamento);

        // Simulazione elaborazione rimborso
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("✅ Rimborso elaborato con successo");
        return true;
    }

    private boolean elaboraRiaddebito() {
        System.out.println("💳 Elaborando ri-addebito...");
        System.out.println("   Transaction: " + transactionRimborso);
        System.out.println("   Importo: €" + importoRimborsato);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("✅ Ri-addebito elaborato");
        return true;
    }

    private void inviaNotificaAnnullamento() {
        System.out.println("📧 Inviando notifica annullamento...");
        System.out.println("   Email: Conferma annullamento e rimborso");
        System.out.println("   SMS: Biglietto annullato, rimborso in elaborazione");
    }

    private void inviaNotificaRipristino() {
        System.out.println("📧 Inviando notifica ripristino biglietto...");
    }

    private void liberaPosto() {
        System.out.println("🪑 Liberando posto per altri passeggeri...");
    }

    private void riprenotaPosto() {
        System.out.println("🪑 Riprenotando posto...");
        // Qui si integrerebbe con sistema prenotazioni posti
    }

    @Override
    public boolean canUndo() {
        return executed &&
                timestamp.isAfter(LocalDateTime.now().minusMinutes(30));
    }

    @Override
    public String getCommandType() {
        return "ANNULLA_BIGLIETTO";
    }

    @Override
    public String getDescription() {
        return "Annullamento biglietto " + biglietto.getId() +
                " con rimborso €" + (importoRimborsato != null ? importoRimborsato : "TBD") +
                " - Motivo: " + motivoAnnullamento;
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

    public String getMotivoAnnullamento() {
        return motivoAnnullamento;
    }

    public boolean isRimborsoCompleto() {
        return rimborsoCompleto;
    }

    public Double getImportoRimborsato() {
        return importoRimborsato;
    }

    public String getTransactionRimborso() {
        return transactionRimborso;
    }

    @Override
    public String toString() {
        return "AnnullaBigliettoCommand{" +
                "id='" + commandId + '\'' +
                ", biglietto=" + biglietto.getId() +
                ", motivo='" + motivoAnnullamento + '\'' +
                ", rimborso=" + importoRimborsato +
                ", executed=" + executed +
                ", canUndo=" + canUndo() +
                '}';
    }
}