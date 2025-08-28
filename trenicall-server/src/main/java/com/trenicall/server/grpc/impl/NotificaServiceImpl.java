package com.trenicall.server.grpc.impl;

import com.trenicall.server.grpc.notifica.InviaNotificaRequest;
import com.trenicall.server.grpc.notifica.NotificaResponse;
import com.trenicall.server.grpc.notifica.NotificaServiceGrpc;
import com.trenicall.server.grpc.notifica.NotificaProto.*;
import com.trenicall.server.grpc.notifica.SeguiTrenoRequest;
import io.grpc.stub.StreamObserver;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NotificaServiceImpl extends NotificaServiceGrpc.NotificaServiceImplBase {

    private final Map<String, List<NotificaResponse>> notifiche = new ConcurrentHashMap<>();

    @Override
    public void seguiTreno(SeguiTrenoRequest request, StreamObserver<NotificaResponse> responseObserver) {
        NotificaResponse n = NotificaResponse.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setClienteId(request.getClienteId())
                .setCanale("PUSH")
                .setMessaggio("Stai seguendo il treno " + request.getTrenoId())
                .setTimestamp(new Date().toString())
                .setLetta(false)
                .build();
        notifiche.computeIfAbsent(request.getClienteId(), k -> new ArrayList<>()).add(n);
        responseObserver.onNext(n);
        responseObserver.onCompleted();
    }

    @Override
    public void inviaNotifica(InviaNotificaRequest request, StreamObserver<NotificaResponse> responseObserver) {
        NotificaResponse n = NotificaResponse.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setClienteId(request.getClienteId())
                .setCanale(request.getCanale())
                .setMessaggio(request.getMessaggio())
                .setTimestamp(new Date().toString())
                .setLetta(false)
                .build();
        notifiche.computeIfAbsent(request.getClienteId(), k -> new ArrayList<>()).add(n);
        responseObserver.onNext(n);
        responseObserver.onCompleted();
    }
}
