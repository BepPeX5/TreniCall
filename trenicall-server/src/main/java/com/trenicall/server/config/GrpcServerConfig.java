package com.trenicall.server.config;

import com.trenicall.server.grpc.impl.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;

@Component
public class GrpcServerConfig {

    @Autowired
    private BiglietteriaServiceImpl biglietteriaService;

    @Autowired
    private ClienteServiceImpl clienteService;

    @Autowired
    private PrenotazioneServiceImpl prenotazioneService;

    @Autowired
    private PromozioneServiceImpl promozioneService;

    @Autowired
    private NotificaServiceImpl notificaService;

    private Server server;

    @EventListener(ApplicationReadyEvent.class)
    public void startGrpcServer() throws IOException {
        server = ServerBuilder.forPort(9090)
                .addService(biglietteriaService)
                .addService(clienteService)
                .addService(prenotazioneService)
                .addService(promozioneService)
                .addService(notificaService)
                .build()
                .start();

        System.out.println("gRPC Server started on port 9090");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down gRPC server");
            server.shutdown();
        }));
    }

    @PreDestroy
    public void stopGrpcServer() {
        if (server != null) {
            server.shutdown();
        }
    }
}