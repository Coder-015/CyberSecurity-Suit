import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.security.*;
import java.util.List;
import java.util.zip.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;

class FileEncryptionPanel extends JPanel {
    private JTextField filePathField;
    private JPasswordField passwordField;
    private JButton browseButton, encryptButton, decryptButton;
    private JTextArea statusArea;
    private JProgressBar progressBar;
    private JCheckBox compressCheckbox;
    private JLabel dragDropLabel;

    // Store original file names for proper restoration
    private java.util.Map<String, String> originalNames = new java.util.HashMap<>();

    public FileEncryptionPanel(Color primary, Color secondary, Color accent,
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
        JLabel title = new JLabel("File & Folder Encryption");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(primary);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Drag & Drop Panel
        JPanel dragDropPanel = createCardPanel(cardColor);
        dragDropPanel.setLayout(new BorderLayout());
        dragDropPanel.setPreferredSize(new Dimension(800, 120));
        dragDropPanel.setMaximumSize(new Dimension(800, 120));

        dragDropLabel = new JLabel("🖱️ Drag & Drop Files or Folders Here", SwingConstants.CENTER);
        dragDropLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        dragDropLabel.setForeground(primary);

        dragDropPanel.add(dragDropLabel, BorderLayout.CENTER);
        setupDragAndDrop();

        // Input Panel
        JPanel inputPanel = createCardPanel(cardColor);
        inputPanel.setLayout(new GridBagLayout());
        inputPanel.setPreferredSize(new Dimension(800, 180));
        inputPanel.setMaximumSize(new Dimension(800, 180));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // File path
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel fileLabel = new JLabel("File/Folder:");
        fileLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        inputPanel.add(fileLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        filePathField = new JTextField();
        styleTextField(filePathField);
        inputPanel.add(filePathField, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0;
        browseButton = new JButton("Browse");
        styleButton(browseButton, secondary);
        browseButton.addActionListener(e -> browseFileOrFolder());
        inputPanel.add(browseButton, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        inputPanel.add(passLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2;
        passwordField = new JPasswordField();
        styleTextField(passwordField);
        inputPanel.add(passwordField, gbc);

        // Compress checkbox
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2;
        compressCheckbox = new JCheckBox("Compress before encryption (saves space)");
        compressCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        compressCheckbox.setOpaque(false);
        inputPanel.add(compressCheckbox, gbc);

        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setPreferredSize(new Dimension(800, 60));
        buttonPanel.setMaximumSize(new Dimension(800, 60));

        encryptButton = new JButton("🔒 Encrypt");
        decryptButton = new JButton("🔓 Decrypt");
        styleButton(encryptButton, accent);
        styleButton(decryptButton, primary);

        encryptButton.addActionListener(e -> encryptFileOrFolder());
        decryptButton.addActionListener(e -> decryptFileOrFolder());

        buttonPanel.add(encryptButton);
        buttonPanel.add(decryptButton);

        // Progress bar
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(800, 25));
        progressBar.setMaximumSize(new Dimension(800, 25));

        // Status Panel
        JPanel statusPanel = createCardPanel(cardColor);
        statusPanel.setLayout(new BorderLayout());
        statusPanel.setPreferredSize(new Dimension(800, 300));
        statusPanel.setMaximumSize(new Dimension(800, 300));

        JLabel statusLabel = new JLabel("📋 Status Log");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        statusArea.setBackground(new Color(250, 250, 250));

        JScrollPane statusScroll = new JScrollPane(statusArea);
        statusScroll.setPreferredSize(new Dimension(760, 220));
        statusScroll.getVerticalScrollBar().setUnitIncrement(16);

        statusPanel.add(statusLabel, BorderLayout.NORTH);
        statusPanel.add(statusScroll, BorderLayout.CENTER);

        // Add all components
        contentPanel.add(title);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        contentPanel.add(dragDropPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(inputPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        contentPanel.add(buttonPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        contentPanel.add(progressBar);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(statusPanel);

        log("Welcome to File Encryption Tool!");
        log("📁 Select a file or folder to encrypt/decrypt");
        log("💡 Tip: Use strong passwords for better security");
    }

    private void setupDragAndDrop() {
        new DropTarget(this, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>) dtde.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);

                    if (!droppedFiles.isEmpty()) {
                        File file = droppedFiles.get(0);
                        filePathField.setText(file.getAbsolutePath());
                        dragDropLabel.setText("✅ File Selected: " + file.getName());
                        dragDropLabel.setForeground(new Color(46, 204, 113));
                        log("📂 File selected via drag & drop: " + file.getName());
                    }
                } catch (Exception ex) {
                    log("❌ Error: " + ex.getMessage());
                } finally {
                    resetDragDropLabel();
                }
            }

            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                dragDropLabel.setText("📥 Drop File Here!");
                dragDropLabel.setForeground(new Color(52, 152, 219));
            }

            @Override
            public void dragExit(DropTargetEvent dte) {
                resetDragDropLabel();
            }
        });
    }

    private void resetDragDropLabel() {
        Timer timer = new Timer(2000, e -> {
            dragDropLabel.setText("🖱️ Drag & Drop Files or Folders Here");
            dragDropLabel.setForeground(new Color(41, 128, 185));
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void browseFileOrFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setDialogTitle("Select File or Folder");

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            filePathField.setText(selected.getAbsolutePath());
            log("📂 Selected: " + selected.getName());
        }
    }

    private void encryptFileOrFolder() {
        processFileOrFolder(true);
    }

    private void decryptFileOrFolder() {
        processFileOrFolder(false);
    }

    private void processFileOrFolder(boolean encrypt) {
        String filePath = filePathField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (filePath.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a file/folder and enter a password",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this,
                    "File or folder does not exist",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Disable buttons during processing
        encryptButton.setEnabled(false);
        decryptButton.setEnabled(false);
        browseButton.setEnabled(false);

        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    if (file.isDirectory()) {
                        processFolder(file, encrypt);
                    } else {
                        processSingleFile(file, encrypt);
                    }

                    publish(encrypt ? "✅ Encryption completed successfully!" :
                            "✅ Decryption completed successfully!");

                } catch (Exception e) {
                    publish("❌ Error: " + e.getMessage());
                    throw e;
                }
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String msg : chunks) {
                    log(msg);
                }
            }

            @Override
            protected void done() {
                progressBar.setVisible(false);
                encryptButton.setEnabled(true);
                decryptButton.setEnabled(true);
                browseButton.setEnabled(true);
            }
        };

        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
        worker.execute();
    }

    private void processFolder(File folder, boolean encrypt) throws Exception {
        File[] files = folder.listFiles();
        if (files == null) return;

        int totalFiles = countFiles(folder);
        int[] processedFiles = {0};

        log((encrypt ? "🔒 Encrypting" : "🔓 Decrypting") + " folder: " + folder.getName());
        log("📊 Total files: " + totalFiles);

        SwingUtilities.invokeLater(() -> {
            progressBar.setIndeterminate(false);
            progressBar.setMaximum(totalFiles);
        });

        processFilesRecursive(folder, encrypt, processedFiles, totalFiles);
    }

    private void processFilesRecursive(File folder, boolean encrypt, int[] processedFiles, int totalFiles) throws Exception {
        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                processFilesRecursive(file, encrypt, processedFiles, totalFiles);
            } else {
                processSingleFile(file, encrypt);
                processedFiles[0]++;

                int progress = processedFiles[0];
                SwingUtilities.invokeLater(() -> {
                    progressBar.setValue(progress);
                    log("⏳ Progress: " + progress + "/" + totalFiles);
                });
            }
        }
    }

    private int countFiles(File folder) {
        File[] files = folder.listFiles();
        if (files == null) return 0;

        int count = 0;
        for (File file : files) {
            if (file.isDirectory()) {
                count += countFiles(file);
            } else {
                count++;
            }
        }
        return count;
    }

    private void processSingleFile(File inputFile, boolean encrypt) throws Exception {
        String password = new String(passwordField.getPassword());

        if (encrypt) {
            String outputPath = inputFile.getAbsolutePath() + ".encrypted";
            File outputFile = new File(outputPath);
            encryptFile(inputFile, outputFile, password);

            // Store original name for decryption
            originalNames.put(outputFile.getAbsolutePath(), inputFile.getName());

            log("🔒 Encrypted: " + inputFile.getName() + " → " + outputFile.getName());

            // Delete original file
            if (inputFile.delete()) {
                log("🗑️ Original file deleted: " + inputFile.getName());
            }
        } else {
            // Remove .encrypted extension
            String originalPath = inputFile.getAbsolutePath();
            if (!originalPath.endsWith(".encrypted")) {
                throw new Exception("File is not encrypted (missing .encrypted extension)");
            }

            String outputPath = originalPath.substring(0, originalPath.length() - 10);
            File outputFile = new File(outputPath);

            decryptFile(inputFile, outputFile, password);
            log("🔓 Decrypted: " + inputFile.getName() + " → " + outputFile.getName());

            // Delete encrypted file
            if (inputFile.delete()) {
                log("🗑️ Encrypted file deleted: " + inputFile.getName());
            }
        }
    }

    private void encryptFile(File inputFile, File outputFile, String password) throws Exception {
        SecretKeySpec key = generateKey(password);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        // Generate random IV
        byte[] iv = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile)) {

            // Write IV first
            fos.write(iv);

            // Encrypt and write file
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] output = cipher.update(buffer, 0, bytesRead);
                if (output != null) {
                    fos.write(output);
                }
            }

            byte[] outputBytes = cipher.doFinal();
            if (outputBytes != null) {
                fos.write(outputBytes);
            }
        }
    }

    private void decryptFile(File inputFile, File outputFile, String password) throws Exception {
        SecretKeySpec key = generateKey(password);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile)) {

            // Read IV first
            byte[] iv = new byte[16];
            if (fis.read(iv) != 16) {
                throw new Exception("Invalid encrypted file format");
            }

            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);

            // Decrypt and write file
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] output = cipher.update(buffer, 0, bytesRead);
                if (output != null) {
                    fos.write(output);
                }
            }

            byte[] outputBytes = cipher.doFinal();
            if (outputBytes != null) {
                fos.write(outputBytes);
            }
        }
    }

    private SecretKeySpec generateKey(String password) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = sha.digest(password.getBytes("UTF-8"));
        return new SecretKeySpec(key, "AES");
    }

    private JPanel createCardPanel(Color color) {
        JPanel panel = new JPanel();
        panel.setBackground(color);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        return panel;
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(150, 150, 150), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setPreferredSize(new Dimension(300, 35));
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

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(color.brighter());
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(color);
            }
        });
    }

    private void log(String message) {
        statusArea.append("[" + new java.util.Date().toString().split(" ")[3] + "] " + message + "\n");
        statusArea.setCaretPosition(statusArea.getDocument().getLength());
    }
}