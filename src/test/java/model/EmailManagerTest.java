package model;

import exception.EmailNotFoundException;
import exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class EmailManagerTest {
    private EmailManager emailManager;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        EmailManager.clearEmailsForTest();
        emailManager = new EmailManager();
        
        // Create test users with all required fields
        user1 = new User("John", "Doe", "john@voidmail.com", "Password123!");
        user2 = new User("Jane", "Smith", "jane@voidmail.com", "Password456!");
        
        // Add users to the manager
        ConcurrentHashMap<String, User> userMap = new ConcurrentHashMap<>();
        userMap.put(user1.getEmail(), user1);
        userMap.put(user2.getEmail(), user2);
        emailManager.setUserMap(userMap);
    }

    @Test
    void sendEmail_Success() throws UserNotFoundException {
        String sender = user1.getEmail();
        String recipient = user2.getEmail();
        String subject = "Test Subject";
        String content = "Test Content";

        String emailId = emailManager.sendEmail(sender, recipient, subject, content);
        
        assertNotNull(emailId);
        assertFalse(emailId.isEmpty());
        
        List<Email> sentEmails = emailManager.getSentEmails(sender);
        assertEquals(1, sentEmails.size());
        assertEquals(subject, sentEmails.get(0).getSubject());
    }

    @Test
    void sendEmail_UserNotFound() {
        String sender = "nonexistent@voidmail.com";
        String recipient = user2.getEmail();
        String subject = "Test Subject";
        String content = "Test Content";

        assertThrows(UserNotFoundException.class, () -> {
            emailManager.sendEmail(sender, recipient, subject, content);
        });
    }

    @Test
    void getReceivedEmails() throws UserNotFoundException {
        String sender = user1.getEmail();
        String recipient = user2.getEmail();
        
        emailManager.sendEmail(sender, recipient, "Subject 1", "Content 1");
        emailManager.sendEmail(sender, recipient, "Subject 2", "Content 2");
        
        List<Email> receivedEmails = emailManager.getReceivedEmails(recipient);
        assertEquals(2, receivedEmails.size());
    }

    @Test
    void getSentEmails() throws UserNotFoundException {
        String sender = user1.getEmail();
        String recipient = user2.getEmail();
        
        emailManager.sendEmail(sender, recipient, "Subject 1", "Content 1");
        emailManager.sendEmail(sender, recipient, "Subject 2", "Content 2");
        
        List<Email> sentEmails = emailManager.getSentEmails(sender);
        assertEquals(2, sentEmails.size());
    }

    @Test
    void getAllEmails() throws UserNotFoundException {
        String sender = user1.getEmail();
        String recipient = user2.getEmail();
        
        emailManager.sendEmail(sender, recipient, "Subject 1", "Content 1");
        emailManager.sendEmail(recipient, sender, "Subject 2", "Content 2");
        
        List<Email> allEmails = emailManager.getAllEmails(sender);
        assertEquals(2, allEmails.size());
    }

    @Test
    void markEmailAsViewed() throws UserNotFoundException, EmailNotFoundException {
        String sender = user1.getEmail();
        String recipient = user2.getEmail();
        
        String emailId = emailManager.sendEmail(sender, recipient, "Subject", "Content");
        
        emailManager.markEmailAsViewed(emailId);
        
        List<Email> receivedEmails = emailManager.getReceivedEmails(recipient);
        assertTrue(receivedEmails.get(0).isViewed());
    }

    @Test
    void markEmailAsViewed_EmailNotFound() {
        assertThrows(EmailNotFoundException.class, () -> {
            emailManager.markEmailAsViewed("nonexistent-email-id");
        });
    }

    @Test
    void setAndGetEmailMap() {
        ConcurrentHashMap<String, Email> testMap = new ConcurrentHashMap<>();
        Email testEmail = Email.builder()
                .id("test-id")
                .sender("test@voidmail.com")
                .recipient("recipient@voidmail.com")
                .subject("Test")
                .content("Content")
                .build();
        testMap.put("test-id", testEmail);
        
        emailManager.setEmailMap(testMap);
        assertEquals(testMap, emailManager.getEmailMap());
    }

    @Test
    void setUserMap_NullInput() {
        // Should not throw, just log a warning
        assertDoesNotThrow(() -> emailManager.setUserMap(null));
    }

    @Test
    void setEmailMap_NullInput() {
        // Should not throw, just log a warning
        assertDoesNotThrow(() -> emailManager.setEmailMap(null));
    }

    @Test
    void getEmailMap_ReturnsCurrentMap() {
        assertNotNull(emailManager.getEmailMap());
        assertTrue(emailManager.getEmailMap() instanceof ConcurrentHashMap);
    }

    @Test
    void sendEmail_BothUsersMissing() {
        // Remove all users
        emailManager.setUserMap(new ConcurrentHashMap<>());
        // Should not throw, as per code (users.size() == 0)
        assertDoesNotThrow(() -> {
            emailManager.sendEmail("a@voidmail.com", "b@voidmail.com", "Sub", "Body");
        });
    }

    @Test
    void sendEmail_SenderMissing() {
        // Only add recipient
        ConcurrentHashMap<String, User> userMap = new ConcurrentHashMap<>();
        userMap.put(user2.getEmail(), user2);
        emailManager.setUserMap(userMap);
        assertThrows(UserNotFoundException.class, () -> {
            emailManager.sendEmail(user1.getEmail(), user2.getEmail(), "Sub", "Body");
        });
    }

    @Test
    void sendEmail_RecipientMissing() {
        // Only add sender
        ConcurrentHashMap<String, User> userMap = new ConcurrentHashMap<>();
        userMap.put(user1.getEmail(), user1);
        emailManager.setUserMap(userMap);
        assertThrows(UserNotFoundException.class, () -> {
            emailManager.sendEmail(user1.getEmail(), user2.getEmail(), "Sub", "Body");
        });
    }

    @Test
    void sendEmail_InvalidEmail_ThrowsException() {
        String sender = user1.getEmail();
        String recipient = user2.getEmail();
        String subject = ""; // Invalid: blank subject
        String content = "Test Content";
        assertThrows(RuntimeException.class, () -> {
            emailManager.sendEmail(sender, recipient, subject, content);
        });
    }
}
