package com.trenicall.server.config;

import com.trenicall.server.domain.entities.*;
import com.trenicall.server.domain.repositories.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @PersistenceContext
    private EntityManager entityManager;


    @Autowired
    private DisponibilitaTrenoRepository disponibilitaTrenoRepository;
    private final TrattaRepository trattaRepository;
    private final TrenoRepository trenoRepository;
    private final ClienteRepository clienteRepository;
    private final PromozioneRepository promozioneRepository;


    public DatabaseInitializer(TrattaRepository trattaRepository,
                               TrenoRepository trenoRepository,
                               ClienteRepository clienteRepository,
                               PromozioneRepository promozioneRepository) {
        this.trattaRepository = trattaRepository;
        this.trenoRepository = trenoRepository;
        this.clienteRepository = clienteRepository;
        this.promozioneRepository = promozioneRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        disponibilitaTrenoRepository.deleteAll();
        trenoRepository.deleteAll();
        trattaRepository.deleteAll();
        promozioneRepository.deleteAll();
        clienteRepository.deleteAll();

        if (trattaRepository.count() == 0) {
            System.out.println("Inizializzando database con tutte le combinazioni di tratte...");
            initializeDatabase();
            System.out.println("Database inizializzato con " + trenoRepository.count() + " treni per " + trattaRepository.count() + " tratte!");
        } else {
            System.out.println("Database già inizializzato, skip.");
        }
    }

    private void initializeDatabase() {
        List<String> stazioni = Arrays.asList(
                "Roma Termini", "Milano Centrale", "Napoli Centrale", "Torino Porta Nuova",
                "Firenze Santa Maria Novella", "Bologna Centrale", "Venice Santa Lucia",
                "Bari Centrale", "Palermo Centrale", "Genova Piazza Principe", "Cosenza"
        );

        Map<String, Map<String, Integer>> distanze = createDistanceMatrix();

        for (String partenza : stazioni) {
            for (String arrivo : stazioni) {
                if (!partenza.equals(arrivo)) {
                    int distanza = distanze.get(partenza).get(arrivo);
                    String trattaId = "T_" + partenza.replaceAll("\\s+", "") + "_" + arrivo.replaceAll("\\s+", "");

                    Tratta tratta = new Tratta(trattaId, partenza, arrivo, distanza);
                    trattaRepository.save(tratta);

                    createTrainsForRoute(tratta);
                }
            }
        }

        createSampleClients();
        createSamplePromotions();
    }

    private void createTrainsForRoute(Tratta tratta) {
        String[] prefissi = {"REG", "IC", "FR"};
        String[] tipiTreno = {"Regionale", "InterCity", "Freccia Rossa"};
        int[] postiTotali = {200, 300, 400};

        for (int i = 0; i < prefissi.length; i++) {
            String trenoId = generaIdTrenoRealistico(prefissi[i]);
            String nome = tipiTreno[i] + " " + trenoId;

            Treno treno = new Treno(trenoId, nome, tratta, postiTotali[i], String.valueOf((i + 1)));
            trenoRepository.save(treno);


            for (int d = 0; d < 30; d++) {
                LocalDate data = LocalDate.now().plusDays(d);
                DisponibilitaTreno disponibilita = new DisponibilitaTreno(treno, data, postiTotali[i]);
                disponibilitaTrenoRepository.save(disponibilita);
            }
        }
    }


    private String generaIdTrenoRealistico(String prefisso) {
        switch (prefisso) {
            case "REG":
                return "REG" + ThreadLocalRandom.current().nextInt(1000, 9999);
            case "IC":
                return "IC" + ThreadLocalRandom.current().nextInt(500, 1999);
            case "FR":
                return "FR" + ThreadLocalRandom.current().nextInt(8000, 9999);
            default:
                return prefisso + ThreadLocalRandom.current().nextInt(1000, 9999);
        }
    }

    private void createSampleClients() {
        Cliente cliente1 = new Cliente("C1", "Mario Rossi", "mario@test.com", "123456789");
        Cliente cliente2 = new Cliente("C2", "Luca Bianchi", "luca@test.com", "987654321");
        cliente2.abilitaFedelta();

        clienteRepository.save(cliente1);
        clienteRepository.save(cliente2);
    }

    private void createSamplePromotions() {
        Promozione promo1 = new Promozione("P1", "Autunno 2025", 0.15,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(30),
                "Roma Termini", "Milano Centrale", false);

        Promozione promo2 = new Promozione("P2", "Fedelta Premium", 0.25,
                LocalDateTime.now().minusDays(5), LocalDateTime.now().plusDays(60),
                "Milano Centrale", "Napoli Centrale", true);

        Promozione promo3 = new Promozione("P2", "Ag4in, sconto per chi possiede un biglietto per una partita del Napoli al Maradona", 0.25,
                LocalDateTime.now().minusDays(5), LocalDateTime.now().plusDays(60),
                "Cosenza", "Napoli Centrale", true);

        promozioneRepository.save(promo1);
        promozioneRepository.save(promo2);
        promozioneRepository.save(promo3);
    }

    private Map<String, Map<String, Integer>> createDistanceMatrix() {
        Map<String, Map<String, Integer>> distances = new HashMap<>();

        Map<String, Integer> roma = new HashMap<>();
        roma.put("Milano Centrale", 574);
        roma.put("Napoli Centrale", 225);
        roma.put("Torino Porta Nuova", 669);
        roma.put("Firenze Santa Maria Novella", 273);
        roma.put("Bologna Centrale", 378);
        roma.put("Venice Santa Lucia", 528);
        roma.put("Bari Centrale", 457);
        roma.put("Palermo Centrale", 933);
        roma.put("Genova Piazza Principe", 501);
        roma.put("Cosenza", 492);
        distances.put("Roma Termini", roma);

        Map<String, Integer> milano = new HashMap<>();
        milano.put("Roma Termini", 574);
        milano.put("Napoli Centrale", 770);
        milano.put("Torino Porta Nuova", 158);
        milano.put("Firenze Santa Maria Novella", 298);
        milano.put("Bologna Centrale", 218);
        milano.put("Venice Santa Lucia", 267);
        milano.put("Bari Centrale", 864);
        milano.put("Palermo Centrale", 1157);
        milano.put("Genova Piazza Principe", 142);
        milano.put("Cosenza", 1050);
        distances.put("Milano Centrale", milano);

        Map<String, Integer> napoli = new HashMap<>();
        napoli.put("Roma Termini", 225);
        napoli.put("Milano Centrale", 770);
        napoli.put("Torino Porta Nuova", 841);
        napoli.put("Firenze Santa Maria Novella", 476);
        napoli.put("Bologna Centrale", 594);
        napoli.put("Venice Santa Lucia", 701);
        napoli.put("Bari Centrale", 261);
        napoli.put("Palermo Centrale", 426);
        napoli.put("Genova Piazza Principe", 669);
        napoli.put("Cosenza", 277);
        distances.put("Napoli Centrale", napoli);

        Map<String, Integer> torino = new HashMap<>();
        torino.put("Roma Termini", 669);
        torino.put("Milano Centrale", 158);
        torino.put("Napoli Centrale", 841);
        torino.put("Firenze Santa Maria Novella", 456);
        torino.put("Bologna Centrale", 376);
        torino.put("Venice Santa Lucia", 425);
        torino.put("Bari Centrale", 1022);
        torino.put("Palermo Centrale", 1315);
        torino.put("Genova Piazza Principe", 168);
        torino.put("Cosenza", 1208);
        distances.put("Torino Porta Nuova", torino);

        Map<String, Integer> firenze = new HashMap<>();
        firenze.put("Roma Termini", 273);
        firenze.put("Milano Centrale", 298);
        firenze.put("Napoli Centrale", 476);
        firenze.put("Torino Porta Nuova", 456);
        firenze.put("Bologna Centrale", 105);
        firenze.put("Venice Santa Lucia", 255);
        firenze.put("Bari Centrale", 561);
        firenze.put("Palermo Centrale", 657);
        firenze.put("Genova Piazza Principe", 228);
        firenze.put("Cosenza", 769);
        distances.put("Firenze Santa Maria Novella", firenze);

        Map<String, Integer> bologna = new HashMap<>();
        bologna.put("Roma Termini", 378);
        bologna.put("Milano Centrale", 218);
        bologna.put("Napoli Centrale", 594);
        bologna.put("Torino Porta Nuova", 376);
        bologna.put("Firenze Santa Maria Novella", 105);
        bologna.put("Venice Santa Lucia", 150);
        bologna.put("Bari Centrale", 679);
        bologna.put("Palermo Centrale", 872);
        bologna.put("Genova Piazza Principe", 328);
        bologna.put("Cosenza", 887);
        distances.put("Bologna Centrale", bologna);

        Map<String, Integer> venezia = new HashMap<>();
        venezia.put("Roma Termini", 528);
        venezia.put("Milano Centrale", 267);
        venezia.put("Napoli Centrale", 701);
        venezia.put("Torino Porta Nuova", 425);
        venezia.put("Firenze Santa Maria Novella", 255);
        venezia.put("Bologna Centrale", 150);
        venezia.put("Bari Centrale", 729);
        venezia.put("Palermo Centrale", 1022);
        venezia.put("Genova Piazza Principe", 408);
        venezia.put("Cosenza", 937);
        distances.put("Venice Santa Lucia", venezia);

        Map<String, Integer> bari = new HashMap<>();
        bari.put("Roma Termini", 457);
        bari.put("Milano Centrale", 864);
        bari.put("Napoli Centrale", 261);
        bari.put("Torino Porta Nuova", 1022);
        bari.put("Firenze Santa Maria Novella", 561);
        bari.put("Bologna Centrale", 679);
        bari.put("Venice Santa Lucia", 729);
        bari.put("Palermo Centrale", 687);
        bari.put("Genova Piazza Principe", 863);
        bari.put("Cosenza", 358);
        distances.put("Bari Centrale", bari);

        Map<String, Integer> palermo = new HashMap<>();
        palermo.put("Roma Termini", 933);
        palermo.put("Milano Centrale", 1157);
        palermo.put("Napoli Centrale", 426);
        palermo.put("Torino Porta Nuova", 1315);
        palermo.put("Firenze Santa Maria Novella", 657);
        palermo.put("Bologna Centrale", 872);
        palermo.put("Venice Santa Lucia", 1022);
        palermo.put("Bari Centrale", 687);
        palermo.put("Genova Piazza Principe", 1156);
        palermo.put("Cosenza", 477);
        distances.put("Palermo Centrale", palermo);

        Map<String, Integer> genova = new HashMap<>();
        genova.put("Roma Termini", 501);
        genova.put("Milano Centrale", 142);
        genova.put("Napoli Centrale", 669);
        genova.put("Torino Porta Nuova", 168);
        genova.put("Firenze Santa Maria Novella", 228);
        genova.put("Bologna Centrale", 328);
        genova.put("Venice Santa Lucia", 408);
        genova.put("Bari Centrale", 863);
        genova.put("Palermo Centrale", 1156);
        genova.put("Cosenza", 1040);
        distances.put("Genova Piazza Principe", genova);

        Map<String, Integer> cosenza = new HashMap<>();
        cosenza.put("Roma Termini", 492);
        cosenza.put("Milano Centrale", 1050);
        cosenza.put("Napoli Centrale", 277);
        cosenza.put("Torino Porta Nuova", 1208);
        cosenza.put("Firenze Santa Maria Novella", 769);
        cosenza.put("Bologna Centrale", 887);
        cosenza.put("Venice Santa Lucia", 937);
        cosenza.put("Bari Centrale", 358);
        cosenza.put("Palermo Centrale", 477);
        cosenza.put("Genova Piazza Principe", 1040);
        distances.put("Cosenza", cosenza);

        return distances;
    }
}