import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class SecuritySuite extends JFrame {
    private CardLayout mainCardLayout;
    private JPanel mainCardPanel;
    private LoginPanel loginPanel;
    private JPanel mainAppPanel;

    // Colors for modern UI
    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private final Color ACCENT_COLOR = new Color(46, 204, 113);
    private final Color DANGER_COLOR = new Color(231, 76, 60);
    private final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private final Color CARD_COLOR = Color.WHITE;

    public SecuritySuite() {
        initializeUI();
        setAppIcon();
        setupComponents();
    }

    private void setAppIcon() {
        try {
            List<Image> icons = new ArrayList<>();

            // Load from resources (place icon in src folder)
            ImageIcon customIcon = new ImageIcon(getClass().getResource("/my-icon.png"));
            Image baseImage = customIcon.getImage();

            // Create multiple sizes from your custom image
            icons.add(baseImage.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
            icons.add(baseImage.getScaledInstance(32, 32, Image.SCALE_SMOOTH));
            icons.add(baseImage.getScaledInstance(48, 48, Image.SCALE_SMOOTH));
            icons.add(baseImage.getScaledInstance(64, 64, Image.SCALE_SMOOTH));

            setIconImages(icons);

        } catch (Exception e) {
            System.out.println("Error loading custom icon: " + e.getMessage());
            e.printStackTrace(); // This will show you the exact error
            // Fallback to programmatic icon
            createProgrammaticIcons();
        }
    }

    private void createProgrammaticIcons() {
        try {
            List<Image> icons = new ArrayList<>();
            icons.add(createIconImage(16, 16).getImage());
            icons.add(createIconImage(32, 32).getImage());
            icons.add(createIconImage(48, 48).getImage());
            icons.add(createIconImage(64, 64).getImage());
            setIconImages(icons);
        } catch (Exception e) {
            System.out.println("Failed to create programmatic icons");
        }
    }

    private ImageIcon createIconImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw shield background
        GradientPaint gradient = new GradientPaint(0, 0, new Color(41, 128, 185),
                width, height, new Color(52, 152, 219));
        g2d.setPaint(gradient);
        g2d.fillRoundRect(2, 2, width-4, height-4, 8, 8);

        // Draw shield border
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(2, 2, width-4, height-4, 8, 8);

        // Draw lock icon inside
        g2d.setColor(Color.WHITE);
        int lockSize = width / 3;
        int lockX = (width - lockSize) / 2;
        int lockY = (height - lockSize) / 2;

        // Lock body
        g2d.fillRect(lockX, lockY + lockSize/3, lockSize, lockSize * 2/3);
        // Lock arch
        g2d.fillArc(lockX - lockSize/6, lockY, lockSize * 4/3, lockSize/2, 0, 180);

        g2d.dispose();
        return new ImageIcon(image);
    }

    private void initializeUI() {
        setTitle("OutCore");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        // Set modern look and feel
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // Main card layout for login/main app switching
        mainCardLayout = new CardLayout();
        mainCardPanel = new JPanel(mainCardLayout);
        mainCardPanel.setBackground(BACKGROUND_COLOR);

        setContentPane(mainCardPanel);
    }

    private void setupComponents() {
        // Create login panel
        loginPanel = new LoginPanel(this);

        // Create main application panel
        mainAppPanel = createMainApplicationPanel();

        // Add both to card layout
        mainCardPanel.add(loginPanel, "login");
        mainCardPanel.add(mainAppPanel, "main");

        // Show login panel first
        showLoginPanel();
    }

    private JPanel createMainApplicationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);

        // Header
        panel.add(createHeader(), BorderLayout.NORTH);

        // Main content area with card layout
        JPanel contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(BACKGROUND_COLOR);

        // Add feature panels
        contentPanel.add(createPasswordCheckerPanel(), "Password Checker");
        contentPanel.add(createNetworkScannerPanel(), "Network Scanner");
        contentPanel.add(createFileEncryptionPanel(), "File Encryption");

        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        header.setPreferredSize(new Dimension(getWidth(), 80));

        // Title
        JLabel title = new JLabel("OutCore - Security Suite");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.LEFT);

        // Navigation panel
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        navPanel.setOpaque(false);

        String[] navItems = {"Password Checker", "Network Scanner", "File Encryption"};
        for (String item : navItems) {
            JButton navButton = createNavButton(item);
            navButton.addActionListener(e -> switchPanel(item));
            navPanel.add(navButton);
        }

        // Add logout button
        JButton logoutBtn = createNavButton("Logout");
        logoutBtn.addActionListener(e -> logout());
        navPanel.add(logoutBtn);

        header.add(title, BorderLayout.WEST);
        header.add(navPanel, BorderLayout.EAST);

        return header;
    }

    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(SECONDARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.WHITE, 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(ACCENT_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(SECONDARY_COLOR);
            }
        });

        return button;
    }

    private void switchPanel(String panelName) {
        CardLayout cl = (CardLayout) ((JPanel) mainAppPanel.getComponent(1)).getLayout();
        cl.show((JPanel) mainAppPanel.getComponent(1), panelName);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?", "Logout",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            showLoginPanel();
        }
    }

    public void showMainApplication() {
        mainCardLayout.show(mainCardPanel, "main");
        switchPanel("Password Checker");
    }

    public void showLoginPanel() {
        mainCardLayout.show(mainCardPanel, "login");
    }

    private JPanel createPasswordCheckerPanel() {
        return new PasswordCheckerPanel(PRIMARY_COLOR, SECONDARY_COLOR, ACCENT_COLOR,
                DANGER_COLOR, BACKGROUND_COLOR, CARD_COLOR);
    }

    private JPanel createNetworkScannerPanel() {
        return new NetworkScannerPanel(PRIMARY_COLOR, SECONDARY_COLOR, ACCENT_COLOR,
                DANGER_COLOR, BACKGROUND_COLOR, CARD_COLOR);
    }

    private JPanel createFileEncryptionPanel() {
        return new FileEncryptionPanel(PRIMARY_COLOR, SECONDARY_COLOR, ACCENT_COLOR,
                DANGER_COLOR, BACKGROUND_COLOR, CARD_COLOR);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SecuritySuite().setVisible(true);
        });
    }
}
