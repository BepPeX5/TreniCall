package com.trenicall.server.grpc.impl;

import com.trenicall.server.business.services.NotificaService;
import com.trenicall.server.grpc.notifica.NotificaResponse;
import com.trenicall.server.grpc.notifica.NotificaServiceGrpc;
import com.trenicall.server.grpc.notifica.InviaNotificaRequest;
import com.trenicall.server.grpc.notifica.SeguiTrenoRequest;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class NotificaServiceImpl extends NotificaServiceGrpc.NotificaServiceImplBase {

    private final NotificaService notificaService;

    public NotificaServiceImpl(NotificaService notificaService) {
        this.notificaService = notificaService;
    }

    @Override
    public void inviaNotifica(InviaNotificaRequest request,
                              StreamObserver<NotificaResponse> responseObserver) {
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
    public void seguiTreno(SeguiTrenoRequest request,
                           StreamObserver<NotificaResponse> responseObserver) {
        NotificaResponse response = NotificaResponse.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setClienteId(request.getClienteId())
                .setCanale("PUSH")
                .setMessaggio("Sei iscritto alle notifiche sul treno " + request.getTrenoId())
                .setTimestamp(LocalDateTime.now().toString())
                .setLetta(false)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}

