package model;

import exception.EmailNotFoundException;
import exception.UserNotFoundException;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public interface EmailManagerInterface {
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
    String sendEmail(String sender, String recipient, String subject, String content)
            throws UserNotFoundException;

    /**
     * Retrieves all emails received by a user.
     *
     * @param userEmail The email address of the user
     * @return A list of emails received by the user
     */
    List<Email> getReceivedEmails(String userEmail);

    /**
     * Retrieves all emails sent by a user.
     *
     * @param userEmail The email address of the user
     * @return A list of emails sent by the user
     */
    List<Email> getSentEmails(String userEmail);

    /**
     * Marks an email as viewed.
     *
     * @param emailId The ID of the email
     * @throws EmailNotFoundException if the email is not found
     */
    boolean markEmailAsViewed(String emailId);

    /**
     * Sets the email map.
     *
     * @return The email map.
     */
    void setEmailMap(ConcurrentHashMap<String, Email> emailMap);
    void markEmailAsViewed(String emailId) throws EmailNotFoundException;
}