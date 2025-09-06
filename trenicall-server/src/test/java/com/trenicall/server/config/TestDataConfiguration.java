package com.trenicall.server.config;

import com.trenicall.server.domain.entities.*;
import com.trenicall.server.domain.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@TestConfiguration
public class TestDataConfiguration {

    @Bean
    public CommandLineRunner loadTestData(
            ClienteRepository clienteRepository,
            TrattaRepository trattaRepository,
            TrenoRepository trenoRepository,
            BigliettoRepository bigliettoRepository,
            PromozioneRepository promozioneRepository) {

        return args -> {
            Cliente cliente1 = new Cliente("C1", "Mario Rossi", "mario@test.com", "123456789");
            Cliente cliente2 = new Cliente("C2", "Luca Bianchi", "luca@test.com", "987654321");
            cliente2.abilitaFedelta();

            clienteRepository.save(cliente1);
            clienteRepository.save(cliente2);

            Tratta tratta1 = new Tratta("T1", "Roma", "Milano", 500);
            Tratta tratta2 = new Tratta("T2", "Milano", "Torino", 150);
            Tratta tratta3 = new Tratta("T3", "Roma", "Napoli", 200);

            trattaRepository.save(tratta1);
            trattaRepository.save(tratta2);
            trattaRepository.save(tratta3);

            Treno treno1 = new Treno("FR001", "Freccia Rossa", tratta1, 300, "1");
            Treno treno2 = new Treno("IC001", "InterCity", tratta2, 200, "3");
            Treno treno3 = new Treno("REG001", "Regionale", tratta3, 150, "2");

            trenoRepository.save(treno1);
            trenoRepository.save(treno2);
            trenoRepository.save(treno3);

            Promozione promo1 = new Promozione("P1", "Estate 2024", 0.15,
                    LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(30),
                    "Roma", "Milano", false);
            Promozione promo2 = new Promozione("P2", "Fedeltà Premium", 0.25,
                    LocalDateTime.now().minusDays(5), LocalDateTime.now().plusDays(60),
                    "Milano", "Torino", true);

            promozioneRepository.save(promo1);
            promozioneRepository.save(promo2);
        };
    }
}