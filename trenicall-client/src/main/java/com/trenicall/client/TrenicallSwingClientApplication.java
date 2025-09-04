package com.trenicall.client;

import com.trenicall.client.service.GrpcClientService;
import com.trenicall.server.grpc.biglietteria.BigliettoResponse;
import com.trenicall.server.grpc.biglietteria.RicercaBigliettiResponse;
import com.trenicall.server.grpc.cliente.ClienteResponse;
import com.trenicall.server.grpc.prenotazione.PrenotazioneResponse;
import com.trenicall.server.grpc.notifica.NotificaResponse;
import com.trenicall.server.grpc.notifica.TrainInfo;
import com.trenicall.server.grpc.promozione.PromozioneResponse;
import io.grpc.stub.StreamObserver;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.regex.Pattern;

public class TrenicallSwingClientApplication extends JFrame {

    private GrpcClientService grpcService;
    private String currentClientId = "C1";
    private JTabbedPane mainTabbedPane;
    private Timer notificationTimer;

    private JComboBox<String> ricercaPartenzaCombo;
    private JComboBox<String> ricercaArrivoCombo;
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

    private final String[] STAZIONI = {
            "Roma Termini", "Milano Centrale", "Napoli Centrale", "Torino Porta Nuova",
            "Firenze Santa Maria Novella", "Bologna Centrale", "Venice Santa Lucia",
            "Bari Centrale", "Palermo Centrale", "Genova Piazza Principe", "Cosenza"
    };

    public TrenicallSwingClientApplication() {
        initializeGrpcService();
        setupLookAndFeel();
        initializeGUI();
        loadClienteInfo();
        aggiornaBiglietti();
        addFollowTrainTab();
        addPromozioniTab();
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
            Font emojiFont = new Font("Segoe UI Emoji", Font.PLAIN, 12);
            if (!emojiFont.getFamily().equals("Segoe UI Emoji")) {
                emojiFont = new Font("Arial Unicode MS", Font.PLAIN, 12);
                if (!emojiFont.getFamily().equals("Arial Unicode MS")) {
                    emojiFont = new Font("Dialog", Font.PLAIN, 12);
                }
            }
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

    private Font getEmojiFont(int style, int size) {
        String[] emojiFonts = {"Segoe UI Emoji", "Noto Color Emoji", "Apple Color Emoji"};
        for (String fontName : emojiFonts) {
            if (Arrays.asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames())
                    .contains(fontName)) {
                return new Font(fontName, style, size);
            }
        }
        return new Font("Arial", style, size);
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
        mainTabbedPane = new JTabbedPane(JTabbedPane.TOP);
        mainTabbedPane.setFont(getEmojiFont(Font.BOLD, 14));
        mainTabbedPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        mainTabbedPane.addTab("🔍 Ricerca Viaggi", createRicercaPanel());
        mainTabbedPane.addTab("🎫 I Miei Biglietti", createBigliettiPanel());
        mainTabbedPane.addTab("📋 Prenotazioni", createPrenotazioniPanel());
        mainTabbedPane.addTab("🔔 Notifiche Treno", createNotifichePanel());
        mainTabbedPane.addTab("👤 Gestione Profilo", createClientePanel());

        return mainTabbedPane;
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
        ricercaPartenzaCombo = new JComboBox<>(STAZIONI);
        ricercaPartenzaCombo.setSelectedItem("Roma Termini");
        ricercaPartenzaCombo.addActionListener(e -> validateStationSelection());
        card.add(ricercaPartenzaCombo, gbc);

        gbc.gridx = 2;
        card.add(new JLabel("Stazione di Arrivo:"), gbc);
        gbc.gridx = 3;
        ricercaArrivoCombo = new JComboBox<>(STAZIONI);
        ricercaArrivoCombo.setSelectedItem("Milano Centrale");
        ricercaArrivoCombo.addActionListener(e -> validateStationSelection());
        card.add(ricercaArrivoCombo, gbc);

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

    private void validateStationSelection() {
        String partenza = (String) ricercaPartenzaCombo.getSelectedItem();
        String arrivo = (String) ricercaArrivoCombo.getSelectedItem();

        if (partenza != null && partenza.equals(arrivo)) {
            showErrorDialog("Errore Selezione", "La stazione di partenza e arrivo non possono essere uguali!");
            if (ricercaArrivoCombo.hasFocus()) {
                ricercaArrivoCombo.setSelectedIndex((ricercaArrivoCombo.getSelectedIndex() + 1) % STAZIONI.length);
            } else {
                ricercaPartenzaCombo.setSelectedIndex((ricercaPartenzaCombo.getSelectedIndex() + 1) % STAZIONI.length);
            }
        }
    }

    private void addSearchResults(JPanel card) {
        JLabel titleLabel = new JLabel("📊 Risultati Ricerca");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        card.add(titleLabel, BorderLayout.NORTH);

        String[] colonne = {"Tipo Treno", "Partenza", "Arrivo", "Orario", "Distanza", "Prezzo", "Posti", "Azioni"};
        risultatiModel = new DefaultTableModel(colonne, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7;
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
        titleLabel.setFont(getEmojiFont(Font.BOLD, 18));

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
        titleLabel.setFont(getEmojiFont(Font.BOLD, 18));

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

        JLabel titleLabel = new JLabel("🔔 Centro Notifiche Unificate");
        titleLabel.setFont(getEmojiFont(Font.BOLD, 18));

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
        notificheArea.setFont(getEmojiFont(Font.PLAIN, 12));
        notificheArea.setBackground(new Color(248, 249, 250));
        notificheArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        notificheArea.setEditable(false);
        notificheArea.setText("📢 CENTRO NOTIFICHE TRENICALL\n" +
                "═══════════════════════════════\n\n" +
                "🔔 TIPI DI NOTIFICHE:\n" +
                "[TRENI SEGUITI] - Treni che scegli di monitorare\n" +
                "[TRENI ACQUISTATI] - Treni per cui hai biglietti\n\n" +
                "ℹ️ Le notifiche appariranno automaticamente qui...\n\n");

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
        authTabs.setFont(getEmojiFont(Font.BOLD, 12));

        authTabs.addTab("🔐 Accedi", createLoginPanel());
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

        JLabel titleLabel = new JLabel("🔐 Accedi al tuo Account");
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

        JLabel titleLabel = new JLabel("⭐ Programma Fedeltà Treno");
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
    }

    private void loginCliente(String nuovoClienteId) {
        if (nuovoClienteId == null || nuovoClienteId.isEmpty()) {
            showErrorDialog("Errore", "Inserisci l'ID cliente");
            return;
        }

        try {
            ClienteResponse cliente = grpcService.dettagliCliente(nuovoClienteId);

            if (this.currentClientId != null && !this.currentClientId.equals(nuovoClienteId)) {
                try {
                    boolean ok = grpcService.logoutNotifiche(this.currentClientId);
                    if (ok) {
                        addNotification("🔕 Disiscritto dalle notifiche di " + this.currentClientId);
                    }
                } catch (Exception ignore) {
                }
                if (notificheArea != null) {
                    notificheArea.setText(
                            "▣ CENTRO NOTIFICHE TRENICALL\n" +
                                    "Le nuove notifiche appariranno qui…\n\n"
                    );
                }
            }

            this.currentClientId = nuovoClienteId;
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
        String partenza = (String) ricercaPartenzaCombo.getSelectedItem();
        String arrivo = (String) ricercaArrivoCombo.getSelectedItem();
        String data = ricercaDataField.getText().trim() + "T10:00:00";

        if (partenza == null || arrivo == null || partenza.equals(arrivo)) {
            showErrorDialog("Errore", "Seleziona stazioni diverse per partenza e arrivo");
            return;
        }

        risultatiModel.setRowCount(0);
        generaRisultatiDaTratti(partenza, arrivo, data);
        statusLabel.setText("Trovati " + risultatiModel.getRowCount() + " risultati");
    }

    private void generaRisultatiDaTratti(String partenza, String arrivo, String data) {
        String[] tipi = {"REGIONALE", "INTERCITY", "FRECCIA_ROSSA"};
        double[] prezziPerKm = {0.08, 0.12, 0.18};
        int[] postiBase = {200, 300, 400};

        int distanza = calcolaDistanzaApprossimativa(partenza, arrivo);

        for (int i = 0; i < tipi.length; i++) {
            int postiDisponibili = postiBase[i] - getPostiAcquistati(partenza, arrivo, tipi[i]);

            Object[] row = {
                    tipi[i],
                    partenza,
                    arrivo,
                    data.substring(0, 10) + " 10:00",
                    distanza + " km",
                    String.format("€ %.2f", distanza * prezziPerKm[i]),
                    postiDisponibili + " posti",
                    "Azioni"
            };
            risultatiModel.addRow(row);
        }
    }

    private int getPostiAcquistati(String partenza, String arrivo, String tipo) {
        try {
            List<BigliettoResponse> biglietti = grpcService.listaBigliettiCliente(currentClientId);
            return (int) biglietti.stream()
                    .filter(b -> b.getPartenza().equals(partenza) &&
                            b.getArrivo().equals(arrivo) &&
                            b.getTipo().equals(tipo) &&
                            "PAGATO".equals(b.getStato()))
                    .count();
        } catch (Exception e) {
            return 0;
        }
    }

    private int calcolaDistanzaApprossimativa(String partenza, String arrivo) {
        Map<String, Map<String, Integer>> distanze = createDistanceMatrix();

        if (distanze.containsKey(partenza) && distanze.get(partenza).containsKey(arrivo)) {
            return distanze.get(partenza).get(arrivo);
        }

        if (distanze.containsKey(arrivo) && distanze.get(arrivo).containsKey(partenza)) {
            return distanze.get(arrivo).get(partenza);
        }

        return 300;
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

    private void aggiornaBiglietti() {
        try {
            List<BigliettoResponse> biglietti = grpcService.listaBigliettiCliente(currentClientId);

            bigliettiModel.setRowCount(0);
            for (BigliettoResponse biglietto : biglietti) {
                String stato = biglietto.getStato();
                if (stato == null || stato.equals("UNKNOWN")) {
                    stato = "PAGATO";
                }

                Object[] row = {
                        biglietto.getId(),
                        biglietto.getTipo(),
                        stato,
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

    private void showCreateReservationDialog() {
        JDialog dialog = new JDialog(this, "Crea Nuova Prenotazione", true);
        dialog.setSize(600, 450);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Nuova Prenotazione (Scadenza: 10 minuti)");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        String[] labels = {"Partenza:", "Arrivo:", "Data (YYYY-MM-DD):", "Tipo Treno:", "Passeggeri:"};
        JComponent[] fields = {
                new JComboBox<>(STAZIONI),
                new JComboBox<>(STAZIONI),
                createStyledTextField(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 15),
                new JComboBox<>(new String[]{"REGIONALE", "INTERCITY", "FRECCIA_ROSSA"}),
                new JSpinner(new SpinnerNumberModel(1, 1, 10, 1))
        };

        ((JComboBox<?>)fields[0]).setSelectedItem("Roma Termini");
        ((JComboBox<?>)fields[1]).setSelectedItem("Milano Centrale");

        for (int i = 0; i < labels.length; i++) {
            gbc.gridy = i + 1;
            gbc.gridx = 0;
            gbc.anchor = GridBagConstraints.WEST;
            panel.add(new JLabel(labels[i]), gbc);

            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(fields[i], gbc);
        }

        gbc.gridy = labels.length + 1;
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        JButton createBtn = createPrimaryButton("Crea Prenotazione");
        createBtn.addActionListener(e -> {
            try {
                String partenza = (String)((JComboBox<?>)fields[0]).getSelectedItem();
                String arrivo = (String)((JComboBox<?>)fields[1]).getSelectedItem();
                String data = ((JTextField)fields[2]).getText().trim() + "T10:00:00";
                String tipo = (String)((JComboBox<?>)fields[3]).getSelectedItem();
                int passeggeri = (Integer)((JSpinner)fields[4]).getValue();

                if (partenza.equals(arrivo)) {
                    showErrorDialog("Errore", "Partenza e arrivo non possono essere uguali");
                    return;
                }

                PrenotazioneResponse prenotazione = grpcService.creaPrenotazione(
                        currentClientId, tipo, partenza, arrivo, data,
                        calcolaDistanzaApprossimativa(partenza, arrivo), 10
                );

                showSuccessMessage("Prenotazione creata! ID: " + prenotazione.getId() +
                        "\nPasseggeri: " + passeggeri +
                        "\nScadenza: 10 minuti");
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

    private void aggiornaPrenotazioni() {
        try {
            List<PrenotazioneResponse> prenotazioni = grpcService.listaPrenotazioniCliente(currentClientId);

            prenotazioniModel.setRowCount(0);
            for (PrenotazioneResponse p : prenotazioni) {
                Object[] row = {
                        p.getId(),
                        p.getClienteId(),
                        p.getBigliettoId(),
                        formatDateTime(p.getDataCreazione()),
                        formatDateTime(p.getScadenza()),
                        p.getAttiva() ? "Attiva" : "Scaduta",
                        "Azioni"
                };
                prenotazioniModel.addRow(row);
            }

            statusLabel.setText("Caricate " + prenotazioni.size() + " prenotazioni");
        } catch (Exception e) {
            statusLabel.setText("Errore caricamento prenotazioni: " + e.getMessage());
        }
    }

    private boolean showPaymentDialog() {
        JDialog dialog = new JDialog(this, "Pagamento Sicuro", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("💳 Inserisci Dati Carta di Credito");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        String[] labels = {"Nome Titolare:", "Numero Carta:", "Scadenza (MM/YY):", "CVV:"};
        JTextField[] fields = {
                createStyledTextField("", 20),
                createStyledTextField("", 20),
                createStyledTextField("", 20),
                createStyledTextField("", 20)
        };

        for (int i = 0; i < labels.length; i++) {
            gbc.gridy = i + 1;
            gbc.gridx = 0;
            panel.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1;
            panel.add(fields[i], gbc);
        }

        final boolean[] paymentSuccess = {false};

        gbc.gridy = labels.length + 1;
        gbc.gridx = 0;
        JButton confirmBtn = createSuccessButton("💰 Conferma Pagamento");
        confirmBtn.addActionListener(e -> {
            String nome = fields[0].getText().trim();
            String numero = fields[1].getText().trim();
            String scadenza = fields[2].getText().trim();
            String cvv = fields[3].getText().trim();

            if (validatePaymentData(nome, numero, scadenza, cvv)) {
                paymentSuccess[0] = true;
                showSuccessMessage("Pagamento elaborato con successo!\nTransazione sicura completata.");
                dialog.dispose();
            }
        });

        gbc.gridx = 1;
        JButton cancelBtn = createSecondaryButton("❌ Annulla");
        cancelBtn.addActionListener(e -> dialog.dispose());

        panel.add(confirmBtn, gbc);
        gbc.gridx = 0;
        panel.add(cancelBtn, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
        return paymentSuccess[0];
    }

    private boolean validatePaymentData(String nome, String numero, String scadenza, String cvv) {
        if (nome.isEmpty()) {
            showErrorDialog("Errore", "Inserisci il nome del titolare");
            return false;
        }

        if (!Pattern.matches("\\d{16}", numero)) {
            showErrorDialog("Errore", "Il numero carta deve essere di 16 cifre");
            return false;
        }

        if (!Pattern.matches("\\d{2}/\\d{2}", scadenza)) {
            showErrorDialog("Errore", "La scadenza deve essere nel formato MM/YY");
            return false;
        }

        try {
            String[] parts = scadenza.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = 2000 + Integer.parseInt(parts[1]);

            if (month < 1 || month > 12) {
                showErrorDialog("Errore", "Mese non valido");
                return false;
            }

            LocalDate expiry = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1);
            if (expiry.isBefore(LocalDate.now())) {
                showErrorDialog("Errore", "La carta è scaduta");
                return false;
            }
        } catch (Exception e) {
            showErrorDialog("Errore", "Data di scadenza non valida");
            return false;
        }

        if (!Pattern.matches("\\d{3}", cvv)) {
            showErrorDialog("Errore", "Il CVV deve essere di 3 cifre");
            return false;
        }

        return true;
    }

    private void addFollowTrainTab() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel controlCard = createCard();
        controlCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);

        JLabel titleLabel = new JLabel("🚂 Gestione Notifiche Treni");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(44, 62, 80));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 4;
        controlCard.add(titleLabel, gbc);

        gbc.gridwidth = 1; gbc.gridy = 1;

        gbc.gridx = 0;
        controlCard.add(new JLabel("Seleziona Treno da Seguire:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> trainsCombo = new JComboBox<>();
        trainsCombo.setFont(new Font("Arial", Font.PLAIN, 12));
        trainsCombo.setPreferredSize(new Dimension(350, 30));
        controlCard.add(trainsCombo, gbc);

        gbc.gridx = 2;
        JButton btnReload = createSecondaryButton("🔄 Carica Treni");
        controlCard.add(btnReload, gbc);

        gbc.gridx = 3;
        JButton btnFollow = createPrimaryButton("🔔 Attiva Notifiche");
        controlCard.add(btnFollow, gbc);

        btnReload.addActionListener(e -> {
            try {
                System.out.println("🔄 Caricamento treni...");
                List<TrainInfo> treni = grpcService.listaTreniAttivi();

                DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
                for (TrainInfo t : treni) {
                    String entry = String.format("%-12s | %-35s | %s",
                            t.getCodice(), t.getTratta(), t.getNome());
                    model.addElement(entry);
                }

                trainsCombo.setModel(model);
                if (model.getSize() > 0) {
                    trainsCombo.setSelectedIndex(0);
                    showSuccessMessage("✅ Caricati " + model.getSize() + " treni");
                } else {
                    showErrorDialog("Attenzione", "Nessun treno disponibile");
                }
            } catch (Exception ex) {
                System.err.println("❌ Errore caricamento treni: " + ex.getMessage());
                showErrorDialog("Errore", "Errore caricamento: " + ex.getMessage());
            }
        });

        btnFollow.addActionListener(e -> {
            String selected = (String) trainsCombo.getSelectedItem();
            if (selected == null || selected.isEmpty()) {
                showErrorDialog("Errore", "Seleziona un treno dalla lista.");
                return;
            }

            String codice = selected.split("\\|")[0].trim();

            // Registrazione per il treno selezionato
            grpcService.seguiTreno(currentClientId, codice, new StreamObserver<NotificaResponse>() {
                @Override
                public void onNext(NotificaResponse value) {
                    SwingUtilities.invokeLater(() -> {
                        String timestamp = value.getTimestamp().length() > 8 ?
                                value.getTimestamp().substring(11, 19) : value.getTimestamp();
                        String notification = String.format("[%s] %s\n", timestamp, value.getMessaggio());
                        notificheArea.append(notification);
                        notificheArea.setCaretPosition(notificheArea.getDocument().getLength());
                    });
                }

                @Override
                public void onError(Throwable t) {
                    SwingUtilities.invokeLater(() ->
                            notificheArea.append("❌ ERRORE notifiche (treno seguito): " + t.getMessage() + "\n"));
                }

                @Override
                public void onCompleted() {
                    SwingUtilities.invokeLater(() ->
                            notificheArea.append("ℹ️ Stream notifiche treno seguito chiuso\n"));
                }
            });

            startUnifiedNotifications();

            showSuccessMessage("🔔 Ora stai seguendo il treno " + codice +
                    " + attivate notifiche automatiche per i tuoi biglietti!");
        });

        mainPanel.add(controlCard, BorderLayout.NORTH);
        int lastIndex = mainTabbedPane.getTabCount() - 2;
        mainTabbedPane.insertTab("🔔 Gestione Notifiche", null, mainPanel, null, lastIndex);
    }


    private void addPromozioniTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("💸 Promozioni Attive");
        title.setFont(new Font("Arial", Font.BOLD, 18));

        DefaultTableModel promoModel = new DefaultTableModel(
                new Object[]{"Nome", "Sconto", "Inizio", "Fine", "Solo Fedeltà", "Tratta"}, 0
        );
        JTable promoTable = new JTable(promoModel);

        JButton btnReload = createPrimaryButton("🔄 Aggiorna");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        btnReload.addActionListener(e -> {
            promoModel.setRowCount(0);
            try {
                List<PromozioneResponse> promos = grpcService.listaPromozioniAttive();
                for (PromozioneResponse p : promos) {
                    promoModel.addRow(new Object[]{
                            p.getNome(),
                            (p.getPercentualeSconto() * 100) + "%",
                            LocalDateTime.parse(p.getInizio()).format(formatter),
                            LocalDateTime.parse(p.getFine()).format(formatter),
                            p.getSoloFedelta() ? "✔" : "✘",
                            p.getTrattaPartenza() + " → " + p.getTrattaArrivo()
                    });
                }
            } catch (Exception ex) {
                showErrorDialog("Errore", "Impossibile caricare promozioni: " + ex.getMessage());
            }
        });

        panel.add(title, BorderLayout.NORTH);
        panel.add(new JScrollPane(promoTable), BorderLayout.CENTER);
        panel.add(btnReload, BorderLayout.SOUTH);
        int lastIndex = mainTabbedPane.getTabCount() - 1;
        mainTabbedPane.insertTab("💸 Promozioni", null, panel, null, lastIndex);
    }




    private void startUnifiedNotifications() {
        grpcService.seguiTreno(currentClientId, "", new StreamObserver<NotificaResponse>() {
            @Override
            public void onNext(NotificaResponse value) {
                SwingUtilities.invokeLater(() -> {
                    String timestamp = value.getTimestamp().length() > 8 ?
                            value.getTimestamp().substring(11, 19) : value.getTimestamp();

                    String notification = String.format("[%s] %s\n", timestamp, value.getMessaggio());
                    notificheArea.append(notification);
                    notificheArea.setCaretPosition(notificheArea.getDocument().getLength());
                });
            }

            @Override
            public void onError(Throwable t) {
                SwingUtilities.invokeLater(() ->
                        notificheArea.append("❌ ERRORE notifiche: " + t.getMessage() + "\n"));
            }

            @Override
            public void onCompleted() {
                SwingUtilities.invokeLater(() ->
                        notificheArea.append("ℹ️ Stream notifiche chiuso\n"));
            }
        });
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
        JDialog dialog = new JDialog(this, "Scegli Azione", true);
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Acquisto o Prenotazione");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1; gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(new JLabel("Numero Passeggeri:"), gbc);
        gbc.gridx = 1;
        JSpinner passeggeriSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        panel.add(passeggeriSpinner, gbc);

        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton acquistoBtn = createPrimaryButton("Acquista Subito");
        acquistoBtn.addActionListener(e -> {
            dialog.dispose();
            int passeggeri = (Integer) passeggeriSpinner.getValue();
            acquistaBigliettoConPasseggeri(row, passeggeri);
        });

        JButton prenotaBtn = createSecondaryButton("Crea Prenotazione");
        prenotaBtn.addActionListener(e -> {
            dialog.dispose();
            int passeggeri = (Integer) passeggeriSpinner.getValue();
            prenotaBigliettoConPasseggeri(row, passeggeri);
        });

        JButton annullaBtn = createDangerButton("Annulla");
        annullaBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(acquistoBtn);
        buttonPanel.add(prenotaBtn);
        buttonPanel.add(annullaBtn);

        panel.add(buttonPanel, gbc);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void acquistaBigliettoConPasseggeri(int row, int passeggeri) {
        if (!showPaymentDialog()) {
            return;
        }

        try {
            String tipo = (String) risultatiModel.getValueAt(row, 0);
            String partenza = (String) risultatiModel.getValueAt(row, 1);
            String arrivo = (String) risultatiModel.getValueAt(row, 2);
            String data = ricercaDataField.getText().trim() + "T10:00:00";
            int distanza = calcolaDistanzaApprossimativa(partenza, arrivo);

            double prezzoTotale = 0;
            double prezzoBase = 0;
            double prezzoFinale = 0;

            for (int i = 0; i < passeggeri; i++) {
                BigliettoResponse response = grpcService.acquistaBiglietto(
                        currentClientId, tipo, partenza, arrivo, data, distanza
                );
                prezzoFinale = response.getPrezzo();
                prezzoTotale += prezzoFinale;


            }

            String msg = "Biglietti acquistati!\n" +
                    "Passeggeri: " + passeggeri +
                    "\nTipo: " + tipo +
                    "\nTratta: " + partenza + " → " + arrivo +
                    "\nPrezzo finale per passeggero: €" + String.format("%.2f", prezzoFinale) +
                    "\nTotale: €" + String.format("%.2f", prezzoTotale);

            showSuccessMessage(msg);

            aggiornaBiglietti();
            addNotification("Acquistati " + passeggeri + " biglietti " + tipo + " " + partenza + "-" + arrivo);

        } catch (Exception e) {
            showErrorDialog("Errore Acquisto", e.getMessage());
        }
    }


    private void prenotaBigliettoConPasseggeri(int row, int passeggeri) {
        try {
            String tipo = (String) risultatiModel.getValueAt(row, 0);
            String partenza = (String) risultatiModel.getValueAt(row, 1);
            String arrivo = (String) risultatiModel.getValueAt(row, 2);
            String data = ricercaDataField.getText().trim() + "T10:00:00";
            int distanza = calcolaDistanzaApprossimativa(partenza, arrivo);

            PrenotazioneResponse response = grpcService.creaPrenotazione(
                    currentClientId, tipo, partenza, arrivo, data, distanza, 10
            );

            showSuccessMessage("Prenotazione creata!\nID: " + response.getId() +
                    "\nPasseggeri: " + passeggeri +
                    "\nScadenza: 10 minuti");

            addNotification("Prenotazione creata: " + tipo + " per " + passeggeri + " passeggeri");

        } catch (Exception e) {
            showErrorDialog("Errore Prenotazione", e.getMessage());
        }
    }

    private void acquistaBiglietto(int row) {
        if (!showPaymentDialog()) {
            return;
        }

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
            showModificaDataDialog(bigliettoId, row);
        } else if (choice == 1 && "PAGATO".equals(stato)) {
            richiedeRimborso(bigliettoId, row);
        } else if (choice == 0) {
            showInfoMessage("Dettagli biglietto " + bigliettoId + " visualizzati");
        }
    }

    private void showModificaDataDialog(String bigliettoId, int row) {
        JDialog dialog = new JDialog(this, "Modifica Biglietto " + bigliettoId, true);
        dialog.setSize(500, 400);
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
                try {
                    String dataCompleta = nuovaData + "T" + nuovoOrario + ":00";
                    grpcService.modificaBiglietto(bigliettoId, dataCompleta);

                    bigliettiModel.setValueAt(nuovaData + " " + nuovoOrario, row, 5);
                    showSuccessMessage("Modifica confermata per il biglietto " + bigliettoId +
                            "\nNuova data: " + nuovaData + " " + nuovoOrario);
                    addNotification("Biglietto " + bigliettoId + " modificato per " + nuovaData + " " + nuovoOrario);
                    dialog.dispose();
                } catch (Exception ex) {
                    showErrorDialog("Errore", "Errore nella modifica: " + ex.getMessage());
                }
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

    private void richiedeRimborso(String bigliettoId, int row) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Sei sicuro di voler richiedere il rimborso per il biglietto " + bigliettoId + "?\n" +
                        "Il rimborso potrebbe essere soggetto a penali secondo le condizioni di vendita.",
                "Conferma Rimborso",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                grpcService.modificaBiglietto(bigliettoId, "RIMBORSA");

                bigliettiModel.setValueAt("RIMBORSATO", row, 2);
                showSuccessMessage("Richiesta di rimborso inviata per il biglietto " + bigliettoId);
                addNotification("Rimborso richiesto per biglietto " + bigliettoId);
            } catch (Exception e) {
                showErrorDialog("Errore", "Errore nel rimborso: " + e.getMessage());
            }
        }
    }

    private void showPrenotazioneActions(int row) {
        String prenotazioneId = (String) prenotazioniModel.getValueAt(row, 0);

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
            confermaAcquisto(prenotazioneId, row);
        } else if (choice == 1) {
            annullaPrenotazione(prenotazioneId, row);
        }
    }

    private void confermaAcquisto(String prenotazioneId, int row) {
        if (!showPaymentDialog()) {
            return;
        }

        try {
            BigliettoResponse biglietto = grpcService.confermaAcquistoPrenotazione(prenotazioneId);
            prenotazioniModel.removeRow(row);
            showSuccessMessage("Prenotazione " + prenotazioneId + " confermata e convertita in biglietto!");
            addNotification("Prenotazione " + prenotazioneId + " convertita in biglietto");
            aggiornaBiglietti();
        } catch (Exception e) {
            showErrorDialog("Errore", "Errore nella conferma: " + e.getMessage());
        }
    }

    private void annullaPrenotazione(String prenotazioneId, int row) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Sei sicuro di voler annullare la prenotazione " + prenotazioneId + "?",
                "Conferma Annullamento",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                grpcService.annullaPrenotazione(prenotazioneId);
                prenotazioniModel.removeRow(row);
                showSuccessMessage("Prenotazione " + prenotazioneId + " annullata!");
                addNotification("Prenotazione " + prenotazioneId + " annullata");
            } catch (Exception e) {
                showErrorDialog("Errore", "Errore nell'annullamento: " + e.getMessage());
            }
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