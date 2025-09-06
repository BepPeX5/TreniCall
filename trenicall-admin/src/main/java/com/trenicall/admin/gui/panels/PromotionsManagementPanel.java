package com.trenicall.admin.gui.panels;

import com.trenicall.admin.service.AdminService;
import com.trenicall.server.grpc.promozione.PromozioneResponse;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PromotionsManagementPanel extends JPanel {

    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private final Color WARNING_COLOR = new Color(241, 196, 15);
    private final Color DANGER_COLOR = new Color(231, 76, 60);
    private final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private final Color CARD_COLOR = Color.WHITE;

    private AdminService adminService;
    private JTable promotionsTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;
    private JTextField searchField;
    private JLabel statusLabel;
    private JLabel promotionsCountLabel;
    private Timer refreshTimer;

    public PromotionsManagementPanel(AdminService adminService) {
        this.adminService = adminService;
        initializeComponents();
        setupLayout();
        loadPromotionsData();
        startAutoRefresh();
    }

    private void initializeComponents() {
        setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] columns = {"ID", "Nome", "Sconto", "Inizio", "Fine", "Tratta", "Solo Fedeltà", "Stato", "Azioni"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 8;
            }
        };

        promotionsTable = new JTable(tableModel);
        setupTable();

        tableSorter = new TableRowSorter<>(tableModel);
        promotionsTable.setRowSorter(tableSorter);

        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        statusLabel = new JLabel("Caricamento promozioni...");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(127, 140, 141));

        promotionsCountLabel = new JLabel("Promozioni: 0");
        promotionsCountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        promotionsCountLabel.setForeground(new Color(44, 62, 80));

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });
    }

    private void setupTable() {
        promotionsTable.setRowHeight(35);
        promotionsTable.setFont(new Font("Arial", Font.PLAIN, 12));
        promotionsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        promotionsTable.getTableHeader().setBackground(new Color(52, 73, 94));
        promotionsTable.getTableHeader().setForeground(Color.WHITE);
        promotionsTable.setGridColor(new Color(220, 220, 220));
        promotionsTable.setSelectionBackground(new Color(174, 214, 241));

        promotionsTable.getColumn("Azioni").setCellRenderer(new PromotionActionRenderer());
        promotionsTable.getColumn("Azioni").setCellEditor(new PromotionActionEditor());
        promotionsTable.getColumn("Stato").setCellRenderer(new PromotionStatusRenderer());
        promotionsTable.getColumn("Solo Fedeltà").setCellRenderer(new BooleanRenderer());
        promotionsTable.getColumn("Sconto").setCellRenderer(new PercentageRenderer());

        promotionsTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        promotionsTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        promotionsTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        promotionsTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        promotionsTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        promotionsTable.getColumnModel().getColumn(5).setPreferredWidth(200);
        promotionsTable.getColumnModel().getColumn(6).setPreferredWidth(90);
        promotionsTable.getColumnModel().getColumn(7).setPreferredWidth(80);
        promotionsTable.getColumnModel().getColumn(8).setPreferredWidth(120);
    }

    private void setupLayout() {
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("💰 Gestione Promozioni");
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

        JButton addPromotionBtn = createActionButton("➕ Nuova Promozione", SUCCESS_COLOR);
        addPromotionBtn.addActionListener(e -> showAddPromotionDialog());

        JButton refreshBtn = createActionButton("🔄 Aggiorna", PRIMARY_COLOR);
        refreshBtn.addActionListener(e -> loadPromotionsData());

        JButton campaignBtn = createActionButton("📊 Campagne", WARNING_COLOR);
        campaignBtn.addActionListener(e -> showCampaignAnalysis());

        JButton exportBtn = createActionButton("📄 Esporta", new Color(149, 165, 166));
        exportBtn.addActionListener(e -> exportPromotionsData());

        panel.add(addPromotionBtn);
        panel.add(refreshBtn);
        panel.add(campaignBtn);
        panel.add(exportBtn);

        return panel;
    }

    private JPanel createMainPanel() {
        JPanel panel = createCard("");
        panel.setLayout(new BorderLayout(0, 15));

        JPanel searchPanel = createSearchPanel();
        JScrollPane tableScrollPane = new JScrollPane(promotionsTable);
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

        JComboBox<String> statusFilter = new JComboBox<>(new String[]{"Tutti gli stati", "Attiva", "Scaduta", "In programma"});
        statusFilter.addActionListener(e -> filterByStatus((String) statusFilter.getSelectedItem()));

        JComboBox<String> fedeltaFilter = new JComboBox<>(new String[]{"Tutti", "Solo Fedeltà", "Tutti i clienti"});
        fedeltaFilter.addActionListener(e -> filterByFedelta((String) fedeltaFilter.getSelectedItem()));

        filterPanel.add(new JLabel("Stato:"));
        filterPanel.add(statusFilter);
        filterPanel.add(Box.createHorizontalStrut(15));
        filterPanel.add(new JLabel("Tipo:"));
        filterPanel.add(fedeltaFilter);

        leftPanel.add(Box.createHorizontalStrut(20));
        leftPanel.add(filterPanel);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        rightPanel.add(promotionsCountLabel);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        panel.add(statusLabel, BorderLayout.WEST);

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        statsPanel.setOpaque(false);

        JLabel statsLabel = new JLabel("📈 Statistiche:");
        statsLabel.setFont(new Font("Arial", Font.BOLD, 11));

        JLabel activeLabel = new JLabel("🟢 Attive: 0");
        activeLabel.setFont(new Font("Arial", Font.PLAIN, 11));

        JLabel expiredLabel = new JLabel("🔴 Scadute: 0");
        expiredLabel.setFont(new Font("Arial", Font.PLAIN, 11));

        statsPanel.add(statsLabel);
        statsPanel.add(activeLabel);
        statsPanel.add(expiredLabel);

        panel.add(statsPanel, BorderLayout.EAST);

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
        refreshTimer = new Timer(45000, e -> loadPromotionsData());
        refreshTimer.start();
    }

    private void loadPromotionsData() {
        SwingUtilities.invokeLater(() -> {
            try {
                statusLabel.setText("🔄 Caricamento promozioni...");

                List<PromozioneResponse> promotions = adminService.getAllPromotions();
                updateTable(promotions);

                promotionsCountLabel.setText("Promozioni: " + promotions.size());
                statusLabel.setText("✅ Dati aggiornati - " + promotions.size() + " promozioni caricate");

            } catch (Exception e) {
                statusLabel.setText("❌ Errore caricamento: " + e.getMessage());
                showErrorDialog("Errore", "Impossibile caricare le promozioni: " + e.getMessage());
            }
        });
    }

    private void updateTable(List<PromozioneResponse> promotions) {
        tableModel.setRowCount(0);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (PromozioneResponse promo : promotions) {
            String status = getPromotionStatus(promo);
            String startDate = formatDate(promo.getInizio());
            String endDate = formatDate(promo.getFine());
            String route = promo.getTrattaPartenza() + " → " + promo.getTrattaArrivo();

            Object[] row = {
                    promo.getId(),
                    promo.getNome(),
                    Math.round(promo.getPercentualeSconto() * 100) + "%",
                    startDate,
                    endDate,
                    route,
                    promo.getSoloFedelta(),
                    status,
                    "Azioni"
            };
            tableModel.addRow(row);
        }
    }

    private String getPromotionStatus(PromozioneResponse promo) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = LocalDateTime.parse(promo.getInizio());
            LocalDateTime end = LocalDateTime.parse(promo.getFine());

            if (now.isBefore(start)) {
                return "In programma";
            } else if (now.isAfter(end)) {
                return "Scaduta";
            } else {
                return "Attiva";
            }
        } catch (Exception e) {
            return "Sconosciuto";
        }
    }

    private String formatDate(String dateTime) {
        try {
            LocalDateTime dt = LocalDateTime.parse(dateTime);
            return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return dateTime;
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

    private void filterByStatus(String status) {
        if ("Tutti gli stati".equals(status)) {
            tableSorter.setRowFilter(null);
        } else {
            tableSorter.setRowFilter(RowFilter.regexFilter(status, 7));
        }
        updateFilteredCount();
    }

    private void filterByFedelta(String type) {
        if ("Tutti".equals(type)) {
            tableSorter.setRowFilter(null);
        } else if ("Solo Fedeltà".equals(type)) {
            tableSorter.setRowFilter(RowFilter.regexFilter("true", 6));
        } else {
            tableSorter.setRowFilter(RowFilter.regexFilter("false", 6));
        }
        updateFilteredCount();
    }

    private void updateFilteredCount() {
        int total = tableModel.getRowCount();
        int filtered = promotionsTable.getRowCount();
        if (filtered == total) {
            promotionsCountLabel.setText("Promozioni: " + total);
        } else {
            promotionsCountLabel.setText("Promozioni: " + filtered + "/" + total);
        }
    }

    private void showAddPromotionDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Nuova Promozione", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        String[] labels = {"Nome Promozione:", "Descrizione:", "Sconto (%):", "Data Inizio:", "Data Fine:", "Tratta Partenza:", "Tratta Arrivo:"};
        JComponent[] fields = {
                new JTextField(20),
                new JTextField(20),
                new JSpinner(new SpinnerNumberModel(10, 1, 50, 1)),
                new JTextField("2025-12-25T00:00:00", 20),
                new JTextField("2025-12-31T23:59:59", 20),
                new JComboBox<>(new String[]{"Roma Termini", "Milano Centrale", "Napoli Centrale", "Torino Porta Nuova",
                        "Firenze Santa Maria Novella", "Bologna Centrale", "Venice Santa Lucia",
                        "Bari Centrale", "Palermo Centrale", "Genova Piazza Principe", "Cosenza"}),
                new JComboBox<>(new String[]{"Roma Termini", "Milano Centrale", "Napoli Centrale", "Torino Porta Nuova",
                        "Firenze Santa Maria Novella", "Bologna Centrale", "Venice Santa Lucia",
                        "Bari Centrale", "Palermo Centrale", "Genova Piazza Principe", "Cosenza"})
        };

        for (int i = 0; i < labels.length; i++) {
            gbc.gridy = i;
            gbc.gridx = 0;
            gbc.anchor = GridBagConstraints.WEST;
            panel.add(new JLabel(labels[i]), gbc);

            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(fields[i], gbc);
        }

        gbc.gridy = labels.length;
        gbc.gridx = 0;
        JCheckBox fedeltaCheck = new JCheckBox("Solo per clienti fedeltà");
        panel.add(fedeltaCheck, gbc);

        gbc.gridy = labels.length + 1;
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton createBtn = createActionButton("Crea Promozione", SUCCESS_COLOR);
        JButton cancelBtn = createActionButton("Annulla", new Color(149, 165, 166));

        createBtn.addActionListener(e -> {
            try {
                String nome = ((JTextField)fields[0]).getText().trim();
                double sconto = ((Integer)((JSpinner)fields[2]).getValue()) / 100.0;
                String dataInizio = ((JTextField)fields[3]).getText().trim();
                String dataFine = ((JTextField)fields[4]).getText().trim();
                String partenza = (String)((JComboBox<?>)fields[5]).getSelectedItem();
                String arrivo = (String)((JComboBox<?>)fields[6]).getSelectedItem();
                boolean fedeltaOnly = fedeltaCheck.isSelected();

                if (nome.isEmpty()) {
                    showErrorDialog("Errore", "Inserisci il nome della promozione");
                    return;
                }

                LocalDateTime inizio = LocalDateTime.parse(dataInizio);
                LocalDateTime fine = LocalDateTime.parse(dataFine);

                adminService.createPromotion(nome, sconto, inizio, fine, partenza, arrivo, fedeltaOnly);

                showInfoMessage("Successo", "Promozione '" + nome + "' creata con successo!");
                dialog.dispose();

                loadPromotionsData();

            } catch (Exception ex) {
                showErrorDialog("Errore", "Errore nella creazione: " + ex.getMessage());
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(createBtn);
        buttonPanel.add(cancelBtn);
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showCampaignAnalysis() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Analisi Campagne", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("📊 Analisi Performance Campagne");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JTextArea analysisArea = new JTextArea(15, 40);
        analysisArea.setEditable(false);
        analysisArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        analysisArea.setText(
                "📈 REPORT CAMPAGNE MARKETING\n" +
                        "=====================================\n\n" +
                        "🎯 Promozioni più efficaci:\n" +
                        "• Estate 2024: +35% vendite\n" +
                        "• Fedeltà Premium: +28% retention\n" +
                        "• Black Friday: +52% conversioni\n\n" +
                        "📍 Tratte più popolari:\n" +
                        "• Roma-Milano: 45% del traffico\n" +
                        "• Milano-Napoli: 23% del traffico\n" +
                        "• Torino-Roma: 18% del traffico\n\n" +
                        "👥 Segmentazione clienti:\n" +
                        "• Fedeltà: 65% utilizzo promozioni\n" +
                        "• Standard: 35% utilizzo promozioni\n\n" +
                        "💰 ROI Medio Campagne: +240%\n" +
                        "📅 Periodo migliore: Dicembre-Gennaio\n\n" +
                        "🔍 Raccomandazioni:\n" +
                        "• Aumentare promozioni fedeltà\n" +
                        "• Focus su tratte ad alto traffico\n" +
                        "• Campagne stagionali mirate"
        );

        JScrollPane scrollPane = new JScrollPane(analysisArea);

        JButton closeBtn = createActionButton("Chiudi", PRIMARY_COLOR);
        closeBtn.addActionListener(e -> dialog.dispose());

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(closeBtn, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void exportPromotionsData() {
        showInfoMessage("Export Dati", "Funzionalità export promozioni in sviluppo.");
    }

    private void showPromotionDetails(int row) {
        String id = (String) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        String discount = (String) tableModel.getValueAt(row, 2);
        String route = (String) tableModel.getValueAt(row, 5);

        String details = String.format(
                "💰 Dettagli Promozione\n\n" +
                        "ID: %s\n" +
                        "Nome: %s\n" +
                        "Sconto: %s\n" +
                        "Tratta: %s\n" +
                        "Inizio: %s\n" +
                        "Fine: %s\n" +
                        "Solo Fedeltà: %s\n" +
                        "Stato: %s",
                id, name, discount, route,
                tableModel.getValueAt(row, 3),
                tableModel.getValueAt(row, 4),
                tableModel.getValueAt(row, 6),
                tableModel.getValueAt(row, 7)
        );

        showInfoMessage("Dettagli Promozione", details);
    }

    class PromotionActionRenderer extends JButton implements javax.swing.table.TableCellRenderer {
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

    class PromotionActionEditor extends DefaultCellEditor {
        protected JButton button;
        private int currentRow;

        public PromotionActionEditor() {
            super(new JCheckBox());
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> {
                fireEditingStopped();
                showPromotionActionMenu(currentRow);
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

        private void showPromotionActionMenu(int row) {
            String[] options = {"📋 Visualizza Dettagli", "✏️ Modifica", "📊 Statistiche", "🗑️ Elimina"};
            int choice = JOptionPane.showOptionDialog(PromotionsManagementPanel.this,
                    "Scegli un'azione per la promozione:",
                    "Azioni Promozione",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);

            switch (choice) {
                case 0: showPromotionDetails(row); break;
                case 1: showInfoMessage("Modifica", "Funzionalità modifica in sviluppo."); break;
                case 2: showInfoMessage("Statistiche", "Funzionalità statistiche in sviluppo."); break;
                case 3: showInfoMessage("Elimina", "Funzionalità eliminazione in sviluppo."); break;
            }
        }
    }

    class PromotionStatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String status = (String) value;
            switch (status) {
                case "Attiva":
                    setForeground(SUCCESS_COLOR);
                    setText("🟢 " + status);
                    break;
                case "Scaduta":
                    setForeground(DANGER_COLOR);
                    setText("🔴 " + status);
                    break;
                case "In programma":
                    setForeground(WARNING_COLOR);
                    setText("🟡 " + status);
                    break;
                default:
                    setForeground(Color.BLACK);
            }

            return this;
        }
    }

    class BooleanRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            Boolean boolValue = (Boolean) value;
            if (boolValue) {
                setText("⭐ Sì");
                setForeground(WARNING_COLOR);
            } else {
                setText("👥 No");
                setForeground(new Color(127, 140, 141));
            }

            return this;
        }
    }

    class PercentageRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setForeground(SUCCESS_COLOR);
            setFont(new Font("Arial", Font.BOLD, 12));

            return this;
        }
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