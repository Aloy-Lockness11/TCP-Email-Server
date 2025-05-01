package utils;


import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@Slf4j
public class TCPutils {
    public static void sendMessage(Socket socket, String message) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(message);
            log.info("Sent: {}", message);
        } catch (IOException e) {
            log.error("Error sending message: {}", e.getMessage(), e);
        }
    }

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
