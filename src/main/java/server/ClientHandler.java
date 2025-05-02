package server;

import exception.*;
import model.UserManager;
import utils.TCPUtils;

import java.net.Socket;

/**
 * ClientHandler is responsible for handling client requests in a separate thread.
 * It processes commands such as REGISTER and LOGIN, and interacts with the UserManager.
 */
public class ClientHandler implements Runnable {
    private final Socket socket;

    /**
     * Constructor to initialize the ClientHandler with a socket.
     *
     * @param socket The socket for the client connection.
     */
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

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
        // Split the request string into parts
        final String SEP = "##";
        String[] parts = request.split(SEP);
        if (parts.length == 0) return "UNKNOWN";

        return switch (parts[0].toUpperCase()) {
            case "REGISTER" -> handleRegister(parts);
            case "LOGIN" -> handleLogin(parts);
            default -> "UNKNOWN_COMMAND";
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
        if (parts.length != 5) return "REGISTER##INVALID_FORMAT";

        // Check if the user already exists
        try {
            UserManager.registerUser(parts[1], parts[2], parts[3], parts[4]);
            return "REGISTER##SUCCESS";
        } catch (UserAlreadyExistsException e) {
            return "REGISTER##USER_ALREADY_EXISTS";
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
        if (parts.length != 3) return "LOGIN##INVALID_FORMAT";

        // Check if the user exists and the credentials are valid
        try {
            UserManager.loginUser(parts[1], parts[2]);
            return "LOGIN##SUCCESS";
        } catch (UserNotFoundException e) {
            return "LOGIN##NO_USER";
        } catch (InvalidUserCredentialsException e) {
            return "LOGIN##FAIL";
        }
    }
}
