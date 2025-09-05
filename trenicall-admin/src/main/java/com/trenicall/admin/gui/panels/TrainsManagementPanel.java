package com.trenicall.admin.gui.panels;

import com.trenicall.admin.service.AdminService;
import com.trenicall.server.grpc.notifica.TrainInfo;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

public class TrainsManagementPanel extends JPanel {

    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private final Color WARNING_COLOR = new Color(241, 196, 15);
    private final Color DANGER_COLOR = new Color(231, 76, 60);
    private final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private final Color CARD_COLOR = Color.WHITE;

    private AdminService adminService;

    private JTable trainsTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;
    private JTextField searchField;
    private JLabel statusLabel;
    private JLabel trainsCountLabel;

    private Timer refreshTimer;

    public TrainsManagementPanel(AdminService adminService) {
        this.adminService = adminService;
        initializeComponents();
        setupLayout();
        loadTrainsData();
        startAutoRefresh();
    }

    private void initializeComponents() {
        setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] columns = {"Codice", "Nome Treno", "Tipo", "Tratta", "Stato", "Capacità", "Azioni"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };

        trainsTable = new JTable(tableModel);
        setupTable();

        tableSorter = new TableRowSorter<>(tableModel);
        trainsTable.setRowSorter(tableSorter);

        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        statusLabel = new JLabel("Caricamento treni...");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(127, 140, 141));

        trainsCountLabel = new JLabel("Treni: 0");
        trainsCountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        trainsCountLabel.setForeground(new Color(44, 62, 80));

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterTable();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterTable();
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterTable();
            }
        });
    }

    private void setupTable() {
        trainsTable.setRowHeight(35);
        trainsTable.setFont(new Font("Arial", Font.PLAIN, 12));
        trainsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        trainsTable.getTableHeader().setBackground(new Color(52, 73, 94));
        trainsTable.getTableHeader().setForeground(Color.WHITE);
        trainsTable.getTableHeader().setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        trainsTable.setGridColor(new Color(220, 220, 220));
        trainsTable.setSelectionBackground(new Color(174, 214, 241));
        trainsTable.setShowVerticalLines(true);
        trainsTable.setShowHorizontalLines(true);

        trainsTable.getColumn("Azioni").setCellRenderer(new TrainActionRenderer());
        trainsTable.getColumn("Azioni").setCellEditor(new TrainActionEditor());
        trainsTable.getColumn("Stato").setCellRenderer(new TrainStatusRenderer());
        trainsTable.getColumn("Tipo").setCellRenderer(new TrainTypeRenderer());

        trainsTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        trainsTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        trainsTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        trainsTable.getColumnModel().getColumn(3).setPreferredWidth(250);
        trainsTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        trainsTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        trainsTable.getColumnModel().getColumn(6).setPreferredWidth(150);
    }

    private void setupLayout() {
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("🚂 Gestione Treni");
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

        JButton addTrainBtn = createActionButton("➕ Nuovo Treno", SUCCESS_COLOR);
        addTrainBtn.addActionListener(e -> showAddTrainDialog());

        JButton refreshBtn = createActionButton("🔄 Aggiorna", PRIMARY_COLOR);
        refreshBtn.addActionListener(e -> loadTrainsData());

        JButton exportBtn = createActionButton("📊 Esporta", new Color(149, 165, 166));
        exportBtn.addActionListener(e -> exportTrainsData());

        JButton notificationBtn = createActionButton("📢 Notifica Broadcast", WARNING_COLOR);
        notificationBtn.addActionListener(e -> showBroadcastDialog());

        panel.add(addTrainBtn);
        panel.add(refreshBtn);
        panel.add(exportBtn);
        panel.add(notificationBtn);

        return panel;
    }

    private JPanel createMainPanel() {
        JPanel panel = createCard("");
        panel.setLayout(new BorderLayout(0, 15));

        JPanel searchPanel = createSearchPanel();
        JScrollPane tableScrollPane = new JScrollPane(trainsTable);
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

        JComboBox<String> typeFilter = new JComboBox<>(new String[]{"Tutti i tipi", "REG", "IC", "FR"});
        typeFilter.addActionListener(e -> filterByType((String) typeFilter.getSelectedItem()));

        JComboBox<String> statusFilter = new JComboBox<>(new String[]{"Tutti gli stati", "Attivo", "Manutenzione", "Fuori servizio"});
        statusFilter.addActionListener(e -> filterByStatus((String) statusFilter.getSelectedItem()));

        JLabel typeLabel = new JLabel("Tipo:");
        typeLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        JLabel statusLabel = new JLabel("Stato:");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));

        filterPanel.add(typeLabel);
        filterPanel.add(typeFilter);
        filterPanel.add(Box.createHorizontalStrut(15));
        filterPanel.add(statusLabel);
        filterPanel.add(statusFilter);

        leftPanel.add(Box.createHorizontalStrut(20));
        leftPanel.add(filterPanel);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        rightPanel.add(trainsCountLabel);

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

        JPanel regPanel = createLegendItem("🟢", "REG", "Regionali");
        JPanel icPanel = createLegendItem("🟡", "IC", "InterCity");
        JPanel frPanel = createLegendItem("🔴", "FR", "Freccia Rossa");

        legendPanel.add(legendLabel);
        legendPanel.add(regPanel);
        legendPanel.add(icPanel);
        legendPanel.add(frPanel);

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
        refreshTimer = new Timer(60000, e -> loadTrainsData());
        refreshTimer.start();
    }

    private void loadTrainsData() {
        SwingUtilities.invokeLater(() -> {
            try {
                statusLabel.setText("🔄 Caricamento dati treni...");

                List<TrainInfo> trains = adminService.getAllTrains();
                updateTable(trains);

                trainsCountLabel.setText("Treni: " + trains.size());
                statusLabel.setText("✅ Dati aggiornati - " + trains.size() + " treni caricati");

            } catch (Exception e) {
                statusLabel.setText("❌ Errore caricamento: " + e.getMessage());
                showErrorDialog("Errore", "Impossibile caricare i dati dei treni: " + e.getMessage());
            }
        });
    }

    private void updateTable(List<TrainInfo> trains) {
        tableModel.setRowCount(0);

        for (TrainInfo train : trains) {
            String trainType = getTrainType(train.getCodice());
            String status = getRandomStatus();
            String capacity = getRandomCapacity();

            Object[] row = {
                    train.getCodice(),
                    train.getNome(),
                    trainType,
                    train.getTratta(),
                    status,
                    capacity,
                    "Azioni"
            };
            tableModel.addRow(row);
        }
    }

    private String getTrainType(String code) {
        if (code.startsWith("REG")) return "REG";
        if (code.startsWith("IC")) return "IC";
        if (code.startsWith("FR")) return "FR";
        return "ALTRO";
    }

    private String getRandomStatus() {
        String[] statuses = {"Attivo", "Attivo", "Attivo", "Manutenzione", "Attivo"};
        return statuses[(int) (Math.random() * statuses.length)];
    }

    private String getRandomCapacity() {
        int[] capacities = {200, 250, 300, 350, 400};
        int capacity = capacities[(int) (Math.random() * capacities.length)];
        int occupied = (int) (Math.random() * capacity * 0.8);
        return occupied + "/" + capacity;
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

    private void filterByType(String type) {
        if ("Tutti i tipi".equals(type)) {
            tableSorter.setRowFilter(null);
        } else {
            tableSorter.setRowFilter(RowFilter.regexFilter(type, 2));
        }
        updateFilteredCount();
    }

    private void filterByStatus(String status) {
        if ("Tutti gli stati".equals(status)) {
            tableSorter.setRowFilter(null);
        } else {
            tableSorter.setRowFilter(RowFilter.regexFilter(status, 4));
        }
        updateFilteredCount();
    }

    private void updateFilteredCount() {
        int total = tableModel.getRowCount();
        int filtered = trainsTable.getRowCount();
        if (filtered == total) {
            trainsCountLabel.setText("Treni: " + total);
        } else {
            trainsCountLabel.setText("Treni: " + filtered + "/" + total);
        }
    }

    private void showAddTrainDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Nuovo Treno", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        String[] labels = {"Codice Treno:", "Nome:", "Tipo:", "Capacità:", "Binario:"};
        JComponent[] fields = {
                new JTextField(15),
                new JTextField(15),
                new JComboBox<>(new String[]{"REG", "IC", "FR"}),
                new JSpinner(new SpinnerNumberModel(200, 50, 500, 10)),
                new JTextField(15)
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
        gbc.gridwidth = 2;

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton createBtn = createActionButton("Crea Treno", SUCCESS_COLOR);
        JButton cancelBtn = createActionButton("Annulla", new Color(149, 165, 166));

        createBtn.addActionListener(e -> {
            showInfoMessage("Nuovo Treno", "Funzionalità creazione treno in sviluppo.");
            dialog.dispose();
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(createBtn);
        buttonPanel.add(cancelBtn);
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showBroadcastDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Notifica Broadcast", true);
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("📢 Invio Notifica a Tutti i Treni");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Tipo Evento:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> eventType = new JComboBox<>(new String[]{
                "INFORMAZIONE", "RITARDO", "CANCELLAZIONE", "CAMBIO_BINARIO", "MANUTENZIONE"
        });
        formPanel.add(eventType, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Messaggio:"), gbc);
        gbc.gridx = 1;
        JTextArea messageArea = new JTextArea(4, 20);
        messageArea.setBorder(new LineBorder(new Color(189, 195, 199), 1));
        JScrollPane scrollPane = new JScrollPane(messageArea);
        formPanel.add(scrollPane, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton sendBtn = createActionButton("📡 Invia Broadcast", WARNING_COLOR);
        JButton cancelBtn = createActionButton("Annulla", new Color(149, 165, 166));

        sendBtn.addActionListener(e -> {
            String message = messageArea.getText().trim();
            if (!message.isEmpty()) {
                boolean success = adminService.broadcastNotification(message, (String) eventType.getSelectedItem());
                if (success) {
                    showInfoMessage("Broadcast Inviato", "Notifica inviata a tutti i treni!");
                    dialog.dispose();
                } else {
                    showErrorDialog("Errore", "Errore nell'invio del broadcast.");
                }
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(sendBtn);
        buttonPanel.add(cancelBtn);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void exportTrainsData() {
        showInfoMessage("Export Dati", "Funzionalità export dati treni in sviluppo.");
    }

    private void showTrainDetails(int row) {
        String trainCode = (String) tableModel.getValueAt(row, 0);
        String trainName = (String) tableModel.getValueAt(row, 1);
        String route = (String) tableModel.getValueAt(row, 3);

        String details = String.format(
                "🚂 Dettagli Treno\n\n" +
                        "Codice: %s\n" +
                        "Nome: %s\n" +
                        "Tratta: %s\n" +
                        "Stato: %s\n" +
                        "Capacità: %s",
                trainCode, trainName, route,
                tableModel.getValueAt(row, 4),
                tableModel.getValueAt(row, 5)
        );

        showInfoMessage("Dettagli Treno", details);
    }

    private void sendTrainNotification(int row) {
        String trainCode = (String) tableModel.getValueAt(row, 0);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Notifica Treno " + trainCode, true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        formPanel.add(new JLabel("Evento:"));
        JComboBox<String> eventCombo = new JComboBox<>(new String[]{
                "RITARDO", "PARTENZA", "ARRIVO", "CAMBIO_BINARIO", "CANCELLAZIONE"
        });
        formPanel.add(eventCombo);

        formPanel.add(new JLabel("Messaggio:"));
        JTextField messageField = new JTextField();
        formPanel.add(messageField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton sendBtn = createActionButton("Invia", PRIMARY_COLOR);
        JButton cancelBtn = createActionButton("Annulla", new Color(149, 165, 166));

        sendBtn.addActionListener(e -> {
            String event = (String) eventCombo.getSelectedItem();
            String message = messageField.getText().trim();
            if (!message.isEmpty()) {
                boolean success = adminService.createTrainNotification(trainCode, event, message);
                if (success) {
                    showInfoMessage("Notifica Inviata", "Notifica inviata per il treno " + trainCode);
                    dialog.dispose();
                }
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(sendBtn);
        buttonPanel.add(cancelBtn);

        panel.add(new JLabel("📢 Invio Notifica Specifica"), BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    class TrainActionRenderer extends JButton implements javax.swing.table.TableCellRenderer {
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

    class TrainActionEditor extends DefaultCellEditor {
        protected JButton button;
        private int currentRow;

        public TrainActionEditor() {
            super(new JCheckBox());
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> {
                fireEditingStopped();
                showTrainActionMenu(currentRow);
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

        private void showTrainActionMenu(int row) {
            String[] options = {"📋 Visualizza Dettagli", "📢 Invia Notifica", "🔧 Modifica", "🗑️ Elimina"};
            int choice = JOptionPane.showOptionDialog(TrainsManagementPanel.this,
                    "Scegli un'azione per il treno:",
                    "Azioni Treno",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);

            switch (choice) {
                case 0:
                    showTrainDetails(row);
                    break;
                case 1:
                    sendTrainNotification(row);
                    break;
                case 2:
                    showInfoMessage("Modifica", "Funzionalità modifica in sviluppo.");
                    break;
                case 3:
                    showInfoMessage("Elimina", "Funzionalità eliminazione in sviluppo.");
                    break;
            }
        }
    }

    class TrainStatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String status = (String) value;
            switch (status) {
                case "Attivo":
                    setForeground(SUCCESS_COLOR);
                    setText("🟢 " + status);
                    break;
                case "Manutenzione":
                    setForeground(WARNING_COLOR);
                    setText("🟡 " + status);
                    break;
                case "Fuori servizio":
                    setForeground(DANGER_COLOR);
                    setText("🔴 " + status);
                    break;
                default:
                    setForeground(Color.BLACK);
            }

            return this;
        }
    }

    class TrainTypeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String type = (String) value;
            switch (type) {
                case "REG":
                    setText("🟢 " + type);
                    break;
                case "IC":
                    setText("🟡 " + type);
                    break;
                case "FR":
                    setText("🔴 " + type);
                    break;
                default:
                    setText(type);
            }

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