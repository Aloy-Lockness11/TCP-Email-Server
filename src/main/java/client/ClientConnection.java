package client;

import java.io.*;
import java.net.Socket;

/**
 * ClientConnection class that provides methods to establish a connection to a server,
 * send messages, receive messages, and close the connection
 */
public class ClientConnection {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    /**
     * Constructor to create a new ClientConnection instance.
     * It initializes the socket, input stream, and output stream.
     *
     * @param host The server's hostname or IP address
     * @param port The server's port number
     * @throws IOException If an I/O error occurs when creating the socket
     */
    public ClientConnection(String host, int port) throws IOException {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    /**
     * Sends a message to the server.
     *
     * @param message The message to send
     */
    public void send(String message) {
        out.println(message);
    }

    /**
     * Receives a message from the server.
     *
     * @return The received message
     * @throws IOException If an I/O error occurs when reading the message
     */
    public String receive() throws IOException {
        return in.readLine();
    }

    /**
     * Closes the connection to the server.
     *
     * @throws IOException If an I/O error occurs when closing the socket
     */
    public void close() throws IOException {
        socket.close();
    }
}
