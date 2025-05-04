package GUI;

import client.ClientConnection;
import exception.SecureConnectionException;
import utils.protocols.CommonProtocol;
import utils.protocols.EmailProtocol;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * DashBoard provides the main email client UI for viewing, composing, searching, and managing emails.
 * Features Gmail-like sidebar, search, inbox/sent folders, and message preview.
 */
public class DashBoard extends JFrame {
    private String username;
    private JTextArea messageArea;
    private JList<String> emailList;
    private DefaultListModel<String> listModel;
    private JPanel composePanel;
    private JPanel mainContentPanel;
    private CardLayout contentCardLayout;
    private Map<Integer, EmailDetails> emailDetailsMap = new HashMap<>();
    private JButton btnInbox;
    private JButton btnSent;
    private int inboxCount = 0;
    private int sentCount = 0;
    private boolean showingSent = false;
    private JPanel previewPanel;
    private JLabel tickLabel;
    private JButton replyButton;
    private EmailDetails currentEmailDetails;

    /**
     * Holds metadata for an email displayed in the list.
     */
    private static class EmailDetails {
        String id;
        String sender;
        String subject;
        String content;
        String timestamp;
        boolean viewed;

        public EmailDetails(String id, String sender, String subject, String content, String timestamp, boolean viewed) {
            this.id = id;
            this.sender = sender;
            this.subject = subject;
            this.content = content;
            this.timestamp = timestamp;
            this.viewed = viewed;
        }
    }

    /**
     * Constructs the dashboard for the given user.
     * Sets up sidebar, search, email list, preview, and compose panel.
     * @param username the user's email address
     */
    public DashBoard(String username) {
        this.username = username;
        setTitle("Email Dashboard - " + username);
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main layout: sidebar + main area
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 245, 245));

        // Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(250, 250, 250));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)));
        sidebar.setPreferredSize(new Dimension(220, getHeight()));

        JButton btnCompose = new JButton("\u2795  Compose");
        btnCompose.setFont(new Font("Arial", Font.BOLD, 16));
        btnCompose.setBackground(new Color(25, 118, 210));
        btnCompose.setForeground(Color.WHITE);
        btnCompose.setFocusPainted(false);
        btnCompose.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));
        btnCompose.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCompose.addActionListener(e -> showComposePanel());
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(btnCompose);
        sidebar.add(Box.createVerticalStrut(30));

        // Folder buttons
        btnInbox = new JButton("\uD83D\uDCE5  Inbox (" + inboxCount + ")");
        btnInbox.setFont(new Font("Arial", Font.PLAIN, 15));
        btnInbox.setBackground(Color.WHITE);
        btnInbox.setFocusPainted(false);
        btnInbox.setHorizontalAlignment(SwingConstants.LEFT);
        btnInbox.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        btnInbox.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnInbox.addActionListener(e -> {
            showingSent = false;
            refreshEmails(false);
            updateFolderButtons();
        });
        sidebar.add(btnInbox);
        sidebar.add(Box.createVerticalStrut(10));

        btnSent = new JButton("\uD83D\uDCE4  Sent (" + sentCount + ")");
        btnSent.setFont(new Font("Arial", Font.PLAIN, 15));
        btnSent.setBackground(Color.WHITE);
        btnSent.setFocusPainted(false);
        btnSent.setHorizontalAlignment(SwingConstants.LEFT);
        btnSent.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        btnSent.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSent.addActionListener(e -> {
            showingSent = true;
            refreshEmails(true);
            updateFolderButtons();
        });
        sidebar.add(btnSent);
        sidebar.add(Box.createVerticalStrut(30));

        // Add user info at the bottom 
        sidebar.add(Box.createVerticalGlue());
        JLabel userLabel = new JLabel("\uD83D\uDC64  " + username);
        userLabel.setFont(new Font("Arial", Font.ITALIC, 13));
        userLabel.setForeground(new Color(120, 120, 120));
        userLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        sidebar.add(userLabel);
        sidebar.add(Box.createVerticalStrut(10));
        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Arial", Font.PLAIN, 13));
        btnLogout.setBackground(new Color(230, 230, 230));
        btnLogout.setFocusPainted(false);
        btnLogout.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogout.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 24));
        btnLogout.addActionListener(e -> {
            dispose();
            new LoginRegistrationGUI();
        });
        sidebar.add(btnLogout);
        sidebar.add(Box.createVerticalStrut(10));

        // Main area (center)
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(245, 245, 245));

        // Search bar
        // Example: To search for emails from or to fire@voidmail.com, type 'fire@voidmail.com'. To search by date, type '2025-05-04'. To search by subject, type part of the subject.
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        searchPanel.setBackground(new Color(245, 245, 245));
        JTextField searchField = new JTextField();
        searchField.setFont(new Font("Arial", Font.PLAIN, 15));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        JButton searchButton = new JButton("Search");
        searchButton.setFocusPainted(false);
        searchButton.setBackground(new Color(230, 230, 230));
        searchButton.setFont(new Font("Arial", Font.PLAIN, 14));
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        centerPanel.add(searchPanel, BorderLayout.NORTH);

        // Email list and preview (split vertically)
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(null);

        // Email list
        listModel = new DefaultListModel<>();
        emailList = new JList<>(listModel);
        emailList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        emailList.setCellRenderer(new EmailListRenderer());
        emailList.setFont(new Font("Arial", Font.PLAIN, 15));
        emailList.setBackground(Color.WHITE);
        emailList.setFixedCellHeight(40);
        emailList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedIndex = emailList.getSelectedIndex();
                if (selectedIndex != -1) {
                    displayEmail(selectedIndex);
                }
            }
        });
        JScrollPane listScrollPane = new JScrollPane(emailList);
        listScrollPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setTopComponent(listScrollPane);

        // Email preview
        previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBackground(Color.WHITE);
        previewPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setFont(new Font("Arial", Font.PLAIN, 15));
        messageArea.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        previewPanel.add(new JScrollPane(messageArea), BorderLayout.CENTER);
        // Bottom right panel for ticks and reply
        JPanel bottomRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomRightPanel.setOpaque(false);
        tickLabel = new JLabel();
        tickLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        replyButton = new JButton("Reply");
        replyButton.setVisible(false);
        replyButton.addActionListener(e -> {
            if (currentEmailDetails != null) {
                showComposePanel(currentEmailDetails.sender, "Re: " + currentEmailDetails.subject);
            }
        });
        bottomRightPanel.add(replyButton);
        bottomRightPanel.add(tickLabel);
        previewPanel.add(bottomRightPanel, BorderLayout.SOUTH);
        splitPane.setBottomComponent(previewPanel);

        centerPanel.add(splitPane, BorderLayout.CENTER);

        // Compose panel (hidden by default)
        createComposePanel();
        centerPanel.add(composePanel, BorderLayout.SOUTH);
        composePanel.setVisible(false);

        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);
        setVisible(true);
        showingSent = false;
        refreshEmails(false);
        updateFolderButtons();

        // In the constructor, after searchButton is created:
        searchButton.addActionListener(e -> {
            String query = searchField.getText().trim();
            if (query.isEmpty()) {
                refreshEmails(showingSent);
                return;
            }
            searchEmails(query);
        });
    }

    /**
     * Custom renderer for Gmail-like email list.
     */
    private class EmailListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setFont(new Font("Arial", Font.PLAIN, 15));
            label.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 8));
            if (isSelected) {
                label.setBackground(new Color(232, 240, 254));
            } else {
                label.setBackground(Color.WHITE);
            }
            return label;
        }
    }

    /**
     * Display the selected email in the preview panel.
     * @param index index of the selected email
     */
    private void displayEmail(int index) {
        if (index >= 0 && index < emailDetailsMap.size()) {
            EmailDetails details = emailDetailsMap.get(index);
            currentEmailDetails = details;
            if (details != null) {
                // Format timestamp
                String formattedTime = details.timestamp;
                try {
                    LocalDateTime dateTime = LocalDateTime.parse(details.timestamp);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    formattedTime = dateTime.format(formatter);
                } catch (Exception ignored) {}
                messageArea.setText("Subject: " + details.subject + "\n\n" +
                        "From: " + details.sender + "\n" +
                        "To: " + username + "\n" +
                        "Date: " + formattedTime + "\n\n" +
                        details.content);
                // Mark as viewed if in inbox and not already viewed
                if (!showingSent) {
                    replyButton.setVisible(true);
                    tickLabel.setVisible(false);
                    markEmailAsViewed(details.id);
                } else {
                    replyButton.setVisible(false);
                    tickLabel.setVisible(true);
                    updateTickStatus(details);
                }
            }
        } else {
            messageArea.setText("");
            replyButton.setVisible(false);
            tickLabel.setVisible(false);
        }
    }

    /**
     * Update the WhatsApp-style tick status for sent emails.
     * @param details email details
     */
    private void updateTickStatus(EmailDetails details) {
        if (details.viewed) {
            tickLabel.setText("<html><span style='color:#2196F3'>&#10003;&#10003;</span> Seen</html>"); // blue ticks
        } else {
            tickLabel.setText("<html><span style='color:#888'>&#10003;&#10003;</span> Delivered</html>"); // gray ticks
        }
    }

    /**
     * Mark an email as viewed (notify server).
     * @param emailId the email ID
     */
    private void markEmailAsViewed(String emailId) {
        // Send MARKASVIEWED to server
        try {
            ClientConnection connection = new ClientConnection("localhost", 12345);
            connection.send(EmailProtocol.MARK_AS_VIEWED + CommonProtocol.SEP + emailId);
            connection.receive();
            connection.close();
        } catch (Exception ignored) {}
    }

    /**
     * Refresh the email list for Inbox or Sent.
     * @param isSent true for Sent folder, false for Inbox
     */
    private void refreshEmails(boolean isSent) {
        try {
            String userEmail = username;
            if (!userEmail.contains("@voidmail.com")) {
                userEmail = userEmail + "@voidmail.com";
            }

            ClientConnection connection = new ClientConnection("localhost", 12345);
            connection.send(EmailProtocol.GET_EMAILS + CommonProtocol.SEP + userEmail + CommonProtocol.SEP + (isSent ? "SENT" : "INBOX"));
            String response = connection.receive();
            connection.close();

            if (response.startsWith(EmailProtocol.GET_EMAILS + CommonProtocol.SEP + EmailProtocol.SUCCESS)) {
                String[] parts = response.split(CommonProtocol.SEP);

                listModel.clear();
                emailDetailsMap.clear();

                if (parts.length > 2) {
                    if (parts.length == 3 && parts[2].equals("NO_EMAILS")) {
                        messageArea.setText("No emails found.");
                        return;
                    }

                    for (int i = 2; i < parts.length; i += 6) {
                        if (i + 5 < parts.length) {
                            String id = parts[i];
                            String sender = parts[i + 1];
                            String subject = parts[i + 2];
                            String content = parts[i + 3];
                            String timestamp = parts[i + 4];
                            boolean viewed = Boolean.parseBoolean(parts[i + 5]);

                            int index = listModel.getSize();
                            listModel.addElement(subject + " - From: " + sender);
                            emailDetailsMap.put(index, new EmailDetails(id, sender, subject, content, timestamp, viewed));
                        }
                    }

                    if (listModel.getSize() > 0) {
                        emailList.setSelectedIndex(0);
                    } else {
                        messageArea.setText("No emails found.");
                    }
                } else {
                    messageArea.setText("No emails found.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to retrieve emails: " + response);
                messageArea.setText("Failed to retrieve emails: " + response);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to server: " + ex.getMessage());
            messageArea.setText("Error connecting to server: " + ex.getMessage());
        } catch (SecureConnectionException e) {
            JOptionPane.showMessageDialog(this, "Unable to establish a secure connection to the server.","Security Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Show the compose panel for a new message.
     */
    private void showComposePanel() {
        composePanel.setVisible(true);
        // Optionally clear fields here
    }

    /**
     * Hide the compose panel.
     */
    private void hideComposePanel() {
        composePanel.setVisible(false);
    }

    /**
     * Create the compose panel UI.
     */
    private void createComposePanel() {
        composePanel = new JPanel(new BorderLayout());
        composePanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));
        composePanel.setBackground(new Color(248, 248, 248));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(240, 240, 240));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        JLabel titleLabel = new JLabel("New Message");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font("Arial", Font.BOLD, 16));
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> hideComposePanel());
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(closeButton, BorderLayout.EAST);
        composePanel.add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(new Color(248, 248, 248));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel toLabel = new JLabel("To");
        toLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        JTextField txtTo = new JTextField();
        txtTo.setFont(new Font("Arial", Font.PLAIN, 14));
        txtTo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        formPanel.add(toLabel);
        formPanel.add(txtTo);
        formPanel.add(Box.createVerticalStrut(8));

        JLabel subjectLabel = new JLabel("Subject");
        subjectLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        JTextField txtSubject = new JTextField();
        txtSubject.setFont(new Font("Arial", Font.PLAIN, 14));
        txtSubject.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        formPanel.add(subjectLabel);
        formPanel.add(txtSubject);
        formPanel.add(Box.createVerticalStrut(8));

        JLabel messageLabel = new JLabel("Message");
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        JTextArea txtMessage = new JTextArea();
        txtMessage.setFont(new Font("Arial", Font.PLAIN, 14));
        txtMessage.setLineWrap(true);
        txtMessage.setWrapStyleWord(true);
        txtMessage.setRows(8);
        txtMessage.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        JScrollPane messageScrollPane = new JScrollPane(txtMessage);
        messageScrollPane.setBorder(BorderFactory.createEmptyBorder());
        formPanel.add(messageLabel);
        formPanel.add(messageScrollPane);

        composePanel.add(formPanel, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        footerPanel.setBackground(new Color(240, 240, 240));
        JButton btnSend = new JButton("Send");
        btnSend.setBackground(new Color(25, 118, 210));
        btnSend.setForeground(Color.WHITE);
        btnSend.setFocusPainted(false);
        btnSend.setFont(new Font("Arial", Font.BOLD, 14));
        btnSend.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 24));
        btnSend.addActionListener(e -> {
            String to = txtTo.getText().trim();
            String subject = txtSubject.getText().trim();
            String message = txtMessage.getText().trim();
            if (to.isEmpty() || subject.isEmpty() || message.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Missing Information", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!to.contains("@voidmail.com")) {
                to = to + "@voidmail.com";
            }
            String sender = username;
            if (!sender.contains("@voidmail.com")) {
                sender = sender + "@voidmail.com";
            }
            try {
                ClientConnection connection = new ClientConnection("localhost", 12345);
                connection.send(EmailProtocol.SEND_EMAIL + CommonProtocol.SEP +
                        sender + CommonProtocol.SEP +
                        to + CommonProtocol.SEP +
                        subject + CommonProtocol.SEP +
                        message);
                String response = connection.receive();
                connection.close();
                if (response.startsWith(EmailProtocol.SEND_EMAIL + CommonProtocol.SEP + EmailProtocol.SUCCESS)) {
                    JOptionPane.showMessageDialog(this, "Email sent successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    hideComposePanel();
                    refreshEmails(false);
                    txtTo.setText("");
                    txtSubject.setText("");
                    txtMessage.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to send email: " + response, "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error connecting to server: " + ex.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            } catch (SecureConnectionException ex) {
                JOptionPane.showMessageDialog(this, "Unable to establish a secure connection to the server.","Security Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        // Add Discard button next to Send
        JButton btnDiscard = new JButton("Discard");
        btnDiscard.setBackground(new Color(230, 230, 230));
        btnDiscard.setForeground(Color.BLACK);
        btnDiscard.setFocusPainted(false);
        btnDiscard.setFont(new Font("Arial", Font.PLAIN, 14));
        btnDiscard.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 24));
        btnDiscard.addActionListener(e -> {
            txtTo.setText("");
            txtSubject.setText("");
            txtMessage.setText("");
            hideComposePanel();
        });
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(btnSend);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(btnDiscard);
        footerPanel.add(buttonPanel, BorderLayout.WEST);
        composePanel.add(footerPanel, BorderLayout.SOUTH);
    }

    /**
     * Update the Inbox and Sent counts in the sidebar.
     */
    private void updateFolderButtons() {
        // Always count all inbox and sent emails, regardless of current folder
        int inbox = 0, sent = 0;
        try {
            String userEmail = username;
            if (!userEmail.contains("@voidmail.com")) {
                userEmail = userEmail + "@voidmail.com";
            }
            // Count inbox
            ClientConnection connectionInbox = new ClientConnection("localhost", 12345);
            connectionInbox.send(EmailProtocol.GET_EMAILS + CommonProtocol.SEP + userEmail + CommonProtocol.SEP + "INBOX");
            String responseInbox = connectionInbox.receive();
            connectionInbox.close();
            if (responseInbox.startsWith(EmailProtocol.GET_EMAILS + CommonProtocol.SEP + EmailProtocol.SUCCESS)) {
                String[] parts = responseInbox.split(CommonProtocol.SEP);
                if (parts.length > 2 && !(parts.length == 3 && parts[2].equals("NO_EMAILS"))) {
                    for (int i = 2; i < parts.length; i += 6) {
                        if (i + 5 < parts.length) inbox++;
                    }
                }
            }
            // Count sent
            ClientConnection connectionSent = new ClientConnection("localhost", 12345);
            connectionSent.send(EmailProtocol.GET_EMAILS + CommonProtocol.SEP + userEmail + CommonProtocol.SEP + "SENT");
            String responseSent = connectionSent.receive();
            connectionSent.close();
            if (responseSent.startsWith(EmailProtocol.GET_EMAILS + CommonProtocol.SEP + EmailProtocol.SUCCESS)) {
                String[] parts = responseSent.split(CommonProtocol.SEP);
                if (parts.length > 2 && !(parts.length == 3 && parts[2].equals("NO_EMAILS"))) {
                    for (int i = 2; i < parts.length; i += 6) {
                        if (i + 5 < parts.length) sent++;
                    }
                }
            }
        } catch (Exception e) {
            // If error, fallback to current folder count
            inbox = 0; sent = 0;
            for (EmailDetails details : emailDetailsMap.values()) {
                if (showingSent) sent++; else inbox++;
            }
        }
        btnInbox.setText("\uD83D\uDCE5  Inbox (" + inbox + ")");
        btnSent.setText("\uD83D\uDCE4  Sent (" + sent + ")");
    }

    /**
     * Show the compose panel with pre-filled recipient and subject (for reply).
     * @param recipient recipient email
     * @param subject subject line
     */
    private void showComposePanel(String recipient, String subject) {
        composePanel.setVisible(true);
        // Find the fields in the compose panel and set their values
        for (Component comp : composePanel.getComponents()) {
            if (comp instanceof JPanel formPanel) {
                for (Component field : formPanel.getComponents()) {
                    if (field instanceof JTextField txtField) {
                        if (((JLabel) formPanel.getComponent(0)).getText().equals("To")) {
                            txtField.setText(recipient);
                        } else if (((JLabel) formPanel.getComponent(2)).getText().equals("Subject")) {
                            txtField.setText(subject);
                        }
                    }
                }
            }
        }
    }

    /**
     * Search both Inbox and Sent for emails matching the query, and display results.
     * @param query the search string
     */
    private void searchEmails(String query) {
        try {
            String userEmail = username;
            if (!userEmail.contains("@voidmail.com")) {
                userEmail = userEmail + "@voidmail.com";
            }
            // Search Inbox (received)
            String commandInbox = EmailProtocol.SEARCH_RECEIVED;
            ClientConnection connectionInbox = new ClientConnection("localhost", 12345);
            connectionInbox.send(commandInbox + CommonProtocol.SEP + userEmail + CommonProtocol.SEP + query);
            String responseInbox = connectionInbox.receive();
            connectionInbox.close();
            // Search Sent
            String commandSent = EmailProtocol.SEARCH_SENT;
            ClientConnection connectionSent = new ClientConnection("localhost", 12345);
            connectionSent.send(commandSent + CommonProtocol.SEP + userEmail + CommonProtocol.SEP + query);
            String responseSent = connectionSent.receive();
            connectionSent.close();

            // DEBUG: Print the raw responses
            System.out.println("[DEBUG] Search Inbox response: " + responseInbox);
            System.out.println("[DEBUG] Search Sent response: " + responseSent);

            listModel.clear();
            emailDetailsMap.clear();
            int index = 0;
            // Parse Inbox results
            String expectedPrefixInbox = commandInbox + CommonProtocol.SEP + EmailProtocol.SUCCESS;
            if (responseInbox.startsWith(expectedPrefixInbox)) {
                String[] parts = responseInbox.split(CommonProtocol.SEP);
                if (parts.length > 2 && !(parts.length == 3 && parts[2].equals(EmailProtocol.NO_EMAILS))) {
                    for (int i = 2; i < parts.length; i += 5) {
                        if (i + 4 < parts.length) {
                            String id = parts[i];
                            String sender = parts[i + 1];
                            String subject = parts[i + 2];
                            String timestamp = parts[i + 3];
                            boolean viewed = Boolean.parseBoolean(parts[i + 4]);
                            String label = subject + " - From: " + sender;
                            listModel.addElement(label);
                            emailDetailsMap.put(index++, new EmailDetails(id, sender, subject, "", timestamp, viewed));
                        }
                    }
                }
            }
            // Parse Sent results
            String expectedPrefixSent = commandSent + CommonProtocol.SEP + EmailProtocol.SUCCESS;
            if (responseSent.startsWith(expectedPrefixSent)) {
                String[] parts = responseSent.split(CommonProtocol.SEP);
                if (parts.length > 2 && !(parts.length == 3 && parts[2].equals(EmailProtocol.NO_EMAILS))) {
                    for (int i = 2; i < parts.length; i += 5) {
                        if (i + 4 < parts.length) {
                            String id = parts[i];
                            String recipient = parts[i + 1];
                            String subject = parts[i + 2];
                            String timestamp = parts[i + 3];
                            boolean viewed = Boolean.parseBoolean(parts[i + 4]);
                            String label = subject + " - To: " + recipient;
                            listModel.addElement(label);
                            emailDetailsMap.put(index++, new EmailDetails(id, recipient, subject, "", timestamp, viewed));
                        }
                    }
                }
            }
            if (listModel.getSize() > 0) {
                emailList.setSelectedIndex(0);
            } else {
                messageArea.setText("No emails found.");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to server: " + ex.getMessage());
            messageArea.setText("Error connecting to server: " + ex.getMessage());
        } catch (SecureConnectionException e) {
            JOptionPane.showMessageDialog(this, "Unable to establish a secure connection to the server.","Security Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}