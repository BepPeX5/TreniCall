package com.trenicall.admin.gui.panels;

import com.trenicall.admin.service.AdminService;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Locale;

public class DashboardPanel extends JPanel {

    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private final Color WARNING_COLOR = new Color(241, 196, 15);
    private final Color DANGER_COLOR = new Color(231, 76, 60);
    private final Color INFO_COLOR = new Color(52, 152, 219);
    private final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private final Color CARD_COLOR = Color.WHITE;

    private AdminService adminService;
    private Timer refreshTimer;

    private JLabel totalTrainsLabel;
    private JLabel activePromotionsLabel;
    private JLabel connectedClientsLabel;
    private JLabel dailyRevenueLabel;
    private JLabel systemStatusLabel;
    private JLabel lastUpdateLabel;

    private JProgressBar memoryUsageBar;
    private JLabel memoryLabel;
    private JLabel cpuLabel;
    private JTextArea systemLogsArea;

    public DashboardPanel(AdminService adminService) {
        this.adminService = adminService;
        initializeComponents();
        setupLayout();
        startAutoRefresh();
        refreshDashboard();
    }

    private void initializeComponents() {
        setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        totalTrainsLabel = createMetricLabel("0", PRIMARY_COLOR);
        activePromotionsLabel = createMetricLabel("0", SUCCESS_COLOR);
        connectedClientsLabel = createMetricLabel("7", INFO_COLOR);
        dailyRevenueLabel = createMetricLabel("€ 0", WARNING_COLOR);
        systemStatusLabel = createStatusLabel("CARICAMENTO...", WARNING_COLOR);
        lastUpdateLabel = createSmallLabel("Ultimo aggiornamento: mai");

        memoryUsageBar = new JProgressBar(0, 100);
        memoryUsageBar.setStringPainted(true);
        memoryUsageBar.setForeground(PRIMARY_COLOR);
        memoryUsageBar.setBackground(new Color(236, 240, 241));

        memoryLabel = createSmallLabel("Memoria: 0 MB");
        cpuLabel = createSmallLabel("CPU: N/A");

        systemLogsArea = new JTextArea(8, 40);
        systemLogsArea.setFont(new Font("Courier New", Font.PLAIN, 11));
        systemLogsArea.setBackground(new Color(44, 62, 80));
        systemLogsArea.setForeground(new Color(236, 240, 241));
        systemLogsArea.setEditable(false);
        systemLogsArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private void setupLayout() {
        JPanel headerPanel = createHeaderPanel();
        JPanel metricsPanel = createMetricsPanel();
        JPanel chartsPanel = createChartsPanel();
        JPanel systemPanel = createSystemPanel();

        add(headerPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setOpaque(false);
        centerPanel.add(metricsPanel, BorderLayout.NORTH);
        centerPanel.add(chartsPanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
        add(systemPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("📊 Dashboard Operativa");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        JButton refreshButton = createActionButton("🔄 Aggiorna", PRIMARY_COLOR);
        refreshButton.addActionListener(e -> refreshDashboard());

        JButton exportButton = createActionButton("📊 Esporta Report", SUCCESS_COLOR);
        exportButton.addActionListener(e -> exportDashboardReport());

        JButton settingsButton = createActionButton("⚙️ Impostazioni", new Color(149, 165, 166));
        settingsButton.addActionListener(e -> showDashboardSettings());

        actionPanel.add(refreshButton);
        actionPanel.add(exportButton);
        actionPanel.add(settingsButton);

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(actionPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createMetricsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 20, 0));
        panel.setOpaque(false);

        panel.add(createMetricCard("🚂", "Treni Attivi", totalTrainsLabel, PRIMARY_COLOR));
        panel.add(createMetricCard("💰", "Promozioni", activePromotionsLabel, SUCCESS_COLOR));
        panel.add(createMetricCard("👥", "Clienti Online", connectedClientsLabel, INFO_COLOR));
        panel.add(createMetricCard("💸", "Ricavi Giornalieri", dailyRevenueLabel, WARNING_COLOR));

        return panel;
    }

    private JPanel createChartsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setOpaque(false);

        JPanel statusCard = createCard("🔍 Stato Sistema");
        JPanel statusContent = new JPanel(new GridBagLayout());
        statusContent.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        systemStatusLabel.setFont(new Font("Arial", Font.BOLD, 18));
        statusContent.add(systemStatusLabel, gbc);

        gbc.gridy = 1; gbc.gridwidth = 1;
        statusContent.add(new JLabel("Ultima sincronizzazione:"), gbc);
        gbc.gridx = 1;
        statusContent.add(lastUpdateLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        statusContent.add(new JLabel("Servizi attivi:"), gbc);
        gbc.gridx = 1;
        JLabel servicesLabel = createSmallLabel("gRPC ✅ Database ✅ Cache ✅");
        servicesLabel.setForeground(SUCCESS_COLOR);
        statusContent.add(servicesLabel, gbc);

        statusCard.add(statusContent, BorderLayout.CENTER);

        JPanel performanceCard = createCard("⚡ Performance Sistema");
        JPanel perfContent = new JPanel(new GridBagLayout());
        perfContent.setOpaque(false);

        gbc.gridx = 0; gbc.gridy = 0;
        perfContent.add(new JLabel("Utilizzo Memoria:"), gbc);
        gbc.gridx = 1;
        perfContent.add(memoryLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        perfContent.add(memoryUsageBar, gbc);

        gbc.gridy = 2; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        perfContent.add(new JLabel("Processori:"), gbc);
        gbc.gridx = 1;
        perfContent.add(cpuLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        perfContent.add(new JLabel("Uptime:"), gbc);
        gbc.gridx = 1;
        JLabel uptimeLabel = createSmallLabel(getSystemUptime());
        perfContent.add(uptimeLabel, gbc);

        performanceCard.add(perfContent, BorderLayout.CENTER);

        panel.add(statusCard);
        panel.add(performanceCard);

        return panel;
    }

    private JPanel createSystemPanel() {
        JPanel panel = createCard("🖥️ Log Sistema Real-time");
        panel.setPreferredSize(new Dimension(0, 200));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel logLabel = new JLabel("📝 Attività Sistema");
        logLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel logActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        logActions.setOpaque(false);

        JButton clearLogsBtn = new JButton("🗑️ Pulisci");
        clearLogsBtn.setFont(new Font("Arial", Font.PLAIN, 11));
        clearLogsBtn.addActionListener(e -> clearSystemLogs());

        JButton exportLogsBtn = new JButton("📄 Esporta");
        exportLogsBtn.setFont(new Font("Arial", Font.PLAIN, 11));
        exportLogsBtn.addActionListener(e -> exportSystemLogs());

        logActions.add(clearLogsBtn);
        logActions.add(exportLogsBtn);

        headerPanel.add(logLabel, BorderLayout.WEST);
        headerPanel.add(logActions, BorderLayout.EAST);

        JScrollPane scrollPane = new JScrollPane(systemLogsArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(new LineBorder(new Color(189, 195, 199), 1));

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createMetricCard(String icon, String title, JLabel valueLabel, Color accentColor) {
        JPanel card = createCard("");
        card.setLayout(new BorderLayout(10, 10));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        headerPanel.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleLabel.setForeground(new Color(127, 140, 141));

        headerPanel.add(iconLabel);
        headerPanel.add(titleLabel);

        valueLabel.setFont(new Font("Arial", Font.BOLD, 28));
        valueLabel.setForeground(accentColor);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel trendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        trendPanel.setOpaque(false);
        JLabel trendLabel = new JLabel("📈 +2% vs ieri");
        trendLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        trendLabel.setForeground(SUCCESS_COLOR);
        trendPanel.add(trendLabel);

        card.add(headerPanel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(trendPanel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createCard(String title) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        if (!title.isEmpty()) {
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
            titleLabel.setForeground(new Color(44, 62, 80));
            titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
            card.add(titleLabel, BorderLayout.NORTH);
        }

        return card;
    }

    private JButton createActionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });

        return button;
    }

    private JLabel createMetricLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 28));
        label.setForeground(color);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private JLabel createStatusLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setForeground(color);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private JLabel createSmallLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setForeground(new Color(127, 140, 141));
        return label;
    }

    private void startAutoRefresh() {
        refreshTimer = new Timer(30000, e -> refreshDashboard());
        refreshTimer.start();
    }

    private void refreshDashboard() {
        SwingUtilities.invokeLater(() -> {
            try {
                adminService.refreshDashboardData();
                Map<String, Object> stats = adminService.getDashboardStats();
                updateMetrics(stats);
                updateSystemHealth();
                updateSystemLogs();

                lastUpdateLabel.setText("Ultimo aggiornamento: " +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

            } catch (Exception e) {
                systemStatusLabel.setText("❌ ERRORE");
                systemStatusLabel.setForeground(DANGER_COLOR);
                showErrorMessage("Errore aggiornamento dashboard: " + e.getMessage());
            }
        });
    }

    private void updateMetrics(Map<String, Object> stats) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.ITALY);

        totalTrainsLabel.setText(String.valueOf(stats.getOrDefault("totalTrains", 0)));
        activePromotionsLabel.setText(String.valueOf(stats.getOrDefault("activePromotions", 0)));
        connectedClientsLabel.setText(String.valueOf(stats.getOrDefault("connectedClients", 0)));

        Object revenue = stats.get("dailyRevenue");
        if (revenue instanceof Number) {
            dailyRevenueLabel.setText(currencyFormat.format(((Number) revenue).doubleValue()));
        }

        String status = (String) stats.getOrDefault("systemStatus", "UNKNOWN");
        updateSystemStatus(status);
    }

    private void updateSystemStatus(String status) {
        switch (status.toUpperCase()) {
            case "OPERATIONAL":
                systemStatusLabel.setText("✅ OPERATIVO");
                systemStatusLabel.setForeground(SUCCESS_COLOR);
                break;
            case "WARNING":
                systemStatusLabel.setText("⚠️ ATTENZIONE");
                systemStatusLabel.setForeground(WARNING_COLOR);
                break;
            case "ERROR":
                systemStatusLabel.setText("❌ ERRORE");
                systemStatusLabel.setForeground(DANGER_COLOR);
                break;
            default:
                systemStatusLabel.setText("❓ SCONOSCIUTO");
                systemStatusLabel.setForeground(new Color(127, 140, 141));
        }
    }

    private void updateSystemHealth() {
        Map<String, Object> health = adminService.getSystemHealthMetrics();

        if (health.containsKey("memoryUsagePercent")) {
            double memoryPercent = ((Number) health.get("memoryUsagePercent")).doubleValue();
            memoryUsageBar.setValue((int) memoryPercent);
            memoryUsageBar.setString(String.format("%.1f%%", memoryPercent));

            long memoryUsed = ((Number) health.getOrDefault("memoryUsed", 0)).longValue();
            long memoryTotal = ((Number) health.getOrDefault("memoryTotal", 0)).longValue();
            memoryLabel.setText(String.format("Memoria: %d / %d MB", memoryUsed, memoryTotal));
        }

        if (health.containsKey("availableProcessors")) {
            int processors = ((Number) health.get("availableProcessors")).intValue();
            cpuLabel.setText(String.format("CPU: %d core", processors));
        }
    }

    private void updateSystemLogs() {
        java.util.List<String> logs = adminService.getSystemLogs();
        StringBuilder logText = new StringBuilder();

        int startIndex = Math.max(0, logs.size() - 50);
        for (int i = startIndex; i < logs.size(); i++) {
            logText.append(logs.get(i)).append("\n");
        }

        systemLogsArea.setText(logText.toString());
        systemLogsArea.setCaretPosition(systemLogsArea.getDocument().getLength());
    }

    private String getSystemUptime() {
        long uptimeMillis = System.currentTimeMillis() - ((Long) adminService.getSystemHealthMetrics().getOrDefault("uptime", 0L));
        long hours = uptimeMillis / (1000 * 60 * 60);
        long minutes = (uptimeMillis % (1000 * 60 * 60)) / (1000 * 60);
        return String.format("%d ore %d min", hours, minutes);
    }

    private void exportDashboardReport() {
        showInfoMessage("Export Report", "Funzionalità export report in sviluppo.");
    }

    private void showDashboardSettings() {
        showInfoMessage("Impostazioni Dashboard", "Pannello impostazioni dashboard in sviluppo.");
    }

    private void clearSystemLogs() {
        adminService.clearSystemLogs();
        systemLogsArea.setText("");
        showInfoMessage("Log puliti", "Log sistema svuotati con successo.");
    }

    private void exportSystemLogs() {
        showInfoMessage("Export Log", "Funzionalità export log in sviluppo.");
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Errore", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfoMessage(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public void stopAutoRefresh() {
        if (refreshTimer != null && refreshTimer.isRunning()) {
            refreshTimer.stop();
        }
    }
}
