package model;

import exception.EmailNotFoundException;
import exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * EmailManager class to manage email operations.
 * It uses a ConcurrentHashMap to store email data for thread safety.
 */
@Slf4j
public class EmailManager implements EmailManagerInterface {
    
    // ConcurrentHashMap to store email data for thread safety
    private static final Map<String, Email> emails = new ConcurrentHashMap<>();

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
    public String sendEmail(String sender, String recipient, String subject, String content)
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
    public List<Email> getReceivedEmails(String userEmail) {
        List<Email> receivedEmails = emails.values().stream()
                .filter(email -> email.getRecipient().equals(userEmail))
                .collect(Collectors.toList());
        
        log.info("Retrieved {} received emails for user: {}", receivedEmails.size(), userEmail);
        return receivedEmails;
    }
    
    /**
     * Retrieves all emails sent by a user.
     * 
     * @param userEmail The email address of the user
     * @return A list of emails sent by the user
     */
    public List<Email> getSentEmails(String userEmail) {
        List<Email> sentEmails = emails.values().stream()
                .filter(email -> email.getSender().equals(userEmail))
                .collect(Collectors.toList());
        
        log.info("Retrieved {} sent emails for user: {}", sentEmails.size(), userEmail);
        return sentEmails;
    }
    
    /**
     * Marks an email as viewed.
     * 
     * @param emailId The ID of the email
     * @throws EmailNotFoundException if the email is not found
     */
    public void markEmailAsViewed(String emailId) throws EmailNotFoundException {
        Email email = emails.get(emailId);
        if (email == null) {
            log.error("Email not found: {}", emailId);
            throw new EmailNotFoundException(emailId);
        }
        
        email.setViewed(true);
        emails.put(emailId, email);
        log.info("Email marked as viewed: {}", emailId);
    }
    
    /**
     * Generates a simple ID for the email.
     * 
     * @return A unique ID for the email
     */
    private String generateEmailId() {
        return "email-" + System.currentTimeMillis() + "-" + emails.size();
    }
}