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
import java.util.regex.Pattern;

public class LoginRegistrationGUI extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    
    // Validation patterns from User.java
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@voidmail\\.com$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

    public LoginRegistrationGUI() {
        setTitle("Email System - Login and Registration");
        setSize(500, 500);  // Increased size to accommodate validation messages
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
        gbc.gridy = 1;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        JTextField txtEmail = new JTextField(15);
        panel.add(txtEmail, gbc);
        
        // Email validation message
        JLabel lblEmailValidation = new JLabel("", JLabel.LEFT);
        lblEmailValidation.setForeground(Color.RED);
        lblEmailValidation.setFont(new Font(lblEmailValidation.getFont().getName(), Font.PLAIN, 10));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(lblEmailValidation, gbc);
        
        // Add document listener for email validation
        txtEmail.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { validateEmail(); }
            @Override
            public void removeUpdate(DocumentEvent e) { validateEmail(); }
            @Override
            public void changedUpdate(DocumentEvent e) { validateEmail(); }
            
            private void validateEmail() {
                String email = txtEmail.getText().trim();
                if (email.isEmpty()) {
                    lblEmailValidation.setText("Email must not be blank");
                } else if (!EMAIL_PATTERN.matcher(email).matches()) {
                    lblEmailValidation.setText("Email must be a valid voidmail.com address");
                } else {
                    lblEmailValidation.setText("");
                }
            }
        });

        // Password label and field
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 3;
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
        
        // Password validation message
        JLabel lblPasswordValidation = new JLabel("", JLabel.LEFT);
        lblPasswordValidation.setForeground(Color.RED);
        lblPasswordValidation.setFont(new Font(lblPasswordValidation.getFont().getName(), Font.PLAIN, 10));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(lblPasswordValidation, gbc);
        
        // Add document listener for password validation
        txtPassword.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { validatePassword(); }
            @Override
            public void removeUpdate(DocumentEvent e) { validatePassword(); }
            @Override
            public void changedUpdate(DocumentEvent e) { validatePassword(); }
            
            private void validatePassword() {
                String password = new String(txtPassword.getPassword());
                if (password.isEmpty()) {
                    lblPasswordValidation.setText("Password must not be blank");
                } else if (!PASSWORD_PATTERN.matcher(password).matches()) {
                    lblPasswordValidation.setText("Password must be at least 8 characters with uppercase, lowercase, number, and special character");
                } else {
                    lblPasswordValidation.setText("");
                }
            }
        });

        // Login Button
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        JButton btnLogin = new JButton("Login");
        panel.add(btnLogin, gbc);

        // Register Button
        gbc.gridy = 6;
        JButton btnRegister = new JButton("New user? Register Here");
        panel.add(btnRegister, gbc);

        // Login action
        btnLogin.addActionListener(e -> {
            String email = txtEmail.getText().trim();
            String password = String.valueOf(txtPassword.getPassword());

            // Validate all fields before submitting
            boolean isValid = true;
            
            if (email.isEmpty() || !EMAIL_PATTERN.matcher(email).matches()) {
                lblEmailValidation.setText("Please enter a valid voidmail.com email address");
                isValid = false;
            }
            
            if (password.isEmpty() || !PASSWORD_PATTERN.matcher(password).matches()) {
                lblPasswordValidation.setText("Please enter a valid password");
                isValid = false;
            }
            
            if (!isValid) {
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
        
        // First Name validation message
        JLabel lblFirstNameValidation = new JLabel("", JLabel.LEFT);
        lblFirstNameValidation.setForeground(Color.RED);
        lblFirstNameValidation.setFont(new Font(lblFirstNameValidation.getFont().getName(), Font.PLAIN, 10));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(lblFirstNameValidation, gbc);
        
        // Add document listener for first name validation
        txtFirstName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { validateFirstName(); }
            @Override
            public void removeUpdate(DocumentEvent e) { validateFirstName(); }
            @Override
            public void changedUpdate(DocumentEvent e) { validateFirstName(); }
            
            private void validateFirstName() {
                String firstName = txtFirstName.getText().trim();
                if (firstName.isEmpty()) {
                    lblFirstNameValidation.setText("First name must not be blank");
                } else if (firstName.length() < 2) {
                    lblFirstNameValidation.setText("First name must be at least 2 characters");
                } else if (firstName.length() > 50) {
                    lblFirstNameValidation.setText("First name must be less than 50 characters");
                } else {
                    lblFirstNameValidation.setText("");
                }
            }
        });

        // Last Name label and field
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1;
        JTextField txtLastName = new JTextField(15);
        panel.add(txtLastName, gbc);
        
        // Last Name validation message
        JLabel lblLastNameValidation = new JLabel("", JLabel.LEFT);
        lblLastNameValidation.setForeground(Color.RED);
        lblLastNameValidation.setFont(new Font(lblLastNameValidation.getFont().getName(), Font.PLAIN, 10));
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(lblLastNameValidation, gbc);
        
        // Add document listener for last name validation
        txtLastName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { validateLastName(); }
            @Override
            public void removeUpdate(DocumentEvent e) { validateLastName(); }
            @Override
            public void changedUpdate(DocumentEvent e) { validateLastName(); }
            
            private void validateLastName() {
                String lastName = txtLastName.getText().trim();
                if (lastName.isEmpty()) {
                    lblLastNameValidation.setText("Last name must not be blank");
                } else if (lastName.length() < 2) {
                    lblLastNameValidation.setText("Last name must be at least 2 characters");
                } else if (lastName.length() > 50) {
                    lblLastNameValidation.setText("Last name must be less than 50 characters");
                } else {
                    lblLastNameValidation.setText("");
                }
            }
        });

        // Email label and field
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        JTextField txtEmail = new JTextField(15);
        panel.add(txtEmail, gbc);
        
        // Email validation message
        JLabel lblEmailValidation = new JLabel("", JLabel.LEFT);
        lblEmailValidation.setForeground(Color.RED);
        lblEmailValidation.setFont(new Font(lblEmailValidation.getFont().getName(), Font.PLAIN, 10));
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        panel.add(lblEmailValidation, gbc);
        
        // Add document listener for email validation
        txtEmail.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { validateEmail(); }
            @Override
            public void removeUpdate(DocumentEvent e) { validateEmail(); }
            @Override
            public void changedUpdate(DocumentEvent e) { validateEmail(); }
            
            private void validateEmail() {
                String email = txtEmail.getText().trim();
                if (email.isEmpty()) {
                    lblEmailValidation.setText("Email must not be blank");
                } else if (!EMAIL_PATTERN.matcher(email).matches()) {
                    lblEmailValidation.setText("Email must be a valid voidmail.com address");
                } else {
                    lblEmailValidation.setText("");
                }
            }
        });

        // Password label and field
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 7;
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
        
        // Password validation message
        JLabel lblPasswordValidation = new JLabel("", JLabel.LEFT);
        lblPasswordValidation.setForeground(Color.RED);
        lblPasswordValidation.setFont(new Font(lblPasswordValidation.getFont().getName(), Font.PLAIN, 10));
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        panel.add(lblPasswordValidation, gbc);

        // Confirm Password label and field
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 9;
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
        gbc.gridy = 10;
        gbc.gridwidth = 2;
        panel.add(lblPasswordMatch, gbc);
        
        // Add document listener for password validation
        txtPassword.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { 
                validatePassword();
                checkPasswordMatch();
            }
            @Override
            public void removeUpdate(DocumentEvent e) { 
                validatePassword();
                checkPasswordMatch();
            }
            @Override
            public void changedUpdate(DocumentEvent e) { 
                validatePassword();
                checkPasswordMatch();
            }
            
            private void validatePassword() {
                String password = new String(txtPassword.getPassword());
                if (password.isEmpty()) {
                    lblPasswordValidation.setText("Password must not be blank");
                } else if (!PASSWORD_PATTERN.matcher(password).matches()) {
                    lblPasswordValidation.setText("Password must be at least 8 characters with uppercase, lowercase, number, and special character");
                } else {
                    lblPasswordValidation.setText("");
                }
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
        });
        
        // Add document listener for confirm password
        txtConfirmPassword.getDocument().addDocumentListener(new DocumentListener() {
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
        });

        // Register Button
        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        JButton btnRegister = new JButton("Register");
        panel.add(btnRegister, gbc);

        // Back to Login Button
        gbc.gridy = 12;
        JButton btnBackToLogin = new JButton("Back to Login");
        panel.add(btnBackToLogin, gbc);

        // Register action
        btnRegister.addActionListener(e -> {
            String firstName = txtFirstName.getText().trim();
            String lastName = txtLastName.getText().trim();
            String email = txtEmail.getText().trim();
            String password = String.valueOf(txtPassword.getPassword());
            String confirmPassword = String.valueOf(txtConfirmPassword.getPassword());

            // Validate all fields before submitting
            boolean isValid = true;
            
            if (firstName.isEmpty() || firstName.length() < 2 || firstName.length() > 50) {
                lblFirstNameValidation.setText("First name must be between 2 and 50 characters");
                isValid = false;
            }
            
            if (lastName.isEmpty() || lastName.length() < 2 || lastName.length() > 50) {
                lblLastNameValidation.setText("Last name must be between 2 and 50 characters");
                isValid = false;
            }
            
            if (email.isEmpty() || !EMAIL_PATTERN.matcher(email).matches()) {
                lblEmailValidation.setText("Please enter a valid voidmail.com email address");
                isValid = false;
            }
            
            if (password.isEmpty() || !PASSWORD_PATTERN.matcher(password).matches()) {
                lblPasswordValidation.setText("Please enter a valid password");
                isValid = false;
            }
            
            if (!password.equals(confirmPassword)) {
                lblPasswordMatch.setText("Passwords do not match");
                lblPasswordMatch.setForeground(Color.RED);
                isValid = false;
            }
            
            if (!isValid) {
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