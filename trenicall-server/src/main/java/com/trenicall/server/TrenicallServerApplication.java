package com.trenicall.server;

import com.trenicall.server.grpc.impl.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TrenicallServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrenicallServerApplication.class, args);
	}

}

