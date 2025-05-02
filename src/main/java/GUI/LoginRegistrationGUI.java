package GUI;

import client.ClientConnection;
import exception.InvalidUserCredentialsException;
import exception.UserAlreadyExistsException;
import exception.UserNotFoundException;
import model.UserManager;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class LoginRegistrationGUI extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public LoginRegistrationGUI() {
        setTitle("Email System - Login and Registration");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Add the login and registration panels
        mainPanel.add(createLoginPanel(), "Login");
        mainPanel.add(createRegistrationPanel(), "Register");

        cardLayout.show(mainPanel, "Login");
        add(mainPanel);
        setVisible(true);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title label
        JLabel lblTitle = new JLabel("Login to Email System", JLabel.CENTER);
        lblTitle.setFont(new Font("Serif", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(lblTitle, gbc);

        // Email label and field
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        JTextField txtEmail = new JTextField(15);
        panel.add(txtEmail, gbc);

        // Password label and field
        gbc.gridx = 0;
        gbc.gridy = 6;
        panel.add(new JLabel("Password:"), gbc);
        
        // Create a panel for password field and show/hide button
        JPanel passwordPanel = new JPanel(new BorderLayout(5, 0));
        JPasswordField txtPassword = new JPasswordField(15);
        passwordPanel.add(txtPassword, BorderLayout.CENTER);
        
        // Add show/hide button
        JToggleButton btnShowHide = new JToggleButton("Show");
        btnShowHide.setPreferredSize(new Dimension(60, txtPassword.getPreferredSize().height));
        btnShowHide.addActionListener(e -> {
            if (btnShowHide.isSelected()) {
                txtPassword.setEchoChar((char) 0); // Show password
                btnShowHide.setText("Hide");
            } else {
                txtPassword.setEchoChar('•'); // Hide password
                btnShowHide.setText("Show");
            }
        });
        passwordPanel.add(btnShowHide, BorderLayout.EAST);
        
        gbc.gridx = 1;
        panel.add(passwordPanel, gbc);

        // Login Button
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        JButton btnLogin = new JButton("Login");
        panel.add(btnLogin, gbc);

        // Register Button
        gbc.gridy = 10;
        JButton btnRegister = new JButton("New user? Register Here");
        panel.add(btnRegister, gbc);

        // Login action
        btnLogin.addActionListener(e -> {
            String email = txtEmail.getText();
            String password = String.valueOf(txtPassword.getPassword());

            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please fill in all fields.");
                return;
            }

            try {
                ClientConnection connection = new ClientConnection("localhost", 12345);
                connection.send("LOGIN##" + email + "##" + password);
                String response = connection.receive();
                connection.close();

                if (response.equals("LOGIN##SUCCESS")) {
                    JOptionPane.showMessageDialog(null, "Login successful!");
                    dispose(); // Close login window
                    new DashBoard(email); // Open dashboard window
                } else {
                    JOptionPane.showMessageDialog(null, "Login failed: " + response);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Connection error: " + ex.getMessage());
            }
        });

        // Register action - switch to registration panel
        btnRegister.addActionListener(e -> {
            cardLayout.show(mainPanel, "Register");
        });

        return panel;
    }
    
    private JPanel createRegistrationPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title label
        JLabel lblTitle = new JLabel("Register New Account", JLabel.CENTER);
        lblTitle.setFont(new Font("Serif", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(lblTitle, gbc);

        // First Name label and field
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1;
        JTextField txtFirstName = new JTextField(15);
        panel.add(txtFirstName, gbc);

        // Last Name label and field
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1;
        JTextField txtLastName = new JTextField(15);
        panel.add(txtLastName, gbc);

        // Email label and field
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        JTextField txtEmail = new JTextField(15);
        panel.add(txtEmail, gbc);

        // Password label and field
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Password:"), gbc);
        
        // Create a panel for password field and show/hide button
        JPanel passwordPanel = new JPanel(new BorderLayout(5, 0));
        JPasswordField txtPassword = new JPasswordField(15);
        passwordPanel.add(txtPassword, BorderLayout.CENTER);
        
        // Add show/hide button
        JToggleButton btnShowHidePass = new JToggleButton("Show");
        btnShowHidePass.setPreferredSize(new Dimension(60, txtPassword.getPreferredSize().height));
        btnShowHidePass.addActionListener(e -> {
            if (btnShowHidePass.isSelected()) {
                txtPassword.setEchoChar((char) 0); // Show password
                btnShowHidePass.setText("Hide");
            } else {
                txtPassword.setEchoChar('•'); // Hide password
                btnShowHidePass.setText("Show");
            }
        });
        passwordPanel.add(btnShowHidePass, BorderLayout.EAST);
        
        gbc.gridx = 1;
        panel.add(passwordPanel, gbc);

        // Confirm Password label and field
        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(new JLabel("Confirm Password:"), gbc);
        
        // Create a panel for confirm password field and show/hide button
        JPanel confirmPasswordPanel = new JPanel(new BorderLayout(5, 0));
        JPasswordField txtConfirmPassword = new JPasswordField(15);
        confirmPasswordPanel.add(txtConfirmPassword, BorderLayout.CENTER);
        
        // Add show/hide button
        JToggleButton btnShowHideConfirm = new JToggleButton("Show");
        btnShowHideConfirm.setPreferredSize(new Dimension(60, txtConfirmPassword.getPreferredSize().height));
        btnShowHideConfirm.addActionListener(e -> {
            if (btnShowHideConfirm.isSelected()) {
                txtConfirmPassword.setEchoChar((char) 0); // Show password
                btnShowHideConfirm.setText("Hide");
            } else {
                txtConfirmPassword.setEchoChar('•'); // Hide password
                btnShowHideConfirm.setText("Show");
            }
        });
        confirmPasswordPanel.add(btnShowHideConfirm, BorderLayout.EAST);
        
        gbc.gridx = 1;
        panel.add(confirmPasswordPanel, gbc);
        
        // Add password match status label
        JLabel lblPasswordMatch = new JLabel("", JLabel.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        panel.add(lblPasswordMatch, gbc);
        
        // Add document listeners to check password match
        DocumentListener passwordListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkPasswordMatch();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkPasswordMatch();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkPasswordMatch();
            }
            
            private void checkPasswordMatch() {
                String password = new String(txtPassword.getPassword());
                String confirmPassword = new String(txtConfirmPassword.getPassword());
                
                if (password.isEmpty() || confirmPassword.isEmpty()) {
                    lblPasswordMatch.setText("");
                } else if (password.equals(confirmPassword)) {
                    lblPasswordMatch.setText("Passwords match");
                    lblPasswordMatch.setForeground(new Color(0, 150, 0)); // Green color
                } else {
                    lblPasswordMatch.setText("Passwords do not match");
                    lblPasswordMatch.setForeground(Color.RED);
                }
            }
        };
        
        txtPassword.getDocument().addDocumentListener(passwordListener);
        txtConfirmPassword.getDocument().addDocumentListener(passwordListener);

        // Register Button
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        JButton btnRegister = new JButton("Register");
        panel.add(btnRegister, gbc);

        // Back to Login Button
        gbc.gridy = 8;
        JButton btnBackToLogin = new JButton("Back to Login");
        panel.add(btnBackToLogin, gbc);

        // Register action
        btnRegister.addActionListener(e -> {
            String firstName = txtFirstName.getText();
            String lastName = txtLastName.getText();
            String email = txtEmail.getText();
            String password = String.valueOf(txtPassword.getPassword());
            String confirmPassword = String.valueOf(txtConfirmPassword.getPassword());

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || 
                password.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please fill in all fields.");
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(null, "Passwords do not match.");
                return;
            }

            try {
                ClientConnection connection = new ClientConnection("localhost", 12345);
                connection.send("REGISTER##" + firstName + "##" + lastName + "##" + email + "##" + password);
                String response = connection.receive();
                connection.close();

                if (response.equals("REGISTER##SUCCESS")) {
                    JOptionPane.showMessageDialog(null, "Registration successful!");
                    cardLayout.show(mainPanel, "Login"); // Go back to login after successful registration
                } else {
                    JOptionPane.showMessageDialog(null, "Registration failed: " + response);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Connection error: " + ex.getMessage());
            }
        });

        // Back to login action
        btnBackToLogin.addActionListener(e -> {
            cardLayout.show(mainPanel, "Login");
        });

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginRegistrationGUI());
    }
}