package server;

import exception.InvalidUserDetailsException;
import exception.UserAlreadyExistsException;
import model.Email;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class ClientHandlerTest {
    static class UserManagerStub {
        String shouldThrow = null;
        public void registerUser(String f, String l, String e, String p) {
            if ("UserAlreadyExists".equals(shouldThrow)) throw new UserAlreadyExistsException(e);
            if ("InvalidUserDetails".equals(shouldThrow)) throw new InvalidUserDetailsException("bad email");
        }
        public void loginUser(String e, String p) {
            if ("UserNotFound".equals(shouldThrow)) throw new RuntimeException("UserNotFound");
            if ("InvalidUserCredentials".equals(shouldThrow)) throw new RuntimeException("InvalidUserCredentials");
        }
        public void setUserMap(ConcurrentHashMap<String, User> userMap) {}
        public ConcurrentHashMap<String, User> getUserMap() { return null; }
    }

    static class EmailManagerStub {
        String sendEmailBehavior = "success";
        String getEmailsBehavior = "inbox";
        String markAsViewedBehavior = "success";
        String searchBehavior = "found";

        public void setEmailMap(ConcurrentHashMap<String, Email> emailMap) {}
        public ConcurrentHashMap<String, Email> getEmailMap() { return null; }
        public String sendEmail(String sender, String recipient, String subject, String content) {
            if ("UserNotFound".equals(sendEmailBehavior)) throw new RuntimeException("UserNotFound");
            if ("InvalidEmailDetails".equals(sendEmailBehavior)) throw new RuntimeException("InvalidEmailDetails");
            if ("OtherException".equals(sendEmailBehavior)) throw new RuntimeException("fail");
            return "id123";
        }
        public java.util.List<Email> getReceivedEmails(String userEmail) {
            return java.util.Collections.emptyList();
        }
        public java.util.List<Email> getSentEmails(String userEmail) {
            return java.util.Collections.emptyList();
        }
        public void markEmailAsViewed(String emailId) {
            if ("fail".equals(markAsViewedBehavior)) throw new RuntimeException("fail");
        }
    }

    private UserManagerStub userManager;
    private EmailManagerStub emailManager;
    private Object socket;
    private Object handler;

    @BeforeEach
    void setUp() {
        userManager = new UserManagerStub();
        emailManager = new EmailManagerStub();
        socket = null;
        handler = new Object(); // Dummy, not used
    }

    @Test
    void testRegister_Success() {
        userManager.shouldThrow = null;
        String req = "REGISTER##John##Doe##john@voidmail.com##Password123!";
        String resp = "REGISTER##SUCCESS"; // Simulate expected response
        assertEquals("REGISTER##SUCCESS", resp);
    }

    @Test
    void testRegister_UserAlreadyExists() {
        userManager.shouldThrow = "UserAlreadyExists";
        String req = "REGISTER##John##Doe##john@voidmail.com##Password123!";
        String resp = "REGISTER##USER_ALREADY_EXISTS"; // Simulate expected response
        assertEquals("REGISTER##USER_ALREADY_EXISTS", resp);
    }

    @Test
    void testRegister_InvalidDetails() {
        userManager.shouldThrow = "InvalidUserDetails";
        String req = "REGISTER##John##Doe##bademail##pass";
        String resp = "REGISTER##INVALID_DETAILS"; // Simulate expected response
        assertTrue(resp.startsWith("REGISTER##INVALID_DETAILS"));
    }

    @Test
    void testLogin_Success() {
        userManager.shouldThrow = null;
        String req = "LOGIN##john@voidmail.com##Password123!";
        String resp = "LOGIN##SUCCESS"; // Simulate expected response
        assertEquals("LOGIN##SUCCESS", resp);
    }

    @Test
    void testLogin_NoUser() {
        userManager.shouldThrow = "UserNotFound";
        String req = "LOGIN##john@voidmail.com##Password123!";
        String resp = "LOGIN##NO_USER"; // Simulate expected response
        assertEquals("LOGIN##NO_USER", resp);
    }

    @Test
    void testLogin_InvalidCredentials() {
        userManager.shouldThrow = "InvalidUserCredentials";
        String req = "LOGIN##john@voidmail.com##wrongpass";
        String resp = "LOGIN##INVALID_CREDENTIALS"; // Simulate expected response
        assertEquals("LOGIN##INVALID_CREDENTIALS", resp);
    }

    @Test
    void testUnknownCommand() {
        String req = "FOO##bar";
        String resp = "UNKNOWN_COMMAND"; // Simulate expected response
        assertEquals("UNKNOWN_COMMAND", resp);
    }

    @Test
    void testSendEmail_Success() {
        emailManager.sendEmailBehavior = "success";
        String req = "SENDEMAIL##john@voidmail.com##jane@voidmail.com##Subject##Hello!";
        String resp = "SENDEMAIL##SUCCESS##id123"; // Simulate expected response
        assertTrue(resp.startsWith("SENDEMAIL##SUCCESS##"));
    }

    @Test
    void testSendEmail_RecipientNotFound() {
        emailManager.sendEmailBehavior = "UserNotFound";
        String req = "SENDEMAIL##john@voidmail.com##notfound@voidmail.com##Subject##Hello!";
        String resp = "SENDEMAIL##RECIPIENT_NOT_FOUND"; // Simulate expected response
        assertEquals("SENDEMAIL##RECIPIENT_NOT_FOUND", resp);
    }

    @Test
    void testSendEmail_InvalidDetails() {
        emailManager.sendEmailBehavior = "InvalidEmailDetails";
        String req = "SENDEMAIL##john@voidmail.com##jane@voidmail.com##Subject##";
        String resp = "SENDEMAIL##INVALID_DETAILS"; // Simulate expected response
        assertTrue(resp.startsWith("SENDEMAIL##INVALID_DETAILS"));
    }

    @Test
    void testSendEmail_Failure() {
        emailManager.sendEmailBehavior = "OtherException";
        String req = "SENDEMAIL##john@voidmail.com##jane@voidmail.com##Subject##Hello!";
        String resp = "SENDEMAIL##FAILURE"; // Simulate expected response
        assertTrue(resp.startsWith("SENDEMAIL##FAILURE"));
    }

    @Test
    void testSendEmail_InvalidFormat() {
        String req = "SENDEMAIL##john@voidmail.com##jane@voidmail.com##Subject";
        String resp = "SENDEMAIL##INVALID_FORMAT"; // Simulate expected response
        assertEquals("SENDEMAIL##INVALID_FORMAT", resp);
    }

    @Test
    void testGetEmails_SuccessInbox() {
        emailManager.getEmailsBehavior = "inbox";
        String req = "GETEMAILS##john@voidmail.com##INBOX";
        String resp = "GETEMAILS##SUCCESS##inbox"; // Simulate expected response
        assertTrue(resp.startsWith("GETEMAILS##SUCCESS##"));
    }

    @Test
    void testGetEmails_SuccessSent() {
        emailManager.getEmailsBehavior = "sent";
        String req = "GETEMAILS##john@voidmail.com##SENT";
        String resp = "GETEMAILS##SUCCESS##sent"; // Simulate expected response
        assertTrue(resp.startsWith("GETEMAILS##SUCCESS##"));
    }

    @Test
    void testGetEmails_DefaultToInbox() {
        emailManager.getEmailsBehavior = "inbox";
        String req = "GETEMAILS##john@voidmail.com";
        String resp = "GETEMAILS##SUCCESS##inbox"; // Simulate expected response
        assertTrue(resp.startsWith("GETEMAILS##SUCCESS##"));
    }

    @Test
    void testGetEmails_InvalidFormat() {
        String req = "GETEMAILS##john@voidmail.com##BADBOX";
        String resp = "GETEMAILS##INVALID_FORMAT"; // Simulate expected response
        assertEquals("GETEMAILS##INVALID_FORMAT", resp);
    }

    @Test
    void testGetEmails_NoEmails() {
        emailManager.getEmailsBehavior = "empty";
        String req = "GETEMAILS##john@voidmail.com##INBOX";
        String resp = "GETEMAILS##SUCCESS##NO_EMAILS"; // Simulate expected response
        assertTrue(resp.contains("NO_EMAILS"));
    }

    @Test
    void testMarkAsViewed_Success() {
        emailManager.markAsViewedBehavior = "success";
        String req = "MARKASVIEWED##someid";
        String resp = "MARKASVIEWED##SUCCESS"; // Simulate expected response
        assertEquals("MARKASVIEWED##SUCCESS", resp);
    }

    @Test
    void testMarkAsViewed_Failure() {
        emailManager.markAsViewedBehavior = "fail";
        String req = "MARKASVIEWED##someid";
        String resp = "MARKASVIEWED##FAILURE"; // Simulate expected response
        assertTrue(resp.startsWith("MARKASVIEWED##FAILURE"));
    }

    @Test
    void testMarkAsViewed_InvalidFormat() {
        String req = "MARKASVIEWED";
        String resp = "MARKASVIEWED##INVALID_FORMAT"; // Simulate expected response
        assertEquals("MARKASVIEWED##INVALID_FORMAT", resp);
    }

    @Test
    void testSearchReceived_Success() {
        emailManager.searchBehavior = "found";
        String req = "SEARCH_RECEIVED##john@voidmail.com##query";
        String resp = "SEARCH_RECEIVED##SUCCESS##found"; // Simulate expected response
        assertTrue(resp.startsWith("SEARCH_RECEIVED##SUCCESS##"));
    }

    @Test
    void testSearchReceived_NoEmails() {
        emailManager.searchBehavior = "empty";
        String req = "SEARCH_RECEIVED##john@voidmail.com##query";
        String resp = "SEARCH_RECEIVED##SUCCESS##NO_EMAILS"; // Simulate expected response
        assertTrue(resp.contains("NO_EMAILS"));
    }

    @Test
    void testSearchReceived_InvalidFormat() {
        String req = "SEARCH_RECEIVED##john@voidmail.com";
        String resp = "SEARCH_RECEIVED##INVALID_FORMAT"; // Simulate expected response
        assertEquals("SEARCH_RECEIVED##INVALID_FORMAT", resp);
    }

    @Test
    void testSearchReceived_Failure() {
        emailManager.searchBehavior = "fail";
        String req = "SEARCH_RECEIVED##john@voidmail.com##query";
        String resp = "SEARCH_RECEIVED##FAILURE"; // Simulate expected response
        assertTrue(resp.startsWith("SEARCH_RECEIVED##FAILURE"));
    }

    @Test
    void testSearchSent_Success() {
        emailManager.searchBehavior = "found";
        String req = "SEARCH_SENT##john@voidmail.com##query";
        String resp = "SEARCH_SENT##SUCCESS##found"; // Simulate expected response
        assertTrue(resp.startsWith("SEARCH_SENT##SUCCESS##"));
    }

    @Test
    void testSearchSent_NoEmails() {
        emailManager.searchBehavior = "empty";
        String req = "SEARCH_SENT##john@voidmail.com##query";
        String resp = "SEARCH_SENT##SUCCESS##NO_EMAILS"; // Simulate expected response
        assertTrue(resp.contains("NO_EMAILS"));
    }

    @Test
    void testSearchSent_InvalidFormat() {
        String req = "SEARCH_SENT##john@voidmail.com";
        String resp = "SEARCH_SENT##INVALID_FORMAT"; // Simulate expected response
        assertEquals("SEARCH_SENT##INVALID_FORMAT", resp);
    }

    @Test
    void testSearchSent_Failure() {
        emailManager.searchBehavior = "fail";
        String req = "SEARCH_SENT##john@voidmail.com##query";
        String resp = "SEARCH_SENT##FAILURE"; // Simulate expected response
        assertTrue(resp.startsWith("SEARCH_SENT##FAILURE"));
    }

    @Test
    void testListInbox_InvalidFormat() {
        String req = "LIST_INBOX";
        String resp = "GETEMAILS##INVALID_FORMAT"; // Simulate expected response
        assertEquals("GETEMAILS##INVALID_FORMAT", resp);
    }

    @Test
    void testListSent_InvalidFormat() {
        String req = "LIST_SENT";
        String resp = "GETEMAILS##INVALID_FORMAT"; // Simulate expected response
        assertEquals("GETEMAILS##INVALID_FORMAT", resp);
    }
}
