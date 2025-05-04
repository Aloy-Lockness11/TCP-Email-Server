package model;

import exception.EmailNotFoundException;
import exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class EmailManagerTest {
    private EmailManager emailManager;
    private User sender;
    private User recipient;

    @BeforeEach
    public void setUp() {
        emailManager = new EmailManager();
        sender = new User("John", "Doe", "john@voidmail.com", "Password1!");
        recipient = new User("Jane", "Smith", "jane@voidmail.com", "Password1!");
        ConcurrentHashMap<String, User> userMap = new ConcurrentHashMap<>();
        userMap.put(sender.getEmail(), sender);
        userMap.put(recipient.getEmail(), recipient);
        emailManager.setUserMap(userMap);
    }

    @Test
    public void sendEmail_success() throws UserNotFoundException {
        String emailId = emailManager.sendEmail(sender.getEmail(), recipient.getEmail(), "Hello", "Test message");
        assertNotNull(emailId);
        List<Email> sent = emailManager.getSentEmails(sender.getEmail());
        assertEquals(1, sent.size());
        assertEquals("Hello", sent.get(0).getSubject());
        List<Email> received = emailManager.getReceivedEmails(recipient.getEmail());
        assertEquals(1, received.size());
        assertEquals("Test message", received.get(0).getContent());
    }

    @Test
    public void sendEmail_userNotFound() {
        assertThrows(UserNotFoundException.class, () -> {
            emailManager.sendEmail("notfound@voidmail.com", recipient.getEmail(), "Hi", "Body");
        });
        assertThrows(UserNotFoundException.class, () -> {
            emailManager.sendEmail(sender.getEmail(), "notfound@voidmail.com", "Hi", "Body");
        });
    }

    @Test
    public void getSentAndReceivedEmails_empty() {
        List<Email> sent = emailManager.getSentEmails(sender.getEmail());
        List<Email> received = emailManager.getReceivedEmails(recipient.getEmail());
        assertTrue(sent.isEmpty());
        assertTrue(received.isEmpty());
    }

    @Test
    public void markEmailAsViewed_success() throws Exception {
        String emailId = emailManager.sendEmail(sender.getEmail(), recipient.getEmail(), "Subj", "Msg");
        Email email = emailManager.getReceivedEmails(recipient.getEmail()).get(0);
        assertFalse(email.isViewed());
        emailManager.markEmailAsViewed(emailId);
        Email updated = emailManager.getReceivedEmails(recipient.getEmail()).get(0);
        assertTrue(updated.isViewed());
    }

    @Test
    public void markEmailAsViewed_notFound() {
        assertThrows(EmailNotFoundException.class, () -> {
            emailManager.markEmailAsViewed("fakeid");
        });
    }
}