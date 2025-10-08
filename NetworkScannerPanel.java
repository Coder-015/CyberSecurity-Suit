import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class NetworkScannerPanel extends JPanel {
    private JTextField ipRangeField;
    private JTextField startPortField, endPortField;
    private JButton scanButton, stopButton;
    private JTextArea logArea;
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private JProgressBar progressBar;
    private JComboBox<String> commonPortsCombo;
    private JTextArea portInfoArea;
    private boolean scanning = false;
    private ExecutorService executor;

    public NetworkScannerPanel(Color primary, Color secondary, Color accent,
                               Color danger, Color bgColor, Color cardColor) {
        setLayout(new BorderLayout());
        setBackground(bgColor);
        initializeComponents(primary, secondary, accent, danger, bgColor, cardColor);
    }

    private void initializeComponents(Color primary, Color secondary, Color accent,
                                      Color danger, Color bgColor, Color cardColor) {
        // Main content panel with proper scrolling
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(bgColor);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Wrap in scroll pane for the entire content
        JScrollPane mainScrollPane = new JScrollPane(contentPanel);
        mainScrollPane.setBorder(BorderFactory.createEmptyBorder());
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScrollPane, BorderLayout.CENTER);

        // Title
        JLabel title = new JLabel("Network Scanner - Beginner Friendly");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(primary);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Tips Panel - LARGER and more readable
        JPanel tipsPanel = createCardPanel(cardColor, 850, 180);
        tipsPanel.setLayout(new BorderLayout());

        JLabel tipsTitle = new JLabel(" Beginner Tips - Understanding Ports");
        tipsTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tipsTitle.setForeground(primary);
        tipsTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JTextArea tipsArea = new JTextArea();
        tipsArea.setText(" WHAT ARE PORTS?\n" +
                "• Ports are like doors on a computer where services run\n" +
                "• Common ports: 80 (Web), 443 (Secure Web), 22 (SSH)\n" +
                "• Open ports mean services are running and accessible\n" +
                "• Closed ports mean no service is listening\n\n" +
                " SCANNING TIPS:\n" +
                "• Start with common ports (1-1000) for basic scan\n" +
                "• Use specific IP ranges like 192.168.1.1-50\n" +
                "• Scan your own network first for practice");
        tipsArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tipsArea.setEditable(false);
        tipsArea.setBackground(cardColor);
        tipsArea.setLineWrap(true);
        tipsArea.setWrapStyleWord(true);
        tipsArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tipsPanel.add(tipsTitle, BorderLayout.NORTH);
        tipsPanel.add(new JScrollPane(tipsArea), BorderLayout.CENTER);

        // Input panel - LARGER and better organized
        JPanel inputPanel = createCardPanel(cardColor, 850, 200);
        inputPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // IP Range
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel ipLabel = new JLabel("IP Range (e.g., 192.168.1.1-100):");
        ipLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        inputPanel.add(ipLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        ipRangeField = new JTextField("192.168.1.1-100");
        styleTextField(ipRangeField);
        inputPanel.add(ipRangeField, gbc);

        // Common Ports Selection
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel commonPortsLabel = new JLabel("Quick Port Selection:");
        commonPortsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        inputPanel.add(commonPortsLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        String[] commonPortOptions = {
                "Custom Range",
                "Common Ports (1-1000)",
                "Web Services (80,443,8080)",
                "Network Services (21,22,23,25,53)",
                "All Ports (1-65535)"
        };
        commonPortsCombo = new JComboBox<>(commonPortOptions);
        commonPortsCombo.addActionListener(e -> updatePortRange());
        styleComboBox(commonPortsCombo);
        inputPanel.add(commonPortsCombo, gbc);

        // Port Range
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel portLabel = new JLabel("Port Range:");
        portLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        inputPanel.add(portLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        JPanel portPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        portPanel.setOpaque(false);

        startPortField = new JTextField("1", 6);
        endPortField = new JTextField("1000", 6);
        styleTextField(startPortField);
        styleTextField(endPortField);

        portPanel.add(startPortField);
        portPanel.add(new JLabel("   to   "));
        portPanel.add(endPortField);
        inputPanel.add(portPanel, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);

        scanButton = new JButton(" Start Scan");
        stopButton = new JButton(" Stop Scan");
        styleButton(scanButton, accent);
        styleButton(stopButton, danger);
        stopButton.setEnabled(false);

        scanButton.addActionListener(e -> startScan());
        stopButton.addActionListener(e -> stopScan());

        buttonPanel.add(scanButton);
        buttonPanel.add(stopButton);
        inputPanel.add(buttonPanel, gbc);

        // Progress bar
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(800, 25));

        // Port Information Panel - LARGER and scrollable
        JPanel portInfoPanel = createCardPanel(cardColor, 850, 220);
        portInfoPanel.setLayout(new BorderLayout());

        JLabel portInfoTitle = new JLabel(" Common Ports Reference Guide");
        portInfoTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        portInfoTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        portInfoArea = new JTextArea();
        portInfoArea.setEditable(false);
        portInfoArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        portInfoArea.setText(getCommonPortsReference());
        portInfoArea.setLineWrap(true);
        portInfoArea.setWrapStyleWord(true);

        JScrollPane portInfoScroll = new JScrollPane(portInfoArea);
        portInfoScroll.setPreferredSize(new Dimension(800, 150));
        portInfoScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        portInfoScroll.getVerticalScrollBar().setUnitIncrement(16);

        portInfoPanel.add(portInfoTitle, BorderLayout.NORTH);
        portInfoPanel.add(portInfoScroll, BorderLayout.CENTER);

        // Results table - LARGER and better organized
        JPanel resultsPanel = createCardPanel(cardColor, 850, 300);
        resultsPanel.setLayout(new BorderLayout());

        JLabel resultsTitle = new JLabel(" Scan Results");
        resultsTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        resultsTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        String[] columns = {"IP Address", "Port", "Status", "Service", "Description"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resultTable = new JTable(tableModel);
        resultTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resultTable.setRowHeight(30); // Taller rows for better readability
        resultTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Set column widths
        resultTable.getColumnModel().getColumn(0).setPreferredWidth(120); // IP
        resultTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Port
        resultTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Status
        resultTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Service
        resultTable.getColumnModel().getColumn(4).setPreferredWidth(350); // Description

        JScrollPane tableScroll = new JScrollPane(resultTable);
        tableScroll.setPreferredSize(new Dimension(800, 200));
        tableScroll.getVerticalScrollBar().setUnitIncrement(16);

        resultsPanel.add(resultsTitle, BorderLayout.NORTH);
        resultsPanel.add(tableScroll, BorderLayout.CENTER);

        // Log area - LARGER and more readable
        JPanel logPanel = createCardPanel(cardColor, 850, 250);
        logPanel.setLayout(new BorderLayout());

        JLabel logLabel = new JLabel(" Scan Log");
        logLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(250, 250, 250));

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(800, 150));
        logScroll.getVerticalScrollBar().setUnitIncrement(16);

        logPanel.add(logLabel, BorderLayout.NORTH);
        logPanel.add(logScroll, BorderLayout.CENTER);

        // Add components to content panel with proper spacing
        contentPanel.add(title);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        contentPanel.add(tipsPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(inputPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        contentPanel.add(progressBar);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(portInfoPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(resultsPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(logPanel);
    }

    // Enhanced card panel with customizable size
    private JPanel createCardPanel(Color color, int width, int height) {
        JPanel panel = new JPanel();
        panel.setBackground(color);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        panel.setPreferredSize(new Dimension(width, height));
        panel.setMaximumSize(new Dimension(width, height));
        panel.setMinimumSize(new Dimension(width, 100));
        return panel;
    }

    // Overloaded method for default size (backward compatibility)
    private JPanel createCardPanel(Color color) {
        return createCardPanel(color, 800, 150);
    }

    private void updatePortRange() {
        String selected = (String) commonPortsCombo.getSelectedItem();
        switch (selected) {
            case "Common Ports (1-1000)":
                startPortField.setText("1");
                endPortField.setText("1000");
                break;
            case "Web Services (80,443,8080)":
                startPortField.setText("80");
                endPortField.setText("443");
                break;
            case "Network Services (21,22,23,25,53)":
                startPortField.setText("21");
                endPortField.setText("53");
                break;
            case "All Ports (1-65535)":
                startPortField.setText("1");
                endPortField.setText("65535");
                break;
            case "Custom Range":
                // Keep current values
                break;
        }
    }

    private String getCommonPortsReference() {
        return " SECURE PORTS:\n" +
                "• Port 22 (SSH) - Secure remote administration\n" +
                "• Port 443 (HTTPS) - Encrypted web browsing\n" +
                "• Port 993 (IMAPS) - Secure email access\n" +
                "• Port 995 (POP3S) - Secure email retrieval\n\n" +

                " WEB PORTS:\n" +
                "• Port 80 (HTTP) - Regular web browsing\n" +
                "• Port 443 (HTTPS) - Secure web browsing\n" +
                "• Port 8080 (HTTP-Alt) - Alternative web port\n" +
                "• Port 8443 (HTTPS-Alt) - Alternative secure web\n\n" +

                " EMAIL PORTS:\n" +
                "• Port 25 (SMTP) - Sending emails\n" +
                "• Port 110 (POP3) - Receiving emails\n" +
                "• Port 143 (IMAP) - Email access\n" +
                "• Port 993/995 - Secure email ports\n\n" +

                " DATABASE PORTS:\n" +
                "• Port 1433 (SQL Server) - Microsoft database\n" +
                "• Port 3306 (MySQL) - Popular database system\n" +
                "• Port 5432 (PostgreSQL) - Advanced database\n\n" +

                " REMOTE ACCESS:\n" +
                "• Port 3389 (RDP) - Windows Remote Desktop\n" +
                "• Port 5900 (VNC) - Virtual Network Computing";
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(150, 150, 150), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setPreferredSize(new Dimension(200, 35));
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBackground(Color.WHITE);
        combo.setPreferredSize(new Dimension(200, 35));
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(color.darker(), 1),
                BorderFactory.createEmptyBorder(12, 25, 12, 25)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effects
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(color.brighter());
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(color);
            }
        });
    }

    // ... (keep the rest of your existing methods: startScan, stopScan, parseIPRange,
    // getServiceName, getPortDescription, addResult, log) exactly as they were ...
    // Only the UI components above have been modified for better sizing

    private void startScan() {
        if (scanning) return;

        String ipRange = ipRangeField.getText().trim();
        int startPort, endPort;

        try {
            startPort = Integer.parseInt(startPortField.getText().trim());
            endPort = Integer.parseInt(endPortField.getText().trim());

            if (startPort < 1 || endPort > 65535 || startPort > endPort) {
                throw new NumberFormatException("Invalid port range");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid port numbers (1-65535)",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        scanning = true;
        scanButton.setEnabled(false);
        stopButton.setEnabled(true);
        progressBar.setVisible(true);
        tableModel.setRowCount(0);
        logArea.setText("");
        log(" Starting network scan...");
        log(" IP Range: " + ipRange);
        log(" Port Range: " + startPort + " to " + endPort);
        log(" Tip: Open ports show services running on devices");

        executor = Executors.newFixedThreadPool(50);

        // Parse IP range and start scanning
        List<String> ipList = parseIPRange(ipRange);
        final int totalScans = ipList.size() * (endPort - startPort + 1);
        final int[] completedScans = {0};

        for (String ip : ipList) {
            for (int port = startPort; port <= endPort; port++) {
                final String currentIP = ip;
                final int currentPort = port;

                executor.submit(() -> {
                    if (!scanning) return;

                    try {
                        Socket socket = new Socket();
                        socket.connect(new InetSocketAddress(currentIP, currentPort), 1000);
                        socket.close();

                        // Open port found
                        SwingUtilities.invokeLater(() -> {
                            String service = getServiceName(currentPort);
                            String description = getPortDescription(currentPort);
                            addResult(currentIP, currentPort, "Open", service, description);
                            log(" OPEN: " + currentIP + ":" + currentPort + " (" + service + ")");
                        });
                    } catch (Exception e) {
                        // Port is closed or filtered
                    }

                    completedScans[0]++;
                    final int progress = (completedScans[0] * 100) / totalScans;
                    SwingUtilities.invokeLater(() -> {
                        progressBar.setValue(progress);
                        if (completedScans[0] == totalScans) {
                            stopScan();
                            log(" Scan completed! Found " + tableModel.getRowCount() + " open ports");
                        }
                    });
                });
            }
        }
    }

    private void stopScan() {
        scanning = false;
        if (executor != null) {
            executor.shutdownNow();
        }
        scanButton.setEnabled(true);
        stopButton.setEnabled(false);
        progressBar.setVisible(false);
        log(" Scan stopped by user");
    }

    private List<String> parseIPRange(String ipRange) {
        List<String> ipList = new ArrayList<>();

        try {
            if (ipRange.contains("-")) {
                String[] parts = ipRange.split("\\.");
                String base = parts[0] + "." + parts[1] + "." + parts[2] + ".";
                String[] rangeParts = parts[3].split("-");

                int start = Integer.parseInt(rangeParts[0]);
                int end = Integer.parseInt(rangeParts[1]);

                for (int i = start; i <= end; i++) {
                    ipList.add(base + i);
                }
            } else {
                ipList.add(ipRange);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid IP range format. Use: 192.168.1.1-100",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        return ipList;
    }

    private String getServiceName(int port) {
        // Common port services
        switch (port) {
            case 21: return "FTP";
            case 22: return "SSH";
            case 23: return "Telnet";
            case 25: return "SMTP";
            case 53: return "DNS";
            case 80: return "HTTP";
            case 110: return "POP3";
            case 143: return "IMAP";
            case 443: return "HTTPS";
            case 993: return "IMAPS";
            case 995: return "POP3S";
            case 3306: return "MySQL";
            case 3389: return "RDP";
            case 5432: return "PostgreSQL";
            case 8080: return "HTTP-Alt";
            case 8443: return "HTTPS-Alt";
            default: return "Unknown";
        }
    }

    private String getPortDescription(int port) {
        // Detailed descriptions for common ports
        switch (port) {
            case 21: return "File Transfer Protocol - for uploading/downloading files";
            case 22: return "Secure Shell - secure remote administration";
            case 23: return "Telnet - unsecure remote login (avoid using)";
            case 25: return "Simple Mail Transfer Protocol - sending emails";
            case 53: return "Domain Name System - translates domain names to IPs";
            case 80: return "Hypertext Transfer Protocol - regular web browsing";
            case 110: return "Post Office Protocol v3 - receiving emails";
            case 143: return "Internet Message Access Protocol - email access";
            case 443: return "HTTP Secure - encrypted web browsing";
            case 993: return "IMAP over SSL - secure email access";
            case 995: return "POP3 over SSL - secure email retrieval";
            case 3306: return "MySQL Database - popular database system";
            case 3389: return "Remote Desktop Protocol - Windows remote access";
            case 5432: return "PostgreSQL Database - advanced database system";
            case 8080: return "HTTP Alternative - often used for web proxies";
            case 8443: return "HTTPS Alternative - secure web services";
            default:
                if (port <= 1024) return "Well-known port - system services";
                else if (port <= 49151) return "Registered port - user applications";
                else return "Dynamic/private port - temporary use";
        }
    }

    private void addResult(String ip, int port, String status, String service, String description) {
        tableModel.addRow(new Object[]{ip, port, status, service, description});
    }

    private void log(String message) {
        logArea.append("[" + new Date().toString().split(" ")[3] + "] " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
