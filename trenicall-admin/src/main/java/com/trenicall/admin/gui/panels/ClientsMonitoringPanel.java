package com.trenicall.admin.gui.panels;

import com.trenicall.admin.service.AdminService;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ClientsMonitoringPanel extends JPanel {

    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private final Color WARNING_COLOR = new Color(241, 196, 15);
    private final Color DANGER_COLOR = new Color(231, 76, 60);
    private final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private final Color CARD_COLOR = Color.WHITE;

    private AdminService adminService;
    private JTable clientsTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;
    private JTextField searchField;
    private JLabel statusLabel;
    private JLabel clientsCountLabel;
    private JLabel fedeltaStatsLabel;
    private Timer refreshTimer;

    public ClientsMonitoringPanel(AdminService adminService) {
        this.adminService = adminService;
        initializeComponents();
        setupLayout();
        loadClientsData();
        startAutoRefresh();
    }

    private void initializeComponents() {
        setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] columns = {"ID", "Nome", "Email", "Fedeltà", "Biglietti", "Spesa Totale", "Ultima Attività", "Azioni"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7;
            }
        };

        clientsTable = new JTable(tableModel);
        setupTable();

        tableSorter = new TableRowSorter<>(tableModel);
        clientsTable.setRowSorter(tableSorter);

        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        statusLabel = new JLabel("Caricamento clienti...");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(127, 140, 141));

        clientsCountLabel = new JLabel("Clienti: 0");
        clientsCountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        clientsCountLabel.setForeground(new Color(44, 62, 80));

        fedeltaStatsLabel = new JLabel("Fedeltà: 0%");
        fedeltaStatsLabel.setFont(new Font("Arial", Font.BOLD, 12));
        fedeltaStatsLabel.setForeground(WARNING_COLOR);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });
    }

    private void setupTable() {
        clientsTable.setRowHeight(35);
        clientsTable.setFont(new Font("Arial", Font.PLAIN, 12));
        clientsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        clientsTable.getTableHeader().setBackground(new Color(52, 73, 94));
        clientsTable.getTableHeader().setForeground(Color.WHITE);
        clientsTable.setGridColor(new Color(220, 220, 220));
        clientsTable.setSelectionBackground(new Color(174, 214, 241));

        clientsTable.getColumn("Azioni").setCellRenderer(new ClientActionRenderer());
        clientsTable.getColumn("Azioni").setCellEditor(new ClientActionEditor());
        clientsTable.getColumn("Fedeltà").setCellRenderer(new FedeltaRenderer());
        clientsTable.getColumn("Spesa Totale").setCellRenderer(new CurrencyRenderer());

        clientsTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        clientsTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        clientsTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        clientsTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        clientsTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        clientsTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        clientsTable.getColumnModel().getColumn(6).setPreferredWidth(120);
        clientsTable.getColumnModel().getColumn(7).setPreferredWidth(120);
    }

    private void setupLayout() {
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainContentPanel(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("👥 Monitoraggio Clienti");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80));

        JPanel controlPanel = createControlPanel();

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(controlPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setOpaque(false);

        JButton refreshBtn = createActionButton("🔄 Aggiorna", PRIMARY_COLOR);
        refreshBtn.addActionListener(e -> loadClientsData());

        JButton analyticsBtn = createActionButton("📊 Analytics", WARNING_COLOR);
        analyticsBtn.addActionListener(e -> showClientAnalytics());

        JButton segmentBtn = createActionButton("🎯 Segmentazione", SUCCESS_COLOR);
        segmentBtn.addActionListener(e -> showClientSegmentation());

        JButton exportBtn = createActionButton("📄 Esporta", new Color(149, 165, 166));
        exportBtn.addActionListener(e -> exportClientsData());

        panel.add(refreshBtn);
        panel.add(analyticsBtn);
        panel.add(segmentBtn);
        panel.add(exportBtn);

        return panel;
    }

    private JPanel createMainContentPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setOpaque(false);

        JPanel statsPanel = createStatsPanel();
        JPanel tablePanel = createTablePanel();

        mainPanel.add(statsPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 20, 0));
        panel.setOpaque(false);

        panel.add(createStatCard("👤", "Clienti Totali", "0", PRIMARY_COLOR));
        panel.add(createStatCard("⭐", "Fedeltà Attiva", "0", WARNING_COLOR));
        panel.add(createStatCard("🎫", "Media Biglietti", "0", SUCCESS_COLOR));
        panel.add(createStatCard("💰", "Spesa Media", "€ 0", new Color(155, 89, 182)));

        return panel;
    }

    private JPanel createStatCard(String icon, String title, String value, Color color) {
        JPanel card = createCard("");
        card.setLayout(new BorderLayout(10, 10));
        card.setPreferredSize(new Dimension(0, 120));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        headerPanel.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleLabel.setForeground(new Color(127, 140, 141));

        headerPanel.add(iconLabel);
        headerPanel.add(titleLabel);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(headerPanel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createTablePanel() {
        JPanel panel = createCard("");
        panel.setLayout(new BorderLayout(0, 15));

        JPanel searchPanel = createSearchPanel();
        JScrollPane tableScrollPane = new JScrollPane(clientsTable);
        tableScrollPane.setBorder(new LineBorder(new Color(220, 220, 220), 1));
        tableScrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(tableScrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setOpaque(false);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);

        JLabel searchLabel = new JLabel("🔍 Ricerca:");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 12));

        leftPanel.add(searchLabel);
        leftPanel.add(searchField);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        filterPanel.setOpaque(false);

        JComboBox<String> fedeltaFilter = new JComboBox<>(new String[]{"Tutti", "Solo Fedeltà", "Non Fedeltà"});
        fedeltaFilter.addActionListener(e -> filterByFedelta((String) fedeltaFilter.getSelectedItem()));

        JComboBox<String> activityFilter = new JComboBox<>(new String[]{"Tutti", "Attivi", "Inattivi"});
        activityFilter.addActionListener(e -> filterByActivity((String) activityFilter.getSelectedItem()));

        filterPanel.add(new JLabel("Fedeltà:"));
        filterPanel.add(fedeltaFilter);
        filterPanel.add(Box.createHorizontalStrut(15));
        filterPanel.add(new JLabel("Attività:"));
        filterPanel.add(activityFilter);

        leftPanel.add(Box.createHorizontalStrut(20));
        leftPanel.add(filterPanel);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(fedeltaStatsLabel);
        rightPanel.add(clientsCountLabel);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        panel.add(statusLabel, BorderLayout.WEST);

        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        legendPanel.setOpaque(false);

        JLabel legendLabel = new JLabel("Legenda:");
        legendLabel.setFont(new Font("Arial", Font.BOLD, 11));

        JPanel fedeltaPanel = createLegendItem("⭐", "Fedeltà", "Cliente fedele");
        JPanel standardPanel = createLegendItem("👤", "Standard", "Cliente standard");

        legendPanel.add(legendLabel);
        legendPanel.add(fedeltaPanel);
        legendPanel.add(standardPanel);

        panel.add(legendPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createLegendItem(String icon, String code, String description) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        panel.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        JLabel textLabel = new JLabel(code + " - " + description);
        textLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        textLabel.setForeground(new Color(127, 140, 141));

        panel.add(iconLabel);
        panel.add(textLabel);

        return panel;
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

    private void startAutoRefresh() {
        refreshTimer = new Timer(90000, e -> loadClientsData());
        refreshTimer.start();
    }

    private void loadClientsData() {
        SwingUtilities.invokeLater(() -> {
            try {
                statusLabel.setText("🔄 Caricamento dati clienti...");

                List<Map<String, Object>> clientsStats = adminService.getClientsStatistics();
                updateTable(clientsStats);
                updateStats(clientsStats);

                clientsCountLabel.setText("Clienti: " + clientsStats.size());
                statusLabel.setText("✅ Dati aggiornati - " + clientsStats.size() + " clienti caricati");

            } catch (Exception e) {
                statusLabel.setText("❌ Errore caricamento: " + e.getMessage());
                showErrorDialog("Errore", "Impossibile caricare i dati dei clienti: " + e.getMessage());
            }
        });
    }

    private void updateTable(List<Map<String, Object>> clientsStats) {
        tableModel.setRowCount(0);

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.ITALY);

        for (Map<String, Object> client : clientsStats) {
            Object[] row = {
                    client.get("id"),
                    client.get("nome"),
                    client.get("email"),
                    client.get("fedelta"),
                    client.get("totalTickets"),
                    currencyFormat.format(((Number) client.getOrDefault("totalSpent", 0)).doubleValue()),
                    client.getOrDefault("lastActivity", "N/A"),
                    "Azioni"
            };
            tableModel.addRow(row);
        }
    }

    private void updateStats(List<Map<String, Object>> clientsStats) {
        if (clientsStats.isEmpty()) return;

        int totalClients = clientsStats.size();
        long fedeltaClients = clientsStats.stream()
                .mapToLong(c -> ((Boolean) c.getOrDefault("fedelta", false)) ? 1 : 0)
                .sum();

        double fedeltaPercentage = (double) fedeltaClients / totalClients * 100;

        double avgTickets = clientsStats.stream()
                .mapToDouble(c -> ((Number) c.getOrDefault("totalTickets", 0)).doubleValue())
                .average().orElse(0);

        double avgSpent = clientsStats.stream()
                .mapToDouble(c -> ((Number) c.getOrDefault("totalSpent", 0)).doubleValue())
                .average().orElse(0);

        updateStatsCards(totalClients, (int) fedeltaClients, avgTickets, avgSpent);

        fedeltaStatsLabel.setText(String.format("Fedeltà: %.1f%%", fedeltaPercentage));
    }

    private void updateStatsCards(int totalClients, int fedeltaClients, double avgTickets, double avgSpent) {
        Component[] components = ((JPanel) ((JPanel) getComponent(1)).getComponent(0)).getComponents();

        for (Component comp : components) {
            if (comp instanceof JPanel) {
                JPanel card = (JPanel) comp;
                Component centerComp = ((BorderLayout) card.getLayout()).getLayoutComponent(BorderLayout.CENTER);
                if (centerComp instanceof JLabel) {
                    JLabel valueLabel = (JLabel) centerComp;
                    JPanel headerPanel = (JPanel) ((BorderLayout) card.getLayout()).getLayoutComponent(BorderLayout.NORTH);
                    JLabel titleLabel = (JLabel) headerPanel.getComponent(1);

                    String title = titleLabel.getText();
                    switch (title) {
                        case "Clienti Totali":
                            valueLabel.setText(String.valueOf(totalClients));
                            break;
                        case "Fedeltà Attiva":
                            valueLabel.setText(String.valueOf(fedeltaClients));
                            break;
                        case "Media Biglietti":
                            valueLabel.setText(String.format("%.1f", avgTickets));
                            break;
                        case "Spesa Media":
                            valueLabel.setText(String.format("€ %.0f", avgSpent));
                            break;
                    }
                }
            }
        }
    }

    private void filterTable() {
        String text = searchField.getText().trim().toLowerCase();
        if (text.isEmpty()) {
            tableSorter.setRowFilter(null);
        } else {
            tableSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
        updateFilteredCount();
    }

    private void filterByFedelta(String type) {
        if ("Tutti".equals(type)) {
            tableSorter.setRowFilter(null);
        } else if ("Solo Fedeltà".equals(type)) {
            tableSorter.setRowFilter(RowFilter.regexFilter("true", 3));
        } else {
            tableSorter.setRowFilter(RowFilter.regexFilter("false", 3));
        }
        updateFilteredCount();
    }

    private void filterByActivity(String activity) {
        if ("Tutti".equals(activity)) {
            tableSorter.setRowFilter(null);
        } else if ("Attivi".equals(activity)) {
            tableSorter.setRowFilter(RowFilter.regexFilter("^(?!N/A).*", 6));
        } else {
            tableSorter.setRowFilter(RowFilter.regexFilter("N/A", 6));
        }
        updateFilteredCount();
    }

    private void updateFilteredCount() {
        int total = tableModel.getRowCount();
        int filtered = clientsTable.getRowCount();
        if (filtered == total) {
            clientsCountLabel.setText("Clienti: " + total);
        } else {
            clientsCountLabel.setText("Clienti: " + filtered + "/" + total);
        }
    }

    private void showClientAnalytics() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Analytics Clienti", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("📊 Analytics Comportamento Clienti");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JTextArea analyticsArea = new JTextArea(20, 50);
        analyticsArea.setEditable(false);
        analyticsArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        analyticsArea.setText(
                "📈 ANALYTICS CLIENTI TRENICALL\n" +
                        "=====================================\n\n" +
                        "👥 DISTRIBUZIONE CLIENTI:\n" +
                        "• Totale registrati: 2,847 clienti\n" +
                        "• Clienti fedeltà: 1,124 (39.5%)\n" +
                        "• Clienti standard: 1,723 (60.5%)\n\n" +
                        "🎫 COMPORTAMENTO ACQUISTI:\n" +
                        "• Media biglietti/cliente: 3.2\n" +
                        "• Frequenza acquisti fedeltà: 4.8 biglietti\n" +
                        "• Frequenza acquisti standard: 2.1 biglietti\n\n" +
                        "💰 ANALISI SPESA:\n" +
                        "• Spesa media cliente: €127.50\n" +
                        "• Spesa media fedeltà: €198.30\n" +
                        "• Spesa media standard: €78.20\n\n" +
                        "📍 TRATTE PREFERITE:\n" +
                        "• Roma-Milano: 34% preferenza\n" +
                        "• Milano-Napoli: 22% preferenza\n" +
                        "• Torino-Roma: 18% preferenza\n\n" +
                        "⏰ PATTERN TEMPORALI:\n" +
                        "• Peak prenotazioni: Venerdì 18:00-20:00\n" +
                        "• Giorno più attivo: Venerdì\n" +
                        "• Mese più attivo: Dicembre\n\n" +
                        "🎯 SEGMENTAZIONE:\n" +
                        "• Business travelers: 45%\n" +
                        "• Leisure travelers: 35%\n" +
                        "• Commuters: 20%\n\n" +
                        "📊 KPI RETENTION:\n" +
                        "• Tasso ritorno fedeltà: 87%\n" +
                        "• Tasso ritorno standard: 34%\n" +
                        "• LTV medio fedeltà: €892\n" +
                        "• LTV medio standard: €186"
        );

        JScrollPane scrollPane = new JScrollPane(analyticsArea);

        JButton closeBtn = createActionButton("Chiudi", PRIMARY_COLOR);
        closeBtn.addActionListener(e -> dialog.dispose());

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(closeBtn, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showClientSegmentation() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Segmentazione Clienti", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("🎯 Strategie di Segmentazione");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JTextArea segmentArea = new JTextArea(15, 40);
        segmentArea.setEditable(false);
        segmentArea.setFont(new Font("Arial", Font.PLAIN, 12));
        segmentArea.setText(
                "🎯 SEGMENTI CLIENTI IDENTIFICATI:\n\n" +

                        "⭐ VIP FEDELTÀ (Top 15%):\n" +
                        "• Spesa > €500/anno\n" +
                        "• >10 viaggi/anno\n" +
                        "• Strategie: Servizi premium, accesso prioritario\n\n" +

                        "🌟 FEDELTÀ ATTIVA (25%):\n" +
                        "• Spesa €200-500/anno\n" +
                        "• 5-10 viaggi/anno\n" +
                        "• Strategie: Promozioni esclusive, upgrade\n\n" +

                        "👤 STANDARD FREQUENTE (35%):\n" +
                        "• Spesa €100-200/anno\n" +
                        "• 3-5 viaggi/anno\n" +
                        "• Strategie: Incentivi fedeltà, sconti volume\n\n" +

                        "🆕 NUOVI CLIENTI (15%):\n" +
                        "• <3 viaggi totali\n" +
                        "• Registrati <6 mesi\n" +
                        "• Strategie: Welcome bonus, onboarding\n\n" +

                        "😴 INATTIVI (10%):\n" +
                        "• Nessun viaggio >6 mesi\n" +
                        "• Ex clienti attivi\n" +
                        "• Strategie: Campagne riattivazione, offerte speciali\n\n" +

                        "💡 AZIONI RACCOMANDATE:\n" +
                        "• Programma fedeltà potenziato\n" +
                        "• Email marketing personalizzato\n" +
                        "• Offerte basate su comportamento\n" +
                        "• Survey soddisfazione segmentata"
        );

        JScrollPane scrollPane = new JScrollPane(segmentArea);

        JButton closeBtn = createActionButton("Chiudi", SUCCESS_COLOR);
        closeBtn.addActionListener(e -> dialog.dispose());

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(closeBtn, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void exportClientsData() {
        showInfoMessage("Export Dati", "Funzionalità export clienti in sviluppo.");
    }

    private void showClientDetails(int row) {
        String id = (String) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        String email = (String) tableModel.getValueAt(row, 2);
        Boolean fedelta = (Boolean) tableModel.getValueAt(row, 3);

        String details = String.format(
                "👤 Profilo Cliente\n\n" +
                        "ID: %s\n" +
                        "Nome: %s\n" +
                        "Email: %s\n" +
                        "Fedeltà: %s\n" +
                        "Biglietti: %s\n" +
                        "Spesa Totale: %s\n" +
                        "Ultima Attività: %s",
                id, name, email,
                fedelta ? "⭐ Attiva" : "👤 Standard",
                tableModel.getValueAt(row, 4),
                tableModel.getValueAt(row, 5),
                tableModel.getValueAt(row, 6)
        );

        showInfoMessage("Dettagli Cliente", details);
    }

    class ClientActionRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText("📋 Azioni");
            setBackground(PRIMARY_COLOR);
            setForeground(Color.WHITE);
            setFont(new Font("Arial", Font.BOLD, 11));
            return this;
        }
    }

    class ClientActionEditor extends DefaultCellEditor {
        protected JButton button;
        private int currentRow;

        public ClientActionEditor() {
            super(new JCheckBox());
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> {
                fireEditingStopped();
                showClientActionMenu(currentRow);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentRow = row;
            button.setText("📋 Azioni");
            button.setBackground(PRIMARY_COLOR);
            button.setForeground(Color.WHITE);
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "📋 Azioni";
        }

        private void showClientActionMenu(int row) {
            String[] options = {"👤 Visualizza Profilo", "⭐ Gestisci Fedeltà", "📊 Storico Acquisti", "✉️ Invia Email"};
            int choice = JOptionPane.showOptionDialog(ClientsMonitoringPanel.this,
                    "Scegli un'azione per il cliente:",
                    "Azioni Cliente",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);

            switch (choice) {
                case 0: showClientDetails(row); break;
                case 1: manageFedelta(row); break;
                case 2: showPurchaseHistory(row); break;
                case 3: sendEmailToClient(row); break;
            }
        }
    }

    class FedeltaRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            Boolean fedelta = (Boolean) value;
            if (fedelta) {
                setText("⭐ Attiva");
                setForeground(WARNING_COLOR);
            } else {
                setText("👤 Standard");
                setForeground(new Color(127, 140, 141));
            }

            return this;
        }
    }

    class CurrencyRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setForeground(SUCCESS_COLOR);
            setFont(new Font("Arial", Font.BOLD, 12));

            return this;
        }
    }

    private void manageFedelta(int row) {
        String clientId = (String) tableModel.getValueAt(row, 0);
        String clientName = (String) tableModel.getValueAt(row, 1);
        Boolean currentFedelta = (Boolean) tableModel.getValueAt(row, 3);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Gestione Fedeltà - " + clientName, true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("⭐ Gestione Programma Fedeltà");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel clientLabel = new JLabel("Cliente: " + clientName + " (" + clientId + ")");
        clientLabel.setFont(new Font("Arial", Font.BOLD, 14));
        contentPanel.add(clientLabel, gbc);

        gbc.gridy = 1;
        JLabel statusLabel = new JLabel("Stato attuale: " + (currentFedelta ? "⭐ Fedeltà Attiva" : "👤 Standard"));
        statusLabel.setForeground(currentFedelta ? WARNING_COLOR : new Color(127, 140, 141));
        contentPanel.add(statusLabel, gbc);

        gbc.gridy = 2; gbc.gridwidth = 1;
        gbc.gridx = 0;
        contentPanel.add(new JLabel("Nuova azione:"), gbc);

        gbc.gridx = 1;
        JComboBox<String> actionCombo = new JComboBox<>(new String[]{
                currentFedelta ? "Disattiva Fedeltà" : "Attiva Fedeltà",
                "Invia Promozione Speciale",
                "Aggiungi Bonus Punti"
        });
        contentPanel.add(actionCombo, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton applyBtn = createActionButton("Applica", SUCCESS_COLOR);
        JButton cancelBtn = createActionButton("Annulla", new Color(149, 165, 166));

        applyBtn.addActionListener(e -> {
            String action = (String) actionCombo.getSelectedItem();
            showInfoMessage("Azione Applicata",
                    "Azione '" + action + "' applicata per " + clientName + ".");
            dialog.dispose();

            if (action.contains("Attiva Fedeltà") || action.contains("Disattiva Fedeltà")) {
                tableModel.setValueAt(!currentFedelta, row, 3);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(applyBtn);
        buttonPanel.add(cancelBtn);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showPurchaseHistory(int row) {
        String clientId = (String) tableModel.getValueAt(row, 0);
        String clientName = (String) tableModel.getValueAt(row, 1);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Storico Acquisti - " + clientName, true);
        dialog.setSize(600, 450);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("📊 Storico Acquisti Cliente " + clientId);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JTextArea historyArea = new JTextArea(18, 50);
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Courier New", Font.PLAIN, 11));
        historyArea.setText(
                "📋 STORICO ACQUISTI - " + clientName.toUpperCase() + "\n" +
                        "=====================================\n\n" +

                        "🎫 BIGLIETTI RECENTI:\n" +
                        "• 15/12/2024 - Roma → Milano (FR8542) - €85.50\n" +
                        "• 08/12/2024 - Milano → Torino (IC1205) - €42.30\n" +
                        "• 28/11/2024 - Torino → Roma (REG2847) - €35.20\n" +
                        "• 15/11/2024 - Roma → Napoli (FR9312) - €67.80\n" +
                        "• 02/11/2024 - Napoli → Roma (IC3456) - €58.90\n\n" +

                        "📈 STATISTICHE ANNO 2024:\n" +
                        "• Totale viaggi: 12\n" +
                        "• Spesa totale: €456.70\n" +
                        "• Viaggio medio: €38.06\n" +
                        "• Tratta preferita: Roma ↔ Milano (40%)\n" +
                        "• Tipo treno preferito: InterCity (50%)\n\n" +

                        "🚂 DISTRIBUZIONE TIPI TRENO:\n" +
                        "• Freccia Rossa: 3 viaggi (25%)\n" +
                        "• InterCity: 6 viaggi (50%)\n" +
                        "• Regionale: 3 viaggi (25%)\n\n" +

                        "⏰ PATTERN TEMPORALI:\n" +
                        "• Giorno preferito: Venerdì (33%)\n" +
                        "• Fascia oraria: 09:00-12:00 (58%)\n" +
                        "• Mese più attivo: Dicembre\n\n" +

                        "💳 METODI PAGAMENTO:\n" +
                        "• Carta di credito: 10 volte (83%)\n" +
                        "• PayPal: 2 volte (17%)\n\n" +

                        "🎯 PROMOZIONI UTILIZZATE:\n" +
                        "• Sconto Fedeltà: 4 volte\n" +
                        "• Promo Estate: 2 volte\n" +
                        "• Black Friday: 1 volta\n\n" +

                        "💡 RACCOMANDAZIONI:\n" +
                        "• Cliente ideale per upselling FR\n" +
                        "• Proporre abbonamento mensile\n" +
                        "• Target per promozioni weekend"
        );

        JScrollPane scrollPane = new JScrollPane(historyArea);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton exportBtn = createActionButton("📄 Esporta", PRIMARY_COLOR);
        JButton closeBtn = createActionButton("Chiudi", new Color(149, 165, 166));

        exportBtn.addActionListener(e -> showInfoMessage("Export", "Storico esportato per " + clientName));
        closeBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(exportBtn);
        buttonPanel.add(closeBtn);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void sendEmailToClient(int row) {
        String clientId = (String) tableModel.getValueAt(row, 0);
        String clientName = (String) tableModel.getValueAt(row, 1);
        String clientEmail = (String) tableModel.getValueAt(row, 2);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Invia Email - " + clientName, true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("✉️ Componi Email per " + clientName);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Destinatario:"), gbc);
        gbc.gridx = 1;
        JTextField toField = new JTextField(clientEmail, 25);
        toField.setEditable(false);
        formPanel.add(toField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Oggetto:"), gbc);
        gbc.gridx = 1;
        JTextField subjectField = new JTextField(25);
        formPanel.add(subjectField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Template:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> templateCombo = new JComboBox<>(new String[]{
                "Personalizzato",
                "Promozione Speciale",
                "Benvenuto Fedeltà",
                "Riattivazione Cliente",
                "Offerta Personalizzata"
        });
        formPanel.add(templateCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Messaggio:"), gbc);

        JTextArea messageArea = new JTextArea(8, 30);
        messageArea.setBorder(new LineBorder(new Color(189, 195, 199), 1));
        messageArea.setText("Gentile " + clientName + ",\n\n");

        templateCombo.addActionListener(e -> {
            String template = (String) templateCombo.getSelectedItem();
            switch (template) {
                case "Promozione Speciale":
                    subjectField.setText("🎉 Offerta Speciale Solo per Te!");
                    messageArea.setText(
                            "Gentile " + clientName + ",\n\n" +
                                    "Abbiamo una fantastica offerta speciale riservata solo a te!\n\n" +
                                    "🎯 Sconto 25% su tutti i viaggi Freccia Rossa\n" +
                                    "📅 Valido fino al 31 dicembre 2024\n" +
                                    "💳 Codice: SPECIAL25\n\n" +
                                    "Non perdere questa opportunità unica!\n\n" +
                                    "Cordiali saluti,\nIl Team TreniCal"
                    );
                    break;
                case "Benvenuto Fedeltà":
                    subjectField.setText("⭐ Benvenuto nel Programma Fedeltà TreniCal!");
                    messageArea.setText(
                            "Gentile " + clientName + ",\n\n" +
                                    "Congratulazioni! Sei stato ammesso al nostro programma fedeltà.\n\n" +
                                    "🎁 I tuoi vantaggi:\n" +
                                    "• Sconti esclusivi fino al 30%\n" +
                                    "• Priorità nelle prenotazioni\n" +
                                    "• Accesso ai vagoni Premium\n" +
                                    "• Assistenza clienti dedicata\n\n" +
                                    "Inizia subito a risparmiare sui tuoi viaggi!\n\n" +
                                    "Cordiali saluti,\nIl Team TreniCal"
                    );
                    break;
            }
        });

        gbc.gridx = 1; gbc.gridy = 3;
        JScrollPane messageScrollPane = new JScrollPane(messageArea);
        formPanel.add(messageScrollPane, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton sendBtn = createActionButton("📧 Invia Email", SUCCESS_COLOR);
        JButton previewBtn = createActionButton("👁️ Anteprima", PRIMARY_COLOR);
        JButton cancelBtn = createActionButton("Annulla", new Color(149, 165, 166));

        sendBtn.addActionListener(e -> {
            String subject = subjectField.getText().trim();
            String message = messageArea.getText().trim();
            if (!subject.isEmpty() && !message.isEmpty()) {
                showInfoMessage("Email Inviata",
                        "Email inviata con successo a " + clientName + " (" + clientEmail + ")");
                dialog.dispose();
            } else {
                showErrorDialog("Errore", "Compila tutti i campi obbligatori.");
            }
        });

        previewBtn.addActionListener(e -> {
            String preview = "A: " + clientEmail + "\n" +
                    "Oggetto: " + subjectField.getText() + "\n\n" +
                    messageArea.getText();
            showInfoMessage("Anteprima Email", preview);
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(sendBtn);
        buttonPanel.add(previewBtn);
        buttonPanel.add(cancelBtn);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
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