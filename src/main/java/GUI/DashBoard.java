package GUI;

import client.ClientConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class DashBoard extends JFrame {
    private String username;
    private JTextArea messageArea;
    private JList<String> emailList;
    private DefaultListModel<String> listModel;

    public DashBoard(String username) {
        this.username = username;
        setTitle("Email Dashboard - " + username);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create the main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create the sidebar for email list
        JPanel sidebarPanel = new JPanel(new BorderLayout());
        JLabel lblInbox = new JLabel("Inbox", JLabel.CENTER);
        lblInbox.setFont(new Font("Arial", Font.BOLD, 16));
        lblInbox.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        sidebarPanel.add(lblInbox, BorderLayout.NORTH);

        // Create the email list
        listModel = new DefaultListModel<>();
        // Add some dummy emails for demonstration
        listModel.addElement("Welcome to Email System");
        listModel.addElement("Your account has been created");
        
        emailList = new JList<>(listModel);
        emailList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        emailList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedIndex = emailList.getSelectedIndex();
                if (selectedIndex != -1) {
                    displayEmail(selectedIndex);
                }
            }
        });
        
        JScrollPane listScrollPane = new JScrollPane(emailList);
        sidebarPanel.add(listScrollPane, BorderLayout.CENTER);
        
        // Create refresh and compose buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        JButton btnRefresh = new JButton("Refresh");
        JButton btnCompose = new JButton("Compose");
        
        btnRefresh.addActionListener(e -> refreshEmails());
        btnCompose.addActionListener(e -> composeEmail());
        
        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnCompose);
        sidebarPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add the sidebar to the main panel
        mainPanel.add(sidebarPanel, BorderLayout.WEST);
        
        // Create the email content area
        JPanel contentPanel = new JPanel(new BorderLayout());
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        JScrollPane contentScrollPane = new JScrollPane(messageArea);
        contentPanel.add(contentScrollPane, BorderLayout.CENTER);
        
        // Add the content panel to the main panel
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Add the main panel to the frame
        add(mainPanel);
        
        // Set the preferred size for the sidebar
        sidebarPanel.setPreferredSize(new Dimension(200, getHeight()));
        
        // Display the first email by default
        if (listModel.getSize() > 0) {
            emailList.setSelectedIndex(0);
        }
        
        setVisible(true);
        
        // Load emails from server
        refreshEmails();
    }
    
    private void displayEmail(int index) {
        String emailSubject = listModel.getElementAt(index);
        // In a real application, you would fetch the email content from the server
        messageArea.setText("Subject: " + emailSubject + "\n\n" +
                "From: system@emailsystem.com\n" +
                "To: " + username + "\n\n" +
                "This is a placeholder for the email content.\n" +
                "In a real application, this would display the actual email content.");
    }
    
    private void refreshEmails() {
        try {
            ClientConnection connection = new ClientConnection("localhost", 12345);
            connection.send("GETEMAILS##" + username);
            String response = connection.receive();
            connection.close();
            
            if (response.startsWith("GETEMAILS##SUCCESS")) {
                // Parse the email list from the response
                String[] parts = response.split("##");
                if (parts.length > 2) {
                    listModel.clear();
                    for (int i = 2; i < parts.length; i++) {
                        listModel.addElement(parts[i]);
                    }
                    
                    if (listModel.getSize() > 0) {
                        emailList.setSelectedIndex(0);
                    } else {
                        messageArea.setText("No emails found.");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to retrieve emails: " + response);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to server: " + ex.getMessage());
        }
    }
    
    private void composeEmail() {
        JDialog composeDialog = new JDialog(this, "Compose Email", true);
        composeDialog.setSize(500, 400);
        composeDialog.setLocationRelativeTo(this);
        
        JPanel composePanel = new JPanel(new BorderLayout(10, 10));
        composePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create form panel for recipient, subject
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        formPanel.add(new JLabel("To:"));
        JTextField txtTo = new JTextField();
        formPanel.add(txtTo);
        formPanel.add(new JLabel("Subject:"));
        JTextField txtSubject = new JTextField();
        formPanel.add(txtSubject);
        
        composePanel.add(formPanel, BorderLayout.NORTH);
        
        // Create message area
        JTextArea txtMessage = new JTextArea();
        txtMessage.setLineWrap(true);
        txtMessage.setWrapStyleWord(true);
        JScrollPane messageScrollPane = new JScrollPane(txtMessage);
        composePanel.add(messageScrollPane, BorderLayout.CENTER);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSend = new JButton("Send");
        JButton btnCancel = new JButton("Cancel");
        
        btnSend.addActionListener(e -> {
            String to = txtTo.getText().trim();
            String subject = txtSubject.getText().trim();
            String message = txtMessage.getText().trim();
            
            if (to.isEmpty() || subject.isEmpty() || message.isEmpty()) {
                JOptionPane.showMessageDialog(composeDialog, "Please fill in all fields.");
                return;
            }
            
            try {
                ClientConnection connection = new ClientConnection("localhost", 12345);
                connection.send("SENDEMAIL##" + username + "##" + to + "##" + subject + "##" + message);
                String response = connection.receive();
                connection.close();
                
                if (response.startsWith("SENDEMAIL##SUCCESS")) {
                    JOptionPane.showMessageDialog(composeDialog, "Email sent successfully!");
                    composeDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(composeDialog, "Failed to send email: " + response);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(composeDialog, "Error connecting to server: " + ex.getMessage());
            }
        });
        
        btnCancel.addActionListener(e -> composeDialog.dispose());
        
        buttonPanel.add(btnSend);
        buttonPanel.add(btnCancel);
        composePanel.add(buttonPanel, BorderLayout.SOUTH);
        
        composeDialog.add(composePanel);
        composeDialog.setVisible(true);
    }
}