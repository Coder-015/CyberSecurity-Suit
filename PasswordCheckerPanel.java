import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.regex.*;

class PasswordCheckerPanel extends JPanel {
    private JPasswordField passwordField;
    private JCheckBox showPassword;
    private JLabel strengthLabel;
    private JProgressBar strengthBar;
    private JTextArea feedbackArea;

    public PasswordCheckerPanel(Color primary, Color secondary, Color accent,
                                Color danger, Color bgColor, Color cardColor) {
        setLayout(new BorderLayout());
        setBackground(bgColor);
        initializeComponents(primary, secondary, accent, danger, bgColor, cardColor);
    }

    private void initializeComponents(Color primary, Color secondary, Color accent,
                                      Color danger, Color bgColor, Color cardColor) {
        // Main content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(bgColor);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        // Title
        JLabel title = new JLabel("Password Strength Checker");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(primary);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Input panel
        JPanel inputPanel = createCardPanel(cardColor);
        inputPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel passwordLabel = new JLabel("Enter Password:");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        inputPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(secondary, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        gbc.gridy = 1;
        inputPanel.add(passwordField, gbc);

        showPassword = new JCheckBox("Show Password");
        showPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        showPassword.setBackground(cardColor);
        showPassword.addActionListener(e -> {
            passwordField.setEchoChar(showPassword.isSelected() ? (char) 0 : '•');
        });
        gbc.gridy = 2;
        inputPanel.add(showPassword, gbc);

        // Check button
        JButton checkButton = new JButton("Check Password Strength");
        styleButton(checkButton, accent);
        checkButton.addActionListener(e -> checkPasswordStrength());
        gbc.gridy = 3;
        inputPanel.add(checkButton, gbc);

        // Strength indicator panel
        JPanel strengthPanel = createCardPanel(cardColor);
        strengthPanel.setLayout(new BoxLayout(strengthPanel, BoxLayout.Y_AXIS));

        strengthBar = new JProgressBar(0, 100);
        strengthBar.setPreferredSize(new Dimension(300, 20));
        strengthBar.setStringPainted(true);

        strengthLabel = new JLabel("Enter a password to check strength");
        strengthLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        strengthLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        strengthPanel.add(strengthLabel);
        strengthPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        strengthPanel.add(strengthBar);

        // Feedback panel
        JPanel feedbackPanel = createCardPanel(cardColor);
        feedbackPanel.setLayout(new BorderLayout());

        JLabel feedbackTitle = new JLabel("Password Analysis:");
        feedbackTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        feedbackTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        feedbackArea = new JTextArea(8, 30);
        feedbackArea.setEditable(false);
        feedbackArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        feedbackArea.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(secondary, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        JScrollPane scrollPane = new JScrollPane(feedbackArea);

        feedbackPanel.add(feedbackTitle, BorderLayout.NORTH);
        feedbackPanel.add(scrollPane, BorderLayout.CENTER);

        // Add components to content panel
        contentPanel.add(title);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(inputPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(strengthPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(feedbackPanel);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createCardPanel(Color color) {
        JPanel panel = new JPanel();
        panel.setBackground(color);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        return panel;
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void checkPasswordStrength() {
        char[] password = passwordField.getPassword();
        if (password.length == 0) {
            strengthLabel.setText("Please enter a password");
            strengthLabel.setForeground(Color.GRAY);
            strengthBar.setValue(0);
            feedbackArea.setText("");
            return;
        }

        String pass = new String(password);
        int score = calculatePasswordScore(pass);
        String strength = getStrengthLevel(score);
        Color color = getStrengthColor(strength);

        strengthBar.setValue(score);
        strengthLabel.setText("Strength: " + strength);
        strengthLabel.setForeground(color);

        // Generate feedback
        StringBuilder feedback = new StringBuilder();
        feedback.append(generateFeedback(pass)).append("\n\n");
        feedback.append("Password Length: ").append(pass.length()).append(" characters\n");

        if (score >= 80) {
            feedback.append("✓ Excellent! This is a very strong password.\n");
        } else if (score >= 60) {
            feedback.append("✓ Good password. Consider adding more complexity.\n");
        } else if (score >= 40) {
            feedback.append("⚠ Moderate strength. Should be improved.\n");
        } else {
            feedback.append("✗ Weak password. Highly recommended to change.\n");
        }

        feedbackArea.setText(feedback.toString());
    }

    private int calculatePasswordScore(String password) {
        int score = 0;

        // Length check
        if (password.length() >= 8) score += 20;
        if (password.length() >= 12) score += 10;

        // Character variety
        if (Pattern.compile("[a-z]").matcher(password).find()) score += 10;
        if (Pattern.compile("[A-Z]").matcher(password).find()) score += 10;
        if (Pattern.compile("[0-9]").matcher(password).find()) score += 10;
        if (Pattern.compile("[^a-zA-Z0-9]").matcher(password).find()) score += 15;

        // Bonus points for complexity
        if (password.length() >= 16) score += 15;
        if (Pattern.compile("(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9])").matcher(password).find())
            score += 20;

        return Math.min(score, 100);
    }

    private String getStrengthLevel(int score) {
        if (score >= 80) return "Very Strong";
        if (score >= 60) return "Strong";
        if (score >= 40) return "Moderate";
        if (score >= 20) return "Weak";
        return "Very Weak";
    }

    private Color getStrengthColor(String strength) {
        switch (strength) {
            case "Very Strong": return new Color(46, 204, 113);
            case "Strong": return new Color(52, 152, 219);
            case "Moderate": return new Color(241, 196, 15);
            case "Weak": return new Color(230, 126, 34);
            case "Very Weak": return new Color(231, 76, 60);
            default: return Color.GRAY;
        }
    }

    private String generateFeedback(String password) {
        StringBuilder feedback = new StringBuilder();

        if (password.length() < 8) {
            feedback.append("✗ Password is too short (minimum 8 characters recommended)\n");
        } else {
            feedback.append("✓ Good password length\n");
        }

        if (!Pattern.compile("[a-z]").matcher(password).find()) {
            feedback.append("✗ Add lowercase letters\n");
        }

        if (!Pattern.compile("[A-Z]").matcher(password).find()) {
            feedback.append("✗ Add uppercase letters\n");
        }

        if (!Pattern.compile("[0-9]").matcher(password).find()) {
            feedback.append("✗ Add numbers\n");
        }

        if (!Pattern.compile("[^a-zA-Z0-9]").matcher(password).find()) {
            feedback.append("✗ Add special characters\n");
        }

        if (Pattern.compile("(.)\\1{2,}").matcher(password).find()) {
            feedback.append("✗ Avoid repeated characters\n");
        }

        if (Pattern.compile("(123|abc|password|admin)").matcher(password.toLowerCase()).find()) {
            feedback.append("✗ Avoid common patterns and words\n");
        }

        if (feedback.length() == 0) {
            feedback.append("✓ All checks passed! Excellent password.\n");
        }

        return feedback.toString();
    }
}