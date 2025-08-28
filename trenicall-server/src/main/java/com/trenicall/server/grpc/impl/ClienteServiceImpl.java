package com.trenicall.server.grpc.impl;

import com.trenicall.server.grpc.cliente.*;
import com.trenicall.server.grpc.cliente.ClienteProto.*;
import io.grpc.stub.StreamObserver;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClienteServiceImpl extends ClienteServiceGrpc.ClienteServiceImplBase {

    private final Map<String, ClienteResponse> clienti = new ConcurrentHashMap<>();

    @Override
    public void registraCliente(RegistraClienteRequest request, StreamObserver<ClienteResponse> responseObserver) {
        ClienteResponse c = ClienteResponse.newBuilder()
                .setId(request.getId())
                .setNome(request.getNome())
                .setEmail(request.getEmail())
                .setTelefono(request.getTelefono())
                .setFedelta(false)
                .build();
        clienti.put(c.getId(), c);
        responseObserver.onNext(c);
        responseObserver.onCompleted();
    }

    @Override
    public void abilitaFedelta(AbilitaFedeltaRequest request, StreamObserver<ClienteResponse> responseObserver) {
        ClienteResponse c = clienti.get(request.getClienteId());
        if (c == null) {
            responseObserver.onError(new IllegalArgumentException("Cliente non trovato"));
            return;
        }
        ClienteResponse updated = ClienteResponse.newBuilder(c).setFedelta(true).build();
        clienti.put(updated.getId(), updated);
        responseObserver.onNext(updated);
        responseObserver.onCompleted();
    }

    @Override
    public void dettagliCliente(DettagliClienteRequest request, StreamObserver<ClienteResponse> responseObserver) {
        ClienteResponse c = clienti.get(request.getClienteId());
        if (c == null) {
            responseObserver.onError(new IllegalArgumentException("Cliente non trovato"));
            return;
        }
        responseObserver.onNext(c);
        responseObserver.onCompleted();
    }
}
