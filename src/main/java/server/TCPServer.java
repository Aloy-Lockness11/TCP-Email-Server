package server;

import lombok.extern.slf4j.Slf4j;
import model.EmailManager;
import model.EmailManagerInterface;
import model.UserManager;
import model.UserManagerInterface;
import utils.StorageManager;

import java.net.Socket;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TCPServer class that implements a simple TCP server.
 * It listens for incoming connections and handles them in separate threads.
 */
@Slf4j
public class TCPServer {
    private static int port = 12345;
    private static boolean serverRunning = true;
    private static boolean programRunning = true;
    private static Thread serverThread;


    private static final UserManagerInterface userManager= new UserManager();
    private static final EmailManagerInterface emailManager= new EmailManager();

    /**
     * Main method to start the TCP server.
     * It provides a menu system to start, stop, and exit the server.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {


        Scanner sc = new Scanner(System.in);

        // Menu System to handle Running of the server
        while (programRunning) {
            printMenu();
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    startServer();
                    break;
                case "2":
                    serverRunning = false;
                    System.out.println("Server Stopped");
                    log.info("Server Stopped");
                    break;
                case "3":
                    programRunning = false;
                    System.out.println("Exiting...");
                    log.info("Application Closed");
                    break;
                case "4":
                    StorageManager.clearUsers();
                    break;
                case "5":
                    StorageManager.clearEmails();
                    break;
                case "6":
                    userManager.setUserMap(new ConcurrentHashMap<>(StorageManager.loadUsers()));
                    emailManager.setEmailMap(new ConcurrentHashMap<>(StorageManager.loadEmails()));
                    log.info("Data manually reloaded from storage.");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }

        }

    }

    /**
     * This method starts the TCP server and listens for incoming connections.
     * It creates a new thread to handle each client connection.
     * It is created in thread so that the menu system can run in parallel
     * this allows the server to be started and stopped without blocking the main thread.
     */
    private static void startServer() {
        // Check if the server is already running before starting a new one
        if (serverThread != null && serverThread.isAlive()) {
            System.out.println("Server is already running.");
            return;
        }

        userManager.setUserMap(new ConcurrentHashMap<>(StorageManager.loadUsers()));
        emailManager.setEmailMap(new ConcurrentHashMap<>(StorageManager.loadEmails()));
        log.info("Data loaded from storage on startup.");

        // Create a new thread for the server
        serverRunning = true;
        serverThread = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("TCP Server Started Listening on port " + port);
                log.info("Server Started and Listening on port {}", port);

                while (serverRunning) {
                    // Accept incoming client connections
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected from " + clientSocket.getInetAddress());
                    log.info("Client connected from {}", clientSocket.getInetAddress());

                    // Create a new thread to handle the client connection
                    new Thread(new ClientHandler(clientSocket,userManager,emailManager)).start();
                }

            } catch (IOException e) {
                System.out.println("Error in server: " + e.getMessage());
                log.error("Error in server: {}", e.getMessage(), e);
            }
        });

        serverThread.start();
    }

    /**
     * This method prints the menu options for the user.
     */
    private static void printMenu() {

        System.out.println("TCP Server Menu:");
        System.out.println("1. Start Server");
        System.out.println("2. Stop Server");
        System.out.println("3. Exit");
        System.out.println("4. Clear User Data");
        System.out.println("5. Clear Email Data");
        System.out.println("6. Load All Data");
        System.out.print("Choose an option: ");

        // Add logic to handle user input and call appropriate methods
    }
}
