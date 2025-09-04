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

    public NotificaServiceImpl(TrenoRepository trenoRepository, BigliettoRepository bigliettoRepository) {
        this.trenoRepository = trenoRepository;
        this.bigliettoRepository = bigliettoRepository;
    }

    @PostConstruct
    public void start() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::pushSimulatedUpdates, 10, 20, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void stop() {
        if (scheduler != null) scheduler.shutdownNow();
        subscribersByTrain.values().forEach(list -> list.forEach(o -> {
            try { o.onCompleted(); } catch (Exception ignored) {}
        }));
        globalSubscribers.values().forEach(o -> {
            try { o.onCompleted(); } catch (Exception ignored) {}
        });
        subscribersByTrain.clear();
        globalSubscribers.clear();
    }

    @Override
    public void seguiTreno(SeguiTrenoRequest request, StreamObserver<NotificaResponse> responseObserver) {
        String codice = request.getTrenoId().trim();
        String clienteId = request.getClienteId();

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
                        "✅ Registrato per notifiche treni " :
                        "✅ Registrato per treno " + codice + " e treni acquistati")
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

    private String getStatoSicuro(Biglietto biglietto) {
        if (biglietto.getStato() != null) {
            return biglietto.getStato().getNomeStato();
        }
        return "SCONOSCIUTO";
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

    @Override
    public void logoutNotifiche(LogoutRequest request, StreamObserver<LogoutResponse> responseObserver) {
        String clienteId = request.getClienteId();

        globalSubscribers.remove(clienteId);

        subscribersByTrain.values().forEach(list ->
                list.removeIf(observer -> {
                    try {
                        observer.onCompleted();
                    } catch (Exception ignored) {}
                    return true;
                })
        );

        LogoutResponse response = LogoutResponse.newBuilder()
                .setSuccess(true)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}


