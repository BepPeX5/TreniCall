package com.trenicall.admin;

import com.trenicall.admin.gui.AdminMainFrame;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.swing.*;
import java.awt.*;

@SpringBootApplication(scanBasePackages = {
        "com.trenicall.admin",
        "com.trenicall.server.business.services",
        "com.trenicall.server.business.patterns",
        "com.trenicall.server.domain.repositories"
})
@EntityScan(basePackages = "com.trenicall.server.domain.entities")
@EnableJpaRepositories(basePackages = "com.trenicall.server.domain.repositories")
public class TrenicallAdminApplication {

    private static final String APP_NAME = "TreniCal Admin Console";
    private static final String VERSION = "v1.0";

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");

        System.setProperty("spring.main.sources", "com.trenicall.admin");

        setupLookAndFeel();
        setupFonts();

        SwingUtilities.invokeLater(() -> {
            try {
                showSplashScreen();

                SpringApplication app = new SpringApplication(TrenicallAdminApplication.class);
                app.setWebApplicationType(org.springframework.boot.WebApplicationType.NONE);

                ConfigurableApplicationContext context = app.run(args);

                AdminMainFrame mainFrame = context.getBean(AdminMainFrame.class);

                mainFrame.initializeGUI();

            } catch (Exception e) {
                e.printStackTrace();
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
                UIManager.put("Label.font", defaultFont);
                UIManager.put("Button.font", defaultFont);
                UIManager.put("TextField.font", defaultFont);
                UIManager.put("TextArea.font", defaultFont);
                UIManager.put("Table.font", defaultFont);
                UIManager.put("ComboBox.font", defaultFont);
            }

        } catch (Exception e) {
            System.err.println("⚠️ Errore configurazione font: " + e.getMessage());
        }
    }

    private static void showSplashScreen() {
        try {
            JWindow splash = new JWindow();
            splash.setSize(400, 250);
            splash.setLocationRelativeTo(null);

            JPanel content = new JPanel(new BorderLayout());
            content.setBackground(new Color(44, 62, 80));
            content.setBorder(BorderFactory.createLineBorder(new Color(52, 73, 94), 2));

            JLabel appLabel = new JLabel("🚄 " + APP_NAME + " " + VERSION, SwingConstants.CENTER);
            appLabel.setForeground(Color.WHITE);
            appLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));

            JLabel statusLabel = new JLabel("Inizializzazione in corso...", SwingConstants.CENTER);
            statusLabel.setForeground(new Color(189, 195, 199));
            statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            progressBar.setBackground(new Color(52, 73, 94));
            progressBar.setForeground(new Color(52, 152, 219));

            content.add(appLabel, BorderLayout.CENTER);
            content.add(statusLabel, BorderLayout.SOUTH);
            content.add(progressBar, BorderLayout.PAGE_END);

            splash.setContentPane(content);
            splash.setVisible(true);

            Thread.sleep(1500);
            splash.dispose();

        } catch (Exception e) {
            System.err.println("⚠️ Errore splash screen: " + e.getMessage());
        }
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