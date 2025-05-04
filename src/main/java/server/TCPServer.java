package server;

import exception.FailedToLoadException;
import lombok.extern.slf4j.Slf4j;
import model.EmailManager;
import model.EmailManagerInterface;
import model.UserManager;
import model.UserManagerInterface;
import utils.StorageManager;

import java.io.File;
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
    private static final int port = 12345;
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
        createStartupDirectories();
        handleMainMenuChoice(sc);
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
        System.out.println("Server is running...");
        log.info("Server Started");
    }

    /**
     * This method handles the main menu options for the user
     * It provides options to start, stop, and exit the server.
     *
     * @param sc Scanner object to read user input
     */
    private static void handleMainMenuChoice(Scanner sc) {

        while (programRunning) {
            mainMenuDisplay();
            System.out.print("Choose an option: ");
            String choice = sc.nextLine();
            System.out.println("-----------------------------------");
            switch (choice) {
                case "1":
                    try{
                        // Load data from storage into memory
                        userManager.setUserMap(new ConcurrentHashMap<>(StorageManager.loadUsers()));
                        emailManager.setEmailMap(new ConcurrentHashMap<>(StorageManager.loadEmails()));
                        log.info("Data loaded from storage on startup.");
                    } catch (FailedToLoadException e) {
                        System.out.println("Failed to load data: " + e.getMessage());
                        log.error("Failed to load data on startup", e);
                    }
                    startServer();

                    break;
                case "2":
                    serverRunning = false;
                    try {
                        StorageManager.saveUsersAndEmails(userManager.getUserMap(), emailManager.getEmailMap());
                        System.out.println("Data saved");
                        log.info("Data saved");
                    } catch (Exception e) {
                        System.out.println("Failed to save data before shutdown: " + e.getMessage());
                        log.error("Failed to save data on shutdown: ", e);
                    }
                    System.out.println("Server Stopped");
                    log.info("Server Stopped");
                    break;
                case "3":
                    programRunning = false;
                    System.out.println("Exiting program...");
                    break;
                case "4":
                    handleFileManagementMenu(sc);
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
            System.out.println("-----------------------------------");

        }
    }

    /**
     * This method handles the file management menu options for the user
     * It provides options to clear, load, and save data files.
     *
     * @param sc Scanner object to read user input
     */
    private static void handleFileManagementMenu(Scanner sc) {
        boolean inFileMenu = true;

        while (inFileMenu) {
            fileManagementMenuDisplay();
            String choice = sc.nextLine();

            System.out.println("-----------------------------------");
            try {
                switch (choice) {
                    case "1":
                        StorageManager.clearUsers();
                        System.out.println("User data cleared.");
                        break;
                    case "2":
                        StorageManager.clearEmails();
                        System.out.println("Email data cleared.");
                        break;
                    case "3":
                        StorageManager.clearFiles();
                        System.out.println("All data files cleared.");
                        break;
                    case "4":
                        userManager.setUserMap(new ConcurrentHashMap<>(StorageManager.loadUsers()));
                        emailManager.setEmailMap(new ConcurrentHashMap<>(StorageManager.loadEmails()));
                        System.out.println("Data loaded into memory.");
                        break;
                    case "5":
                        StorageManager.saveUsers(userManager.getUserMap());
                        StorageManager.saveEmails(emailManager.getEmailMap());
                        System.out.println("Data saved to files.");
                        break;
                    case "6":
                        inFileMenu = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                log.error("File management operation failed", e);
            }
            System.out.println("-----------------------------------");
        }
    }

    /**
     * Ensures required directories like 'logs/' and 'data/' exist on startup.
     */
    private static void createStartupDirectories() {
        String[] requiredDirs = {"logs", "data"};

        for (String dir : requiredDirs) {
            File directory = new File(dir);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                if (created) {
                    log.info("Created missing directory: {}", dir);
                } else {
                    log.warn("Failed to create directory: {}", dir);
                }
            } else {
                log.debug("Directory already exists: {}", dir);
            }
        }
    }

    /**
     * This method prints the file management menu options for the user.
     */
    private static void fileManagementMenuDisplay() {
        System.out.println("\n--- File Management Menu ---");
        System.out.println("1. Clear Users");
        System.out.println("2. Clear Emails");
        System.out.println("3. Clear Both Files");
        System.out.println("4. Load All Data");
        System.out.println("5. Save All Data");
        System.out.println("6. Back to Main Menu");
        System.out.print("Choose an option: ");
    }

    /**
     * This method prints the menu options for the user.
     */
    private static void mainMenuDisplay() {

        System.out.println("=== TCP Server Main Menu ===");
        System.out.println("1. Start Server");
        System.out.println("2. Stop Server");
        System.out.println("3. Exit and Save");
        System.out.println("4. File Management Options");

    }



}
