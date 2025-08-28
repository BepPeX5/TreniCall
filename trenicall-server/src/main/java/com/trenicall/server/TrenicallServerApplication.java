package com.trenicall.server;

import com.trenicall.server.grpc.impl.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class TrenicallServerApplication {

	public static void main(String[] args) throws Exception {
		Server server = ServerBuilder.forPort(50051)
				.addService(new BiglietteriaServiceImpl())
				.addService(new PrenotazioneServiceImpl())
				.addService(new PromozioneServiceImpl())
				.addService(new ClienteServiceImpl())
				.addService(new NotificaServiceImpl())
				.build();

		System.out.println("🚂 TreniCal gRPC Server avviato sulla porta 50051...");
		server.start();
		server.awaitTermination();
	}
}
