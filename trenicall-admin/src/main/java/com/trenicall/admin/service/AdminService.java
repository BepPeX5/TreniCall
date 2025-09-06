package com.trenicall.admin.service;

import com.trenicall.server.business.services.*;
import com.trenicall.server.domain.entities.*;
import com.trenicall.server.domain.repositories.*;
import com.trenicall.server.grpc.notifica.TrainInfo;
import com.trenicall.server.grpc.promozione.PromozioneResponse;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final ClienteService clienteService;
    private final BiglietteriaService biglietteriaService;
    private final PrenotazioneService prenotazioneService;
    private final PromozioneService promozioneService;
    private final NotificaService notificaService;

    private final TrenoRepository trenoRepository;
    private final BigliettoRepository bigliettoRepository;
    private final ClienteRepository clienteRepository;

    private final Map<String, Object> dashboardCache;
    private final List<String> systemLogs;
    private final Set<String> connectedClients;

    private static final String NOTIFICATIONS_FILE = System.getProperty("java.io.tmpdir") + "trenicall_notifications.txt";
    private final Object fileWriteLock = new Object();

    public AdminService(ClienteService clienteService,
                        BiglietteriaService biglietteriaService,
                        PrenotazioneService prenotazioneService,
                        PromozioneService promozioneService,
                        NotificaService notificaService,
                        TrenoRepository trenoRepository,
                        BigliettoRepository bigliettoRepository,
                        ClienteRepository clienteRepository) {

        this.clienteService = clienteService;
        this.biglietteriaService = biglietteriaService;
        this.prenotazioneService = prenotazioneService;
        this.promozioneService = promozioneService;
        this.notificaService = notificaService;
        this.trenoRepository = trenoRepository;
        this.bigliettoRepository = bigliettoRepository;
        this.clienteRepository = clienteRepository;

        this.dashboardCache = new HashMap<>();
        this.systemLogs = new ArrayList<>();
        this.connectedClients = new HashSet<>();

        initializeService();
    }

    private void initializeService() {
        logSystemEvent("🚀 AdminService inizializzato (accesso diretto al database)");
        initializeNotificationFile();
        refreshDashboardData();
    }

    private void initializeNotificationFile() {
        try {
            File notifFile = new File(NOTIFICATIONS_FILE);
            if (!notifFile.exists()) {
                notifFile.createNewFile();
                logSystemEvent("📁 File notifiche creato: " + notifFile.getAbsolutePath());
            }
        } catch (IOException e) {
            logSystemEvent("❌ Errore creazione file notifiche: " + e.getMessage());
        }
    }

    public void shutdown() {
        logSystemEvent("🔌 AdminService disconnesso");
    }

    public Map<String, Object> getDashboardStats() {
        return new HashMap<>(dashboardCache);
    }

    public void refreshDashboardData() {
        try {
            Map<String, Object> stats = new HashMap<>();

            List<Treno> trains = trenoRepository.findAll();
            stats.put("totalTrains", trains.size());
            stats.put("activeTrains", trains.size());

            List<Promozione> promotions = promozioneService.getPromozioni();
            stats.put("activePromotions", promotions.size());

            List<Cliente> allClients = clienteRepository.findAll();
            stats.put("totalClients", allClients.size());

            Collection<Prenotazione> activeBookings = prenotazioneService.getPrenotazioniAttive();
            stats.put("totalBookings", activeBookings.size());

            double dailyRevenue = calculateRealDailyRevenue();
            stats.put("dailyRevenue", dailyRevenue);

            stats.put("connectedClients", connectedClients.size());
            stats.put("systemStatus", "OPERATIONAL");
            stats.put("lastUpdate", LocalDateTime.now());

            dashboardCache.putAll(stats);
            logSystemEvent("📊 Dati dashboard aggiornati (reali)");

        } catch (Exception e) {
            logSystemEvent("⚠️ Errore aggiornamento dashboard: " + e.getMessage());
            dashboardCache.put("systemStatus", "ERROR");
        }
    }

    public boolean createTrainNotification(String trainId, String eventType, String message) {
        synchronized (fileWriteLock) {
            try {
                String timestamp = String.valueOf(System.currentTimeMillis());
                String notification = String.format("%s|%s|%s|%s%n",
                        trainId, eventType, message, timestamp);

                // Scrive in modo atomico aggiungendo al file
                Files.write(
                        new File(NOTIFICATIONS_FILE).toPath(),
                        notification.getBytes(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                );

                logSystemEvent("📢 Notifica treno " + trainId + " scritta su file: " + message);
                return true;

            } catch (Exception e) {
                logSystemEvent("❌ Errore scrittura notifica: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
    }

    public boolean broadcastNotification(String message, String eventType) {
        synchronized (fileWriteLock) {
            try {
                String timestamp = String.valueOf(System.currentTimeMillis());
                String notification = String.format("BROADCAST|%s|%s|%s%n",
                        eventType, message, timestamp);

                // Scrive in modo atomico aggiungendo al file
                Files.write(
                        new File(NOTIFICATIONS_FILE).toPath(),
                        notification.getBytes(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                );

                logSystemEvent("📡 Broadcast scritto su file: " + message);
                return true;

            } catch (Exception e) {
                logSystemEvent("❌ Errore scrittura broadcast: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
    }

    public boolean testNotificationSystem() {
        boolean trainNotificationOk = createTrainNotification("TEST001", "TEST", "Test notifica treno");
        boolean broadcastOk = broadcastNotification("Test broadcast globale", "TEST");

        logSystemEvent("🧪 Test sistema notifiche: Treno=" + trainNotificationOk + ", Broadcast=" + broadcastOk);
        return trainNotificationOk && broadcastOk;
    }

    public String getNotificationFileStatus() {
        File notifFile = new File(NOTIFICATIONS_FILE);
        if (!notifFile.exists()) {
            return "❌ File non esiste";
        }

        try {
            List<String> lines = Files.readAllLines(notifFile.toPath());
            return String.format("✅ File esiste, %d linee, %d bytes",
                    lines.size(), notifFile.length());
        } catch (IOException e) {
            return "❌ Errore lettura file: " + e.getMessage();
        }
    }

    public List<TrainInfo> getAllTrains() {
        try {
            List<Treno> trains = trenoRepository.findAll();
            List<TrainInfo> result = trains.stream()
                    .map(this::trenoToTrainInfo)
                    .collect(Collectors.toList());
            logSystemEvent("🚂 Caricati " + trains.size() + " treni (database)");
            return result;
        } catch (Exception e) {
            logSystemEvent("⚠️ Errore caricamento treni: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> getBigliettiOverview() {
        List<Map<String, Object>> biglietti = new ArrayList<>();

        try {
            List<Biglietto> allBiglietti = bigliettoRepository.findAll();

            for (Biglietto biglietto : allBiglietti) {
                Map<String, Object> bigliettoData = new HashMap<>();
                bigliettoData.put("id", biglietto.getId());
                bigliettoData.put("clienteId", biglietto.getClienteId());
                bigliettoData.put("tipo", biglietto.getTipo());
                bigliettoData.put("partenza", biglietto.getPartenza());
                bigliettoData.put("arrivo", biglietto.getArrivo());
                bigliettoData.put("dataViaggio", biglietto.getDataViaggio().toString());
                bigliettoData.put("prezzo", biglietto.getPrezzo());
                bigliettoData.put("stato", biglietto.getStato() != null ?
                        biglietto.getStato().getNomeStato() : "PAGATO");
                bigliettoData.put("trenoAssociato", biglietto.getTrenoAssociato());

                biglietti.add(bigliettoData);
            }

            logSystemEvent("🎫 Panoramica " + biglietti.size() + " biglietti caricata (database)");

        } catch (Exception e) {
            logSystemEvent("⚠️ Errore caricamento biglietti: " + e.getMessage());
        }

        return biglietti;
    }

    public List<TrainInfo> searchTrains(String query) {
        try {
            List<Treno> allTrains = trenoRepository.findAll();
            if (query == null || query.trim().isEmpty()) {
                return allTrains.stream().map(this::trenoToTrainInfo).collect(Collectors.toList());
            }

            String searchTerm = query.toLowerCase().trim();
            return allTrains.stream()
                    .filter(train ->
                            train.getId().toLowerCase().contains(searchTerm) ||
                                    train.getNome().toLowerCase().contains(searchTerm) ||
                                    (train.getTratta() != null && (
                                            train.getTratta().getStazionePartenza().toLowerCase().contains(searchTerm) ||
                                                    train.getTratta().getStazioneArrivo().toLowerCase().contains(searchTerm)
                                    ))
                    )
                    .map(this::trenoToTrainInfo)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logSystemEvent("⚠️ Errore ricerca treni: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<PromozioneResponse> getAllPromotions() {
        try {
            List<Promozione> promotions = promozioneService.getPromozioni();
            List<PromozioneResponse> result = promotions.stream()
                    .map(this::promozioneToResponse)
                    .collect(Collectors.toList());
            logSystemEvent("💰 Caricate " + promotions.size() + " promozioni (database)");
            return result;
        } catch (Exception e) {
            logSystemEvent("⚠️ Errore caricamento promozioni: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public Promozione createPromotion(String nome, double percentualeSconto,
                                      LocalDateTime inizio, LocalDateTime fine,
                                      String trattaPartenza, String trattaArrivo,
                                      boolean soloFedelta) {
        try {
            Promozione nuovaPromozione = new Promozione(
                    UUID.randomUUID().toString(),
                    nome, percentualeSconto, inizio, fine,
                    trattaPartenza, trattaArrivo, soloFedelta
            );

            Promozione salvata = promozioneService.aggiungiPromozione(nuovaPromozione);
            logSystemEvent("✅ Promozione creata: " + nome);
            refreshDashboardData();
            return salvata;
        } catch (Exception e) {
            logSystemEvent("⚠️ Errore creazione promozione: " + e.getMessage());
            throw e;
        }
    }

    public List<Map<String, Object>> getClientsStatistics() {
        List<Map<String, Object>> clientStats = new ArrayList<>();

        try {
            List<Cliente> allClients = clienteRepository.findAll();

            for (Cliente client : allClients) {
                try {
                    List<Biglietto> tickets = bigliettoRepository.findByClienteId(client.getId());

                    Map<String, Object> stats = new HashMap<>();
                    stats.put("id", client.getId());
                    stats.put("nome", client.getNome());
                    stats.put("email", client.getEmail());
                    stats.put("telefono", client.getTelefono());
                    stats.put("fedelta", client.isFedelta());
                    stats.put("totalTickets", tickets.size());
                    stats.put("totalSpent", calculateTotalSpent(tickets));
                    stats.put("lastActivity", getLastActivity(tickets));

                    clientStats.add(stats);
                } catch (Exception e) {
                    logSystemEvent("⚠️ Errore elaborazione cliente " + client.getId() + ": " + e.getMessage());
                }
            }

            logSystemEvent("👥 Statistiche " + clientStats.size() + " clienti elaborate (database)");

        } catch (Exception e) {
            logSystemEvent("⚠️ Errore elaborazione statistiche clienti: " + e.getMessage());
        }

        return clientStats;
    }

    public List<Map<String, Object>> getBookingsOverview() {
        List<Map<String, Object>> bookings = new ArrayList<>();

        try {
            Collection<Prenotazione> allBookings = prenotazioneService.getPrenotazioniAttive();

            for (Prenotazione booking : allBookings) {
                Map<String, Object> bookingData = new HashMap<>();
                bookingData.put("id", booking.getId());
                bookingData.put("clienteId", booking.getCliente().getId());
                bookingData.put("bigliettoId", booking.getBiglietto().getId());
                bookingData.put("dataCreazione", booking.getDataCreazione().toString());
                bookingData.put("scadenza", booking.getScadenza().toString());
                bookingData.put("attiva", booking.isAttiva());
                bookingData.put("status", booking.isAttiva() ? "ATTIVA" : "SCADUTA");

                bookings.add(bookingData);
            }

            logSystemEvent("🎫 Panoramica " + bookings.size() + " prenotazioni caricata (database)");

        } catch (Exception e) {
            logSystemEvent("⚠️ Errore caricamento prenotazioni: " + e.getMessage());
        }

        return bookings;
    }

    public Map<String, Object> getSystemHealthMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        try {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            long maxMemory = runtime.maxMemory();

            metrics.put("memoryUsed", usedMemory / 1024 / 1024);
            metrics.put("memoryTotal", maxMemory / 1024 / 1024);
            metrics.put("memoryFree", freeMemory / 1024 / 1024);
            metrics.put("memoryUsagePercent", (double) usedMemory / maxMemory * 100);

            metrics.put("availableProcessors", runtime.availableProcessors());
            metrics.put("javaVersion", System.getProperty("java.version"));
            metrics.put("osName", System.getProperty("os.name"));
            metrics.put("osVersion", System.getProperty("os.version"));

            metrics.put("uptime", System.currentTimeMillis());
            metrics.put("timestamp", LocalDateTime.now());
            metrics.put("localConnectionStatus", true);

            metrics.put("totalClients", clienteRepository.count());
            metrics.put("totalTrains", trenoRepository.count());
            metrics.put("totalTickets", bigliettoRepository.count());

            logSystemEvent("💻 Metriche sistema elaborate");

        } catch (Exception e) {
            logSystemEvent("⚠️ Errore raccolta metriche sistema: " + e.getMessage());
        }

        return metrics;
    }

    public List<String> getSystemLogs() {
        return new ArrayList<>(systemLogs);
    }

    public void clearSystemLogs() {
        systemLogs.clear();
        logSystemEvent("🗑️ Log sistema puliti");
    }

    public Map<String, Object> getTrainStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            List<Treno> trains = trenoRepository.findAll();

            Map<String, Long> trainsByType = trains.stream()
                    .collect(Collectors.groupingBy(
                            train -> train.getId().substring(0, Math.min(3, train.getId().length())),
                            Collectors.counting()
                    ));

            stats.put("totalTrains", trains.size());
            stats.put("trainsByType", trainsByType);
            stats.put("mostActiveRoute", findMostActiveRoute(trains));
            stats.put("averageCapacity", trains.stream()
                    .mapToInt(Treno::getPostiTotali)
                    .average().orElse(0.0));

            logSystemEvent("🚂 Statistiche treni elaborate (database)");

        } catch (Exception e) {
            logSystemEvent("⚠️ Errore elaborazione statistiche treni: " + e.getMessage());
        }

        return stats;
    }

    public Map<String, Object> getFinancialMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        try {
            double dailyRevenue = calculateRealDailyRevenue();
            double weeklyRevenue = calculateRealWeeklyRevenue();
            double monthlyRevenue = calculateRealMonthlyRevenue();

            metrics.put("dailyRevenue", Math.round(dailyRevenue * 100.0) / 100.0);
            metrics.put("weeklyRevenue", Math.round(weeklyRevenue * 100.0) / 100.0);
            metrics.put("monthlyRevenue", Math.round(monthlyRevenue * 100.0) / 100.0);

            List<Biglietto> allTickets = bigliettoRepository.findAll();
            double averagePrice = allTickets.stream()
                    .mapToDouble(Biglietto::getPrezzo)
                    .average().orElse(0.0);

            metrics.put("averageTicketPrice", Math.round(averagePrice * 100.0) / 100.0);
            metrics.put("totalTransactions", allTickets.size());

            logSystemEvent("💰 Metriche finanziarie elaborate (database)");

        } catch (Exception e) {
            logSystemEvent("⚠️ Errore elaborazione metriche finanziarie: " + e.getMessage());
        }

        return metrics;
    }

    public void registerClientConnection(String clientId) {
        connectedClients.add(clientId);
        logSystemEvent("👤 Cliente " + clientId + " connesso");
    }

    public void unregisterClientConnection(String clientId) {
        connectedClients.remove(clientId);
        logSystemEvent("👤 Cliente " + clientId + " disconnesso");
    }

    public Set<String> getConnectedClients() {
        return new HashSet<>(connectedClients);
    }

    private void logSystemEvent(String event) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String logEntry = String.format("[%s] %s", timestamp, event);
        systemLogs.add(logEntry);

        if (systemLogs.size() > 1000) {
            systemLogs.remove(0);
        }

        System.out.println(logEntry);
    }

    private TrainInfo trenoToTrainInfo(Treno treno) {
        return TrainInfo.newBuilder()
                .setCodice(treno.getId())
                .setNome(treno.getNome() != null ? treno.getNome() : treno.getId())
                .setTratta(treno.getTratta() != null ?
                        (treno.getTratta().getStazionePartenza() + " → " + treno.getTratta().getStazioneArrivo()) :
                        "N/A")
                .build();
    }

    private PromozioneResponse promozioneToResponse(Promozione promo) {
        return PromozioneResponse.newBuilder()
                .setId(promo.getId())
                .setNome(promo.getNome())
                .setPercentualeSconto(promo.getPercentualeSconto())
                .setInizio(promo.getInizio().toString())
                .setFine(promo.getFine().toString())
                .setTrattaPartenza(promo.getTrattaPartenza() != null ? promo.getTrattaPartenza() : "")
                .setTrattaArrivo(promo.getTrattaArrivo() != null ? promo.getTrattaArrivo() : "")
                .setSoloFedelta(promo.isSoloFedelta())
                .build();
    }

    private double calculateTotalSpent(List<Biglietto> tickets) {
        return tickets.stream()
                .mapToDouble(Biglietto::getPrezzo)
                .sum();
    }

    private String getLastActivity(List<Biglietto> tickets) {
        if (tickets.isEmpty()) {
            return "Nessuna attività";
        }

        return tickets.stream()
                .map(Biglietto::getDataViaggio)
                .max(LocalDateTime::compareTo)
                .map(date -> date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .orElse("N/A");
    }

    private String findMostActiveRoute(List<Treno> trains) {
        Map<String, Long> routeCount = trains.stream()
                .filter(t -> t.getTratta() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getTratta().getStazionePartenza() + " → " + t.getTratta().getStazioneArrivo(),
                        Collectors.counting()));

        return routeCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
    }

    private double calculateRealDailyRevenue() {
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return bigliettoRepository.findAll().stream()
                .filter(b -> b.getDataViaggio().isAfter(today))
                .mapToDouble(Biglietto::getPrezzo)
                .sum();
    }

    private double calculateRealWeeklyRevenue() {
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        return bigliettoRepository.findAll().stream()
                .filter(b -> b.getDataViaggio().isAfter(weekAgo))
                .mapToDouble(Biglietto::getPrezzo)
                .sum();
    }

    private double calculateRealMonthlyRevenue() {
        LocalDateTime monthAgo = LocalDateTime.now().minusMonths(1);
        return bigliettoRepository.findAll().stream()
                .filter(b -> b.getDataViaggio().isAfter(monthAgo))
                .mapToDouble(Biglietto::getPrezzo)
                .sum();
    }
}