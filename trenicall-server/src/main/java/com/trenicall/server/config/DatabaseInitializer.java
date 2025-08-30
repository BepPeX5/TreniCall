package com.trenicall.server.config;

import com.trenicall.server.domain.entities.*;
import com.trenicall.server.domain.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DatabaseInitializer implements CommandLineRunner {

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
        if (trattaRepository.count() == 0) {
            System.out.println("Inizializzando database con stazioni e treni italiani...");
            initializeDatabase();
            System.out.println("Database inizializzato con successo!");
        } else {
            System.out.println("Database già inizializzato, skip.");
        }
    }

    private void initializeDatabase() {
        List<String> stazioni = Arrays.asList(
                "Roma Termini", "Milano Centrale", "Napoli Centrale", "Torino Porta Nuova",
                "Firenze Santa Maria Novella", "Bologna Centrale", "Venice Santa Lucia", "Bari Centrale",
                "Palermo Centrale", "Genova Piazza Principe", "Verona Porta Nuova", "Trieste Centrale",
                "Padova", "Brescia", "Parma", "Modena", "Reggio Emilia AV", "Piacenza",
                "Rimini", "Ancona", "Pescara Centrale", "Foggia", "Lecce", "Brindisi",
                "Taranto", "Cosenza", "Reggio Calabria Centrale", "Catania Centrale", "Messina Centrale",
                "Cagliari", "Sassari", "Bolzano", "Trento", "Udine", "Perugia",
                "L'Aquila", "Campobasso", "Potenza Centrale", "Catanzaro Lido", "Aosta"
        );

        Map<String, Map<String, Integer>> distanze = createDistanceMatrix();

        for (int i = 0; i < stazioni.size(); i++) {
            for (int j = 0; j < stazioni.size(); j++) {
                if (i != j) {
                    String partenza = stazioni.get(i);
                    String arrivo = stazioni.get(j);

                    int distanza = getDistance(partenza, arrivo, distanze);

                    String trattaId = "T" + String.format("%03d", i) + String.format("%03d", j);

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
        String[] tipiTreno = {"Regionale", "InterCity", "Freccia Rossa"};
        int[] postiTotali = {200, 300, 400};
        String[] prefissi = {"REG", "IC", "FR"};

        for (int i = 0; i < tipiTreno.length; i++) {
            String trenoId = prefissi[i] + "-" + tratta.getId();
            String nome = tipiTreno[i] + " " + tratta.getId();

            Treno treno = new Treno(trenoId, nome, tratta, postiTotali[i], String.valueOf((i + 1)));
            trenoRepository.save(treno);
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
        Promozione promo1 = new Promozione("P1", "Estate 2024", 0.15,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(30),
                "Roma Termini", "Milano Centrale", false);

        Promozione promo2 = new Promozione("P2", "Fedeltà Premium", 0.25,
                LocalDateTime.now().minusDays(5), LocalDateTime.now().plusDays(60),
                "Milano Centrale", "Napoli Centrale", true);

        promozioneRepository.save(promo1);
        promozioneRepository.save(promo2);
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
        roma.put("Verona Porta Nuova", 474);
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
        milano.put("Verona Porta Nuova", 157);
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
        napoli.put("Verona Porta Nuova", 647);
        distances.put("Napoli Centrale", napoli);


        return distances;
    }

    private int getDistance(String partenza, String arrivo, Map<String, Map<String, Integer>> distances) {

        if (distances.containsKey(partenza) && distances.get(partenza).containsKey(arrivo)) {
            return distances.get(partenza).get(arrivo);
        }

        if (distances.containsKey(arrivo) && distances.get(arrivo).containsKey(partenza)) {
            return distances.get(arrivo).get(partenza);
        }

        return calculateApproximateDistance(partenza, arrivo);
    }

    private int calculateApproximateDistance(String partenza, String arrivo) {
        // Mappa approssimativa delle regioni
        Map<String, Integer> regioniNord = Map.of(
                "Milano Centrale", 1, "Torino Porta Nuova", 1, "Genova Piazza Principe", 1,
                "Verona Porta Nuova", 1, "Venice Santa Lucia", 1, "Bologna Centrale", 2,
                "Brescia", 1, "Padova", 1, "Trieste Centrale", 1, "Bolzano", 1
        );

        Map<String, Integer> regioniCentro = Map.of(
                "Roma Termini", 3, "Firenze Santa Maria Novella", 3, "Perugia", 3,
                "Ancona", 3, "Pescara Centrale", 3
        );

        Map<String, Integer> regioniSud = Map.of(
                "Napoli Centrale", 4, "Bari Centrale", 4, "Lecce", 4,
                "Brindisi", 4, "Taranto", 4, "Cosenza", 4, "Reggio Calabria", 5,
                "Palermo Centrale", 5, "Messina Centrale", 5, "Catania Centrale", 5
        );

        int regionePartenza = getRegion(partenza, regioniNord, regioniCentro, regioniSud);
        int regioneArrivo = getRegion(arrivo, regioniNord, regioniCentro, regioniSud);

        int differenzaRegioni = Math.abs(regionePartenza - regioneArrivo);

        return 150 + (differenzaRegioni * 200) + (int)(Math.random() * 100);
    }

    private int getRegion(String stazione, Map<String, Integer> nord, Map<String, Integer> centro, Map<String, Integer> sud) {
        if (nord.containsKey(stazione)) return nord.get(stazione);
        if (centro.containsKey(stazione)) return centro.get(stazione);
        if (sud.containsKey(stazione)) return sud.get(stazione);
        return 3;
    }
}