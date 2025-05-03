package model;

import exception.EmailNotFoundException;
import exception.UserNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import utils.EmailValidator;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private static final Map<String, User> users = new ConcurrentHashMap<>();

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
        // Check if sender and recipient exist
        if (users.size() > 0) {
            if (!users.containsKey(sender)) {
                log.error("Sender not found: {}", sender);
                throw new UserNotFoundException(sender);
            }
            
            if (!users.containsKey(recipient)) {
                log.error("Recipient not found: {}", recipient);
                throw new UserNotFoundException(recipient);
            }
        }
        
        String emailId = generateEmailId(sender, recipient, subject, content);
        Email email = Email.builder()
                .id(emailId)
                .sender(sender)
                .recipient(recipient)
                .subject(subject)
                .content(content)
                .timestamp(LocalDateTime.now())
                .viewed(false)
                .build();

        EmailValidator.validate(email);

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
     * Retrieves all emails (both sent and received) for a user.
     * 
     * @param userEmail The email address of the user
     * @return A list of all emails for the user
     */
    public List<Email> getAllEmails(String userEmail) {
        List<Email> allEmails = new ArrayList<>();
        allEmails.addAll(getSentEmails(userEmail));
        allEmails.addAll(getReceivedEmails(userEmail));
        
        log.info("Retrieved {} total emails for user: {}", allEmails.size(), userEmail);
        return allEmails;
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
     * Sets the email map
     * This method is used to load email data into memory
     * It clears the existing email map and loads the new data.
     *
     * @param emailMap The email map to set
     */
    @Override
    public void setEmailMap(ConcurrentHashMap<String, Email> emailMap) {
        if (emailMap != null) {
            emails.clear();
            emails.putAll(emailMap);
            log.info("Email data loaded into memory. Total emails: {}", emails.size());
        } else {
            log.warn("Attempted to load null email data. Skipping.");
        }
    }
    
    /**
     * Sets the user map for email validation
     * This method is used to load user data into memory for email validation
     * 
     * @param userMap The user map to set
     */
    public void setUserMap(ConcurrentHashMap<String, User> userMap) {
        if (userMap != null) {
            users.clear();
            users.putAll(userMap);
            log.info("User data loaded into EmailManager. Total users: {}", users.size());
        } else {
            log.warn("Attempted to load null user data into EmailManager. Skipping.");
        }
    }

    /**
     * Generates a SHA-256 hash ID for the email.
     * 
     * @param sender The sender of the email
     * @param recipient The recipient of the email
     * @param subject The subject of the email
     * @param content The content of the email
     * @return A SHA-256 hash ID for the email
     */
    private String generateEmailId(String sender, String recipient, String subject, String content) {
        try {
            String input = sender + recipient + subject + content + System.currentTimeMillis();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            // Convert byte array to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to generate email ID: {}", e.getMessage());
            // Fallback to a simpler ID if SHA-256 is not available
            return "email-" + System.currentTimeMillis() + "-" + emails.size();
        }
    }
}