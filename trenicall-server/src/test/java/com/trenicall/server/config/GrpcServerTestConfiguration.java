package com.trenicall.server.config;

import com.trenicall.server.grpc.impl.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.ServerInterceptor;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ForwardingServerCallListener;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.util.logging.Logger;

@TestConfiguration
public class GrpcServerTestConfiguration {

    private static final Logger logger = Logger.getLogger(GrpcServerTestConfiguration.class.getName());

    @Bean
    public Server grpcTestServer(
            BiglietteriaServiceImpl biglietteriaService,
            ClienteServiceImpl clienteService,
            PrenotazioneServiceImpl prenotazioneService,
            PromozioneServiceImpl promozioneService,
            NotificaServiceImpl notificaService) throws IOException {

        ServerInterceptor loggingInterceptor = new ServerInterceptor() {
            @Override
            public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
                    ServerCall<ReqT, RespT> call, Metadata headers,
                    io.grpc.ServerCallHandler<ReqT, RespT> next) {

                return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(
                        next.startCall(call, headers)) {
                    @Override
                    public void onHalfClose() {
                        try {
                            super.onHalfClose();
                        } catch (Exception e) {
                            logger.severe("gRPC Error in " + call.getMethodDescriptor().getFullMethodName() + ": " + e.getMessage());
                            e.printStackTrace();
                            call.close(io.grpc.Status.INTERNAL.withDescription(e.getMessage()), new Metadata());
                        }
                    }
                };
            }
        };

        Server server = ServerBuilder.forPort(9091)
                .addService(ServerInterceptors.intercept(biglietteriaService, loggingInterceptor))
                .addService(ServerInterceptors.intercept(clienteService, loggingInterceptor))
                .addService(ServerInterceptors.intercept(prenotazioneService, loggingInterceptor))
                .addService(ServerInterceptors.intercept(promozioneService, loggingInterceptor))
                .addService(ServerInterceptors.intercept(notificaService, loggingInterceptor))
                .build();

        server.start();
        logger.info("gRPC Test Server started on port 9091");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down gRPC Test Server");
            server.shutdown();
        }));

        return server;
    }
}