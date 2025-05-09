package server;

import exception.FailedToLoadException;
import exception.FailedToSaveException;
import lombok.extern.slf4j.Slf4j;
import model.*;
import utils.StorageManager;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyStore;
import java.util.List;
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
        if (serverThread != null && serverThread.isAlive()) {
            System.out.println("Server is already running.");
            return;
        }

        serverRunning = true;
        serverThread = new Thread(() -> {
            try {
                // Load keystore
                char[] password = "aloysirin".toCharArray(); // replace with your password
                KeyStore keyStore = KeyStore.getInstance("JKS");
                keyStore.load(new FileInputStream("serverkeystore.jks"), password);

                // Setup key manager
                KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                kmf.init(keyStore, password);

                // Setup SSL context
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(kmf.getKeyManagers(), null, null);

                SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();

                try (SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket(port)) {
                    System.out.println("Secure TCP Server Started Listening on port " + port);
                    log.info("Secure server started on port {}", port);

                    while (serverRunning) {
                        Socket clientSocket = serverSocket.accept();
                        log.info("Secure client connected from {}", clientSocket.getInetAddress());
                        new Thread(new ClientHandler(clientSocket, userManager, emailManager)).start();
                    }
                }
            } catch (Exception e) {
                System.out.println("Secure server error: " + e.getMessage());
                log.error("Secure server error: {}", e.getMessage(), e);
            }
        });

        serverThread.start();
        System.out.println("Server is running securely over SSL...");
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
                    //load before starting the server
                    loadAllUsersAndEmails();
                    startServer();

                    break;
                case "2":
                    serverRunning = false;
                    // Stop the server and save data
                    saveAllUsersAndEmails();
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
                case "5":
                    listLoggedInUsers();
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
            switch (choice) {
                case "1":
                    try {
                        StorageManager.clearUsers();
                        System.out.println("User data cleared.");
                    } catch (FailedToSaveException e) {
                        System.out.println("Failed to clear user data: " + e.getMessage());
                        log.error("Failed to clear users", e);
                    } catch (Exception e) {
                        System.out.println("Unexpected error while clearing users: " + e.getMessage());
                        log.error("Unexpected error clearing users", e);
                    }
                    break;

                case "2":
                    try {
                        StorageManager.clearEmails();
                        System.out.println("Email data cleared.");
                    } catch (FailedToSaveException e) {
                        System.out.println("Failed to clear email data: " + e.getMessage());
                        log.error("Failed to clear emails", e);
                    } catch (Exception e) {
                        System.out.println("Unexpected error while clearing emails: " + e.getMessage());
                        log.error("Unexpected error clearing emails", e);
                    }
                    break;

                case "3":
                    try {
                        StorageManager.clearFiles();
                        System.out.println("All data files cleared.");
                    } catch (FailedToSaveException e) {
                        System.out.println("Failed to clear data files: " + e.getMessage());
                        log.error("Failed to clear all data files", e);
                    } catch (Exception e) {
                        System.out.println("Unexpected error while clearing files: " + e.getMessage());
                        log.error("Unexpected error clearing all files", e);
                    }
                    break;

                case "4":
                    loadAllUsersAndEmails();
                    break;

                case "5":
                    saveAllUsersAndEmails();
                    break;

                case "6":
                    try {
                        StorageManager.saveUsers(userManager.getUserMap());
                        System.out.println("Users saved to file.");
                    } catch (FailedToSaveException e) {
                        System.out.println("Failed to save users: " + e.getMessage());
                        log.error("Failed to save users", e);
                    } catch (Exception e) {
                        System.out.println("Unexpected error while saving users: " + e.getMessage());
                        log.error("Unexpected error saving users", e);
                    }
                    break;

                case "7":
                    try {
                        StorageManager.saveEmails(emailManager.getEmailMap());
                        System.out.println("Emails saved to file.");
                    } catch (FailedToSaveException e) {
                        System.out.println("Failed to save emails: " + e.getMessage());
                        log.error("Failed to save emails", e);
                    } catch (Exception e) {
                        System.out.println("Unexpected error while saving emails: " + e.getMessage());
                        log.error("Unexpected error saving emails", e);
                    }
                    break;

                case "8":
                    inFileMenu = false;
                    break;

                default:
                    System.out.println("Invalid choice. Try again.");
            }

            System.out.println("-----------------------------------");
        }
    }

    /**
     * This method loads all users and emails from the data files into memory.
     * It uses the StorageManager to load the data and sets it in the UserManager and EmailManager.
     * It's an extract method to keep the code clean and organized.
     */
    private static void loadAllUsersAndEmails() {
        try {
            userManager.setUserMap(new ConcurrentHashMap<>(StorageManager.loadUsers()));
            emailManager.setEmailMap(new ConcurrentHashMap<>(StorageManager.loadEmails()));
            System.out.println("Data loaded into memory.");
        } catch (FailedToLoadException e) {
            System.out.println("Failed to load data: " + e.getMessage());
            log.error("Failed to load data from files", e);
        } catch (Exception e) {
            System.out.println("Unexpected error while loading data: " + e.getMessage());
            log.error("Unexpected error loading data", e);
        }
    }

    /**
     * This method saves all users and emails to the data files.
     * It uses the StorageManager to save the data from the UserManager and EmailManager.
     * It's an extract method to keep the code clean and organized.
     */
    private static void saveAllUsersAndEmails() {
        try {
            StorageManager.saveUsersAndEmails(userManager.getUserMap(), emailManager.getEmailMap());
            System.out.println("Data saved to files.");
        } catch (FailedToSaveException e) {
            System.out.println("Failed to save data: " + e.getMessage());
            log.error("Failed to save data to files", e);
        } catch (Exception e) {
            System.out.println("Unexpected error while saving data: " + e.getMessage());
            log.error("Unexpected error saving data", e);
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

        // Create JSON files if they don't exist
        createFileIfMissing("data/users.json");
        createFileIfMissing("data/emails.json");
    }

    /**
     * This method creates a file if it does not exist.
     * It initializes the file with an empty JSON object or array based on the file name.
     *
     * @param path The path of the file to create
     */
    private static void createFileIfMissing(String path) {
        File file = new File(path);
        if (!file.exists()) {
            try {
                boolean created = file.createNewFile();
                if (created) {
                    try (FileWriter writer = new FileWriter(file)) {
                        writer.write("{}");
                    }
                    log.info("Created and initialized file: {}", path);
                } else {
                    log.warn("Failed to create file: {}", path);
                }
            } catch (IOException e) {
                log.error("Error creating file {}: {}", path, e.getMessage());
            }
        } else {
            log.debug("File already exists: {}", path);
        }
    }

    /**
     * This method lists all logged-in users
     * It retrieves the list of logged-in users from the UserManager and displays them.
     */
    private static void listLoggedInUsers() {
        List<User> loggedInUsers = userManager.getLoggedInUsers();
        if (loggedInUsers.isEmpty()) {
            System.out.println("No users are currently logged in.");
        } else {
            System.out.println("Logged-in users:");
            loggedInUsers.forEach(user -> System.out.println("- " + user.getEmail()));
        }
    }

    /**
     * This method prints the main menu options for the user.
     */
    private static void mainMenuDisplay() {

        System.out.println("=== TCP Server Main Menu ===");
        System.out.println("1. Start Server");
        System.out.println("2. Stop Server");
        System.out.println("3. Exit Program");
        System.out.println("4. File Management Options");
        System.out.println("5. View Logged-In Users");

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
        System.out.println("6. Save Only Users");
        System.out.println("7. Save Only Emails");
        System.out.println("8. Back to Main Menu");
        System.out.print("Choose an option: ");
    }



}
