package GUI;

import client.ClientConnection;
import model.User;
import utils.validators.UserValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.Set;
import java.util.regex.Pattern; 

/**
 * LoginRegistrationGUI provides a graphical user interface for user login and registration.
 * It uses a card layout to switch between login and registration panels.
 * The class handles client-side validation and server communication for authentication.
 */
public class LoginRegistrationGUI extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    /** Regular expression pattern for validating email addresses (must end with @voidmail.com) */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@voidmail\\.com$");
    
    /** 
     * Regular expression pattern for validating passwords.
     * Password must contain at least:
     * - 8 characters
     * - One lowercase letter
     * - One uppercase letter
     * - One digit
     * - One special character
     */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

    // Components need to be class members to be accessed in listeners and methods
    private JTextField txtLoginEmail;
    private JPasswordField txtLoginPassword;
    private JLabel lblLoginEmailValidation;
    private JLabel lblLoginPasswordValidation;

    private JTextField txtRegFirstName;
    private JTextField txtRegLastName;
    private JTextField txtRegEmail;
    private JPasswordField txtRegPassword;
    private JPasswordField txtRegConfirmPassword;
    private JLabel lblRegFirstNameValidation;
    private JLabel lblRegLastNameValidation;
    private JLabel lblRegEmailValidation;
    private JLabel lblRegPasswordValidation;
    private JLabel lblRegPasswordMatch;

    /**
     * Main method to launch the application.
     * Uses SwingUtilities.invokeLater to ensure GUI creation happens on the Event Dispatch Thread.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginRegistrationGUI::new);
    }

    /**
     * Constructor for the LoginRegistrationGUI.
     * Sets up the main frame, initializes the card layout, and adds login and registration panels.
     */
    public LoginRegistrationGUI() {
        setTitle("Email System - Login and Registration");
        setSize(500, 600);
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

    /**
     * Creates and configures the login panel with all necessary components.
     * Includes email and password fields with validation, and login/register buttons.
     * 
     * @return JPanel The configured login panel
     */
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
        txtLoginEmail = new JTextField(15); 
        panel.add(txtLoginEmail, gbc);

        // Email validation message with fixed height
        lblLoginEmailValidation = new JLabel(" ", JLabel.LEFT);
        lblLoginEmailValidation.setForeground(Color.RED);
        lblLoginEmailValidation.setFont(new Font(lblLoginEmailValidation.getFont().getName(), Font.PLAIN, 10));
        lblLoginEmailValidation.setPreferredSize(new Dimension(300, 15)); // Fixed height
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(lblLoginEmailValidation, gbc);

        // Add document listener for email validation
        txtLoginEmail.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { validateLoginEmail(); }
            @Override public void removeUpdate(DocumentEvent e) { validateLoginEmail(); }
            @Override public void changedUpdate(DocumentEvent e) { validateLoginEmail(); }

            private void validateLoginEmail() {
                String email = txtLoginEmail.getText().trim();
                if (email.isEmpty()) {
                    lblLoginEmailValidation.setText("Email cannot be blank");
                } else if (!EMAIL_PATTERN.matcher(email).matches()) { // Basic format check
                    lblLoginEmailValidation.setText("Must be a valid @voidmail.com address");
                } else {
                    lblLoginEmailValidation.setText(" "); // Clear message
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
        txtLoginPassword = new JPasswordField(15);
        passwordPanel.add(txtLoginPassword, BorderLayout.CENTER);

        // Add show/hide button
        JToggleButton btnShowHide = new JToggleButton("Show");
        btnShowHide.setPreferredSize(new Dimension(60, txtLoginPassword.getPreferredSize().height));
        btnShowHide.addActionListener(e -> {
            if (btnShowHide.isSelected()) {
                txtLoginPassword.setEchoChar((char) 0); 
                btnShowHide.setText("Hide");
            } else {
                txtLoginPassword.setEchoChar('•'); 
                btnShowHide.setText("Show");
            }
        });
        passwordPanel.add(btnShowHide, BorderLayout.EAST);

        gbc.gridx = 1;
        panel.add(passwordPanel, gbc);

        // Password validation message with fixed height
        lblLoginPasswordValidation = new JLabel(" ", JLabel.LEFT);
        lblLoginPasswordValidation.setForeground(Color.RED);
        lblLoginPasswordValidation.setFont(new Font(lblLoginPasswordValidation.getFont().getName(), Font.PLAIN, 10));
        lblLoginPasswordValidation.setPreferredSize(new Dimension(300, 15)); // Fixed height
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(lblLoginPasswordValidation, gbc);

        // Add document listener for password validation (basic check)
        txtLoginPassword.getDocument().addDocumentListener(new DocumentListener() {
             @Override public void insertUpdate(DocumentEvent e) { validateLoginPassword(); }
             @Override public void removeUpdate(DocumentEvent e) { validateLoginPassword(); }
             @Override public void changedUpdate(DocumentEvent e) { validateLoginPassword(); }

             private void validateLoginPassword() {
                 String password = new String(txtLoginPassword.getPassword());
                 if (password.isEmpty()) {
                     lblLoginPasswordValidation.setText("Password cannot be blank");
                 }
          
                 else {
                     lblLoginPasswordValidation.setText(" ");
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
            String email = txtLoginEmail.getText().trim();
            String password = String.valueOf(txtLoginPassword.getPassword());

            // Basic client-side checks before sending
            if (email.isEmpty() || password.isEmpty()) {
                 JOptionPane.showMessageDialog(this, "Email and Password cannot be empty.", "Login Error", JOptionPane.WARNING_MESSAGE);
                 return;
            }
             if (!EMAIL_PATTERN.matcher(email).matches()) {
                 JOptionPane.showMessageDialog(this, "Invalid email format.", "Login Error", JOptionPane.WARNING_MESSAGE);
                 return;
             }
            // No need to use UserValidator here, server handles login validation

            try {
                ClientConnection connection = new ClientConnection("localhost", 12345); // Use CommonProtocol constants ideally
                connection.send("LOGIN##" + email + "##" + password); 
                String response = connection.receive();
                connection.close();

                if (response.equals("LOGIN##SUCCESS")) { // Use UserProtocol constants ideally
                    JOptionPane.showMessageDialog(this, "Login successful!");
                    dispose(); // Close login window
                    // Pass the validated email (username) to the dashboard
                    new DashBoard(email); // Open dashboard window
                } else {
                    // Provide more specific feedback if possible based on server response
                    JOptionPane.showMessageDialog(this, "Login failed: " + response, "Login Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Connection error during login: " + ex.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Register action - switch to registration panel
        btnRegister.addActionListener(e -> {
            clearRegistrationForm(); // Clear form when switching
            cardLayout.show(mainPanel, "Register");
        });

        return panel;
    }

    /**
     * Creates and configures the registration panel with all necessary components.
     * Includes fields for first name, last name, email, password, and confirm password,
     * along with real-time validation and registration/back buttons.
     * 
     * @return JPanel The configured registration panel
     */
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

        // --- First Name ---
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1;
        txtRegFirstName = new JTextField(15); // Assign to class member
        panel.add(txtRegFirstName, gbc);
        lblRegFirstNameValidation = createValidationLabel(); // Use helper
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(lblRegFirstNameValidation, gbc);
        addValidationListener(txtRegFirstName, lblRegFirstNameValidation, this::validateRegFirstName);


        // --- Last Name ---
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1;
        txtRegLastName = new JTextField(15); // Assign to class member
        panel.add(txtRegLastName, gbc);
        lblRegLastNameValidation = createValidationLabel(); // Use helper
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(lblRegLastNameValidation, gbc);
        addValidationListener(txtRegLastName, lblRegLastNameValidation, this::validateRegLastName);


        // --- Email ---
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        txtRegEmail = new JTextField(15); // Assign to class member
        panel.add(txtRegEmail, gbc);
        lblRegEmailValidation = createValidationLabel(); // Use helper
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        panel.add(lblRegEmailValidation, gbc);
        addValidationListener(txtRegEmail, lblRegEmailValidation, this::validateRegEmail);


        // --- Password ---
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 7;
        panel.add(new JLabel("Password:"), gbc);
        JPanel passwordPanel = new JPanel(new BorderLayout(5, 0));
        txtRegPassword = new JPasswordField(15); // Assign to class member
        passwordPanel.add(txtRegPassword, BorderLayout.CENTER);
        JToggleButton btnShowHidePass = createShowHideButton(txtRegPassword);
        passwordPanel.add(btnShowHidePass, BorderLayout.EAST);
        gbc.gridx = 1;
        panel.add(passwordPanel, gbc);
        lblRegPasswordValidation = createValidationLabel(); // Use helper
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        panel.add(lblRegPasswordValidation, gbc);
        // Combined listener for password and confirm password check
        DocumentListener passwordListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { validateRegPassword(); checkPasswordMatch(); }
            @Override public void removeUpdate(DocumentEvent e) { validateRegPassword(); checkPasswordMatch(); }
            @Override public void changedUpdate(DocumentEvent e) { validateRegPassword(); checkPasswordMatch(); }
        };
        txtRegPassword.getDocument().addDocumentListener(passwordListener);


        // --- Confirm Password ---
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 9;
        panel.add(new JLabel("Confirm Password:"), gbc);
        JPanel confirmPasswordPanel = new JPanel(new BorderLayout(5, 0));
        txtRegConfirmPassword = new JPasswordField(15); // Assign to class member
        confirmPasswordPanel.add(txtRegConfirmPassword, BorderLayout.CENTER);
        JToggleButton btnShowHideConfirm = createShowHideButton(txtRegConfirmPassword);
        confirmPasswordPanel.add(btnShowHideConfirm, BorderLayout.EAST);
        gbc.gridx = 1;
        panel.add(confirmPasswordPanel, gbc);
        lblRegPasswordMatch = createValidationLabel(); // Use helper for match status
        lblRegPasswordMatch.setHorizontalAlignment(JLabel.CENTER);
        gbc.gridx = 0; gbc.gridy = 10; gbc.gridwidth = 2;
        panel.add(lblRegPasswordMatch, gbc);
        // Listener only for checking match when confirm password changes
        txtRegConfirmPassword.getDocument().addDocumentListener(new DocumentListener() {
             @Override public void insertUpdate(DocumentEvent e) { checkPasswordMatch(); }
             @Override public void removeUpdate(DocumentEvent e) { checkPasswordMatch(); }
             @Override public void changedUpdate(DocumentEvent e) { checkPasswordMatch(); }
        });


        // --- Buttons ---
        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        JButton btnRegister = new JButton("Register");
        panel.add(btnRegister, gbc);

        gbc.gridy = 12;
        JButton btnBackToLogin = new JButton("Back to Login");
        panel.add(btnBackToLogin, gbc);

        // Register action
        btnRegister.addActionListener(e -> {
            // Clear previous validation messages
            clearValidationLabels();

            String firstName = txtRegFirstName.getText().trim();
            String lastName = txtRegLastName.getText().trim();
            String email = txtRegEmail.getText().trim();
            String password = String.valueOf(txtRegPassword.getPassword());
            String confirmPassword = String.valueOf(txtRegConfirmPassword.getPassword());

            // Perform basic client-side checks first (empty, password match)
            boolean basicValidationPassed = performBasicRegistrationValidation(firstName, lastName, email, password, confirmPassword);
            if (!basicValidationPassed) {
                return; // Stop if basic checks fail
            }

            // Create User object for validation using UserValidator
            User userToValidate = new User(firstName, lastName, email, password);

            try {
                // *** Use UserValidator ***
                UserValidator.validate(userToValidate);

                // If validation passes, proceed with sending to server
                ClientConnection connection = new ClientConnection("localhost", 12345); // Use CommonProtocol constants
                connection.send("REGISTER##" + firstName + "##" + lastName + "##" + email + "##" + password); // Use UserProtocol constants
                String response = connection.receive();
                connection.close();

                if (response.equals("REGISTER##SUCCESS")) { // Use UserProtocol constants
                    JOptionPane.showMessageDialog(this, "Registration successful! Please login.", "Registration Success", JOptionPane.INFORMATION_MESSAGE);
                    clearRegistrationForm();
                    cardLayout.show(mainPanel, "Login"); // Switch back to login
                } else {
                    // Handle specific server-side errors (e.g., user already exists)
                    JOptionPane.showMessageDialog(this, "Registration failed: " + response, "Registration Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (ConstraintViolationException validationEx) {
                // *** Handle validation errors from UserValidator ***
                StringBuilder errorMsg = new StringBuilder("Please correct the following errors:\n");
                Set<ConstraintViolation<?>> violations = validationEx.getConstraintViolations();
                for (ConstraintViolation<?> violation : violations) {
                    errorMsg.append("- ").append(violation.getPropertyPath()).append(": ").append(violation.getMessage()).append("\n");
                    // Optionally, update specific validation labels based on propertyPath
                    updateValidationLabel(violation.getPropertyPath().toString(), violation.getMessage());
                }
                JOptionPane.showMessageDialog(this, errorMsg.toString(), "Validation Error", JOptionPane.WARNING_MESSAGE);

            } catch (Exception connectionEx) {
                // Handle connection errors
                connectionEx.printStackTrace();
                JOptionPane.showMessageDialog(this, "Connection error during registration: " + connectionEx.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Back to Login action
        btnBackToLogin.addActionListener(e -> {
            clearRegistrationForm(); // Clear form when switching
            cardLayout.show(mainPanel, "Login");
        });

        return panel;
    }

    // --- Helper Methods for Registration Panel ---

    /**
     * Creates a standardized validation label for displaying error messages.
     * 
     * @return JLabel A configured validation label with consistent styling
     */
    private JLabel createValidationLabel() {
        JLabel label = new JLabel(" ", JLabel.LEFT);
        label.setForeground(Color.RED);
        label.setFont(new Font(label.getFont().getName(), Font.PLAIN, 10));
        label.setPreferredSize(new Dimension(300, 15)); // Fixed height
        return label;
    }

    /**
     * Creates a show/hide toggle button for password fields.
     * 
     * @param passwordField The password field to associate with this button
     * @return JToggleButton A configured show/hide button
     */
    private JToggleButton createShowHideButton(JPasswordField passwordField) {
        JToggleButton button = new JToggleButton("Show");
        button.setPreferredSize(new Dimension(60, passwordField.getPreferredSize().height));
        button.addActionListener(e -> {
            if (button.isSelected()) {
                passwordField.setEchoChar((char) 0);
                button.setText("Hide");
            } else {
                passwordField.setEchoChar('•');
                button.setText("Show");
            }
        });
        return button;
    }

    /**
     * Adds a document listener to a text field for real-time validation.
     * 
     * @param field The text field to monitor
     * @param label The validation label to update
     * @param validationLogic The validation logic to run when text changes
     */
    private void addValidationListener(JTextField field, JLabel label, Runnable validationLogic) {
         field.getDocument().addDocumentListener(new DocumentListener() {
             @Override public void insertUpdate(DocumentEvent e) { validationLogic.run(); }
             @Override public void removeUpdate(DocumentEvent e) { validationLogic.run(); }
             @Override public void changedUpdate(DocumentEvent e) { validationLogic.run(); }
         });
    }

    /**
     * Validates the first name field in the registration form.
     * Updates the validation label with appropriate error messages.
     */
    private void validateRegFirstName() {
        String name = txtRegFirstName.getText().trim();
        if (name.isEmpty()) lblRegFirstNameValidation.setText("First name cannot be blank");
        else if (name.length() < 2) lblRegFirstNameValidation.setText("First name too short (min 2)");
        else if (name.length() > 50) lblRegFirstNameValidation.setText("First name too long (max 50)");
        else lblRegFirstNameValidation.setText(" ");
    }

    /**
     * Validates the last name field in the registration form.
     * Updates the validation label with appropriate error messages.
     */
    private void validateRegLastName() {
        String name = txtRegLastName.getText().trim();
        if (name.isEmpty()) lblRegLastNameValidation.setText("Last name cannot be blank");
        else if (name.length() < 2) lblRegLastNameValidation.setText("Last name too short (min 2)");
        else if (name.length() > 50) lblRegLastNameValidation.setText("Last name too long (max 50)");
        else lblRegLastNameValidation.setText(" ");
    }

    /**
     * Validates the email field in the registration form.
     * Checks for empty field and proper email format.
     * Updates the validation label with appropriate error messages.
     */
    private void validateRegEmail() {
        String email = txtRegEmail.getText().trim();
        if (email.isEmpty()) {
            lblRegEmailValidation.setText("Email cannot be blank");
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            lblRegEmailValidation.setText("Must be a valid @voidmail.com address");
        } else {
            lblRegEmailValidation.setText(" ");
        }
    }

    /**
     * Validates the password field in the registration form.
     * Checks for empty field, minimum length, and complexity requirements.
     * Updates the validation label with appropriate error messages.
     */
    private void validateRegPassword() {
        String password = new String(txtRegPassword.getPassword());
        if (password.isEmpty()) {
            lblRegPasswordValidation.setText("Password cannot be blank");
        } else if (password.length() < 8) {
            lblRegPasswordValidation.setText("Password too short (min 8 characters)");
        } else if (!PASSWORD_PATTERN.matcher(password).matches()) {
            lblRegPasswordValidation.setText("Password must include uppercase, lowercase, digit, and special character");
        } else {
            lblRegPasswordValidation.setText(" ");
        }
    }

    /**
     * Checks if the password and confirm password fields match.
     * Updates the password match label with appropriate messages.
     */
    private void checkPasswordMatch() {
        String password = new String(txtRegPassword.getPassword());
        String confirmPassword = new String(txtRegConfirmPassword.getPassword());
        
        if (confirmPassword.isEmpty()) {
            lblRegPasswordMatch.setText(" ");
            lblRegPasswordMatch.setForeground(Color.RED);
        } else if (!password.equals(confirmPassword)) {
            lblRegPasswordMatch.setText("Passwords do not match");
            lblRegPasswordMatch.setForeground(Color.RED);
        } else {
            lblRegPasswordMatch.setText("Passwords match");
            lblRegPasswordMatch.setForeground(new Color(0, 150, 0)); // Dark green
        }
    }

    /**
     * Performs basic validation on registration form fields.
     * Checks for empty fields and password match.
     * 
     * @param firstName The first name entered
     * @param lastName The last name entered
     * @param email The email entered
     * @param password The password entered
     * @param confirmPassword The confirm password entered
     * @return boolean True if basic validation passes, false otherwise
     */
    private boolean performBasicRegistrationValidation(String firstName, String lastName, String email, 
                                                      String password, String confirmPassword) {
        // Check for empty fields
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        // Check password match
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        // Check email format
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            JOptionPane.showMessageDialog(this, "Invalid email format. Must be a valid @voidmail.com address.", 
                                         "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        return true;
    }

    /**
     * Updates a specific validation label based on the property path from validation errors.
     * 
     * @param propertyPath The property path from the validation error
     * @param message The error message to display
     */
    private void updateValidationLabel(String propertyPath, String message) {
        switch (propertyPath) {
            case "firstName":
                lblRegFirstNameValidation.setText(message);
                break;
            case "lastName":
                lblRegLastNameValidation.setText(message);
                break;
            case "email":
                lblRegEmailValidation.setText(message);
                break;
            case "password":
                lblRegPasswordValidation.setText(message);
                break;
            default:
                // No specific label to update
                break;
        }
    }

    /**
     * Clears all validation labels in the registration form.
     */
    private void clearValidationLabels() {
        lblRegFirstNameValidation.setText(" ");
        lblRegLastNameValidation.setText(" ");
        lblRegEmailValidation.setText(" ");
        lblRegPasswordValidation.setText(" ");
        lblRegPasswordMatch.setText(" ");
    }

    /**
     * Clears all fields in the registration form.
     */
    private void clearRegistrationForm() {
        txtRegFirstName.setText("");
        txtRegLastName.setText("");
        txtRegEmail.setText("");
        txtRegPassword.setText("");
        txtRegConfirmPassword.setText("");
        clearValidationLabels();
    }
}