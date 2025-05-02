package GUI;

import javax.swing.*;
import java.awt.*;

public class DashBoard extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private String username;

    public DashBoard(String username) {
        this.username = username;

        setTitle("Email Dashboard - " + username);
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create card layout to switch panels
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Add placeholder panels for now
        contentPanel.add(new JLabel("📥 Inbox Panel"), "Inbox");
        contentPanel.add(new JLabel("📤 Sent Panel"), "Sent");
        contentPanel.add(new JLabel("✉️ Compose Panel"), "Compose");
        contentPanel.add(new JLabel("🔍 Search Panel"), "Search");

        // Sidebar/menu
        JPanel sidePanel = new JPanel(new GridLayout(6, 1, 5, 5));
        sidePanel.setPreferredSize(new Dimension(150, 0));

        JButton btnInbox = new JButton("Inbox");
        JButton btnSent = new JButton("Sent");
        JButton btnCompose = new JButton("Compose");
        JButton btnSearch = new JButton("Search");
        JButton btnLogout = new JButton("Logout");

        sidePanel.add(new JLabel("Welcome, " + username, JLabel.CENTER));
        sidePanel.add(btnInbox);
        sidePanel.add(btnSent);
        sidePanel.add(btnCompose);
        sidePanel.add(btnSearch);
        sidePanel.add(btnLogout);

        // Event listeners
        btnInbox.addActionListener(e -> cardLayout.show(contentPanel, "Inbox"));
        btnSent.addActionListener(e -> cardLayout.show(contentPanel, "Sent"));
        btnCompose.addActionListener(e -> cardLayout.show(contentPanel, "Compose"));
        btnSearch.addActionListener(e -> cardLayout.show(contentPanel, "Search"));
        btnLogout.addActionListener(e -> {
            dispose();
            new LoginRegistrationGUI(); // Go back to login
        });

        // Layout setup
        setLayout(new BorderLayout());
        add(sidePanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        setVisible(true);
    }
}
