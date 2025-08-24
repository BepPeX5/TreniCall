package com.trenicall.server.business.patterns.observer.observers;

import com.trenicall.server.business.patterns.observer.NotificaObserver;
import com.trenicall.server.business.patterns.observer.TrenoEvento;
import java.util.List;
import java.util.ArrayList;

public class EmailNotifier implements NotificaObserver {

    private final List<String> indirizziEmail;
    private boolean attivo;
    private final String templateEmail;

    public EmailNotifier() {
        this.indirizziEmail = new ArrayList<>();
        this.attivo = true;
        this.templateEmail = "TreniCal";
    }

    public EmailNotifier(List<String> indirizziEmail) {
        this.indirizziEmail = new ArrayList<>(indirizziEmail);
        this.attivo = true;
        this.templateEmail = "TreniCal";
    }

    @Override
    public void notifica(TrenoEvento evento) {
        if (!attivo) {
            System.out.println("📧 EmailNotifier disattivato - notifica ignorata");
            return;
        }

        if (indirizziEmail.isEmpty()) {
            System.out.println("📧 Nessun indirizzo email configurato");
            return;
        }

        String oggetto = creaOggetto(evento);
        String corpo = creaCorpoEmail(evento);

        System.out.println("📧 =========================");
        System.out.println("📧 INVIO EMAIL NOTIFICA");
        System.out.println("📧 A: " + indirizziEmail.size() + " destinatari");
        System.out.println("📧 Oggetto: " + oggetto);
        System.out.println("📧 =========================");
        System.out.println(corpo);
        System.out.println("📧 =========================");

        simulaInvioEmail(oggetto, corpo);
    }

    private String creaOggetto(TrenoEvento evento) {
        switch (evento.getTipoEvento()) {
            case "RITARDO":
                return "🚂 Ritardo treno " + evento.getNumeroTreno() + " - " + evento.getRitardoMinuti() + " minuti";
            case "CANCELLAZIONE":
                return "❌ Cancellazione treno " + evento.getNumeroTreno();
            case "CAMBIO_BINARIO":
                return "🔄 Cambio binario treno " + evento.getNumeroTreno();
            case "PARTENZA":
                return "🚀 Partenza treno " + evento.getNumeroTreno();
            case "ARRIVO":
                return "🏁 Arrivo treno " + evento.getNumeroTreno();
            default:
                return "📢 Aggiornamento treno " + evento.getNumeroTreno();
        }
    }

    private String creaCorpoEmail(TrenoEvento evento) {
        StringBuilder corpo = new StringBuilder();
        corpo.append("Gentile Cliente,\n\n");
        corpo.append("La informiamo di un aggiornamento relativo al Suo viaggio:\n\n");
        corpo.append("🚂 TRENO: ").append(evento.getNumeroTreno()).append("\n");
        corpo.append("🛤️ TRATTA: ").append(evento.getPartenza()).append(" → ").append(evento.getArrivo()).append("\n");
        corpo.append("📅 DATA: ").append(evento.getTimestampEvento().toLocalDate()).append("\n");

        if (evento.getOrarioPartenzaPrevisto() != null) {
            corpo.append("⏰ PARTENZA PREVISTA: ").append(evento.getOrarioPartenzaPrevisto()).append("\n");
        }

        if (evento.getBinarioPartenza() != null) {
            corpo.append("🚉 BINARIO: ").append(evento.getBinarioPartenza()).append("\n");
        }

        corpo.append("\n📢 AGGIORNAMENTO:\n");
        corpo.append(evento.getMessaggio()).append("\n\n");

        if (evento.isRitardo()) {
            corpo.append("⏱️ RITARDO: ").append(evento.getRitardoMinuti()).append(" minuti\n");
            if (evento.getOrarioPartenzaEffettivo() != null) {
                corpo.append("🕐 NUOVA PARTENZA: ").append(evento.getOrarioPartenzaEffettivo()).append("\n");
            }
        }

        if (evento.isCancellazione() && evento.getMotivoRitardo() != null) {
            corpo.append("❓ MOTIVO: ").append(evento.getMotivoRitardo()).append("\n");
            corpo.append("\nPer informazioni sui rimborsi, contatti il nostro servizio clienti.\n");
        }

        corpo.append("\nCi scusiamo per il disagio.\n\n");
        corpo.append("Cordiali saluti,\n");
        corpo.append("Il Team TreniCal\n\n");
        corpo.append("📱 App: TreniCal Mobile\n");
        corpo.append("🌐 Web: www.trenicall.it\n");
        corpo.append("☎️ Assistenza: 199.303.060");

        return corpo.toString();
    }

    private void simulaInvioEmail(String oggetto, String corpo) {
        // Simulazione invio email
        for (String email : indirizziEmail) {
            System.out.println("📧 Email inviata a: " + email);
        }
        System.out.println("📧 Totale email inviate: " + indirizziEmail.size());
    }

    public void aggiungiDestinatario(String email) {
        if (email != null && email.contains("@")) {
            indirizziEmail.add(email);
            System.out.println("📧 Aggiunto destinatario email: " + email);
        }
    }

    public void rimuoviDestinatario(String email) {
        indirizziEmail.remove(email);
        System.out.println("📧 Rimosso destinatario email: " + email);
    }

    @Override
    public String getTipoNotifica() {
        return "EMAIL";
    }

    @Override
    public boolean isAttivo() {
        return attivo;
    }

    @Override
    public void attiva() {
        this.attivo = true;
        System.out.println("📧 EmailNotifier attivato");
    }

    @Override
    public void disattiva() {
        this.attivo = false;
        System.out.println("📧 EmailNotifier disattivato");
    }

    public int getNumeroDestinatari() {
        return indirizziEmail.size();
    }

    @Override
    public String toString() {
        return "EmailNotifier{" +
                "destinatari=" + indirizziEmail.size() +
                ", attivo=" + attivo +
                '}';
    }
}