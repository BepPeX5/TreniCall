package com.trenicall.server.business.patterns.observer.observers;

import com.trenicall.server.business.patterns.observer.NotificaObserver;
import com.trenicall.server.business.patterns.observer.TrenoEvento;
import java.util.List;
import java.util.ArrayList;

public class FedeltaTrenoNotifier implements NotificaObserver {

    private final List<String> clientiFedelta;
    private boolean attivo;
    private final String livelloMinimo;

    public FedeltaTrenoNotifier() {
        this.clientiFedelta = new ArrayList<>();
        this.attivo = true;
        this.livelloMinimo = "BRONZE";
    }

    public FedeltaTrenoNotifier(String livelloMinimo) {
        this.clientiFedelta = new ArrayList<>();
        this.attivo = true;
        this.livelloMinimo = livelloMinimo;
    }

    @Override
    public void notifica(TrenoEvento evento) {
        if (!attivo) {
            System.out.println("💎 FedeltaTrenoNotifier disattivato - notifica ignorata");
            return;
        }

        if (clientiFedelta.isEmpty()) {
            System.out.println("💎 Nessun cliente FedeltàTreno registrato");
            return;
        }

        // Notifiche prioritarie per clienti fedeltà
        if (dovrebbeNotificare(evento)) {
            inviaNotificaFedelta(evento);
        }
    }

    private boolean dovrebbeNotificare(TrenoEvento evento) {
        // Clienti fedeltà ricevono notifiche per tutti gli eventi importanti
        return evento.isRitardo() || evento.isCancellazione() ||
                evento.isCambioBinario() || evento.getTipoEvento().equals("AGGIORNAMENTO");
    }

    private void inviaNotificaFedelta(TrenoEvento evento) {
        System.out.println("💎 =========================");
        System.out.println("💎 NOTIFICA FEDELTÀTRENO VIP");
        System.out.println("💎 Livello minimo: " + livelloMinimo);
        System.out.println("💎 Clienti notificati: " + clientiFedelta.size());
        System.out.println("💎 =========================");

        String messaggioVip = creaMessaggioVip(evento);
        System.out.println(messaggioVip);

        // Notifica via canali premium
        inviaNotificaEmail(evento, messaggioVip);
        inviaNotificaApp(evento, messaggioVip);

        // Per eventi critici, notifica anche via SMS
        if (evento.isCancellazione() || (evento.isRitardo() && evento.getRitardoMinuti() > 30)) {
            inviaNotificaSmsUrgente(evento);
        }

        System.out.println("💎 =========================");
    }

    private String creaMessaggioVip(TrenoEvento evento) {
        StringBuilder messaggio = new StringBuilder();
        messaggio.append("🌟 NOTIFICA PRIORITARIA FEDELTÀTRENO 🌟\n\n");
        messaggio.append("Gentile Cliente Premium,\n\n");

        switch (evento.getTipoEvento()) {
            case "RITARDO":
                messaggio.append("⚠️ RITARDO SIGNIFICATIVO\n");
                messaggio.append("Il Suo treno ").append(evento.getNumeroTreno())
                        .append(" ha accumulato ").append(evento.getRitardoMinuti()).append(" minuti di ritardo.\n\n");
                messaggio.append("Come Cliente FedeltàTreno, può:\n");
                messaggio.append("• Cambio gratuito su treno successivo\n");
                messaggio.append("• Rimborso integrale entro 24h\n");
                messaggio.append("• Punti bonus per il disagio\n");
                break;

            case "CANCELLAZIONE":
                messaggio.append("🚨 CANCELLAZIONE TRENO\n");
                messaggio.append("Il Suo treno ").append(evento.getNumeroTreno()).append(" è stato cancellato.\n\n");
                messaggio.append("SERVIZIO VIP ATTIVATO:\n");
                messaggio.append("• Riprotezione automatica su prossimo treno\n");
                messaggio.append("• Voucher sconto 20% per prossimo viaggio\n");
                messaggio.append("• Assistenza dedicata: 800.123.456\n");
                break;

            case "CAMBIO_BINARIO":
                messaggio.append("🔄 CAMBIO BINARIO\n");
                messaggio.append("Il Suo treno ").append(evento.getNumeroTreno());
                if (evento.getBinarioPartenza() != null) {
                    messaggio.append(" si trova ora al binario ").append(evento.getBinarioPartenza());
                }
                messaggio.append(".\n\nLounge FedeltàTreno disponibile per l'attesa.\n");
                break;

            default:
                messaggio.append("📢 AGGIORNAMENTO IMPORTANTE\n");
                messaggio.append(evento.getMessaggio()).append("\n");
                break;
        }

        messaggio.append("\n🚂 DETTAGLI VIAGGIO:\n");
        messaggio.append("• Treno: ").append(evento.getNumeroTreno()).append("\n");
        messaggio.append("• Tratta: ").append(evento.getPartenza()).append(" → ").append(evento.getArrivo()).append("\n");

        if (evento.getOrarioPartenzaPrevisto() != null) {
            messaggio.append("• Partenza prevista: ").append(evento.getOrarioPartenzaPrevisto()).append("\n");
        }

        messaggio.append("\n💎 Il Team FedeltàTreno è a Sua disposizione");

        return messaggio.toString();
    }

    private void inviaNotificaEmail(TrenoEvento evento, String messaggio) {
        System.out.println("📧 Email VIP inviata a " + clientiFedelta.size() + " clienti Premium");
    }

    private void inviaNotificaApp(TrenoEvento evento, String messaggio) {
        System.out.println("📱 Notifica in-app prioritaria inviata");
    }

    private void inviaNotificaSmsUrgente(TrenoEvento evento) {
        System.out.println("🚨 SMS urgente inviato per evento critico");
    }

    public void aggiungiClienteFedelta(String clienteId) {
        if (clienteId != null && !clientiFedelta.contains(clienteId)) {
            clientiFedelta.add(clienteId);
            System.out.println("💎 Cliente FedeltàTreno aggiunto: " + clienteId);
        }
    }

    public void rimuoviClienteFedelta(String clienteId) {
        clientiFedelta.remove(clienteId);
        System.out.println("💎 Cliente FedeltàTreno rimosso: " + clienteId);
    }

    @Override
    public String getTipoNotifica() {
        return "FEDELTA_TRENO_VIP";
    }

    @Override
    public boolean isAttivo() {
        return attivo;
    }

    @Override
    public void attiva() {
        this.attivo = true;
        System.out.println("💎 FedeltaTrenoNotifier attivato");
    }

    @Override
    public void disattiva() {
        this.attivo = false;
        System.out.println("💎 FedeltaTrenoNotifier disattivato");
    }

    public int getNumeroClientiFedelta() {
        return clientiFedelta.size();
    }

    public String getLivelloMinimo() {
        return livelloMinimo;
    }

    @Override
    public String toString() {
        return "FedeltaTrenoNotifier{" +
                "clienti=" + clientiFedelta.size() +
                ", livelloMinimo='" + livelloMinimo + '\'' +
                ", attivo=" + attivo +
                '}';
    }
}