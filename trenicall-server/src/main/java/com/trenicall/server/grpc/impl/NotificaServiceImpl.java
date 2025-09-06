package com.trenicall.server.grpc.impl;

import com.google.protobuf.Empty;
import com.trenicall.server.domain.entities.Treno;
import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.repositories.TrenoRepository;
import com.trenicall.server.domain.repositories.BigliettoRepository;
import com.trenicall.server.grpc.notifica.*;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class NotificaServiceImpl extends NotificaServiceGrpc.NotificaServiceImplBase {

    private final TrenoRepository trenoRepository;
    private final BigliettoRepository bigliettoRepository;

    private final Map<String, CopyOnWriteArrayList<StreamObserver<NotificaResponse>>> subscribersByTrain = new ConcurrentHashMap<>();
    private final Map<String, StreamObserver<NotificaResponse>> globalSubscribers = new ConcurrentHashMap<>();

    private ScheduledExecutorService scheduler;
    private final Object fileOperationLock = new Object();

    private static final String NOTIFICATIONS_FILE = System.getProperty("java.io.tmpdir") + "trenicall_notifications.txt";

    public NotificaServiceImpl(TrenoRepository trenoRepository, BigliettoRepository bigliettoRepository) {
        this.trenoRepository = trenoRepository;
        this.bigliettoRepository = bigliettoRepository;
    }

    @PostConstruct
    public void start() {
        scheduler = Executors.newSingleThreadScheduledExecutor();

        initializeNotificationFile();

        scheduler.scheduleAtFixedRate(this::checkAdminNotifications, 1, 1, TimeUnit.SECONDS);

        scheduler.scheduleAtFixedRate(this::pushSimulatedUpdates, 10, 45, TimeUnit.SECONDS);

        System.out.println("✅ NotificaServiceImpl avviato - Monitoraggio file attivo");
    }

    private void initializeNotificationFile() {
        try {
            File notifFile = new File(NOTIFICATIONS_FILE);
            if (!notifFile.exists()) {
                notifFile.createNewFile();
                System.out.println("📁 File notifiche creato: " + notifFile.getAbsolutePath());
            } else {
                Files.write(notifFile.toPath(), new byte[0], StandardOpenOption.TRUNCATE_EXISTING);
                System.out.println("🧹 File notifiche pulito: " + notifFile.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("❌ Errore inizializzazione file notifiche: " + e.getMessage());
        }
    }

    private void checkAdminNotifications() {
        synchronized (fileOperationLock) {
            File notifFile = new File(NOTIFICATIONS_FILE);
            if (!notifFile.exists() || notifFile.length() == 0) {
                return;
            }

            try {
                List<String> lines = Files.readAllLines(notifFile.toPath());
                if (!lines.isEmpty()) {
                    System.out.println("📬 Trovate " + lines.size() + " notifiche admin da processare");

                    for (String line : lines) {
                        if (!line.trim().isEmpty()) {
                            processAdminNotification(line.trim());
                        }
                    }

                    Files.write(notifFile.toPath(), new byte[0], StandardOpenOption.TRUNCATE_EXISTING);
                    System.out.println("🧹 File notifiche pulito dopo elaborazione");
                }
            } catch (Exception e) {
                System.err.println("❌ Errore lettura notifiche admin: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void processAdminNotification(String line) {
        try {
            String[] parts = line.split("\\|");
            if (parts.length >= 3) {
                String trainIdOrBroadcast = parts[0];
                String eventType = parts[1];
                String message = parts[2];

                System.out.println("🔄 Processando notifica: " + trainIdOrBroadcast + " | " + eventType + " | " + message);

                if ("BROADCAST".equals(trainIdOrBroadcast)) {
                    inviaNotificaBroadcastGlobale(message, eventType);
                } else {
                    inviaNotificaAdminATreno(trainIdOrBroadcast, eventType, message);
                }
            } else {
                System.err.println("⚠️ Formato notifica non valido: " + line);
            }
        } catch (Exception e) {
            System.err.println("❌ Errore processamento notifica: " + line + " - " + e.getMessage());
        }
    }

    public void inviaNotificaAdminATreno(String trainId, String eventType, String message) {
        System.out.println("📢 Invio notifica admin per treno " + trainId + ": " + message);

        NotificaResponse adminNotifica = NotificaResponse.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setClienteId("")
                .setCanale("ADMIN")
                .setTipo("ADMIN_NOTIFICATION")
                .setMessaggio(String.format("[ADMIN-ALERT] Treno %s: %s - %s", trainId, eventType, message))
                .setTimestamp(LocalDateTime.now().toString())
                .setLetta(false)
                .build();

        int notificheMandateCount = 0;

        List<StreamObserver<NotificaResponse>> trainObservers = subscribersByTrain.get(trainId);
        if (trainObservers != null) {
            Iterator<StreamObserver<NotificaResponse>> it = trainObservers.iterator();
            while (it.hasNext()) {
                StreamObserver<NotificaResponse> observer = it.next();
                try {
                    observer.onNext(adminNotifica);
                    notificheMandateCount++;
                } catch (Exception e) {
                    System.err.println("❌ Errore invio notifica admin (treno seguito): " + e.getMessage());
                    it.remove();
                }
            }
        }

        Iterator<Map.Entry<String, StreamObserver<NotificaResponse>>> globalIt = globalSubscribers.entrySet().iterator();
        while (globalIt.hasNext()) {
            Map.Entry<String, StreamObserver<NotificaResponse>> entry = globalIt.next();
            String clienteId = entry.getKey();
            StreamObserver<NotificaResponse> observer = entry.getValue();

            if (clienteHaBigliettiPerTreno(clienteId, trainId)) {
                try {
                    NotificaResponse clientNotifica = NotificaResponse.newBuilder()
                            .setId(UUID.randomUUID().toString())
                            .setClienteId(clienteId)
                            .setCanale("ADMIN")
                            .setTipo("ADMIN_NOTIFICATION")
                            .setMessaggio(String.format("[BIGLIETTO-ALERT] Il tuo treno %s: %s - %s", trainId, eventType, message))
                            .setTimestamp(LocalDateTime.now().toString())
                            .setLetta(false)
                            .build();

                    observer.onNext(clientNotifica);
                    notificheMandateCount++;
                } catch (Exception e) {
                    System.err.println("❌ Errore invio notifica admin (biglietto): " + e.getMessage());
                    globalIt.remove();
                }
            }
        }

        System.out.println("✅ Notifica admin treno " + trainId + " inviata a " + notificheMandateCount + " client");
    }

    public void inviaNotificaBroadcastGlobale(String message, String eventType) {
        System.out.println("📡 Invio broadcast globale: " + message);

        NotificaResponse broadcast = NotificaResponse.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setClienteId("")
                .setCanale("BROADCAST")
                .setTipo("GLOBAL_BROADCAST")
                .setMessaggio(String.format("%s", message))
                .setTimestamp(LocalDateTime.now().toString())
                .setLetta(false)
                .build();

        int broadcastCount = 0;

        Iterator<Map.Entry<String, StreamObserver<NotificaResponse>>> it = globalSubscribers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, StreamObserver<NotificaResponse>> entry = it.next();
            String clienteId = entry.getKey();
            StreamObserver<NotificaResponse> observer = entry.getValue();

            try {
                observer.onNext(broadcast);
                broadcastCount++;
            } catch (Exception e) {
                System.err.println("❌ Errore invio broadcast a " + clienteId + ": " + e.getMessage());
                it.remove();
            }
        }

        System.out.println("✅ Broadcast inviato a " + broadcastCount + " client");
    }

    @Override
    public void seguiTreno(SeguiTrenoRequest request, StreamObserver<NotificaResponse> responseObserver) {
        String codice = request.getTrenoId().trim();
        String clienteId = request.getClienteId();

        System.out.println("👤 Cliente " + clienteId + " si registra per notifiche" +
                (codice.isEmpty() ? " globali" : " treno " + codice));

        globalSubscribers.put(clienteId, responseObserver);

        if (!codice.isEmpty()) {
            Optional<Treno> trenoOpt = trenoRepository.findById(codice);
            if (trenoOpt.isEmpty()) {
                responseObserver.onError(io.grpc.Status.NOT_FOUND
                        .withDescription("Treno " + codice + " non esistente").asRuntimeException());
                return;
            }

            subscribersByTrain.computeIfAbsent(codice, k -> new CopyOnWriteArrayList<>()).add(responseObserver);
        }

        NotificaResponse ack = NotificaResponse.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setClienteId(clienteId)
                .setCanale("PUSH")
                .setMessaggio(codice.isEmpty() ?
                        "✅ Registrato per notifiche treni e amministrative" :
                        "✅ Registrato per treno " + codice + " e notifiche amministrative")
                .setTimestamp(LocalDateTime.now().toString())
                .setLetta(false)
                .build();
        responseObserver.onNext(ack);
    }

    @Override
    public void listaTreniAttivi(Empty request, StreamObserver<ListaTreniResponse> responseObserver) {
        List<Treno> all = trenoRepository.findAll();
        ListaTreniResponse.Builder out = ListaTreniResponse.newBuilder();

        for (Treno t : all) {
            String tratta = (t.getTratta() != null) ?
                    (t.getTratta().getStazionePartenza() + " → " + t.getTratta().getStazioneArrivo()) : "N/A";

            TrainInfo info = TrainInfo.newBuilder()
                    .setCodice(t.getId())
                    .setNome(t.getNome() != null ? t.getNome() : t.getId())
                    .setTratta(tratta)
                    .build();
            out.addTreni(info);
        }

        responseObserver.onNext(out.build());
        responseObserver.onCompleted();
    }

    @Override
    public void inviaNotifica(InviaNotificaRequest request, StreamObserver<NotificaResponse> responseObserver) {
        NotificaResponse response = NotificaResponse.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setClienteId(request.getClienteId())
                .setCanale(request.getCanale())
                .setMessaggio(request.getMessaggio())
                .setTimestamp(LocalDateTime.now().toString())
                .setLetta(false)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void logoutNotifiche(LogoutRequest request, StreamObserver<LogoutResponse> responseObserver) {
        String clienteId = request.getClienteId();

        System.out.println("👋 Cliente " + clienteId + " si disconnette dalle notifiche");

        StreamObserver<NotificaResponse> removed = globalSubscribers.remove(clienteId);

        subscribersByTrain.values().forEach(list ->
                list.removeIf(observer -> observer == removed)
        );

        LogoutResponse response = LogoutResponse.newBuilder()
                .setSuccess(true)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    private boolean clienteHaBigliettiPerTreno(String clienteId, String trainId) {
        try {
            return bigliettoRepository.findAll().stream()
                    .anyMatch(b -> clienteId.equals(b.getClienteId()) &&
                            trainId.equals(b.getTrenoAssociato()) &&
                            "PAGATO".equals(getStatoSicuro(b)));
        } catch (Exception e) {
            System.err.println("Errore verifica biglietti cliente: " + e.getMessage());
            return false;
        }
    }

    private String getStatoSicuro(Biglietto biglietto) {
        if (biglietto.getStato() != null) {
            return biglietto.getStato().getNomeStato();
        }
        return "SCONOSCIUTO";
    }

    private void pushSimulatedUpdates() {
        pushNotifichetreniSeguiti();
        pushNotificheTreniAcquistati();
    }

    private void pushNotifichetreniSeguiti() {
        if (subscribersByTrain.isEmpty()) return;

        List<String> codici = new ArrayList<>(subscribersByTrain.keySet());
        Collections.shuffle(codici);

        for (String codice : codici) {
            List<StreamObserver<NotificaResponse>> observers = subscribersByTrain.get(codice);
            if (observers == null || observers.isEmpty()) continue;

            String msg = "[TRENI SEGUITI] " + randomUpdateMessage(codice);
            NotificaResponse payload = NotificaResponse.newBuilder()
                    .setId(UUID.randomUUID().toString())
                    .setClienteId("")
                    .setCanale("PUSH")
                    .setMessaggio(msg)
                    .setTimestamp(LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")))
                    .setLetta(false)
                    .build();

            Iterator<StreamObserver<NotificaResponse>> it = observers.iterator();
            while (it.hasNext()) {
                StreamObserver<NotificaResponse> obs = it.next();
                try {
                    obs.onNext(payload);
                } catch (Exception e) {
                    it.remove();
                }
            }
        }
    }

    private void pushNotificheTreniAcquistati() {
        if (globalSubscribers.isEmpty()) return;

        for (Map.Entry<String, StreamObserver<NotificaResponse>> entry : globalSubscribers.entrySet()) {
            String clienteId = entry.getKey();
            StreamObserver<NotificaResponse> observer = entry.getValue();

            List<String> treniCliente = getTreniConBiglietti(clienteId);

            if (!treniCliente.isEmpty() && Math.random() > 0.7) {
                String trenoSelezionato = treniCliente.get(new Random().nextInt(treniCliente.size()));
                String msg = "[TRENI ACQUISTATI] " + randomUpdateMessage(trenoSelezionato);

                NotificaResponse payload = NotificaResponse.newBuilder()
                        .setId(UUID.randomUUID().toString())
                        .setClienteId(clienteId)
                        .setCanale("PUSH")
                        .setMessaggio(msg)
                        .setTimestamp(LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")))
                        .setLetta(false)
                        .build();

                try {
                    observer.onNext(payload);
                } catch (Exception e) {
                    globalSubscribers.remove(clienteId);
                }
            }
        }
    }

    private List<String> getTreniConBiglietti(String clienteId) {
        try {
            return bigliettoRepository.findAll().stream()
                    .filter(b -> clienteId.equals(b.getClienteId()))
                    .filter(b -> "PAGATO".equals(getStatoSicuro(b)))
                    .filter(b -> b.getDataViaggio().isAfter(LocalDateTime.now()))
                    .filter(b -> b.getTrenoAssociato() != null)
                    .map(Biglietto::getTrenoAssociato)
                    .distinct()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Errore getTreniConBiglietti: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private String randomUpdateMessage(String codice) {
        String[] templates = {
                "🚄 Treno " + codice + ": viaggio regolare, in orario",
                "⏰ Treno " + codice + ": ritardo di %d minuti",
                " Treno " + codice + ": partenza confermata dal binario %d",
                " Treno " + codice + ": servizio puntuale, nessun disservizio",
                " Treno " + codice + ": arrivo previsto tra %d minuti",
                "🔧 Treno " + codice + ": operazioni tecniche completate",
                " Treno " + codice + ": fermata prolungata di %d minuti",
                " Treno " + codice + ": velocità ridotta per maltempo"
        };

        Random r = new Random();
        String template = templates[r.nextInt(templates.length)];

        if (template.contains("%d")) {
            if (template.contains("ritardo") || template.contains("fermata")) {
                return String.format(template, 2 + r.nextInt(15));
            } else if (template.contains("binario")) {
                return String.format(template, 1 + r.nextInt(8));
            } else {
                return String.format(template, 5 + r.nextInt(25));
            }
        }
        return template;
    }

    @PreDestroy
    public void stop() {
        System.out.println("🔴 Arresto NotificaServiceImpl...");

        if (scheduler != null) {
            scheduler.shutdownNow();
        }

        subscribersByTrain.values().forEach(list -> list.forEach(o -> {
            try {
                o.onCompleted();
            } catch (Exception ignored) {}
        }));

        globalSubscribers.values().forEach(o -> {
            try {
                o.onCompleted();
            } catch (Exception ignored) {}
        });

        subscribersByTrain.clear();
        globalSubscribers.clear();

        System.out.println("✅ NotificaServiceImpl arrestato");
    }
}