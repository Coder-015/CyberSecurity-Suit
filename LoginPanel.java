import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class LoginPanel extends JPanel {
    private CardLayout cardLayout;
    private JPanel mainCardPanel;
    private JTextField loginUsername, signupUsername;
    private JPasswordField loginPassword, signupPassword, confirmPassword;
    private JCheckBox showPassword;
    private SecuritySuite mainApp;

    // Colors
    private final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private final Color ACCENT_COLOR = new Color(46, 204, 113);
    private final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private final Color CARD_COLOR = Color.WHITE;

    public LoginPanel(SecuritySuite mainApp) {
        this.mainApp = mainApp;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);

        cardLayout = new CardLayout();
        mainCardPanel = new JPanel(cardLayout);
        mainCardPanel.setBackground(BACKGROUND_COLOR);

        // Create panels
        mainCardPanel.add(createWelcomePanel(), "welcome");
        mainCardPanel.add(createLoginPanel(), "login");
        mainCardPanel.add(createSignupPanel(), "signup");

        add(createHeader(), BorderLayout.NORTH);
        add(mainCardPanel, BorderLayout.CENTER);

        showWelcomePanel();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setBackground(PRIMARY_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        header.setPreferredSize(new Dimension(getWidth(), 70));

        JLabel title = new JLabel("Security Suite");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);

        header.add(title);
        return header;
    }

private JPanel createWelcomePanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBackground(BACKGROUND_COLOR);
    panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

    // Content panel
    JPanel contentPanel = new JPanel();
    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
    contentPanel.setBackground(BACKGROUND_COLOR);

    // Welcome message
    JLabel welcomeLabel = new JLabel("Welcome to Security Suite");
    welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
    welcomeLabel.setForeground(PRIMARY_COLOR);
    welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

    JLabel subtitle = new JLabel("Your All-in-One Security Solution");
    subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 18));
    subtitle.setForeground(Color.DARK_GRAY);
    subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

    // Features list
    JPanel featuresPanel = createCardPanel(CARD_COLOR);
    featuresPanel.setLayout(new BoxLayout(featuresPanel, BoxLayout.Y_AXIS));
    featuresPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

    String[] features = {
            " Advanced Password Strength Checker",
            " Comprehensive Network Scanner",
            " Secure File Encryption with AES",
            " Real-time Security Analysis",
            " Enterprise-grade Protection"
    };

    for (String feature : features) {
        JLabel featureLabel = new JLabel(feature);
        featureLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        featureLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        featuresPanel.add(featureLabel);
    }

    // Button panel
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
    buttonPanel.setOpaque(false);
    buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

    JButton loginBtn = createStyledButton("Login", SECONDARY_COLOR, 150, 45);
    JButton signupBtn = createStyledButton("Sign Up", ACCENT_COLOR, 150, 45);

    loginBtn.addActionListener(e -> showLoginPanel());
    signupBtn.addActionListener(e -> showSignupPanel());

    buttonPanel.add(loginBtn);
    buttonPanel.add(signupBtn);

    // Add components
    contentPanel.add(welcomeLabel);
    contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    contentPanel.add(subtitle);
    contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));
    contentPanel.add(featuresPanel);
    contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));
    contentPanel.add(buttonPanel);

    panel.add(contentPanel);
    return panel;
}

    private JPanel createLoginPanel() {
        JPanel panel = createCardPanel(CARD_COLOR);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Title
        JLabel title = new JLabel("Login to Your Account");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(PRIMARY_COLOR);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(3, 2, 10, 15));
        formPanel.setOpaque(false);
        formPanel.setMaximumSize(new Dimension(400, 150));

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginUsername = new JTextField();
        styleTextField(loginUsername);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginPassword = new JPasswordField();
        styleTextField(loginPassword);

        JLabel empty = new JLabel();
        showPassword = new JCheckBox("Show Password");
        showPassword.setOpaque(false);
        showPassword.addActionListener(e -> {
            loginPassword.setEchoChar(showPassword.isSelected() ? (char) 0 : '•');
        });

        formPanel.add(userLabel);
        formPanel.add(loginUsername);
        formPanel.add(passLabel);
        formPanel.add(loginPassword);
        formPanel.add(empty);
        formPanel.add(showPassword);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        JButton loginBtn = createStyledButton("Login", ACCENT_COLOR, 120, 40);
        JButton backBtn = createStyledButton("Back", SECONDARY_COLOR, 120, 40);

        loginBtn.addActionListener(e -> performLogin());
        backBtn.addActionListener(e -> showWelcomePanel());

        // Enter key listener for login
        loginPassword.addActionListener(e -> performLogin());

        buttonPanel.add(backBtn);
        buttonPanel.add(loginBtn);

        // Add components
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(formPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(buttonPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        return panel;
    }

    private JPanel createSignupPanel() {
        JPanel panel = createCardPanel(CARD_COLOR);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Title
        JLabel title = new JLabel("Create New Account");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(PRIMARY_COLOR);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(4, 2, 10, 15));
        formPanel.setOpaque(false);
        formPanel.setMaximumSize(new Dimension(400, 200));

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        signupUsername = new JTextField();
        styleTextField(signupUsername);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        signupPassword = new JPasswordField();
        styleTextField(signupPassword);

        JLabel confirmLabel = new JLabel("Confirm Password:");
        confirmLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        confirmPassword = new JPasswordField();
        styleTextField(confirmPassword);

        JLabel empty = new JLabel();
        JCheckBox showSignupPassword = new JCheckBox("Show Password");
        showSignupPassword.setOpaque(false);
        showSignupPassword.addActionListener(e -> {
            char echoChar = showSignupPassword.isSelected() ? (char) 0 : '•';
            signupPassword.setEchoChar(echoChar);
            confirmPassword.setEchoChar(echoChar);
        });

        formPanel.add(userLabel);
        formPanel.add(signupUsername);
        formPanel.add(passLabel);
        formPanel.add(signupPassword);
        formPanel.add(confirmLabel);
        formPanel.add(confirmPassword);
        formPanel.add(empty);
        formPanel.add(showSignupPassword);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        JButton signupBtn = createStyledButton("Sign Up", ACCENT_COLOR, 120, 40);
        JButton backBtn = createStyledButton("Back", SECONDARY_COLOR, 120, 40);

        signupBtn.addActionListener(e -> performSignup());
        backBtn.addActionListener(e -> showWelcomePanel());

        buttonPanel.add(backBtn);
        buttonPanel.add(signupBtn);

        // Add components
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(formPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(buttonPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        return panel;
    }

    private JButton createStyledButton(String text, Color color, int width, int height) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(width, height));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    private JPanel createCardPanel(Color color) {
        JPanel panel = new JPanel();
        panel.setBackground(color);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        panel.setMaximumSize(new Dimension(500, 600));
        return panel;
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
    }

    private void showWelcomePanel() {
        cardLayout.show(mainCardPanel, "welcome");
        clearForms();
    }

    private void showLoginPanel() {
        cardLayout.show(mainCardPanel, "login");
        loginUsername.requestFocus();
    }

    private void showSignupPanel() {
        cardLayout.show(mainCardPanel, "signup");
        signupUsername.requestFocus();
    }

    private void clearForms() {
        if (loginUsername != null) loginUsername.setText("");
        if (loginPassword != null) loginPassword.setText("");
        if (signupUsername != null) signupUsername.setText("");
        if (signupPassword != null) signupPassword.setText("");
        if (confirmPassword != null) confirmPassword.setText("");
        if (showPassword != null) {
            showPassword.setSelected(false);
            if (loginPassword != null) loginPassword.setEchoChar('•');
        }
    }

    private void performLogin() {
        String username = loginUsername.getText().trim();
        String password = new String(loginPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (User.login(username, password)) {
            JOptionPane.showMessageDialog(this, "Login successful! Welcome back, " + username, "Success", JOptionPane.INFORMATION_MESSAGE);
            mainApp.showMainApplication();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performSignup() {
        String username = signupUsername.getText().trim();
        String password = new String(signupPassword.getPassword());
        String confirm = new String(confirmPassword.getPassword());

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters long", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (User.register(username, password)) {
            JOptionPane.showMessageDialog(this, "Account created successfully! You can now login.", "Success", JOptionPane.INFORMATION_MESSAGE);
            showLoginPanel();
        } else {
            JOptionPane.showMessageDialog(this, "Username already exists", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}