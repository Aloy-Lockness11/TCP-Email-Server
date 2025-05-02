package model;

import exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * EmailManager class to manage email operations.
 * It uses a HashMap to store email data.
 */
@Slf4j
public class EmailManager {
    private static final Map<String, Email> emails = new HashMap<>();

    /**
     * Sends an email from one user to another.
     * 
     * @param sender The email address of the sender
     * @param recipient The email address of the recipient
     * @param subject The subject of the email
     * @param content The content of the email
     * @return The ID of the sent email
     * @throws UserNotFoundException if the sender or recipient does not exist
     */
    public static String sendEmail(String sender, String recipient, String subject, String content) 
            throws UserNotFoundException {
        // Create a new email
        String emailId = generateEmailId();
        Email email = Email.builder()
                .id(emailId)
                .sender(sender)
                .recipient(recipient)
                .subject(subject)
                .content(content)
                .timestamp(LocalDateTime.now())
                .viewed(false)
                .build();
        
        // Store the email
        emails.put(emailId, email);
        
        log.info("Email sent successfully from {} to {}: {}", sender, recipient, subject);
        return emailId;
    }
    
    /**
     * Retrieves all emails received by a user.
     * 
     * @param userEmail The email address of the user
     * @return A list of emails received by the user
     */
    public static List<Email> getReceivedEmails(String userEmail) {
        return emails.values().stream()
                .filter(email -> email.getRecipient().equals(userEmail))
                .collect(Collectors.toList());
    }
    
    /**
     * Retrieves all emails sent by a user.
     * 
     * @param userEmail The email address of the user
     * @return A list of emails sent by the user
     */
    public static List<Email> getSentEmails(String userEmail) {
        return emails.values().stream()
                .filter(email -> email.getSender().equals(userEmail))
                .collect(Collectors.toList());
    }
    
    /**
     * Marks an email as viewed.
     * 
     * @param emailId The ID of the email
     * @return true if the email was marked as viewed, false if the email was not found
     */
    public static boolean markEmailAsViewed(String emailId) {
        Email email = emails.get(emailId);
        if (email == null) {
            log.error("Email not found: {}", emailId);
            return false;
        }
        
        email.setViewed(true);
        log.info("Email marked as viewed: {}", emailId);
        return true;
    }
    
    /**
     * Generates a simple ID for the email.
     * 
     * @return A unique ID for the email
     */
    private static String generateEmailId() {
        return "email-" + System.currentTimeMillis() + "-" + emails.size();
    }
}