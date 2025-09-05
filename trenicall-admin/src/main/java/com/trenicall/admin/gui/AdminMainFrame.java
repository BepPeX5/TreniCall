package com.trenicall.admin.gui;

import com.trenicall.admin.gui.panels.*;
import com.trenicall.admin.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class AdminMainFrame extends JFrame {

    private final Color PRIMARY_COLOR = new Color(44, 62, 80);
    private final Color SECONDARY_COLOR = new Color(52, 73, 94);
    private final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private final Color WARNING_COLOR = new Color(241, 196, 15);
    private final Color DANGER_COLOR = new Color(231, 76, 60);
    private final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private final Color CARD_COLOR = Color.WHITE;
    private final Color SIDEBAR_COLOR = new Color(52, 73, 94);

    @Autowired
    private AdminService adminService;

    private JLabel statusLabel;
    private JLabel connectionLabel;
    private JLabel timeLabel;
    private Timer clockTimer;

    private DashboardPanel dashboardPanel;
    private TrainsManagementPanel trainsPanel;
    private PromotionsManagementPanel promotionsPanel;
    private ClientsMonitoringPanel clientsPanel;
    private BookingsMonitoringPanel bookingsPanel;
    private NotificationCenterPanel notificationsPanel;

    public void initializeGUI() {
        SwingUtilities.invokeLater(() -> {
            setupMainFrame();
            createMenuBar();
            initializeComponents();
            setupLayout();
            startStatusClock();
            setVisible(true);
        });
    }

    private void setupMainFrame() {
        setTitle("🔧 TreniCal Admin Console - Sistema Gestione Ferroviaria (Locale)");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1600, 1000);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1400, 800));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleApplicationExit();
            }
        });

        getContentPane().setBackground(BACKGROUND_COLOR);
        setIconImage(createAppIcon());
    }

    private Image createAppIcon() {
        return Toolkit.getDefaultToolkit().createImage(new byte[0]);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(PRIMARY_COLOR);
        menuBar.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        JMenu fileMenu = createStyledMenu("📁 File");
        fileMenu.add(createMenuItem("🔄 Ricarica Dati", e -> refreshData()));
        fileMenu.addSeparator();
        fileMenu.add(createMenuItem("🚪 Esci", e -> handleApplicationExit()));

        JMenu viewMenu = createStyledMenu("👁️ Visualizza");
        viewMenu.add(createMenuItem("📊 Dashboard", e -> showDashboard()));
        viewMenu.add(createMenuItem("🚂 Gestione Treni", e -> showTrainsManagement()));
        viewMenu.add(createMenuItem("💰 Promozioni", e -> showPromotionsManagement()));
        viewMenu.add(createMenuItem("👥 Clienti", e -> showClientsMonitoring()));

        JMenu toolsMenu = createStyledMenu("🛠️ Strumenti");
        toolsMenu.add(createMenuItem("📢 Centro Notifiche", e -> showNotificationCenter()));
        toolsMenu.add(createMenuItem("📋 Log Sistema", e -> showSystemLogs()));
        toolsMenu.addSeparator();
        toolsMenu.add(createMenuItem("⚙️ Impostazioni", e -> showSettings()));

        JMenu helpMenu = createStyledMenu("❓ Aiuto");
        helpMenu.add(createMenuItem("📖 Manuale Utente", e -> showUserManual()));
        helpMenu.add(createMenuItem("ℹ️ Info Sistema", e -> showSystemInfo()));

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(toolsMenu);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private JMenu createStyledMenu(String text) {
        JMenu menu = new JMenu(text);
        menu.setForeground(Color.WHITE);
        menu.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return menu;
    }

    private JMenuItem createMenuItem(String text, java.awt.event.ActionListener action) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(action);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return item;
    }

    private void initializeComponents() {
        dashboardPanel = new DashboardPanel(adminService);
        trainsPanel = new TrainsManagementPanel(adminService);
        promotionsPanel = new PromotionsManagementPanel(adminService);
        clientsPanel = new ClientsMonitoringPanel(adminService);
        bookingsPanel = new BookingsMonitoringPanel(adminService);
        notificationsPanel = new NotificationCenterPanel(adminService);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(0, 0));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainContentArea(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel leftSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        leftSection.setOpaque(false);

        JLabel logoLabel = new JLabel("🔧 TreniCal Admin");
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel versionLabel = new JLabel("Console v1.0 (Locale)");
        versionLabel.setForeground(new Color(189, 195, 199));
        versionLabel.setFont(new Font("Arial", Font.ITALIC, 12));

        leftSection.add(logoLabel);
        leftSection.add(versionLabel);

        JPanel rightSection = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightSection.setOpaque(false);

        connectionLabel = new JLabel("🔗 Connesso (Locale)");
        connectionLabel.setForeground(SUCCESS_COLOR);
        connectionLabel.setFont(new Font("Arial", Font.BOLD, 12));

        timeLabel = new JLabel();
        timeLabel.setForeground(Color.WHITE);
        timeLabel.setFont(new Font("Courier New", Font.BOLD, 14));

        rightSection.add(connectionLabel);
        rightSection.add(timeLabel);

        headerPanel.add(leftSection, BorderLayout.WEST);
        headerPanel.add(rightSection, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createMainContentArea() {
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(BACKGROUND_COLOR);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(createSidebarNavigation());
        splitPane.setRightComponent(createContentPanel());
        splitPane.setDividerLocation(280);
        splitPane.setDividerSize(2);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);

        mainContent.add(splitPane, BorderLayout.CENTER);
        return mainContent;
    }

    private JPanel createSidebarNavigation() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_COLOR);
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        sidebar.setPreferredSize(new Dimension(280, 0));

        String[][] menuItems = {
                {"📊", "Dashboard", "Panoramica generale sistema"},
                {"🚂", "Gestione Treni", "Treni, tratte e orari"},
                {"💰", "Promozioni", "Offerte e sconti attivi"},
                {"👥", "Clienti", "Monitoraggio utenti"},
                {"🎫", "Biglietti", "Prenotazioni e vendite"},
                {"📢", "Notifiche", "Centro comunicazioni"}
        };

        for (int i = 0; i < menuItems.length; i++) {
            final int index = i;
            JPanel menuItem = createSidebarMenuItem(
                    menuItems[i][0],
                    menuItems[i][1],
                    menuItems[i][2],
                    e -> switchToPanel(index),
                    i == 0
            );
            sidebar.add(menuItem);
            if (i < menuItems.length - 1) {
                sidebar.add(Box.createVerticalStrut(2));
            }
        }

        sidebar.add(Box.createVerticalGlue());

        JPanel systemInfo = new JPanel();
        systemInfo.setLayout(new BoxLayout(systemInfo, BoxLayout.Y_AXIS));
        systemInfo.setOpaque(false);
        systemInfo.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

        JLabel sysLabel = new JLabel("💻 Sistema Locale");
        sysLabel.setForeground(new Color(149, 165, 166));
        sysLabel.setFont(new Font("Arial", Font.BOLD, 11));
        sysLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);

        JLabel memoryLabel = new JLabel("RAM: " + getMemoryUsage());
        memoryLabel.setForeground(new Color(149, 165, 166));
        memoryLabel.setFont(new Font("Courier New", Font.PLAIN, 10));
        memoryLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);

        systemInfo.add(sysLabel);
        systemInfo.add(Box.createVerticalStrut(5));
        systemInfo.add(memoryLabel);

        sidebar.add(systemInfo);

        return sidebar;
    }

    private JPanel createSidebarMenuItem(String icon, String title, String description,
                                         java.awt.event.ActionListener action, boolean selected) {
        JPanel item = new JPanel();
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        item.setOpaque(true);
        item.setBackground(selected ? new Color(41, 128, 185) : SIDEBAR_COLOR);
        item.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleRow.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        titleRow.add(iconLabel);
        titleRow.add(titleLabel);

        JLabel descLabel = new JLabel(description);
        descLabel.setForeground(new Color(189, 195, 199));
        descLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        descLabel.setBorder(BorderFactory.createEmptyBorder(0, 35, 0, 0));

        item.add(titleRow);
        item.add(descLabel);

        item.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                action.actionPerformed(null);
            }
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!selected) {
                    item.setBackground(new Color(44, 62, 80));
                }
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!selected) {
                    item.setBackground(SIDEBAR_COLOR);
                }
            }
        });

        return item;
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        contentPanel.add(dashboardPanel, "dashboard");
        contentPanel.add(trainsPanel, "trains");
        contentPanel.add(promotionsPanel, "promotions");
        contentPanel.add(clientsPanel, "clients");
        contentPanel.add(bookingsPanel, "bookings");
        contentPanel.add(notificationsPanel, "notifications");

        return contentPanel;
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(236, 240, 241));
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));

        statusLabel = new JLabel("✅ Sistema operativo - Accesso diretto database");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(44, 62, 80));

        JLabel copyrightLabel = new JLabel("© 2025 TreniCal Admin System");
        copyrightLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        copyrightLabel.setForeground(new Color(127, 140, 141));

        statusBar.add(statusLabel, BorderLayout.WEST);
        statusBar.add(copyrightLabel, BorderLayout.EAST);

        return statusBar;
    }

    private void switchToPanel(int index) {
        CardLayout layout = (CardLayout) ((JPanel) ((JSplitPane)
                ((JPanel) getContentPane().getComponent(1)).getComponent(0)).getRightComponent()).getLayout();

        String[] panelNames = {"dashboard", "trains", "promotions", "clients", "bookings", "notifications"};
        layout.show((JPanel) ((JSplitPane)
                ((JPanel) getContentPane().getComponent(1)).getComponent(0)).getRightComponent(), panelNames[index]);

        updateSidebarSelection(index);
        updateStatusForPanel(index);
    }

    private void updateSidebarSelection(int selectedIndex) {
        JPanel sidebar = (JPanel) ((JSplitPane)
                ((JPanel) getContentPane().getComponent(1)).getComponent(0)).getLeftComponent();

        for (int i = 0; i < 6; i++) {
            JPanel item = (JPanel) sidebar.getComponent(i * 2);
            item.setBackground(i == selectedIndex ? new Color(41, 128, 185) : SIDEBAR_COLOR);
        }
    }

    private void updateStatusForPanel(int panelIndex) {
        String[] statusMessages = {
                "📊 Visualizzazione dashboard principale (dati reali)",
                "🚂 Gestione treni e tratte ferroviarie (database)",
                "💰 Amministrazione promozioni e offerte (database)",
                "👥 Monitoraggio clienti e statistiche (database)",
                "🎫 Controllo biglietti e prenotazioni (database)",
                "📢 Centro notifiche e comunicazioni"
        };
        statusLabel.setText(statusMessages[panelIndex]);
    }

    private void startStatusClock() {
        clockTimer = new Timer(1000, e -> {
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            timeLabel.setText("🕐 " + currentTime);
        });
        clockTimer.start();
    }

    private String getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        return String.format("%.1f/%.1f MB",
                usedMemory / 1024.0 / 1024.0,
                runtime.maxMemory() / 1024.0 / 1024.0);
    }

    private void refreshData() {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("🔄 Ricaricamento dati dal database...");
            try {
                adminService.refreshDashboardData();
                statusLabel.setText("✅ Dati aggiornati con successo");
                showSuccessMessage("Dati ricaricati dal database!");
            } catch (Exception e) {
                statusLabel.setText("⚠ Errore ricaricamento dati");
                showErrorDialog("Errore Aggiornamento",
                        "Impossibile aggiornare i dati: " + e.getMessage());
            }
        });
    }

    private void showDashboard() { switchToPanel(0); }
    private void showTrainsManagement() { switchToPanel(1); }
    private void showPromotionsManagement() { switchToPanel(2); }
    private void showClientsMonitoring() { switchToPanel(3); }
    private void showNotificationCenter() { switchToPanel(5); }

    private void showSystemLogs() {
        JFrame logFrame = new JFrame("Log Sistema TreniCal");
        logFrame.setSize(800, 600);
        logFrame.setLocationRelativeTo(this);

        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Courier New", Font.PLAIN, 12));

        java.util.List<String> logs = adminService.getSystemLogs();
        logArea.setText(String.join("\n", logs));

        JScrollPane scrollPane = new JScrollPane(logArea);
        logFrame.add(scrollPane);
        logFrame.setVisible(true);
    }

    private void showSettings() {
        showInfoMessage("Impostazioni", "Pannello impostazioni admin in sviluppo.");
    }

    private void showUserManual() {
        showInfoMessage("Manuale Utente", "Documentazione disponibile su docs.trenicall.com");
    }

    private void showSystemInfo() {
        String info = String.format(
                "🔧 TreniCal Admin Console v1.0\n\n" +
                        "💻 Sistema: %s\n" +
                        "☕ Java: %s\n" +
                        "🖥️ Memoria: %s\n" +
                        "🌐 Modalità: Accesso diretto database\n" +
                        "📊 Dati: Reali dal database MySQL\n\n" +
                        "📅 Build: Dicembre 2024",
                System.getProperty("os.name"),
                System.getProperty("java.version"),
                getMemoryUsage()
        );
        showInfoMessage("Informazioni Sistema", info);
    }

    private void handleApplicationExit() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Sei sicuro di voler chiudere l'applicazione admin?",
                "Conferma Uscita",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            if (clockTimer != null) {
                clockTimer.stop();
            }
            if (adminService != null) {
                adminService.shutdown();
            }
            System.exit(0);
        }
    }

    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Successo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void showInfoMessage(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
}