package com.trenicall.server.business.patterns.observer.observers;

import com.trenicall.server.business.patterns.observer.NotificaObserver;
import com.trenicall.server.business.patterns.observer.TrenoEvento;
import java.util.List;
import java.util.ArrayList;

public class SMSNotifier implements NotificaObserver {

    private final List<String> numeriTelefono;
    private boolean attivo;
    private final int limiteCaratteri;

    public SMSNotifier() {
        this.numeriTelefono = new ArrayList<>();
        this.attivo = true;
        this.limiteCaratteri = 160; // Limite SMS standard
    }

    public SMSNotifier(List<String> numeriTelefono) {
        this.numeriTelefono = new ArrayList<>(numeriTelefono);
        this.attivo = true;
        this.limiteCaratteri = 160;
    }

    @Override
    public void notifica(TrenoEvento evento) {
        if (!attivo) {
            System.out.println("📱 SMSNotifier disattivato - notifica ignorata");
            return;
        }

        if (numeriTelefono.isEmpty()) {
            System.out.println("📱 Nessun numero di telefono configurato");
            return;
        }

        String messaggioSMS = creaMessaggioSMS(evento);

        System.out.println("📱 =========================");
        System.out.println("📱 INVIO SMS NOTIFICA");
        System.out.println("📱 A: " + numeriTelefono.size() + " numeri");
        System.out.println("📱 Caratteri: " + messaggioSMS.length() + "/" + limiteCaratteri);
        System.out.println("📱 =========================");
        System.out.println(messaggioSMS);
        System.out.println("📱 =========================");

        // Qui si integrerebbe con servizio SMS reale (Twilio, AWS SNS, etc.)
        simulaInvioSMS(messaggioSMS);
    }

    private String creaMessaggioSMS(TrenoEvento evento) {
        StringBuilder messaggio = new StringBuilder();

        // Header compatto
        messaggio.append("TreniCal: ");

        switch (evento.getTipoEvento()) {
            case "RITARDO":
                messaggio.append("RITARDO ").append(evento.getRitardoMinuti()).append("min");
                break;
            case "CANCELLAZIONE":
                messaggio.append("CANCELLATO");
                break;
            case "CAMBIO_BINARIO":
                messaggio.append("CAMBIO BINARIO");
                break;
            case "PARTENZA":
                messaggio.append("PARTITO");
                break;
            case "ARRIVO":
                messaggio.append("ARRIVATO");
                break;
            default:
                messaggio.append("AGGIORNAMENTO");
                break;
        }

        // Info treno
        messaggio.append("\n🚂").append(evento.getNumeroTreno());
        messaggio.append(" ").append(evento.getPartenza()).append("→").append(evento.getArrivo());

        // Info specifiche per evento
        if (evento.isRitardo() && evento.getOrarioPartenzaEffettivo() != null) {
            messaggio.append("\n⏰Nuovo orario: ").append(evento.getOrarioPartenzaEffettivo());
        }

        if (evento.isCambioBinario() && evento.getBinarioPartenza() != null) {
            messaggio.append("\n🚉Binario: ").append(evento.getBinarioPartenza());
        }

        if (evento.isCancellazione()) {
            messaggio.append("\n❌Rimborso disponibile");
        }

        // Limita la lunghezza del messaggio
        String messaggioFinale = messaggio.toString();
        if (messaggioFinale.length() > limiteCaratteri) {
            messaggioFinale = messaggioFinale.substring(0, limiteCaratteri - 3) + "...";
        }

        return messaggioFinale;
    }

    private void simulaInvioSMS(String messaggio) {
        // Simulazione invio SMS
        for (String numero : numeriTelefono) {
            System.out.println("📱 SMS inviato a: " + mascheraNumeroParziale(numero));
        }
        System.out.println("📱 Totale SMS inviati: " + numeriTelefono.size());
    }

    private String mascheraNumeroParziale(String numero) {
        if (numero == null || numero.length() < 4) {
            return "***";
        }
        return numero.substring(0, 3) + "***" + numero.substring(numero.length() - 2);
    }

    public void aggiungiNumero(String numero) {
        if (numero != null && isNumeroValido(numero)) {
            numeriTelefono.add(numero);
            System.out.println("📱 Aggiunto numero SMS: " + mascheraNumeroParziale(numero));
        } else {
            System.out.println("📱 Numero non valido: " + numero);
        }
    }

    public void rimuoviNumero(String numero) {
        numeriTelefono.remove(numero);
        System.out.println("📱 Rimosso numero SMS: " + mascheraNumeroParziale(numero));
    }

    private boolean isNumeroValido(String numero) {
        // Validazione base numero italiano
        return numero.matches("^\\+39\\d{9,10}$") || numero.matches("^3\\d{8,9}$");
    }

    @Override
    public String getTipoNotifica() {
        return "SMS";
    }

    @Override
    public boolean isAttivo() {
        return attivo;
    }

    @Override
    public void attiva() {
        this.attivo = true;
        System.out.println("📱 SMSNotifier attivato");
    }

    @Override
    public void disattiva() {
        this.attivo = false;
        System.out.println("📱 SMSNotifier disattivato");
    }

    public int getNumeroDestinatari() {
        return numeriTelefono.size();
    }

    @Override
    public String toString() {
        return "SMSNotifier{" +
                "destinatari=" + numeriTelefono.size() +
                ", attivo=" + attivo +
                ", limiteCaratteri=" + limiteCaratteri +
                '}';
    }
}