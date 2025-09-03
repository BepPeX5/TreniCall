package com.trenicall.server.grpc.impl;

import com.google.protobuf.Empty;
import com.trenicall.server.domain.entities.Treno;
import com.trenicall.server.domain.repositories.TrenoRepository;
import com.trenicall.server.grpc.notifica.*;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Service
public class NotificaServiceImpl extends NotificaServiceGrpc.NotificaServiceImplBase {

    private final TrenoRepository trenoRepository;
    private final Map<String, CopyOnWriteArrayList<StreamObserver<NotificaResponse>>> subscribersByTrain = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler;

    public NotificaServiceImpl(TrenoRepository trenoRepository) {
        this.trenoRepository = trenoRepository;
    }

    @PostConstruct
    public void start() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::pushSimulatedUpdates, 5, 70, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void stop() {
        if (scheduler != null) scheduler.shutdownNow();
        subscribersByTrain.values().forEach(list -> list.forEach(o -> {
            try { o.onCompleted(); } catch (Exception ignored) {}
        }));
        subscribersByTrain.clear();
    }

    @Override
    public void seguiTreno(SeguiTrenoRequest request, StreamObserver<NotificaResponse> responseObserver) {
        String codice = request.getTrenoId().trim();

        Optional<Treno> trenoOpt = trenoRepository.findById(codice);
        if (trenoOpt.isEmpty()) {
            System.err.println("Treno non trovato: " + codice + " (disponibili: " +
                    trenoRepository.findAll().stream().map(t -> t.getId()).toList() + ")");
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription("Treno " + codice + " non esistente").asRuntimeException());
            return;
        }

        subscribersByTrain.computeIfAbsent(codice, k -> new CopyOnWriteArrayList<>()).add(responseObserver);

        NotificaResponse ack = NotificaResponse.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setClienteId(request.getClienteId())
                .setCanale("PUSH")
                .setMessaggio("✅ Iscritto alle notifiche del treno " + codice +
                        " (" + trenoOpt.get().getTratta().getStazionePartenza() +
                        " → " + trenoOpt.get().getTratta().getStazioneArrivo() + ")")
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

    private void pushSimulatedUpdates() {
        if (subscribersByTrain.isEmpty()) return;

        List<String> codici = new ArrayList<>(subscribersByTrain.keySet());
        Collections.shuffle(codici);

        for (String codice : codici) {
            List<StreamObserver<NotificaResponse>> observers = subscribersByTrain.get(codice);
            if (observers == null || observers.isEmpty()) continue;

            String msg = randomUpdateMessage(codice);
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

    private String randomUpdateMessage(String codice) {
        String[] templates = {
                "🚄 Treno " + codice + ": viaggio regolare, in orario",
                "⏰ Treno " + codice + ": ritardo di %d minuti",
                "🛤️ Treno " + codice + ": partenza confermata dal binario %d",
                "✅ Treno " + codice + ": servizio puntuale, nessun disservizio",
                "📍 Treno " + codice + ": arrivo previsto tra %d minuti",
                "🔧 Treno " + codice + ": operazioni tecniche completate",
                "🚩 Treno " + codice + ": fermata prolungata di %d minuti",
                "⚡ Treno " + codice + ": velocità ridotta per maltempo"
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
}


