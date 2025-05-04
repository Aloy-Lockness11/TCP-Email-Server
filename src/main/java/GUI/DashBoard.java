package GUI;

import client.ClientConnection;
import utils.protocols.CommonProtocol;
import utils.protocols.EmailProtocol;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * DashBoard provides a graphical user interface for the email client application.
 * It displays the user's emails, allows viewing email content, and provides functionality
 * for composing and sending new emails.
 * <p>
 * The dashboard uses a card layout to switch between email viewing and composing modes.
 * It communicates with the server to retrieve and send emails using the ClientConnection class.
 * </p>
 */
public class DashBoard extends JFrame {
    /** The username (email) of the currently logged-in user */
    private String username;
    
    /** Text area for displaying the content of selected emails */
    private JTextArea messageArea;
    
    /** List component for displaying email subjects in the sidebar */
    private JList<String> emailList;
    
    /** Model for the email list to dynamically add/remove emails */
    private DefaultListModel<String> listModel;
    
    /** Panel containing the email composition interface */
    private JPanel composePanel;
    
    /** Main content panel that switches between email view and compose view */
    private JPanel mainContentPanel;
    
    /** Card layout for switching between email view and compose view */
    private CardLayout contentCardLayout;
    
    /** Map to store email details for display, keyed by list index */
    private Map<Integer, EmailDetails> emailDetailsMap = new HashMap<>();
    
    /**
     * Inner class to store email details for display.
     * Contains all the information needed to display an email.
     */
    private static class EmailDetails {
        /** Unique identifier for the email */
        String id;
        
        /** Email address of the sender */
        String sender;
        
        /** Subject line of the email */
        String subject;
        
        /** Main content/body of the email */
        String content;
        
        /** Timestamp when the email was sent */
        String timestamp;
        
        /**
         * Constructor to create an EmailDetails object with all necessary information.
         *
         * @param id The unique identifier for the email
         * @param sender The email address of the sender
         * @param subject The subject line of the email
         * @param content The main content/body of the email
         * @param timestamp The timestamp when the email was sent
         */
        public EmailDetails(String id, String sender, String subject, String content, String timestamp) {
            this.id = id;
            this.sender = sender;
            this.subject = subject;
            this.content = content;
            this.timestamp = timestamp;
        }
    }

    /**
     * Constructor for the DashBoard.
     * Sets up the main frame, initializes all UI components, and loads emails from the server.
     *
     * @param username The username (email) of the currently logged-in user
     */
    public DashBoard(String username) {
        this.username = username;
        setTitle("Email Dashboard - " + username);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create the main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create top toolbar with compose button and search bar
        JPanel toolbarPanel = new JPanel(new BorderLayout(10, 0));
        toolbarPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // Create compose button in Gmail style
        JButton btnCompose = new JButton("Compose");
        btnCompose.setBackground(new Color(25, 118, 210));
        btnCompose.setForeground(Color.WHITE);
        btnCompose.setFocusPainted(false);
        btnCompose.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btnCompose.addActionListener(e -> showComposePanel());
        
        // Create search panel
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        
        JTextField searchField = new JTextField();
        searchField.setBorder(BorderFactory.createEmptyBorder());
        searchField.setPreferredSize(new Dimension(200, 30));
        
        JButton searchButton = new JButton("Search");
        searchButton.setFocusPainted(false);
        
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        
        toolbarPanel.add(btnCompose, BorderLayout.WEST);
        toolbarPanel.add(searchPanel, BorderLayout.CENTER);
        
        mainPanel.add(toolbarPanel, BorderLayout.NORTH);

        // Create the sidebar for email list
        JPanel sidebarPanel = new JPanel(new BorderLayout());
        JLabel lblInbox = new JLabel("Inbox", JLabel.CENTER);
        lblInbox.setFont(new Font("Arial", Font.BOLD, 16));
        lblInbox.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        sidebarPanel.add(lblInbox, BorderLayout.NORTH);

        // Create the email list
        listModel = new DefaultListModel<>();
        
        emailList = new JList<>(listModel);
        emailList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        emailList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedIndex = emailList.getSelectedIndex();
                if (selectedIndex != -1) {
                    displayEmail(selectedIndex);
                    // Switch to email view if in compose mode
                    contentCardLayout.show(mainContentPanel, "email");
                }
            }
        });
        
        JScrollPane listScrollPane = new JScrollPane(emailList);
        sidebarPanel.add(listScrollPane, BorderLayout.CENTER);
        
        // Create refresh button
        JPanel refreshPanel = new JPanel(new BorderLayout());
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> refreshEmails());
        refreshPanel.add(btnRefresh, BorderLayout.CENTER);
        sidebarPanel.add(refreshPanel, BorderLayout.SOUTH);
        
        // Add the sidebar to the main panel
        mainPanel.add(sidebarPanel, BorderLayout.WEST);
        
        // Create the main content panel with card layout to switch between email view and compose
        contentCardLayout = new CardLayout();
        mainContentPanel = new JPanel(contentCardLayout);
        
        // Create the email content area
        JPanel emailPanel = new JPanel(new BorderLayout());
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        JScrollPane contentScrollPane = new JScrollPane(messageArea);
        emailPanel.add(contentScrollPane, BorderLayout.CENTER);
        
        // Create the compose panel (will be shown/hidden with card layout)
        createComposePanel();
        
        // Add both panels to the card layout
        mainContentPanel.add(emailPanel, "email");
        mainContentPanel.add(composePanel, "compose");
        
        // Show email panel by default
        contentCardLayout.show(mainContentPanel, "email");
        
        // Add the content panel to the main panel
        mainPanel.add(mainContentPanel, BorderLayout.CENTER);
        
        // Add the main panel to the frame
        add(mainPanel);
        
        // Set the preferred size for the sidebar
        sidebarPanel.setPreferredSize(new Dimension(200, getHeight()));
        
        setVisible(true);
        
        // Load emails from server
        refreshEmails();
    }
    
    /**
     * Displays the content of the selected email in the message area.
     * Retrieves email details from the emailDetailsMap using the selected index.
     * If the email details are not found, displays a placeholder message.
     *
     * @param index The index of the selected email in the list
     */
    private void displayEmail(int index) {
        if (index >= 0 && index < emailDetailsMap.size()) {
            EmailDetails details = emailDetailsMap.get(index);
            if (details != null) {
                messageArea.setText("Subject: " + details.subject + "\n\n" +
                        "From: " + details.sender + "\n" +
                        "To: " + username + "\n" +
                        "Date: " + details.timestamp + "\n\n" +
                        details.content);
            }
        } else {
            String emailSubject = listModel.getElementAt(index);
            messageArea.setText("Subject: " + emailSubject + "\n\n" +
                    "From: system@emailsystem.com\n" +
                    "To: " + username + "\n\n" +
                    "This is a placeholder for the email content.\n" +
                    "In a real application, this would display the actual email content.");
        }
    }
    
    /**
     * Refreshes the email list by fetching emails from the server.
     * Clears the existing email list and details map, then populates them with new data.
     * Displays an appropriate message if no emails are found or if an error occurs.
     */
    private void refreshEmails() {
        try {
            // Ensure username is properly formatted as an email
            String userEmail = username;
            if (!userEmail.contains("@voidmail.com")) {
                userEmail = userEmail + "@voidmail.com";
            }
            
            ClientConnection connection = new ClientConnection("localhost", 12345);
            connection.send(EmailProtocol.GET_EMAILS + CommonProtocol.SEP + userEmail);
            String response = connection.receive();
            connection.close();
            
            if (response.startsWith(EmailProtocol.GET_EMAILS + CommonProtocol.SEP + EmailProtocol.SUCCESS)) {
                // Parse the email list from the response
                String[] parts = response.split(CommonProtocol.SEP);
                
                // Clear existing data
                listModel.clear();
                emailDetailsMap.clear();
                
                if (parts.length > 2) {
                    if (parts.length == 3 && parts[2].equals("NO_EMAILS")) {
                        messageArea.setText("No emails found.");
                        return;
                    }
                    
                    // Process emails - format is: id, sender, subject, content, timestamp
                    for (int i = 2; i < parts.length; i += 5) {
                        if (i + 4 < parts.length) {
                            String id = parts[i];
                            String sender = parts[i + 1];
                            String subject = parts[i + 2];
                            String content = parts[i + 3];
                            String timestamp = parts[i + 4];
                            
                            // Add to list model
                            int index = listModel.getSize();
                            listModel.addElement(subject + " - From: " + sender);
                            
                            // Store details for later display
                            emailDetailsMap.put(index, new EmailDetails(id, sender, subject, content, timestamp));
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
        }
    }
    
    /**
     * Creates and configures the compose panel for writing new emails.
     * Sets up the header, recipient and subject fields, message area, and action buttons.
     * Configures action listeners for the send and discard buttons.
     */
    private void createComposePanel() {
        composePanel = new JPanel(new BorderLayout());
        composePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create header panel with title and close button
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
        closeButton.addActionListener(e -> contentCardLayout.show(mainContentPanel, "email"));
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(closeButton, BorderLayout.EAST);
        
        composePanel.add(headerPanel, BorderLayout.NORTH);
        
        // Create form panel for recipient, subject with modern styling
        JPanel formPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        formPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        // Recipients row
        JPanel recipientPanel = new JPanel(new BorderLayout());
        recipientPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        recipientPanel.setBackground(Color.WHITE);
        
        JLabel recipientLabel = new JLabel("To");
        recipientLabel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 5));
        recipientLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JTextField txtTo = new JTextField();
        txtTo.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 15));
        
        recipientPanel.add(recipientLabel, BorderLayout.WEST);
        recipientPanel.add(txtTo, BorderLayout.CENTER);
        
        // Subject row
        JPanel subjectPanel = new JPanel(new BorderLayout());
        subjectPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
        subjectPanel.setBackground(Color.WHITE);
        
        JLabel subjectLabel = new JLabel("Subject");
        subjectLabel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 5));
        subjectLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JTextField txtSubject = new JTextField();
        txtSubject.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 15));
        
        subjectPanel.add(subjectLabel, BorderLayout.WEST);
        subjectPanel.add(txtSubject, BorderLayout.CENTER);
        
        formPanel.add(recipientPanel);
        formPanel.add(subjectPanel);
        
        composePanel.add(formPanel, BorderLayout.NORTH);
        
        // Create message area with styling
        JTextArea txtMessage = new JTextArea();
        txtMessage.setLineWrap(true);
        txtMessage.setWrapStyleWord(true);
        txtMessage.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        JScrollPane messageScrollPane = new JScrollPane(txtMessage);
        messageScrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        composePanel.add(messageScrollPane, BorderLayout.CENTER);
        
        // Create footer panel with buttons and formatting options
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        footerPanel.setBackground(new Color(240, 240, 240));
        
        // Send button with Gmail-like styling
        JButton btnSend = new JButton("Send");
        btnSend.setBackground(new Color(25, 118, 210));
        btnSend.setForeground(Color.WHITE);
        btnSend.setFocusPainted(false);
        
        // Discard button
        JButton btnDiscard = new JButton("Discard");
        btnDiscard.setFocusPainted(false);
        
        // Panel for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonPanel.setBackground(new Color(240, 240, 240));
        buttonPanel.add(btnSend);
        buttonPanel.add(btnDiscard);
        
        // Simple formatting toolbar (just for visual effect)
        JPanel formattingPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        formattingPanel.setBackground(new Color(240, 240, 240));
        
        // Add some formatting buttons (non-functional, just for appearance)
        JButton boldButton = new JButton("B");
        boldButton.setFont(new Font("Arial", Font.BOLD, 12));
        boldButton.setFocusPainted(false);
        boldButton.setPreferredSize(new Dimension(30, 25));
        
        JButton italicButton = new JButton("I");
        italicButton.setFont(new Font("Arial", Font.ITALIC, 12));
        italicButton.setFocusPainted(false);
        italicButton.setPreferredSize(new Dimension(30, 25));
        
        JButton underlineButton = new JButton("U");
        underlineButton.setFont(new Font("Arial", Font.PLAIN, 12));
        underlineButton.setFocusPainted(false);
        underlineButton.setPreferredSize(new Dimension(30, 25));
        
        formattingPanel.add(boldButton);
        formattingPanel.add(italicButton);
        formattingPanel.add(underlineButton);
        
        footerPanel.add(buttonPanel, BorderLayout.WEST);
        footerPanel.add(formattingPanel, BorderLayout.EAST);
        
        composePanel.add(footerPanel, BorderLayout.SOUTH);
        
        // Send button action
        btnSend.addActionListener(e -> {
            String to = txtTo.getText().trim();
            String subject = txtSubject.getText().trim();
            String message = txtMessage.getText().trim();
            
            if (to.isEmpty() || subject.isEmpty() || message.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.", 
                        "Missing Information", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Ensure email addresses are properly formatted
            if (!to.contains("@voidmail.com")) {
                to = to + "@voidmail.com";
            }
            
            // Make sure the username is properly formatted as an email
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
                    JOptionPane.showMessageDialog(this, "Email sent successfully!", 
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    contentCardLayout.show(mainContentPanel, "email");
                    refreshEmails(); // Refresh to show the sent email
                    
                    // Clear the form
                    txtTo.setText("");
                    txtSubject.setText("");
                    txtMessage.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to send email: " + response, 
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error connecting to server: " + ex.getMessage(), 
                        "Connection Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Discard button action
        btnDiscard.addActionListener(e -> {
            // Ask for confirmation if there's content
            if (!txtMessage.getText().trim().isEmpty() || 
                !txtSubject.getText().trim().isEmpty() || 
                !txtTo.getText().trim().isEmpty()) {
                
                int result = JOptionPane.showConfirmDialog(this, 
                        "Discard this message?", 
                        "Confirm", 
                        JOptionPane.YES_NO_OPTION);
                
                if (result == JOptionPane.YES_OPTION) {
                    contentCardLayout.show(mainContentPanel, "email");
                    // Clear the form
                    txtTo.setText("");
                    txtSubject.setText("");
                    txtMessage.setText("");
                }
            } else {
                contentCardLayout.show(mainContentPanel, "email");
            }
        });
    }
    
    /**
     * Shows the compose panel by switching the card layout.
     * This method is called when the user clicks the Compose button.
     */
    private void showComposePanel() {
        contentCardLayout.show(mainContentPanel, "compose");
    }
}