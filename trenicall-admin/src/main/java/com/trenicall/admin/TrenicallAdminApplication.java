package com.trenicall.admin;

import com.trenicall.admin.gui.AdminMainFrame;

import javax.swing.*;
import java.awt.*;

public class TrenicallAdminApplication {

    private static final String APP_NAME = "TreniCal Admin Console";
    private static final String VERSION = "v1.0";

    public static void main(String[] args) {
        setupLookAndFeel();

        setupFonts();

        SwingUtilities.invokeLater(() -> {
            try {
                showSplashScreen();
                Thread.sleep(1500);

                AdminMainFrame mainFrame = new AdminMainFrame();
                mainFrame.setVisible(true);

            } catch (Exception e) {
                showErrorDialog("Errore Avvio",
                        "Impossibile avviare l'applicazione admin:\n" + e.getMessage());
                System.exit(1);
            }
        });
    }

    private static void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getLookAndFeel());

            UIManager.put("Button.focus", new Color(0, 0, 0, 0));
            UIManager.put("TabbedPane.contentOpaque", false);
            UIManager.put("TabbedPane.opaque", false);

        } catch (Exception e) {
            System.err.println("⚠️ Impossibile impostare Look & Feel: " + e.getMessage());
        }
    }


    private static void setupFonts() {
        try {
            Font defaultFont = new Font("Segoe UI", Font.PLAIN, 12);
            Font emojiFont = new Font("Segoe UI Emoji", Font.PLAIN, 12);

            if (!emojiFont.getFamily().equals("Segoe UI Emoji")) {
                emojiFont = new Font("Arial Unicode MS", Font.PLAIN, 12);
                if (!emojiFont.getFamily().equals("Arial Unicode MS")) {
                    emojiFont = defaultFont;
                }
            }

            UIManager.put("Label.font", emojiFont);
            UIManager.put("Button.font", emojiFont);
            UIManager.put("TabbedPane.font", emojiFont);
            UIManager.put("Table.font", defaultFont);
            UIManager.put("TableHeader.font", new Font("Arial", Font.BOLD, 12));

        } catch (Exception e) {
            System.err.println("⚠️ Errore configurazione font: " + e.getMessage());
        }
    }

    private static void showSplashScreen() {
        JWindow splash = new JWindow();
        splash.setSize(450, 300);
        splash.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(44, 62, 80));
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);

        JLabel logoLabel = new JLabel("🔧 " + APP_NAME);
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(logoLabel, gbc);

        JLabel versionLabel = new JLabel(VERSION);
        versionLabel.setForeground(new Color(149, 165, 166));
        versionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridy = 1;
        panel.add(versionLabel, gbc);

        JLabel descLabel = new JLabel("Sistema Amministrativo Ferroviario");
        descLabel.setForeground(new Color(189, 195, 199));
        descLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        gbc.gridy = 2;
        panel.add(descLabel, gbc);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setForeground(new Color(41, 128, 185));
        progressBar.setPreferredSize(new Dimension(300, 20));
        gbc.gridy = 3;
        gbc.insets = new Insets(40, 20, 20, 20);
        panel.add(progressBar, gbc);

        JLabel statusLabel = new JLabel("Caricamento moduli...");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        gbc.gridy = 4;
        gbc.insets = new Insets(10, 20, 20, 20);
        panel.add(statusLabel, gbc);

        splash.add(panel);
        splash.setVisible(true);

        Timer timer = new Timer(1500, e -> splash.dispose());
        timer.setRepeats(false);
        timer.start();
    }


    private static void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(
                null,
                message,
                title,
                JOptionPane.ERROR_MESSAGE
        );
    }
}