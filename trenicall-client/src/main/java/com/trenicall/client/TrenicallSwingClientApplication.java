package com.trenicall.client;

import com.trenicall.client.service.GrpcClientService;
import com.trenicall.server.grpc.biglietteria.BigliettoResponse;
import com.trenicall.server.grpc.biglietteria.RicercaBigliettiResponse;
import com.trenicall.server.grpc.cliente.ClienteResponse;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

public class TrenicallSwingClientApplication extends JFrame {

    private GrpcClientService grpcService;
    private String currentClientId = "C1";

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

    private JTable bigliettiTable;
    private DefaultTableModel bigliettiModel;

    public TrenicallSwingClientApplication() {
        initializeGrpcService();
        initializeGUI();
        loadClienteInfo();
        aggiornaBiglietti();
    }

    private void initializeGrpcService() {
        try {
            grpcService = new GrpcClientService("localhost", 9090);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Errore connessione server: " + e.getMessage(),
                    "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeGUI() {
        setTitle("TreniCal - Sistema Gestione Viaggi");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        // Look and Feel moderno
        try {
            UIManager.setLookAndFeel(UIManager.getLookAndFeel());
        } catch (Exception e) {
            // Usa il default se non riesce
        }
        // Layout principale
        setLayout(new BorderLayout());

        // Header
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Content con tabs
        add(createContentPanel(), BorderLayout.CENTER);

        // Footer
        add(createFooterPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(52, 73, 94));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel logoLabel = new JLabel("TreniCal");
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setFont(new Font("Arial", Font.BOLD, 24));

        clienteStatusLabel = new JLabel("Caricamento cliente...");
        clienteStatusLabel.setForeground(Color.WHITE);
        clienteStatusLabel.setOpaque(true);
        clienteStatusLabel.setBackground(new Color(39, 174, 96));
        clienteStatusLabel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        headerPanel.add(logoLabel, BorderLayout.WEST);
        headerPanel.add(clienteStatusLabel, BorderLayout.EAST);

        return headerPanel;
    }

    private JTabbedPane createContentPanel() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tabbedPane.addTab("Ricerca e Acquisto", createRicercaPanel());
        tabbedPane.addTab("I Miei Biglietti", createBigliettiPanel());
        tabbedPane.addTab("Profilo Cliente", createClientePanel());

        return tabbedPane;
    }

    private JPanel createRicercaPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Form ricerca
        JPanel ricercaFormPanel = new JPanel(new GridBagLayout());
        ricercaFormPanel.setBorder(BorderFactory.createTitledBorder("Ricerca Biglietti"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Partenza
        gbc.gridx = 0; gbc.gridy = 0;
        ricercaFormPanel.add(new JLabel("Partenza:"), gbc);
        gbc.gridx = 1;
        ricercaPartenzaField = new JTextField(15);
        ricercaPartenzaField.setText("Roma");
        ricercaFormPanel.add(ricercaPartenzaField, gbc);

        // Arrivo
        gbc.gridx = 2; gbc.gridy = 0;
        ricercaFormPanel.add(new JLabel("Arrivo:"), gbc);
        gbc.gridx = 3;
        ricercaArrivoField = new JTextField(15);
        ricercaArrivoField.setText("Milano");
        ricercaFormPanel.add(ricercaArrivoField, gbc);

        // Data
        gbc.gridx = 0; gbc.gridy = 1;
        ricercaFormPanel.add(new JLabel("Data (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        ricercaDataField = new JTextField(15);
        ricercaDataField.setText(LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        ricercaFormPanel.add(ricercaDataField, gbc);

        // Bottone ricerca
        gbc.gridx = 2; gbc.gridy = 1;
        JButton ricercaBtn = new JButton("Cerca Treni");
        ricercaBtn.setBackground(new Color(52, 152, 219));
        ricercaBtn.setForeground(Color.WHITE);
        ricercaBtn.addActionListener(e -> ricercaBiglietti());
        ricercaFormPanel.add(ricercaBtn, gbc);

        // Tabella risultati
        String[] colonne = {"Tipo", "Partenza", "Arrivo", "Data", "Distanza", "Prezzo", "Azioni"};
        risultatiModel = new DefaultTableModel(colonne, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Solo colonna azioni editabile
            }
        };
        risultatiTable = new JTable(risultatiModel);
        risultatiTable.setRowHeight(35);

        // Renderer per bottoni nella tabella
        risultatiTable.getColumn("Azioni").setCellRenderer(new ButtonRenderer());
        risultatiTable.getColumn("Azioni").setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(risultatiTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Risultati Ricerca"));
        scrollPane.setPreferredSize(new Dimension(0, 300));

        panel.add(ricercaFormPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBigliettiPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header con bottone aggiorna
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("I Miei Biglietti");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JButton aggiornaBigliettiBtn = new JButton("Aggiorna");
        aggiornaBigliettiBtn.addActionListener(e -> aggiornaBiglietti());

        headerPanel.add(titleLabel);
        headerPanel.add(aggiornaBigliettiBtn);

        // Tabella biglietti
        String[] colonne = {"ID", "Tipo", "Stato", "Partenza", "Arrivo", "Data", "Prezzo"};
        bigliettiModel = new DefaultTableModel(colonne, 0);
        bigliettiTable = new JTable(bigliettiModel);
        bigliettiTable.setRowHeight(30);

        JScrollPane scrollPane = new JScrollPane(bigliettiTable);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createClientePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Form registrazione
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Registrazione Nuovo Cliente"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // ID Cliente
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("ID Cliente:"), gbc);
        gbc.gridx = 1;
        clienteIdField = new JTextField(20);
        formPanel.add(clienteIdField, gbc);

        // Nome
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Nome Completo:"), gbc);
        gbc.gridx = 1;
        clienteNomeField = new JTextField(20);
        formPanel.add(clienteNomeField, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        clienteEmailField = new JTextField(20);
        formPanel.add(clienteEmailField, gbc);

        // Telefono
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Telefono:"), gbc);
        gbc.gridx = 1;
        clienteTelefonoField = new JTextField(20);
        formPanel.add(clienteTelefonoField, gbc);

        // Bottone registra
        gbc.gridx = 1; gbc.gridy = 4;
        JButton registraBtn = new JButton("Registra Cliente");
        registraBtn.setBackground(new Color(52, 152, 219));
        registraBtn.setForeground(Color.WHITE);
        registraBtn.addActionListener(e -> registraCliente());
        formPanel.add(registraBtn, gbc);

        // Info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setBorder(BorderFactory.createTitledBorder("Informazioni"));
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.add(new JLabel("• Utilizza l'ID cliente C1 per accedere con un profilo predefinito"));
        infoPanel.add(new JLabel("• I clienti registrati possono accedere a promozioni esclusive"));
        infoPanel.add(new JLabel("• Il programma FedeltàTreno offre sconti speciali"));

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(infoPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(new Color(236, 240, 241));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        statusLabel = new JLabel("Connesso al server TreniCal");
        footerPanel.add(statusLabel, BorderLayout.WEST);

        return footerPanel;
    }

    private void loadClienteInfo() {
        try {
            ClienteResponse cliente = grpcService.dettagliCliente(currentClientId);
            clienteStatusLabel.setText("Cliente: " + cliente.getNome() + " (" + cliente.getId() + ")");
            clienteStatusLabel.setBackground(new Color(39, 174, 96));
        } catch (Exception e) {
            clienteStatusLabel.setText("Cliente non trovato. Registra un nuovo cliente.");
            clienteStatusLabel.setBackground(new Color(243, 156, 18));
        }
    }

    private void ricercaBiglietti() {
        try {
            String partenza = ricercaPartenzaField.getText().trim();
            String arrivo = ricercaArrivoField.getText().trim();
            String data = ricercaDataField.getText().trim() + "T10:00:00";

            if (partenza.isEmpty() || arrivo.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Compila tutti i campi di ricerca",
                        "Errore", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Pulisci tabella
            risultatiModel.setRowCount(0);

            // Genera risultati demo (il server potrebbe non avere dati)
            generaRisultatiDemo(partenza, arrivo, data);

            statusLabel.setText("Trovati " + risultatiModel.getRowCount() + " risultati");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Errore nella ricerca: " + e.getMessage(),
                    "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generaRisultatiDemo(String partenza, String arrivo, String data) {
        Random random = new Random();
        String[] tipi = {"REGIONALE", "INTERCITY", "FRECCIA_ROSSA"};
        double[] prezziPerKm = {0.08, 0.12, 0.18};
        int distanza = 200 + random.nextInt(500);

        for (int i = 0; i < tipi.length; i++) {
            Object[] row = {
                    tipi[i],
                    partenza,
                    arrivo,
                    data.substring(0, 10) + " 10:00",
                    distanza + " km",
                    String.format("€ %.2f", distanza * prezziPerKm[i]),
                    "Acquista/Prenota"
            };
            risultatiModel.addRow(row);
        }
    }

    private void registraCliente() {
        try {
            String id = clienteIdField.getText().trim();
            String nome = clienteNomeField.getText().trim();
            String email = clienteEmailField.getText().trim();
            String telefono = clienteTelefonoField.getText().trim();

            if (id.isEmpty() || nome.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Compila tutti i campi obbligatori",
                        "Errore", JOptionPane.ERROR_MESSAGE);
                return;
            }

            ClienteResponse response = grpcService.registraCliente(id, nome, email, telefono);
            currentClientId = response.getId();

            clienteStatusLabel.setText("Cliente: " + response.getNome() + " (" + response.getId() + ")");
            clienteStatusLabel.setBackground(new Color(39, 174, 96));

            // Pulisci form
            clienteIdField.setText("");
            clienteNomeField.setText("");
            clienteEmailField.setText("");
            clienteTelefonoField.setText("");

            JOptionPane.showMessageDialog(this, "Cliente registrato correttamente!",
                    "Successo", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Errore nella registrazione: " + e.getMessage(),
                    "Errore", JOptionPane.ERROR_MESSAGE);
        }
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
                        String.format("€ %.2f", biglietto.getPrezzo())
                };
                bigliettiModel.addRow(row);
            }

            statusLabel.setText("Caricati " + bigliettiModel.getRowCount() + " biglietti");

        } catch (Exception e) {
            statusLabel.setText("Errore nel caricamento biglietti: " + e.getMessage());
        }
    }

    private String formatDateTime(String dateTime) {
        try {
            LocalDateTime dt = LocalDateTime.parse(dateTime);
            return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (Exception e) {
            return dateTime;
        }
    }

    // Renderer e Editor per i bottoni nella tabella
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText("Acquista");
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentRow = row;
            label = "Acquista";
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                acquistaBigliettoDaTabella(currentRow);
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

    private void acquistaBigliettoDaTabella(int row) {
        try {
            String tipo = (String) risultatiModel.getValueAt(row, 0);
            String partenza = (String) risultatiModel.getValueAt(row, 1);
            String arrivo = (String) risultatiModel.getValueAt(row, 2);
            String data = ricercaDataField.getText().trim() + "T10:00:00";

            BigliettoResponse response = grpcService.acquistaBiglietto(
                    currentClientId, tipo, partenza, arrivo, data, 300
            );

            JOptionPane.showMessageDialog(this,
                    "Biglietto acquistato!\nID: " + response.getId() +
                            "\nPrezzo: €" + String.format("%.2f", response.getPrezzo()),
                    "Successo", JOptionPane.INFORMATION_MESSAGE);

            aggiornaBiglietti();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Errore nell'acquisto: " + e.getMessage(),
                    "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TrenicallSwingClientApplication().setVisible(true);
        });
    }
}
