package com.trenicall.admin.gui.panels;

import com.trenicall.admin.service.AdminService;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NotificationCenterPanel extends JPanel {

    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private final Color WARNING_COLOR = new Color(241, 196, 15);
    private final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private final Color CARD_COLOR = Color.WHITE;

    private AdminService adminService;
    private JTextArea logArea;
    private JTextField messageField;

    public NotificationCenterPanel(AdminService adminService) {
        this.adminService = adminService;
        initializeGUI();
    }

    private void initializeGUI() {
        setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
        add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("📢 Centro Notifiche");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80));

        panel.add(titleLabel);
        return panel;
    }

    private JPanel createMainPanel() {
        JPanel panel = createCard();
        panel.setLayout(new BorderLayout(15, 15));

        logArea = new JTextArea(20, 50);
        logArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        logArea.setBackground(new Color(44, 62, 80));
        logArea.setForeground(Color.WHITE);
        logArea.setEditable(false);
        logArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(new LineBorder(new Color(189, 195, 199), 1));

        JLabel logLabel = new JLabel("📝 Log Notifiche Real-time");
        logLabel.setFont(new Font("Arial", Font.BOLD, 14));

        panel.add(logLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        loadInitialLogs();
        return panel;
    }

    private JPanel createControlPanel() {
        JPanel panel = createCard();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel broadcastLabel = new JLabel("📡 Broadcast Globale");
        broadcastLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(broadcastLabel, gbc);

        gbc.gridy = 1; gbc.gridwidth = 1;
        gbc.gridx = 0;
        panel.add(new JLabel("Messaggio:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        messageField = new JTextField(30);
        messageField.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(messageField, gbc);

        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;

        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton broadcastBtn = createButton("📢 Broadcast", WARNING_COLOR);
        broadcastBtn.addActionListener(e -> sendBroadcast());

        JButton clearBtn = createButton("🗑️ Pulisci Log", new Color(149, 165, 166));
        clearBtn.addActionListener(e -> clearLogs());

        buttonPanel.add(broadcastBtn);
        buttonPanel.add(clearBtn);

        panel.add(buttonPanel, gbc);
        return panel;
    }

    private JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        return card;
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        return button;
    }

    private void loadInitialLogs() {
        logArea.setText(
                "[" + getCurrentTime() + "] 🚀 Sistema notifiche attivato\n" +
                        "[" + getCurrentTime() + "] 📡 Server gRPC connesso\n" +
                        "[" + getCurrentTime() + "] ✅ Centro notifiche operativo\n\n"
        );
    }

    private void sendBroadcast() {
        String message = messageField.getText().trim();
        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Inserisci un messaggio!", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = adminService.broadcastNotification(message, "BROADCAST");

        if (success) {
            String logEntry = "[" + getCurrentTime() + "] 📢 BROADCAST: " + message + "\n";
            logArea.append(logEntry);
            logArea.setCaretPosition(logArea.getDocument().getLength());
            messageField.setText("");

            JOptionPane.showMessageDialog(this, "Broadcast inviato con successo!", "Successo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Errore nell'invio del broadcast", "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearLogs() {
        logArea.setText("");
        loadInitialLogs();
    }

    private String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
}