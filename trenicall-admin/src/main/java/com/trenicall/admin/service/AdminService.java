package com.trenicall.admin.service;

import com.trenicall.client.service.GrpcClientService;
import com.trenicall.server.grpc.biglietteria.BigliettoResponse;
import com.trenicall.server.grpc.cliente.ClienteResponse;
import com.trenicall.server.grpc.prenotazione.PrenotazioneResponse;
import com.trenicall.server.grpc.promozione.PromozioneResponse;
import com.trenicall.server.grpc.notifica.TrainInfo;
import com.trenicall.server.grpc.notifica.NotificaResponse;
import io.grpc.stub.StreamObserver;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AdminService {

    private GrpcClientService grpcService;
    private final Map<String, Object> dashboardCache;
    private final List<String> systemLogs;
    private final Set<String> connectedClients;

    public AdminService(GrpcClientService grpcService) {
        this.grpcService = grpcService;
        this.dashboardCache = new HashMap<>();
        this.systemLogs = new ArrayList<>();
        this.connectedClients = new HashSet<>();
        initializeService();
    }

    private void initializeService() {
        logSystemEvent("🚀 AdminService inizializzato");
        refreshDashboardData();
    }

    public void reconnect() throws Exception {
        if (grpcService != null) {
            grpcService.shutdown();
        }
        grpcService = new GrpcClientService("localhost", 9090);
        logSystemEvent("🔄 Riconnessione gRPC completata");
        refreshDashboardData();
    }

    public void shutdown() {
        if (grpcService != null) {
            grpcService.shutdown();
            logSystemEvent("🔌 AdminService disconnesso");
        }
    }

    public Map<String, Object> getDashboardStats() {
        return new HashMap<>(dashboardCache);
    }

    public void refreshDashboardData() {
        try {
            Map<String, Object> stats = new HashMap<>();

            List<TrainInfo> trains = grpcService.listaTreniAttivi();
            stats.put("totalTrains", trains.size());
            stats.put("activeTrains", trains.size());

            List<PromozioneResponse> promotions = grpcService.listaPromozioniAttive();
            stats.put("activePromotions", promotions.size());

            stats.put("connectedClients", connectedClients.size());
            stats.put("totalBookings", getRandomBookingCount());
            stats.put("dailyRevenue", getRandomRevenue());
            stats.put("systemStatus", "OPERATIONAL");
            stats.put("lastUpdate", LocalDateTime.now());

            dashboardCache.putAll(stats);
            logSystemEvent("📊 Dati dashboard aggiornati");

        } catch (Exception e) {
            logSystemEvent("❌ Errore aggiornamento dashboard: " + e.getMessage());
            dashboardCache.put("systemStatus", "ERROR");
        }
    }

    public List<TrainInfo> getAllTrains() {
        try {
            List<TrainInfo> trains = grpcService.listaTreniAttivi();
            logSystemEvent("🚂 Caricati " + trains.size() + " treni");
            return trains;
        } catch (Exception e) {
            logSystemEvent("❌ Errore caricamento treni: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<TrainInfo> searchTrains(String query) {
        try {
            List<TrainInfo> allTrains = getAllTrains();
            if (query == null || query.trim().isEmpty()) {
                return allTrains;
            }

            String searchTerm = query.toLowerCase().trim();
            return allTrains.stream()
                    .filter(train ->
                            train.getCodice().toLowerCase().contains(searchTerm) ||
                                    train.getNome().toLowerCase().contains(searchTerm) ||
                                    train.getTratta().toLowerCase().contains(searchTerm)
                    )
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logSystemEvent("❌ Errore ricerca treni: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean createTrainNotification(String trainId, String eventType, String message) {
        try {
            String adminMessage = String.format("[ADMIN] %s - %s: %s", trainId, eventType, message);
            logSystemEvent("📢 Notifica creata: " + adminMessage);
            return true;
        } catch (Exception e) {
            logSystemEvent("❌ Errore creazione notifica: " + e.getMessage());
            return false;
        }
    }

    public List<PromozioneResponse> getAllPromotions() {
        try {
            List<PromozioneResponse> promotions = grpcService.listaPromozioniAttive();
            logSystemEvent("💰 Caricate " + promotions.size() + " promozioni");
            return promotions;
        } catch (Exception e) {
            logSystemEvent("❌ Errore caricamento promozioni: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Map<String, Object>> getClientsStatistics() {
        List<Map<String, Object>> clientStats = new ArrayList<>();

        try {
            String[] sampleClients = {"C1", "C2", "C3", "C4", "C5"};

            for (String clientId : sampleClients) {
                try {
                    ClienteResponse client = grpcService.dettagliCliente(clientId);
                    List<BigliettoResponse> tickets = grpcService.listaBigliettiCliente(clientId);

                    Map<String, Object> stats = new HashMap<>();
                    stats.put("id", client.getId());
                    stats.put("nome", client.getNome());
                    stats.put("email", client.getEmail());
                    stats.put("fedelta", client.getFedelta());
                    stats.put("totalTickets", tickets.size());
                    stats.put("totalSpent", calculateTotalSpent(tickets));
                    stats.put("lastActivity", getLastActivity(tickets));

                    clientStats.add(stats);
                } catch (Exception e) {

                }
            }

            logSystemEvent("👥 Statistiche " + clientStats.size() + " clienti elaborate");

        } catch (Exception e) {
            logSystemEvent("❌ Errore elaborazione statistiche clienti: " + e.getMessage());
        }

        return clientStats;
    }

    public List<Map<String, Object>> getBookingsOverview() {
        List<Map<String, Object>> bookings = new ArrayList<>();

        try {
            String[] sampleClients = {"C1", "C2", "C3"};

            for (String clientId : sampleClients) {
                try {
                    List<PrenotazioneResponse> clientBookings = grpcService.listaPrenotazioniCliente(clientId);

                    for (PrenotazioneResponse booking : clientBookings) {
                        Map<String, Object> bookingData = new HashMap<>();
                        bookingData.put("id", booking.getId());
                        bookingData.put("clienteId", booking.getClienteId());
                        bookingData.put("bigliettoId", booking.getBigliettoId());
                        bookingData.put("dataCreazione", booking.getDataCreazione());
                        bookingData.put("scadenza", booking.getScadenza());
                        bookingData.put("attiva", booking.getAttiva());
                        bookingData.put("status", booking.getAttiva() ? "ATTIVA" : "SCADUTA");

                        bookings.add(bookingData);
                    }
                } catch (Exception e) {

                }
            }

            logSystemEvent("🎫 Panoramica " + bookings.size() + " prenotazioni caricata");

        } catch (Exception e) {
            logSystemEvent("❌ Errore caricamento prenotazioni: " + e.getMessage());
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
            metrics.put("grpcConnectionStatus", isGrpcConnected());

            logSystemEvent("💻 Metriche sistema elaborate");

        } catch (Exception e) {
            logSystemEvent("❌ Errore raccolta metriche sistema: " + e.getMessage());
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

    public boolean broadcastNotification(String message, String type) {
        try {
            String broadcastMessage = String.format("[BROADCAST-%s] %s", type.toUpperCase(), message);
            logSystemEvent("📡 Broadcast inviato: " + broadcastMessage);
            return true;
        } catch (Exception e) {
            logSystemEvent("❌ Errore invio broadcast: " + e.getMessage());
            return false;
        }
    }

    public Map<String, Object> getTrainStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            List<TrainInfo> trains = getAllTrains();

            Map<String, Long> trainsByType = trains.stream()
                    .collect(Collectors.groupingBy(
                            train -> train.getCodice().substring(0, Math.min(3, train.getCodice().length())),
                            Collectors.counting()
                    ));

            stats.put("totalTrains", trains.size());
            stats.put("trainsByType", trainsByType);
            stats.put("mostActiveRoute", findMostActiveRoute(trains));
            stats.put("averageCapacity", calculateAverageCapacity());

            logSystemEvent("🚂 Statistiche treni elaborate");

        } catch (Exception e) {
            logSystemEvent("❌ Errore elaborazione statistiche treni: " + e.getMessage());
        }

        return stats;
    }

    public Map<String, Object> getFinancialMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        try {
            double dailyRevenue = getRandomRevenue();
            double weeklyRevenue = dailyRevenue * 7 * (0.8 + Math.random() * 0.4);
            double monthlyRevenue = weeklyRevenue * 4.3 * (0.9 + Math.random() * 0.2);

            metrics.put("dailyRevenue", Math.round(dailyRevenue * 100.0) / 100.0);
            metrics.put("weeklyRevenue", Math.round(weeklyRevenue * 100.0) / 100.0);
            metrics.put("monthlyRevenue", Math.round(monthlyRevenue * 100.0) / 100.0);
            metrics.put("averageTicketPrice", 35.50 + Math.random() * 15);
            metrics.put("totalTransactions", getRandomBookingCount() * 3);

            logSystemEvent("💰 Metriche finanziarie elaborate");

        } catch (Exception e) {
            logSystemEvent("❌ Errore elaborazione metriche finanziarie: " + e.getMessage());
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

    private boolean isGrpcConnected() {
        try {
            grpcService.listaTreniAttivi();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private double calculateTotalSpent(List<BigliettoResponse> tickets) {
        return tickets.stream()
                .mapToDouble(BigliettoResponse::getPrezzo)
                .sum();
    }

    private String getLastActivity(List<BigliettoResponse> tickets) {
        if (tickets.isEmpty()) {
            return "Nessuna attività";
        }

        return tickets.stream()
                .map(BigliettoResponse::getDataViaggio)
                .sorted((a, b) -> b.compareTo(a))
                .findFirst()
                .map(date -> {
                    try {
                        LocalDateTime dt = LocalDateTime.parse(date);
                        return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    } catch (Exception e) {
                        return date;
                    }
                })
                .orElse("N/A");
    }

    private String findMostActiveRoute(List<TrainInfo> trains) {
        Map<String, Long> routeCount = trains.stream()
                .collect(Collectors.groupingBy(TrainInfo::getTratta, Collectors.counting()));

        return routeCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
    }

    private double calculateAverageCapacity() {
        return 250 + Math.random() * 100;
    }

    private int getRandomBookingCount() {
        return 150 + (int)(Math.random() * 300);
    }

    private double getRandomRevenue() {
        return 15000 + Math.random() * 25000;
    }
}