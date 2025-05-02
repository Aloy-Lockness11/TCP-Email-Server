package model;

import java.util.List;

public interface EmailManagerInterface {

    /**
     * Sends an email from the sender to the recipient with the specified subject and content.
     *
     * @param sender    The email address of the sender.
     * @param recipient The email address of the recipient.
     * @param subject   The subject of the email.
     * @param content   The content of the email.
     * @return A confirmation message indicating whether the email was sent successfully or not.
     */
    String sendEmail(String sender, String recipient, String subject, String content);

    /**
     * Retrieves all emails received by the specified user.
     *
     * @param userEmail The email address of the user.
     * @return A list of emails received by the user.
     */
    List<Email> getReceivedEmails(String userEmail);

    /**
     * Retrieves all emails sent by the specified user.
     *
     * @param userEmail The email address of the user.
     * @return A list of emails sent by the user.
     */
    List<Email> getSentEmails(String userEmail);

    /**
     * Marks the specified email as viewed.
     *
     * @param emailId The ID of the email to mark as viewed.
     * @return A boolean indicating whether the email was marked as viewed successfully or not.
     */
    boolean markEmailAsViewed(String emailId);
}
