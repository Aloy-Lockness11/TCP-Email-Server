package client;

import exception.SecureConnectionException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.security.KeyStore;

/**
 * ClientConnection class that provides methods to establish a connection to a server,
 * send messages, receive messages, and close the connection
 * This utilizes SSL for secure communication.
 * It uses a keystore to manage the server's public key.
 */
public class ClientConnection {
    private SSLSocket socket;
    private BufferedReader in;
    private PrintWriter out;

    /**
     * Establishes a secure SSL connection to the server.
     *
     * @param host The server host
     * @param port The server port
     * @throws Exception if an error occurs during connection
     */
    public ClientConnection(String host, int port) throws SecureConnectionException{
        try {
            char[] password = "aloysirin".toCharArray(); // Replace as needed
            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(new FileInputStream("serverkeystore.jks"), password);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);

            SSLSocketFactory factory = sslContext.getSocketFactory();
            socket = (SSLSocket) factory.createSocket(host, port);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

        } catch (Exception e) {
            throw new SecureConnectionException("Failed to establish secure connection to server", e);
        }
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
