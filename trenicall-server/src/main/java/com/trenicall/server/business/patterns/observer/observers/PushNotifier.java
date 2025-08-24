package com.trenicall.server.business.patterns.observer.observers;

import com.trenicall.server.business.patterns.observer.NotificaObserver;
import com.trenicall.server.business.patterns.observer.TrenoEvento;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class PushNotifier implements NotificaObserver {

    private final List<String> deviceTokens;
    private boolean attivo;
    private final String appName;
    private final Map<String, String> iconeEventi;

    public PushNotifier() {
        this.deviceTokens = new ArrayList<>();
        this.attivo = true;
        this.appName = "TreniCal";
        this.iconeEventi = initIconeEventi();
    }

    public PushNotifier(List<String> deviceTokens) {
        this.deviceTokens = new ArrayList<>(deviceTokens);
        this.attivo = true;
        this.appName = "TreniCal";
        this.iconeEventi = initIconeEventi();
    }

    private Map<String, String> initIconeEventi() {
        Map<String, String> icone = new HashMap<>();
        icone.put("RITARDO", "⏰");
        icone.put("CANCELLAZIONE", "❌");
        icone.put("CAMBIO_BINARIO", "🔄");
        icone.put("PARTENZA", "🚀");
        icone.put("ARRIVO", "🏁");
        icone.put("AGGIORNAMENTO", "📢");
        return icone;
    }

    @Override
    public void notifica(TrenoEvento evento) {
        if (!attivo) {
            System.out.println("📲 PushNotifier disattivato - notifica ignorata");
            return;
        }

        if (deviceTokens.isEmpty()) {
            System.out.println("📲 Nessun device token configurato");
            return;
        }

        PushMessage pushMessage = creaPushMessage(evento);

        System.out.println("📲 =========================");
        System.out.println("📲 INVIO PUSH NOTIFICATION");
        System.out.println("📲 A: " + deviceTokens.size() + " dispositivi");
        System.out.println("📲 Titolo: " + pushMessage.getTitolo());
        System.out.println("📲 Messaggio: " + pushMessage.getMessaggio());
        System.out.println("📲 =========================");

        // Qui si integrerebbe con servizio push reale (Firebase, APNs, etc.)
        simulaInvioPush(pushMessage);
    }

    private PushMessage creaPushMessage(TrenoEvento evento) {
        String icona = iconeEventi.getOrDefault(evento.getTipoEvento(), "🚂");
        String titolo = icona + " " + appName;
        String messaggio = creaMessaggioPush(evento);

        Map<String, Object> datiExtra = new HashMap<>();
        datiExtra.put("numeroTreno", evento.getNumeroTreno());
        datiExtra.put("tipoEvento", evento.getTipoEvento());
        datiExtra.put("partenza", evento.getPartenza());
        datiExtra.put("arrivo", evento.getArrivo());
        datiExtra.put("timestamp", evento.getTimestampEvento().toString());

        if (evento.isRitardo()) {
            datiExtra.put("ritardoMinuti", evento.getRitardoMinuti());
        }

        return new PushMessage(titolo, messaggio, icona, datiExtra);
    }

    private String creaMessaggioPush(TrenoEvento evento) {
        StringBuilder messaggio = new StringBuilder();

        switch (evento.getTipoEvento()) {
            case "RITARDO":
                messaggio.append("Treno ").append(evento.getNumeroTreno())
                        .append(" in ritardo di ").append(evento.getRitardoMinuti()).append(" min");
                if (evento.getOrarioPartenzaEffettivo() != null) {
                    messaggio.append(" - Partenza: ").append(evento.getOrarioPartenzaEffettivo());
                }
                break;

            case "CANCELLAZIONE":
                messaggio.append("Treno ").append(evento.getNumeroTreno()).append(" cancellato");
                break;

            case "CAMBIO_BINARIO":
                messaggio.append("Treno ").append(evento.getNumeroTreno());
                if (evento.getBinarioPartenza() != null) {
                    messaggio.append(" spostato al binario ").append(evento.getBinarioPartenza());
                } else {
                    messaggio.append(" - cambio binario");
                }
                break;

            case "PARTENZA":
                messaggio.append("Treno ").append(evento.getNumeroTreno()).append(" partito da ").append(evento.getPartenza());
                break;

            case "ARRIVO":
                messaggio.append("Treno ").append(evento.getNumeroTreno()).append(" arrivato a ").append(evento.getArrivo());
                break;

            default:
                messaggio.append("Aggiornamento per treno ").append(evento.getNumeroTreno());
                break;
        }

        return messaggio.toString();
    }

    private void simulaInvioPush(PushMessage pushMessage) {
        // Simulazione invio push notification
        for (String token : deviceTokens) {
            System.out.println("📲 Push inviata a device: " + mascheraToken(token));
        }
        System.out.println("📲 Totale push inviate: " + deviceTokens.size());
        System.out.println("📲 Payload: " + pushMessage.getDatiExtra().size() + " campi extra");
    }

    private String mascheraToken(String token) {
        if (token == null || token.length() < 8) {
            return "***";
        }
        return token.substring(0, 4) + "***" + token.substring(token.length() - 4);
    }

    public void aggiungiDevice(String deviceToken) {
        if (deviceToken != null && deviceToken.length() > 10) {
            deviceTokens.add(deviceToken);
            System.out.println("📲 Aggiunto device: " + mascheraToken(deviceToken));
        }
    }

    public void rimuoviDevice(String deviceToken) {
        deviceTokens.remove(deviceToken);
        System.out.println("📲 Rimosso device: " + mascheraToken(deviceToken));
    }

    @Override
    public String getTipoNotifica() {
        return "PUSH";
    }

    @Override
    public boolean isAttivo() {
        return attivo;
    }

    @Override
    public void attiva() {
        this.attivo = true;
        System.out.println("📲 PushNotifier attivato");
    }

    @Override
    public void disattiva() {
        this.attivo = false;
        System.out.println("📲 PushNotifier disattivato");
    }

    public int getNumeroDispositivi() {
        return deviceTokens.size();
    }

    // Classe interna per il messaggio push
    public static class PushMessage {
        private final String titolo;
        private final String messaggio;
        private final String icona;
        private final Map<String, Object> datiExtra;

        public PushMessage(String titolo, String messaggio, String icona, Map<String, Object> datiExtra) {
            this.titolo = titolo;
            this.messaggio = messaggio;
            this.icona = icona;
            this.datiExtra = datiExtra;
        }

        public String getTitolo() { return titolo; }
        public String getMessaggio() { return messaggio; }
        public String getIcona() { return icona; }
        public Map<String, Object> getDatiExtra() { return datiExtra; }
    }

    @Override
    public String toString() {
        return "PushNotifier{" +
                "dispositivi=" + deviceTokens.size() +
                ", attivo=" + attivo +
                ", app='" + appName + '\'' +
                '}';
    }
}