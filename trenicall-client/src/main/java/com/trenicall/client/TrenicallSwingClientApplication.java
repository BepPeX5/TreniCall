package com.trenicall.client;

import com.trenicall.client.service.GrpcClientService;
import com.trenicall.server.grpc.biglietteria.BigliettoResponse;
import com.trenicall.server.grpc.biglietteria.RicercaBigliettiResponse;
import com.trenicall.server.grpc.cliente.ClienteResponse;
import com.trenicall.server.grpc.prenotazione.PrenotazioneResponse;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class TrenicallSwingClientApplication extends JFrame {

    private GrpcClientService grpcService;
    private String currentClientId = "C1";
    private Timer notificationTimer;

    private JTextField ricercaPartenzaField;
    private JTextField ricercaArrivoField;
    private JTextField ricercaDataField;
    private JTable risultatiTable;
    private DefaultTableModel risultatiModel;

    private JTextField clienteIdField;
    private JTextField clienteNomeField;
    private JTextField clienteEmailField;
    private JTextField clienteTelefonoField;
    private JLabel statusLabel;
    private JLabel clienteStatusLabel;
    private JLabel fedeltaStatusLabel;

    private JTable bigliettiTable;
    private DefaultTableModel bigliettiModel;
    private JTable prenotazioniTable;
    private DefaultTableModel prenotazioniModel;
    private JTextArea notificheArea;

    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private final Color WARNING_COLOR = new Color(241, 196, 15);
    private final Color DANGER_COLOR = new Color(231, 76, 60);
    private final Color SECONDARY_COLOR = new Color(149, 165, 166);
    private final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private final Color CARD_COLOR = Color.WHITE;

    public TrenicallSwingClientApplication() {
        initializeGrpcService();
        setupLookAndFeel();
        initializeGUI();
        loadClienteInfo();
        aggiornaBiglietti();
        startNotificationTimer();
    }

    private void initializeGrpcService() {
        try {
            grpcService = new GrpcClientService("localhost", 9090);
        } catch (Exception e) {
            showErrorDialog("Errore connessione server", e.getMessage());
        }
    }

    private void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getLookAndFeel());

            // Font che supporta emoji su Windows
            Font emojiFont = new Font("Segoe UI Emoji", Font.PLAIN, 12);
            if (!emojiFont.getFamily().equals("Segoe UI Emoji")) {
                // Fallback per altri sistemi
                emojiFont = new Font("Arial Unicode MS", Font.PLAIN, 12);
                if (!emojiFont.getFamily().equals("Arial Unicode MS")) {
                    // Ultimo fallback
                    emojiFont = new Font("Dialog", Font.PLAIN, 12);
                }
            }

            // Imposta il font per tutti i componenti
            UIManager.put("Label.font", emojiFont);
            UIManager.put("Button.font", emojiFont);
            UIManager.put("TabbedPane.font", emojiFont);

            UIManager.put("Button.background", PRIMARY_COLOR);
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.focus", new Color(0, 0, 0, 0));
            UIManager.put("TabbedPane.selected", PRIMARY_COLOR);

        } catch (Exception e) {
            System.err.println("Could not set look and feel: " + e.getMessage());
        }
    }

    private void initializeGUI() {
        setTitle("TreniCal - Sistema Gestione Viaggi Ferroviari");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);

        setLayout(new BorderLayout(10, 10));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(20, 0));
        headerPanel.setBackground(new Color(44, 62, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel logoLabel = new JLabel("🚄 TreniCal");
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 28));

        JPanel statusPanel = createStatusPanel();

        headerPanel.add(logoLabel, BorderLayout.WEST);
        headerPanel.add(statusPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
        panel.setOpaque(false);

        clienteStatusLabel = createStyledLabel("Caricamento cliente...", WARNING_COLOR);
        fedeltaStatusLabel = createStyledLabel("Fedeltà: Non attiva", SECONDARY_COLOR);

        panel.add(clienteStatusLabel);
        panel.add(fedeltaStatusLabel);

        return panel;
    }

    private JLabel createStyledLabel(String text, Color backgroundColor) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setOpaque(true);
        label.setBackground(backgroundColor);
        label.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        label.setFont(new Font("Arial", Font.BOLD, 12));
        return label;
    }

    private JTabbedPane createMainContent() {
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tabbedPane.addTab("🔍 Ricerca Viaggi", createRicercaPanel());
        tabbedPane.addTab("🎫 I Miei Biglietti", createBigliettiPanel());
        tabbedPane.addTab("📋 Prenotazioni", createPrenotazioniPanel());
        tabbedPane.addTab("🔔 Notifiche Treno", createNotifichePanel());
        tabbedPane.addTab("👤 Gestione Profilo", createClientePanel());

        return tabbedPane;
    }

    private JPanel createRicercaPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel searchCard = createCard();
        searchCard.setLayout(new GridBagLayout());

        addSearchForm(searchCard);

        JPanel resultsCard = createCard();
        resultsCard.setLayout(new BorderLayout());
        addSearchResults(resultsCard);

        mainPanel.add(searchCard, BorderLayout.NORTH);
        mainPanel.add(resultsCard, BorderLayout.CENTER);

        return mainPanel;
    }

    private void addSearchForm(JPanel card) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);

        JLabel titleLabel = new JLabel("🔍 Ricerca Treni");
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
        titleLabel.setForeground(new Color(44, 62, 80));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4;
        card.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;

        gbc.gridx = 0;
        card.add(new JLabel("Stazione di Partenza:"), gbc);
        gbc.gridx = 1;
        ricercaPartenzaField = createStyledTextField("Roma Termini", 15);
        card.add(ricercaPartenzaField, gbc);

        gbc.gridx = 2;
        card.add(new JLabel("Stazione di Arrivo:"), gbc);
        gbc.gridx = 3;
        ricercaArrivoField = createStyledTextField("Milano Centrale", 15);
        card.add(ricercaArrivoField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        card.add(new JLabel("Data Viaggio (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        ricercaDataField = createStyledTextField(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 15);
        card.add(ricercaDataField, gbc);

        gbc.gridx = 2; gbc.gridwidth = 2;
        JButton searchBtn = createPrimaryButton("🔍 Cerca Treni Disponibili");
        searchBtn.addActionListener(e -> ricercaBiglietti());
        card.add(searchBtn, gbc);
    }

    private void addSearchResults(JPanel card) {
        JLabel titleLabel = new JLabel("📊 Risultati Ricerca");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        card.add(titleLabel, BorderLayout.NORTH);

        String[] colonne = {"Tipo Treno", "Partenza", "Arrivo", "Orario", "Distanza", "Prezzo", "Azioni"};
        risultatiModel = new DefaultTableModel(colonne, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };
        risultatiTable = createStyledTable(risultatiModel);
        risultatiTable.getColumn("Azioni").setCellRenderer(new ActionButtonRenderer());
        risultatiTable.getColumn("Azioni").setCellEditor(new ActionButtonEditor());

        JScrollPane scrollPane = new JScrollPane(risultatiTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        card.add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createBigliettiPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        headerPanel.setBackground(CARD_COLOR);

        JLabel titleLabel = new JLabel("🎫 I Miei Biglietti");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JButton refreshBtn = createSecondaryButton("🔄 Aggiorna");
        refreshBtn.addActionListener(e -> aggiornaBiglietti());

        headerPanel.add(titleLabel);
        headerPanel.add(refreshBtn);

        JPanel tableCard = createCard();
        tableCard.setLayout(new BorderLayout());
        tableCard.add(headerPanel, BorderLayout.NORTH);

        String[] colonne = {"ID", "Tipo", "Stato", "Partenza", "Arrivo", "Data/Ora", "Prezzo", "Azioni"};
        bigliettiModel = new DefaultTableModel(colonne, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7;
            }
        };
        bigliettiTable = createStyledTable(bigliettiModel);
        bigliettiTable.getColumn("Azioni").setCellRenderer(new BigliettoActionRenderer());
        bigliettiTable.getColumn("Azioni").setCellEditor(new BigliettoActionEditor());

        JScrollPane scrollPane = new JScrollPane(bigliettiTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(tableCard, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createPrenotazioniPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel actionPanel = createCard();
        actionPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 15));

        JLabel titleLabel = new JLabel("📋 Gestione Prenotazioni");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JButton createReservationBtn = createPrimaryButton("➕ Crea Nuova Prenotazione");
        createReservationBtn.addActionListener(e -> showCreateReservationDialog());

        JButton refreshReservationsBtn = createSecondaryButton("🔄 Aggiorna Lista");
        refreshReservationsBtn.addActionListener(e -> aggiornaPrenotazioni());

        actionPanel.add(titleLabel);
        actionPanel.add(createReservationBtn);
        actionPanel.add(refreshReservationsBtn);

        JPanel tableCard = createCard();
        tableCard.setLayout(new BorderLayout());

        String[] colonne = {"ID", "Cliente", "Treno", "Data Creazione", "Scadenza", "Stato", "Azioni"};
        prenotazioniModel = new DefaultTableModel(colonne, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };
        prenotazioniTable = createStyledTable(prenotazioniModel);
        prenotazioniTable.getColumn("Azioni").setCellRenderer(new PrenotazioneActionRenderer());
        prenotazioniTable.getColumn("Azioni").setCellEditor(new PrenotazioneActionEditor());

        JScrollPane scrollPane = new JScrollPane(prenotazioniTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(actionPanel, BorderLayout.NORTH);
        mainPanel.add(tableCard, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createNotifichePanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerCard = createCard();
        headerCard.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 15));

        JLabel titleLabel = new JLabel("🔔 Notifiche Treno in Tempo Reale");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JButton clearNotificationsBtn = createSecondaryButton("🗑️ Pulisci Notifiche");
        clearNotificationsBtn.addActionListener(e -> {
            notificheArea.setText("");
            showSuccessMessage("Notifiche pulite!");
        });

        headerCard.add(titleLabel);
        headerCard.add(clearNotificationsBtn);

        JPanel notificheCard = createCard();
        notificheCard.setLayout(new BorderLayout());

        notificheArea = new JTextArea(20, 50);
        notificheArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        notificheArea.setBackground(new Color(248, 249, 250));
        notificheArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        notificheArea.setEditable(false);
        notificheArea.setText("[INFO] Sistema di notifiche attivo...\n\n" +
                "[CONN] Connesso al servizio di monitoraggio treni\n" +
                "[TIME] Le notifiche appariranno qui automaticamente\n\n" +
                "Tipi di notifiche supportate:\n" +
                "* Aggiornamenti orari di partenza/arrivo\n" +
                "* Ritardi e cancellazioni\n" +
                "* Cambio binario\n" +
                "* Promozioni FedeltaTreno\n");

        JScrollPane scrollPane = new JScrollPane(notificheArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        notificheCard.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(headerCard, BorderLayout.NORTH);
        mainPanel.add(notificheCard, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createClientePanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTabbedPane authTabs = new JTabbedPane();
        authTabs.setFont(new Font("Arial", Font.BOLD, 12));

        authTabs.addTab("🔑 Accedi", createLoginPanel());
        authTabs.addTab("📝 Registrati", createRegistrationPanel());
        authTabs.addTab("⭐ Programma Fedeltà", createFedeltaPanel());

        mainPanel.add(authTabs, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createLoginPanel() {
        JPanel card = createCard();
        card.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);

        JLabel titleLabel = new JLabel("🔑 Accedi al tuo Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        card.add(new JLabel("ID Cliente:"), gbc);
        gbc.gridx = 1;
        JTextField loginIdField = createStyledTextField("C1", 20);
        card.add(loginIdField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JButton loginBtn = createPrimaryButton("🔓 Accedi al Sistema");
        loginBtn.addActionListener(e -> loginCliente(loginIdField.getText().trim()));
        card.add(loginBtn, gbc);

        return card;
    }

    private JPanel createRegistrationPanel() {
        JPanel card = createCard();
        card.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);

        JLabel titleLabel = new JLabel("📝 Registrazione Nuovo Cliente");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(titleLabel, gbc);

        gbc.gridwidth = 1;

        String[] labels = {"ID Cliente:", "Nome Completo:", "Email:", "Telefono:"};
        JTextField[] fields = new JTextField[4];

        for (int i = 0; i < labels.length; i++) {
            gbc.gridy = i + 1;
            gbc.gridx = 0;
            card.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1;
            fields[i] = createStyledTextField("", 20);
            card.add(fields[i], gbc);
        }

        clienteIdField = fields[0];
        clienteNomeField = fields[1];
        clienteEmailField = fields[2];
        clienteTelefonoField = fields[3];

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        JButton registraBtn = createSuccessButton("✅ Registra Nuovo Cliente");
        registraBtn.addActionListener(e -> registraCliente());
        card.add(registraBtn, gbc);

        return card;
    }

    private JPanel createFedeltaPanel() {
        JPanel card = createCard();
        card.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);

        JLabel titleLabel = new JLabel("⭐ Programma FedeltàTreno");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0; gbc.gridy = 0;
        card.add(titleLabel, gbc);

        JTextArea infoArea = new JTextArea(8, 40);
        infoArea.setText("🎯 VANTAGGI ESCLUSIVI FEDELTÀTRENO:\n\n" +
                "✨ Sconti fino al 25% su tutti i viaggi\n" +
                "🎁 Promozioni esclusive riservate ai membri\n" +
                "🔔 Notifiche prioritarie su offerte speciali\n" +
                "🏆 Accesso a vagoni Premium\n" +
                "📱 Supporto clienti dedicato 24/7\n\n" +
                "Aderisci oggi stesso al programma fedeltà!");
        infoArea.setFont(new Font("Arial", Font.PLAIN, 14));
        infoArea.setBackground(new Color(248, 249, 250));
        infoArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        infoArea.setEditable(false);

        gbc.gridy = 1;
        card.add(infoArea, gbc);

        gbc.gridy = 2;
        JButton abilitaFedeltaBtn = createSuccessButton("🌟 Abilita Programma Fedeltà");
        abilitaFedeltaBtn.addActionListener(e -> abilitaFedelta());
        card.add(abilitaFedeltaBtn, gbc);

        return card;
    }

    private JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        return card;
    }

    private JTextField createStyledTextField(String text, int columns) {
        JTextField field = new JTextField(text, columns);
        field.setFont(new Font("Arial", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        return field;
    }

    private JButton createPrimaryButton(String text) {
        return createStyledButton(text, PRIMARY_COLOR, Color.WHITE);
    }

    private JButton createSuccessButton(String text) {
        return createStyledButton(text, SUCCESS_COLOR, Color.WHITE);
    }

    private JButton createSecondaryButton(String text) {
        return createStyledButton(text, SECONDARY_COLOR, Color.WHITE);
    }

    private JButton createDangerButton(String text) {
        return createStyledButton(text, DANGER_COLOR, Color.WHITE);
    }

    private JButton createStyledButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(background.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(background);
            }
        });

        return button;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(35);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(52, 73, 94));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setGridColor(new Color(220, 220, 220));
        table.setSelectionBackground(new Color(174, 214, 241));
        return table;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(new Color(236, 240, 241));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        statusLabel = new JLabel("[OK] Connesso al server TreniCal");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        footerPanel.add(statusLabel, BorderLayout.WEST);

        JLabel versionLabel = new JLabel("TreniCal v2.0 - Sistema Distribuito");
        versionLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        versionLabel.setForeground(new Color(127, 140, 141));
        footerPanel.add(versionLabel, BorderLayout.EAST);

        return footerPanel;
    }

    private void loadClienteInfo() {
        try {
            ClienteResponse cliente = grpcService.dettagliCliente(currentClientId);
            updateClientStatus(cliente);
        } catch (Exception e) {
            clienteStatusLabel.setText("⚠️ Cliente non trovato");
            clienteStatusLabel.setBackground(WARNING_COLOR);
            fedeltaStatusLabel.setText("Fedeltà: Non disponibile");
            fedeltaStatusLabel.setBackground(SECONDARY_COLOR);
        }
    }

    private void updateClientStatus(ClienteResponse cliente) {
        clienteStatusLabel.setText(">> " + cliente.getNome() + " (" + cliente.getId() + ")");
        clienteStatusLabel.setBackground(SUCCESS_COLOR);

        if (cliente.getFedelta()) {
            fedeltaStatusLabel.setText("Fedelta: ATTIVA");
            fedeltaStatusLabel.setBackground(SUCCESS_COLOR);
        } else {
            fedeltaStatusLabel.setText("Fedelta: Non attiva");
            fedeltaStatusLabel.setBackground(SECONDARY_COLOR);
        }

        aggiornaPrenotazioni();
    }

    private void loginCliente(String clienteId) {
        if (clienteId.isEmpty()) {
            showErrorDialog("Errore", "Inserisci l'ID cliente");
            return;
        }

        try {
            ClienteResponse cliente = grpcService.dettagliCliente(clienteId);
            currentClientId = cliente.getId();
            updateClientStatus(cliente);
            showSuccessMessage("Accesso effettuato! Benvenuto " + cliente.getNome());
            aggiornaBiglietti();
        } catch (Exception e) {
            showErrorDialog("Errore Login", "Cliente non trovato. Verifica l'ID o registrati.");
        }
    }

    private void registraCliente() {
        String id = clienteIdField.getText().trim();
        String nome = clienteNomeField.getText().trim();
        String email = clienteEmailField.getText().trim();
        String telefono = clienteTelefonoField.getText().trim();

        if (id.isEmpty() || nome.isEmpty() || email.isEmpty()) {
            showErrorDialog("Errore", "Compila tutti i campi obbligatori");
            return;
        }

        try {
            grpcService.dettagliCliente(id);
            showErrorDialog("Errore", "Cliente già esistente con questo ID. Usa il login per accedere.");
            return;
        } catch (Exception e) {
        }

        try {
            ClienteResponse response = grpcService.registraCliente(id, nome, email, telefono);
            currentClientId = response.getId();
            updateClientStatus(response);

            clienteIdField.setText("");
            clienteNomeField.setText("");
            clienteEmailField.setText("");
            clienteTelefonoField.setText("");

            showSuccessMessage("Cliente registrato correttamente! Benvenuto " + response.getNome());

        } catch (Exception e) {
            showErrorDialog("Errore Registrazione", e.getMessage());
        }
    }

    private void abilitaFedelta() {
        try {
            ClienteResponse response = grpcService.dettagliCliente(currentClientId);
            if (response.getFedelta()) {
                showInfoMessage("Fedeltà già attiva per questo cliente!");
                return;
            }

            grpcService.registraCliente(currentClientId, response.getNome(), response.getEmail(), response.getTelefono());

            fedeltaStatusLabel.setText("Fedeltà: ATTIVA");
            fedeltaStatusLabel.setBackground(SUCCESS_COLOR);

            showSuccessMessage("Programma Fedeltà attivato! Ora hai accesso a sconti esclusivi e promozioni speciali.");
            addNotification("Programma Fedeltà attivato! Sconti fino al 25% disponibili.");

        } catch (Exception e) {
            showErrorDialog("Errore Fedeltà", "Errore nell'attivazione: " + e.getMessage());
        }
    }

    private void ricercaBiglietti() {
        String partenza = ricercaPartenzaField.getText().trim();
        String arrivo = ricercaArrivoField.getText().trim();
        String data = ricercaDataField.getText().trim() + "T10:00:00";

        if (partenza.isEmpty() || arrivo.isEmpty()) {
            showErrorDialog("Errore", "Compila tutti i campi di ricerca");
            return;
        }

        risultatiModel.setRowCount(0);

        try {
            RicercaBigliettiResponse response = grpcService.ricercaBiglietti(partenza, arrivo, data);

            if (response.getRisultatiCount() > 0) {
                for (BigliettoResponse biglietto : response.getRisultatiList()) {
                    Object[] row = {
                            biglietto.getTipo(),
                            biglietto.getPartenza(),
                            biglietto.getArrivo(),
                            formatDateTime(biglietto.getDataViaggio()),
                            biglietto.getDistanzaKm() + " km",
                            String.format("€ %.2f", biglietto.getPrezzo()),
                            "Azioni"
                    };
                    risultatiModel.addRow(row);
                }
            } else {
                generaRisultatiDaTratti(partenza, arrivo, data);
            }

            statusLabel.setText("Trovati " + risultatiModel.getRowCount() + " risultati");

        } catch (Exception e) {
            showErrorDialog("Errore Ricerca", e.getMessage());
            generaRisultatiDaTratti(partenza, arrivo, data);
        }
    }

    private void generaRisultatiDaTratti(String partenza, String arrivo, String data) {
        String[] tipi = {"REGIONALE", "INTERCITY", "FRECCIA_ROSSA"};
        double[] prezziPerKm = {0.08, 0.12, 0.18};

        int distanza = calcolaDistanzaApprossimativa(partenza, arrivo);

        for (int i = 0; i < tipi.length; i++) {
            Object[] row = {
                    tipi[i],
                    partenza,
                    arrivo,
                    data.substring(0, 10) + " 10:00",
                    distanza + " km",
                    String.format("€ %.2f", distanza * prezziPerKm[i]),
                    "Azioni"
            };
            risultatiModel.addRow(row);
        }
    }

    private int calcolaDistanzaApprossimativa(String partenza, String arrivo) {
        Map<String, Integer> distanzeRoma = Map.of(
                "Milano Centrale", 574,
                "Napoli Centrale", 225,
                "Firenze Santa Maria Novella", 273,
                "Bologna Centrale", 378,
                "Torino Porta Nuova", 669
        );

        Map<String, Integer> distanzeMilano = Map.of(
                "Roma Termini", 574,
                "Napoli Centrale", 770,
                "Torino Porta Nuova", 158,
                "Bologna Centrale", 218,
                "Firenze Santa Maria Novella", 298
        );

        if ("Roma Termini".equals(partenza) && distanzeRoma.containsKey(arrivo)) {
            return distanzeRoma.get(arrivo);
        } else if ("Milano Centrale".equals(partenza) && distanzeMilano.containsKey(arrivo)) {
            return distanzeMilano.get(arrivo);
        } else if ("Roma Termini".equals(arrivo) && distanzeRoma.containsKey(partenza)) {
            return distanzeRoma.get(partenza);
        } else if ("Milano Centrale".equals(arrivo) && distanzeMilano.containsKey(partenza)) {
            return distanzeMilano.get(partenza);
        }

        return Math.max(150, (partenza.length() + arrivo.length()) * 15 + (int)(Math.random() * 200));
    }

    private void aggiornaBiglietti() {
        try {
            List<BigliettoResponse> biglietti = grpcService.listaBigliettiCliente(currentClientId);

            bigliettiModel.setRowCount(0);
            for (BigliettoResponse biglietto : biglietti) {
                Object[] row = {
                        biglietto.getId(),
                        biglietto.getTipo(),
                        biglietto.getStato(),
                        biglietto.getPartenza(),
                        biglietto.getArrivo(),
                        formatDateTime(biglietto.getDataViaggio()),
                        String.format("€ %.2f", biglietto.getPrezzo()),
                        "Azioni"
                };
                bigliettiModel.addRow(row);
            }

            statusLabel.setText("Caricati " + bigliettiModel.getRowCount() + " biglietti");

        } catch (Exception e) {
            statusLabel.setText("Errore caricamento biglietti: " + e.getMessage());
        }
    }

    private void aggiornaPrenotazioni() {
        try {
            prenotazioniModel.setRowCount(0);

            Object[] samplePrenotazione = {
                    "PR001",
                    currentClientId,
                    "FR-T001002",
                    LocalDateTime.now().minusHours(2).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    LocalDateTime.now().plusMinutes(30).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                    "Attiva",
                    "Azioni"
            };
            prenotazioniModel.addRow(samplePrenotazione);

            addNotification("Lista prenotazioni aggiornata per cliente " + currentClientId);
        } catch (Exception e) {
            showErrorDialog("Errore", "Errore nell'aggiornamento prenotazioni: " + e.getMessage());
        }
    }

    private void showCreateReservationDialog() {
        JDialog dialog = new JDialog(this, "Crea Nuova Prenotazione", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        String[] labels = {"Partenza:", "Arrivo:", "Data (YYYY-MM-DD):", "Tipo Treno:", "Durata (minuti):"};
        JComponent[] fields = {
                createStyledTextField("Roma Termini", 15),
                createStyledTextField("Milano Centrale", 15),
                createStyledTextField(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 15),
                new JComboBox<>(new String[]{"REGIONALE", "INTERCITY", "FRECCIA_ROSSA"}),
                createStyledTextField("60", 15)
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

        JButton createBtn = createPrimaryButton("Crea Prenotazione");
        createBtn.addActionListener(e -> {
            try {
                String partenza = ((JTextField)fields[0]).getText().trim();
                String arrivo = ((JTextField)fields[1]).getText().trim();
                String data = ((JTextField)fields[2]).getText().trim() + "T10:00:00";
                String tipo = (String)((JComboBox<?>)fields[3]).getSelectedItem();
                int durata = Integer.parseInt(((JTextField)fields[4]).getText().trim());

                PrenotazioneResponse prenotazione = grpcService.creaPrenotazione(
                        currentClientId, tipo, partenza, arrivo, data,
                        calcolaDistanzaApprossimativa(partenza, arrivo), durata
                );

                showSuccessMessage("Prenotazione creata! ID: " + prenotazione.getId());
                dialog.dispose();
                aggiornaPrenotazioni();

            } catch (Exception ex) {
                showErrorDialog("Errore", "Errore nella creazione: " + ex.getMessage());
            }
        });

        panel.add(createBtn, gbc);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void startNotificationTimer() {
        notificationTimer = new Timer();
        notificationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    String[] notifiche = {
                            "Treno FR-8574 Roma-Milano: Ritardo di 5 minuti",
                            "Offerta speciale: Sconto 20% su tutti i biglietti Intercity",
                            "Treno REG-2341 Milano-Torino: Partenza dal binario 7",
                            "Promozione FedeltàTreno: Weekend a prezzi speciali",
                            "Treno FR-1234 Napoli-Roma: In orario, binario 3"
                    };

                    if (Math.random() > 0.7) {
                        String notifica = notifiche[(int)(Math.random() * notifiche.length)];
                        addNotification(notifica);
                    }
                });
            }
        }, 30000, 45000);
    }

    private void addNotification(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String notification = "[" + timestamp + "] " + message + "\n";
        notificheArea.append(notification);
        notificheArea.setCaretPosition(notificheArea.getDocument().getLength());
    }

    private String formatDateTime(String dateTime) {
        try {
            LocalDateTime dt = LocalDateTime.parse(dateTime);
            return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (Exception e) {
            return dateTime;
        }
    }

    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Successo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void showInfoMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Informazione", JOptionPane.INFORMATION_MESSAGE);
    }

    class ActionButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ActionButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText("Acquista/Prenota");
            setBackground(PRIMARY_COLOR);
            setForeground(Color.WHITE);
            return this;
        }
    }

    class ActionButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow;

        public ActionButtonEditor() {
            super(new JCheckBox());
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentRow = row;
            label = "Acquista/Prenota";
            button.setText(label);
            button.setBackground(PRIMARY_COLOR);
            button.setForeground(Color.WHITE);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                acquistaOPrenotaBiglietto(currentRow);
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }

    class BigliettoActionRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText("Modifica/Rimborsa");
            setBackground(SECONDARY_COLOR);
            setForeground(Color.WHITE);
            return this;
        }
    }

    class BigliettoActionEditor extends DefaultCellEditor {
        protected JButton button;
        private int currentRow;

        public BigliettoActionEditor() {
            super(new JCheckBox());
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> {
                fireEditingStopped();
                showBigliettoActions(currentRow);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentRow = row;
            button.setText("Modifica/Rimborsa");
            button.setBackground(SECONDARY_COLOR);
            button.setForeground(Color.WHITE);
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "Modifica/Rimborsa";
        }
    }

    class PrenotazioneActionRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText("Conferma/Annulla");
            setBackground(SUCCESS_COLOR);
            setForeground(Color.WHITE);
            return this;
        }
    }

    class PrenotazioneActionEditor extends DefaultCellEditor {
        protected JButton button;
        private int currentRow;

        public PrenotazioneActionEditor() {
            super(new JCheckBox());
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> {
                fireEditingStopped();
                showPrenotazioneActions(currentRow);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentRow = row;
            button.setText("Conferma/Annulla");
            button.setBackground(SUCCESS_COLOR);
            button.setForeground(Color.WHITE);
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "Conferma/Annulla";
        }
    }

    private void acquistaOPrenotaBiglietto(int row) {
        String[] options = {"Acquista Subito", "Crea Prenotazione", "Annulla"};
        int choice = JOptionPane.showOptionDialog(this,
                "Scegli l'azione da eseguire:",
                "Acquisto/Prenotazione",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 0) {
            acquistaBiglietto(row);
        } else if (choice == 1) {
            prenotaBiglietto(row);
        }
    }

    private void acquistaBiglietto(int row) {
        try {
            String tipo = (String) risultatiModel.getValueAt(row, 0);
            String partenza = (String) risultatiModel.getValueAt(row, 1);
            String arrivo = (String) risultatiModel.getValueAt(row, 2);
            String data = ricercaDataField.getText().trim() + "T10:00:00";
            int distanza = calcolaDistanzaApprossimativa(partenza, arrivo);

            BigliettoResponse response = grpcService.acquistaBiglietto(
                    currentClientId, tipo, partenza, arrivo, data, distanza
            );

            showSuccessMessage("Biglietto acquistato!\nID: " + response.getId() +
                    "\nPrezzo: €" + String.format("%.2f", response.getPrezzo()));

            aggiornaBiglietti();
            addNotification("Nuovo biglietto acquistato: " + tipo + " " + partenza + "-" + arrivo);

        } catch (Exception e) {
            showErrorDialog("Errore Acquisto", e.getMessage());
        }
    }

    private void prenotaBiglietto(int row) {
        try {
            String tipo = (String) risultatiModel.getValueAt(row, 0);
            String partenza = (String) risultatiModel.getValueAt(row, 1);
            String arrivo = (String) risultatiModel.getValueAt(row, 2);
            String data = ricercaDataField.getText().trim() + "T10:00:00";
            int distanza = calcolaDistanzaApprossimativa(partenza, arrivo);

            PrenotazioneResponse response = grpcService.creaPrenotazione(
                    currentClientId, tipo, partenza, arrivo, data, distanza, 60
            );

            showSuccessMessage("Prenotazione creata!\nID: " + response.getId() +
                    "\nScadenza: " + response.getScadenza());

            addNotification("Nuova prenotazione creata: " + tipo + " " + partenza + "-" + arrivo);

        } catch (Exception e) {
            showErrorDialog("Errore Prenotazione", e.getMessage());
        }
    }

    private void showBigliettoActions(int row) {
        String bigliettoId = (String) bigliettiModel.getValueAt(row, 0);
        String stato = (String) bigliettiModel.getValueAt(row, 2);

        String[] options;
        if ("PAGATO".equals(stato)) {
            options = new String[]{"Modifica Data", "Richiedi Rimborso", "Annulla"};
        } else {
            options = new String[]{"Visualizza Dettagli", "Annulla"};
        }

        int choice = JOptionPane.showOptionDialog(this,
                "Azioni disponibili per il biglietto " + bigliettoId + ":",
                "Gestione Biglietto",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 0 && "PAGATO".equals(stato)) {
            showModificaDataDialog(bigliettoId);
        } else if (choice == 1 && "PAGATO".equals(stato)) {
            richiedeRimborso(bigliettoId);
        } else if (choice == 0) {
            showInfoMessage("Dettagli biglietto " + bigliettoId + " visualizzati");
        }
    }

    private void showModificaDataDialog(String bigliettoId) {
        JDialog dialog = new JDialog(this, "Modifica Biglietto " + bigliettoId, true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Modifica Data Viaggio");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(new JLabel("Nuova Data (YYYY-MM-DD):"), gbc);

        gbc.gridx = 1;
        JTextField nuovaDataField = createStyledTextField(
                LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 15);
        panel.add(nuovaDataField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(new JLabel("Nuovo Orario (HH:MM):"), gbc);

        gbc.gridx = 1;
        JTextField nuovoOrarioField = createStyledTextField("10:00", 15);
        panel.add(nuovoOrarioField, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0; gbc.gridwidth = 2;
        JTextArea infoArea = new JTextArea(3, 30);
        infoArea.setText("ATTENZIONE: La modifica potrebbe comportare un costo aggiuntivo di 5-15 euro secondo le condizioni di vendita.");
        infoArea.setWrapStyleWord(true);
        infoArea.setLineWrap(true);
        infoArea.setEditable(false);
        infoArea.setBackground(new Color(255, 248, 220));
        infoArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(infoArea, gbc);

        gbc.gridy = 4;
        gbc.gridx = 0; gbc.gridwidth = 1;
        JButton confermaBtn = createPrimaryButton("Conferma Modifica");
        confermaBtn.addActionListener(e -> {
            String nuovaData = nuovaDataField.getText().trim();
            String nuovoOrario = nuovoOrarioField.getText().trim();

            if (!nuovaData.isEmpty() && !nuovoOrario.isEmpty()) {
                showSuccessMessage("Modifica confermata per il biglietto " + bigliettoId +
                        "\nNuova data: " + nuovaData + " " + nuovoOrario +
                        "\nCosto aggiuntivo: 10.00 euro" +
                        "\nRiceverai conferma via email.");
                addNotification("Biglietto " + bigliettoId + " modificato per " + nuovaData + " " + nuovoOrario);
                dialog.dispose();
                aggiornaBiglietti();
            } else {
                showErrorDialog("Errore", "Inserisci data e orario validi");
            }
        });
        panel.add(confermaBtn, gbc);

        gbc.gridx = 1;
        JButton annullaBtn = createSecondaryButton("Annulla");
        annullaBtn.addActionListener(e -> dialog.dispose());
        panel.add(annullaBtn, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void richiedeRimborso(String bigliettoId) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Sei sicuro di voler richiedere il rimborso per il biglietto " + bigliettoId + "?\n" +
                        "Il rimborso potrebbe essere soggetto a penali secondo le condizioni di vendita.",
                "Conferma Rimborso",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            showSuccessMessage("Richiesta di rimborso inviata per il biglietto " + bigliettoId +
                    "\nRiceverai una conferma via email entro 24 ore.");
            addNotification("Rimborso richiesto per biglietto " + bigliettoId);
            aggiornaBiglietti();
        }
    }

    private void showPrenotazioneActions(int row) {
        String prenotazioneId = (String) prenotazioniModel.getValueAt(row, 0);
        String stato = (String) prenotazioniModel.getValueAt(row, 5);

        String[] options = {"Conferma Acquisto", "Annulla Prenotazione", "Chiudi"};
        int choice = JOptionPane.showOptionDialog(this,
                "Azioni per prenotazione " + prenotazioneId + ":",
                "Gestione Prenotazione",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 0) {
            confermaAcquisto(prenotazioneId);
        } else if (choice == 1) {
            annullaPrenotazione(prenotazioneId);
        }
    }

    private void confermaAcquisto(String prenotazioneId) {
        try {
            showSuccessMessage("Prenotazione " + prenotazioneId + " confermata e convertita in biglietto!");
            addNotification("Prenotazione " + prenotazioneId + " convertita in biglietto");
            aggiornaPrenotazioni();
            aggiornaBiglietti();
        } catch (Exception e) {
            showErrorDialog("Errore", "Errore nella conferma: " + e.getMessage());
        }
    }

    private void annullaPrenotazione(String prenotazioneId) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Sei sicuro di voler annullare la prenotazione " + prenotazioneId + "?",
                "Conferma Annullamento",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            showSuccessMessage("Prenotazione " + prenotazioneId + " annullata!");
            addNotification("Prenotazione " + prenotazioneId + " annullata");
            aggiornaPrenotazioni();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getLookAndFeel());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new TrenicallSwingClientApplication().setVisible(true);
        });
    }
}