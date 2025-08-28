package com.trenicall.server.grpc.impl;

import com.trenicall.server.business.services.ClienteService;
import com.trenicall.server.domain.entities.Cliente;
import com.trenicall.server.grpc.cliente.ClienteResponse;
import com.trenicall.server.grpc.cliente.ClienteServiceGrpc;
import com.trenicall.server.grpc.cliente.RegistraClienteRequest;
import com.trenicall.server.grpc.cliente.AbilitaFedeltaRequest;
import com.trenicall.server.grpc.cliente.DettagliClienteRequest;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

@Service
public class ClienteServiceImpl extends ClienteServiceGrpc.ClienteServiceImplBase {

    private final ClienteService clienteService;

    public ClienteServiceImpl(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @Override
    public void registraCliente(RegistraClienteRequest request,
                                StreamObserver<ClienteResponse> responseObserver) {
        Cliente cliente = new Cliente(
                request.getId(),
                request.getNome(),
                request.getEmail(),
                request.getTelefono()
        );

        Cliente salvato = clienteService.registraCliente(cliente);

        ClienteResponse response = ClienteResponse.newBuilder()
                .setId(salvato.getId())
                .setNome(salvato.getNome())
                .setEmail(salvato.getEmail())
                .setTelefono(salvato.getTelefono())
                .setFedelta(salvato.isFedelta())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    @Override
    public void abilitaFedelta(AbilitaFedeltaRequest request,
                               StreamObserver<ClienteResponse> responseObserver) {
        clienteService.abilitaFedelta(request.getClienteId());
        ClienteResponse response = ClienteResponse.newBuilder()
                .setId(request.getClienteId())
                .setFedelta(true)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void dettagliCliente(DettagliClienteRequest request,
                                StreamObserver<ClienteResponse> responseObserver) {
        Cliente cliente = clienteService.getCliente(request.getClienteId());

        ClienteResponse response = ClienteResponse.newBuilder()
                .setId(cliente.getId())
                .setNome(cliente.getNome())
                .setEmail(cliente.getEmail())
                .setTelefono(cliente.getTelefono())
                .setFedelta(cliente.isFedelta())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}

