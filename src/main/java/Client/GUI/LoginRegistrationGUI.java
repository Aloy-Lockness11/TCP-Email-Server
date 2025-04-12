package Client.GUI;

import javax.swing.*;
import java.awt.*;

public class LoginRegistrationGUI extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public LoginRegistrationGUI() {
        setTitle("Email System - Login and Registration");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // login panel
        mainPanel.add(createLoginPanel(), "Login");

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

        // Username label and field
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        JTextField txtUsername = new JTextField(15);
        panel.add(txtUsername, gbc);

        // Password label and field
        gbc.gridx = 0;
        gbc.gridy = 6;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField txtPassword = new JPasswordField(15);
        panel.add(txtPassword, gbc);


        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginRegistrationGUI());
    }
}
