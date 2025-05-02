package utils;


import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * TCPUtils class that provides utility methods for sending and receiving messages over TCP
 * It includes methods to send a message, receive a message, and close the socket
 */
@Slf4j
public class TCPUtils {

    /**
     * Sends a message to the specified socket.
     *
     * @param socket  The socket to send the message to
     * @param message The message to send
     */
    public static void sendMessage(Socket socket, String message) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(message);
            log.info("Sent: {}", message);
        } catch (IOException e) {
            log.error("Error sending message: {}", e.getMessage(), e);
        }
    }

    /**
     * Receives a message from the specified socket.
     *
     * @param socket The socket to receive the message from
     * @return The received message
     */
    public static String receiveMessage(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String message = in.readLine();
            log.info("Received: {}", message);
            return message;
        } catch (IOException e) {
            log.error("Error receiving message: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Closes the specified socket.
     *
     * @param socket The socket to close
     */
    public static void closeSocket(Socket socket) {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
                log.info("Socket closed");
            } catch (IOException e) {
                log.error("Error closing socket: {}", e.getMessage(), e);
            }
        }
    }
}
