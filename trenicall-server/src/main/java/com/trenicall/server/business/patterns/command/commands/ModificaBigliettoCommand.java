package com.trenicall.server.business.patterns.command.commands;

import com.trenicall.server.business.patterns.command.Command;
import com.trenicall.server.domain.entities.Biglietto;
import java.time.LocalDateTime;
import java.util.UUID;

public class ModificaBigliettoCommand implements Command {

    private final String commandId;
    private final Biglietto biglietto;
    private final String nuovaPartenza;
    private final String nuovoArrivo;
    private final Integer nuovaDistanza;
    private final Double nuovoPrezzo;
    private final String motivoModifica;
    private final LocalDateTime timestamp;

    // Backup dati originali per undo
    private String partenzaOriginale;
    private String arrivoOriginale;
    private Integer distanzaOriginale;
    private Double prezzoOriginale;
    private boolean executed;
    private Double penaleApplicata;

    public ModificaBigliettoCommand(Biglietto biglietto, String nuovaPartenza, String nuovoArrivo,
                                    Integer nuovaDistanza, Double nuovoPrezzo, String motivoModifica) {
        this.commandId = "MOD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.biglietto = biglietto;
        this.nuovaPartenza = nuovaPartenza;
        this.nuovoArrivo = nuovoArrivo;
        this.nuovaDistanza = nuovaDistanza;
        this.nuovoPrezzo = nuovoPrezzo;
        this.motivoModifica = motivoModifica;
        this.timestamp = LocalDateTime.now();
        this.executed = false;
        this.penaleApplicata = 0.0;
    }

    @Override
    public void execute() {
        if (executed) {
            throw new IllegalStateException("Comando già eseguito: " + commandId);
        }

        System.out.println("✏️ Modificando biglietto " + biglietto.getId());

        // Verifica che il biglietto possa essere modificato
        if (!biglietto.getStato().isModificabile()) {
            throw new IllegalStateException("Biglietto in stato " + biglietto.getStato().getNomeStato() +
                    " non può essere modificato");
        }

        // Salva dati originali per undo
        partenzaOriginale = biglietto.getPartenza();
        arrivoOriginale = biglietto.getArrivo();
        distanzaOriginale = biglietto.getDistanzaKm();
        prezzoOriginale = biglietto.getPrezzo();

        // Calcola penale se necessaria
        penaleApplicata = calcolaPenaleModifica();

        // Applica modifiche
        if (nuovaPartenza != null) {
            biglietto.setPartenza(nuovaPartenza);
        }
        if (nuovoArrivo != null) {
            biglietto.setArrivo(nuovoArrivo);
        }
        if (nuovaDistanza != null) {
            biglietto.setDistanzaKm(nuovaDistanza);
        }
        if (nuovoPrezzo != null) {
            Double prezzoFinale = nuovoPrezzo + penaleApplicata;
            biglietto.setPrezzo(prezzoFinale);
        }

        // Log modifica
        System.out.println("✅ Modifica completata:");
        System.out.println("   Biglietto: " + biglietto.getId());
        System.out.println("   Tratta originale: " + partenzaOriginale + " → " + arrivoOriginale);
        System.out.println("   Nuova tratta: " + biglietto.getPartenza() + " → " + biglietto.getArrivo());
        System.out.println("   Prezzo originale: €" + prezzoOriginale);
        System.out.println("   Nuovo prezzo: €" + biglietto.getPrezzo());
        if (penaleApplicata > 0) {
            System.out.println("   Penale applicata: €" + penaleApplicata);
        }
        System.out.println("   Motivo: " + motivoModifica);

        executed = true;

        // Invia notifica modifica
        inviaNotificaModifica();
    }

    @Override
    public void undo() {
        if (!executed) {
            throw new IllegalStateException("Comando non ancora eseguito: " + commandId);
        }

        if (!canUndo()) {
            throw new IllegalStateException("Comando non può essere annullato: " + commandId);
        }

        System.out.println("🔄 Annullando modifica biglietto " + biglietto.getId());

        // Ripristina dati originali
        biglietto.setPartenza(partenzaOriginale);
        biglietto.setArrivo(arrivoOriginale);
        biglietto.setDistanzaKm(distanzaOriginale);
        biglietto.setPrezzo(prezzoOriginale);

        // Se era stata applicata una penale, elabora rimborso
        if (penaleApplicata > 0) {
            elaboraRimborsoPenale();
        }

        System.out.println("✅ Modifica annullata:");
        System.out.println("   Biglietto: " + biglietto.getId() + " ripristinato");
        System.out.println("   Tratta ripristinata: " + partenzaOriginale + " → " + arrivoOriginale);
        System.out.println("   Prezzo ripristinato: €" + prezzoOriginale);
        if (penaleApplicata > 0) {
            System.out.println("   Penale rimborsata: €" + penaleApplicata);
        }

        executed = false;

        // Invia notifica annullamento
        inviaNotificaAnnullamentoModifica();
    }

    private Double calcolaPenaleModifica() {
        // Calcola penale basata su tipo di modifica e stato biglietto
        double penale = 0.0;

        // Penale base per modifica
        penale += 5.00;

        // Penale aggiuntiva se cambia tratta
        boolean cambioTratta = (nuovaPartenza != null && !nuovaPartenza.equals(partenzaOriginale)) ||
                (nuovoArrivo != null && !nuovoArrivo.equals(arrivoOriginale));

        if (cambioTratta) {
            penale += 10.00;
        }

        // Penale ridotta per clienti fedeltà (simulato)
        boolean clienteFedelta = Math.random() > 0.7; // 30% clienti fedeltà
        if (clienteFedelta) {
            penale *= 0.5; // 50% sconto penale
            System.out.println("💎 Sconto penale FedeltàTreno applicato");
        }

        System.out.println("💰 Penale modifica calcolata: €" + penale);
        return penale;
    }

    private void elaboraRimborsoPenale() {
        System.out.println("💸 Elaborando rimborso penale: €" + penaleApplicata);
        // Simulazione rimborso
        System.out.println("✅ Penale rimborsata");
    }

    private void inviaNotificaModifica() {
        System.out.println("📧 Inviando notifica modifica...");
        // Qui si integrerebbe con sistema notifiche
    }

    private void inviaNotificaAnnullamentoModifica() {
        System.out.println("📧 Inviando notifica annullamento modifica...");
    }

    @Override
    public boolean canUndo() {
        // Una modifica può essere annullata solo se:
        // 1. È stata eseguita
        // 2. Sono passate meno di 2 ore
        // 3. Il biglietto è ancora modificabile
        return executed &&
                timestamp.isAfter(LocalDateTime.now().minusHours(2)) &&
                biglietto.getStato().isModificabile();
    }

    @Override
    public String getCommandType() {
        return "MODIFICA_BIGLIETTO";
    }

    @Override
    public String getDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append("Modifica biglietto ").append(biglietto.getId());

        if (nuovaPartenza != null || nuovoArrivo != null) {
            desc.append(" - Tratta: ");
            if (partenzaOriginale != null && arrivoOriginale != null) {
                desc.append(partenzaOriginale).append("→").append(arrivoOriginale);
                desc.append(" to ");
            }
            desc.append((nuovaPartenza != null ? nuovaPartenza : biglietto.getPartenza())).append("→")
                    .append((nuovoArrivo != null ? nuovoArrivo : biglietto.getArrivo()));
        }

        if (motivoModifica != null) {
            desc.append(" (").append(motivoModifica).append(")");
        }

        return desc.toString();
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

    public String getMotivoModifica() {
        return motivoModifica;
    }

    public Double getPenaleApplicata() {
        return penaleApplicata;
    }

    @Override
    public String toString() {
        return "ModificaBigliettoCommand{" +
                "id='" + commandId + '\'' +
                ", biglietto=" + biglietto.getId() +
                ", nuovaTratta='" + nuovaPartenza + "→" + nuovoArrivo + '\'' +
                ", penale=" + penaleApplicata +
                ", executed=" + executed +
                ", canUndo=" + canUndo() +
                '}';
    }
}