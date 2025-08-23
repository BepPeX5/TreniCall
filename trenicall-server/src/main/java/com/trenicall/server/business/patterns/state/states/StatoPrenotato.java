package com.trenicall.server.business.patterns.state.states;

import com.trenicall.server.business.patterns.state.StatoBiglietto;
import com.trenicall.server.domain.entities.Biglietto;

public class StatoPrenotato implements StatoBiglietto {

    @Override
    public void confermaPrenotazione(Biglietto biglietto) {
        System.out.println("🎫 Confermando prenotazione biglietto " + biglietto.getId());
        System.out.println("💳 Elaborando pagamento...");

        biglietto.setStato(new StatoPagato());

        System.out.println("✅ Prenotazione confermata! Il Biglietto ora è PAGATO");

    }

    @Override
    public void utilizzaBiglietto(Biglietto biglietto) {
        throw new IllegalStateException(
                "❌ Impossibile utilizzare biglietto PRENOTATO. " +
                        "Prima deve essere confermato con il pagamento."
        );
    }

    @Override
    public void rimborsaBiglietto(Biglietto biglietto) {
        System.out.println("💸 Cancellando prenotazione biglietto " + biglietto.getId());

        biglietto.setStato(new StatoRimborsato());

        System.out.println("✅ Prenotazione cancellata. Nessun addebito applicato.");
    }

    @Override
    public void modificaBiglietto(Biglietto biglietto) {
        System.out.println("✏️ Modificando prenotazione biglietto " + biglietto.getId());
        System.out.println("🔄 Aggiornando data/orario/classe...");

        System.out.println("✅ Prenotazione modificata con successo");
    }

    @Override
    public void scadenzaBiglietto(Biglietto biglietto) {
        System.out.println("⏰ Prenotazione scaduta per biglietto " + biglietto.getId());

        biglietto.setStato(new StatoScaduto());

        System.out.println("❌ Prenotazione scaduta. Deve essere rinnovata.");
    }

    @Override
    public String getNomeStato() {
        return "PRENOTATO";
    }

    @Override
    public boolean isUtilizzabile() {
        return false;
    }

    @Override
    public boolean isModificabile() {
        return true;
    }

    @Override
    public boolean isRimborsabile() {
        return true;
    }

    @Override
    public String toString() {
        return "StatoPrenotato{" +
                "utilizzabile=" + isUtilizzabile() +
                ", modificabile=" + isModificabile() +
                ", rimborsabile=" + isRimborsabile() +
                '}';
    }
}
