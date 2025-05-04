package server;

import exception.*;
import lombok.AllArgsConstructor;
import model.Email;
import model.EmailManagerInterface;
import model.UserManagerInterface;
import utils.TCPUtils;

import java.net.Socket;
import java.util.List;
import java.time.format.DateTimeFormatter;

import utils.protocols.UserProtocol;
import utils.protocols.EmailProtocol;
import utils.protocols.CommonProtocol;

/**
 * ClientHandler is responsible for handling client requests in a separate thread.
 * It processes commands such as REGISTER and LOGIN, and interacts with the UserManager.
 */
@AllArgsConstructor
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final UserManagerInterface userManager;
    private final EmailManagerInterface emailManager;

    /**
     * The run method is executed when the thread is started.
     * It continuously listens for incoming messages from the client,
     * processes them, and sends back responses.
     */
    @Override
    public void run() {
        try {
            while (true) {
                // Receive message from client
                String request = TCPUtils.receiveMessage(socket);
                if (request == null) break;

                // If the request is empty, break the loop
                String response = handleRequest(request.trim());
                TCPUtils.sendMessage(socket, response);
            }
        } catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
        } finally {
            TCPUtils.closeSocket(socket);
        }
    }

    /**
     * Handles incoming requests from clients.
     * It processes commands like REGISTER and LOGIN.
     * If the command is not recognized, it returns "UNKNOWN_COMMAND".
     *
     * @param request The request string from the client.
     * @return The response string to be sent back to the client.
     */
    private String handleRequest(String request) {
        final String SEP = CommonProtocol.SEP;
        String[] parts = request.split(SEP);
        if (parts.length == 0) return EmailProtocol.UNKNOWN_COMMAND;

        return switch (parts[0].toUpperCase()) {
            case UserProtocol.REGISTER -> handleRegister(parts);
            case UserProtocol.LOGIN -> handleLogin(parts);
            case EmailProtocol.SEND_EMAIL -> handleSendEmail(parts);
            case EmailProtocol.GET_EMAILS -> handleGetEmailsFlexible(parts);
            case EmailProtocol.LIST_INBOX -> handleListEmails(parts, false);
            case EmailProtocol.LIST_SENT -> handleListEmails(parts, true);
            case EmailProtocol.MARK_AS_VIEWED -> handleMarkAsViewed(parts);
            case EmailProtocol.SEARCH_RECEIVED -> handleSearchEmails(parts, false);
            case EmailProtocol.SEARCH_SENT -> handleSearchEmails(parts, true);
            default -> EmailProtocol.UNKNOWN_COMMAND;
        };
    }

    /**
     * Handles the REGISTER command
     * It registers a new user with the provided details
     * If the user already exists, it returns an appropriate message.
     *
     * @param parts The parts of the request string.
     * @return The response string indicating the result of the registration.
     */
    private String handleRegister(String[] parts) {
        if (parts.length != 5) return UserProtocol.REGISTER + CommonProtocol.SEP + UserProtocol.INVALID_FORMAT;

        // Check if the user already exists
        try {
            userManager.registerUser(parts[1], parts[2], parts[3], parts[4]);
            return UserProtocol.REGISTER + CommonProtocol.SEP + UserProtocol.SUCCESS;
        } catch (UserAlreadyExistsException e) {
            return UserProtocol.REGISTER + CommonProtocol.SEP + UserProtocol.USER_ALREADY_EXISTS;
        } catch (InvalidUserDetailsException e) {
            return UserProtocol.REGISTER + CommonProtocol.SEP + UserProtocol.INVALID_DETAILS + CommonProtocol.SEP + e.getMessage();
        }
    }

    /**
     * Handles the LOGIN command.
     * It logs in a user with the provided username and password
     * If the user is not found or the credentials are invalid, it returns an appropriate message.
     *
     * @param parts The parts of the request string.
     * @return The response string indicating the result of the login attempt.
     */
    private String handleLogin(String[] parts) {
        if (parts.length != 3) return UserProtocol.LOGIN + CommonProtocol.SEP + UserProtocol.INVALID_FORMAT;

        try {
            userManager.loginUser(parts[1], parts[2]);
            return UserProtocol.LOGIN + CommonProtocol.SEP + UserProtocol.SUCCESS;
        } catch (UserNotFoundException e) {
            return UserProtocol.LOGIN + CommonProtocol.SEP + UserProtocol.NO_USER;
        } catch (InvalidUserCredentialsException e) {
            return UserProtocol.LOGIN + CommonProtocol.SEP + UserProtocol.INVALID_CREDENTIALS;
        }
    }

    /**
     * Handles the SEND_EMAIL command.
     * It sends an email from one user to another.
     *
     * @param parts The parts of the request string.
     * @return The response string indicating the result of the email sending.
     */
    private String handleSendEmail(String[] parts) {
        // Format should be: SENDEMAIL##sender##recipient##subject##content
        if (parts.length != 5) return EmailProtocol.SEND_EMAIL + CommonProtocol.SEP + EmailProtocol.INVALID_FORMAT;

        String sender = parts[1];
        String recipient = parts[2];
        String subject = parts[3];
        String content = parts[4];

        try {
            String emailId = emailManager.sendEmail(sender, recipient, subject, content);
            return EmailProtocol.SEND_EMAIL + CommonProtocol.SEP + EmailProtocol.SUCCESS + CommonProtocol.SEP + emailId;
        } catch (UserNotFoundException e) {
            return EmailProtocol.SEND_EMAIL + CommonProtocol.SEP + EmailProtocol.RECIPIENT_NOT_FOUND;
        } catch (InvalidEmailDetailsException e) {
            return EmailProtocol.SEND_EMAIL + CommonProtocol.SEP + EmailProtocol.INVALID_DETAILS + CommonProtocol.SEP + e.getMessage();
        } catch (Exception e) {
            return EmailProtocol.SEND_EMAIL + CommonProtocol.SEP + EmailProtocol.FAILURE + CommonProtocol.SEP + e.getMessage();
        }
    }

    /**
     * Handles the LIST_INBOX and LIST_SENT commands.
     * It retrieves emails for a user, either from the inbox or sent items.
     *
     * @param parts The parts of the request string.
     * @param isSent Indicates whether to retrieve sent emails (true) or received emails (false).
     * @return The response string indicating the result of the email retrieval.
     */
    private String handleListEmails(String[] parts, boolean isSent) {
        if (parts.length != 2) return EmailProtocol.GET_EMAILS + CommonProtocol.SEP + EmailProtocol.INVALID_FORMAT;

        String userEmail = parts[1];

        try {
            List<Email> emails = isSent ?
                    emailManager.getSentEmails(userEmail) :
                    emailManager.getReceivedEmails(userEmail);

            StringBuilder response = new StringBuilder(EmailProtocol.GET_EMAILS + CommonProtocol.SEP + EmailProtocol.SUCCESS);

            if (emails.isEmpty()) {
                response.append(CommonProtocol.SEP).append(EmailProtocol.NO_EMAILS);
            } else {
                for (Email email : emails) {
                    response.append(CommonProtocol.SEP)
                            .append(email.getId())
                            .append(CommonProtocol.SEP)
                            .append(email.getSender())
                            .append(CommonProtocol.SEP)
                            .append(email.getSubject())
                            .append(CommonProtocol.SEP)
                            .append(email.getContent())
                            .append(CommonProtocol.SEP)
                            .append(email.getTimestamp())
                            .append(CommonProtocol.SEP)
                            .append(email.isViewed());
                }
            }

            return response.toString();
        } catch (UserNotFoundException e) {
            return EmailProtocol.GET_EMAILS + CommonProtocol.SEP + UserProtocol.NO_USER;
        } catch (Exception e) {
            return EmailProtocol.GET_EMAILS + CommonProtocol.SEP + EmailProtocol.FAILURE + CommonProtocol.SEP + e.getMessage();
        }
    }

    /**
     * Handles the GETEMAILS command.
     * It retrieves emails for a user, either from the inbox or sent items.
     *
     * @param parts The parts of the request string.
     * @return The response string indicating the result of the email retrieval.
     */
    // New flexible GETEMAILS handler
    private String handleGetEmailsFlexible(String[] parts) {
        // Supports: GETEMAILS##user, GETEMAILS##user##INBOX, GETEMAILS##user##SENT
        if (parts.length == 2) {
            // Default to inbox
            return handleListEmails(new String[]{EmailProtocol.GET_EMAILS, parts[1]}, false);
        } else if (parts.length == 3) {
            String box = parts[2].toUpperCase();
            if (box.equals(EmailProtocol.INBOX)) {
                return handleListEmails(new String[]{EmailProtocol.GET_EMAILS, parts[1]}, false);
            } else if (box.equals(EmailProtocol.SENT)) {
                return handleListEmails(new String[]{EmailProtocol.GET_EMAILS, parts[1]}, true);
            } else {
                return EmailProtocol.GET_EMAILS + CommonProtocol.SEP + EmailProtocol.INVALID_FORMAT;
            }
        } else {
            return EmailProtocol.GET_EMAILS + CommonProtocol.SEP + EmailProtocol.INVALID_FORMAT;
        }
    }

    /**
     * Handles the MARKASVIEWED command.
     * It marks an email as viewed.
     *
     * @param parts The parts of the request string.
     * @return The response string indicating the result of the email marking.
     */
    private String handleMarkAsViewed(String[] parts) {
        // Format: MARKASVIEWED##emailId
        if (parts.length != 2) return EmailProtocol.MARK_AS_VIEWED + CommonProtocol.SEP + EmailProtocol.INVALID_FORMAT;
        String emailId = parts[1];
        try {
            emailManager.markEmailAsViewed(emailId);
            return EmailProtocol.MARK_AS_VIEWED + CommonProtocol.SEP + EmailProtocol.SUCCESS;
        } catch (Exception e) {
            return EmailProtocol.MARK_AS_VIEWED + CommonProtocol.SEP + EmailProtocol.FAILURE + CommonProtocol.SEP + e.getMessage();
        }
    }

    /**
     * Handles the SEARCH_RECEIVED and SEARCH_SENT commands.
     * It searches for emails based on a query string.
     *
     * @param parts The parts of the request string.
     * @param isSent Indicates whether to search in sent emails (true) or received emails (false).
     * @return The response string indicating the result of the email search.
     */
    private String handleSearchEmails(String[] parts, boolean isSent) {
        // Format: SEARCH_RECEIVED##userEmail##query or SEARCH_SENT##userEmail##query
        if (parts.length != 3) return (isSent ? EmailProtocol.SEARCH_SENT : EmailProtocol.SEARCH_RECEIVED) + CommonProtocol.SEP + EmailProtocol.INVALID_FORMAT;
        String userEmail = parts[1];
        String query = parts[2].toLowerCase();
        try {
            List<Email> emails = isSent ? emailManager.getSentEmails(userEmail) : emailManager.getReceivedEmails(userEmail);
            StringBuilder response = new StringBuilder((isSent ? EmailProtocol.SEARCH_SENT : EmailProtocol.SEARCH_RECEIVED) + CommonProtocol.SEP + EmailProtocol.SUCCESS);
            boolean found = false;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (Email email : emails) {
                String formattedDate = "";
                try {
                    formattedDate = email.getTimestamp().format(formatter);
                } catch (Exception ignored) {}
                boolean match = email.getSubject().toLowerCase().contains(query)
                        || email.getSender().toLowerCase().contains(query)
                        || email.getRecipient().toLowerCase().contains(query)
                        || formattedDate.contains(query);
                if (match) {
                    found = true;
                    if (isSent) {
                        // ID, recipient, subject, timestamp, viewed
                        response.append(CommonProtocol.SEP)
                                .append(email.getId())
                                .append(CommonProtocol.SEP)
                                .append(email.getRecipient())
                                .append(CommonProtocol.SEP)
                                .append(email.getSubject())
                                .append(CommonProtocol.SEP)
                                .append(email.getTimestamp())
                                .append(CommonProtocol.SEP)
                                .append(email.isViewed());
                    } else {
                        // ID, sender, subject, timestamp, viewed
                        response.append(CommonProtocol.SEP)
                                .append(email.getId())
                                .append(CommonProtocol.SEP)
                                .append(email.getSender())
                                .append(CommonProtocol.SEP)
                                .append(email.getSubject())
                                .append(CommonProtocol.SEP)
                                .append(email.getTimestamp())
                                .append(CommonProtocol.SEP)
                                .append(email.isViewed());
                    }
                }
            }
            if (!found) {
                response.append(CommonProtocol.SEP).append(EmailProtocol.NO_EMAILS);
            }
            return response.toString();
        } catch (Exception e) {
            return (isSent ? EmailProtocol.SEARCH_SENT : EmailProtocol.SEARCH_RECEIVED) + CommonProtocol.SEP + EmailProtocol.FAILURE + CommonProtocol.SEP + e.getMessage();
        }
    }
}