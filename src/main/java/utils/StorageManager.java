package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import model.Email;
import model.User;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The StorageManager class is responsible for managing the storage of data in the application.
 * It provides methods to save, retrieve, and delete data from the storage into a file.
 */
@Slf4j
public class StorageManager {

    private static final String USERS_FILE = "users.json";
    private static final String EMAILS_FILE = "emails.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Saves the given map of users to a JSON file.
     *
     * @param users The map of users to save
     */
    public static void saveUsers(Map<String, User> users) {
        try (FileWriter writer = new FileWriter(USERS_FILE)) {
            gson.toJson(users, writer);
            log.info("Users saved to '{}'", USERS_FILE);
        } catch (IOException e) {
            log.error("Failed to save users: {}", e.getMessage(), e);
        }
    }

    /**
     * Saves the given map of emails to a JSON file.
     *
     * @param emails The map of emails to save
     */
    public static void saveEmails(Map<String, Email> emails) {
        try (FileWriter writer = new FileWriter(EMAILS_FILE)) {
            gson.toJson(emails, writer);
            log.info("Emails saved to '{}'", EMAILS_FILE);
        } catch (IOException e) {
            log.error("Failed to save emails: {}", e.getMessage(), e);
        }
    }

    /**
     * Loads the map of users from a JSON file.
     *
     * @return The map of users
     */
    public static Map<String, User> loadUsers() {
        try (FileReader reader = new FileReader(USERS_FILE)) {
            Type type = new TypeToken<Map<String, User>>() {}.getType();
            Map<String, User> users = gson.fromJson(reader, type);
            log.info("Users loaded from '{}'", USERS_FILE);
            return users != null ? new ConcurrentHashMap<>(users) : new ConcurrentHashMap<>();
        } catch (IOException e) {
            log.warn("No users loaded (file not found or error): {}", e.getMessage());
            return new ConcurrentHashMap<>();
        }
    }

    /**
     * Loads the map of emails from a JSON file.
     *
     * @return The map of emails
     */
    public static Map<String, Email> loadEmails() {
        try (FileReader reader = new FileReader(EMAILS_FILE)) {
            Type type = new TypeToken<Map<String, Email>>() {}.getType();
            Map<String, Email> emails = gson.fromJson(reader, type);
            log.info("Emails loaded from '{}'", EMAILS_FILE);
            return emails != null ? new ConcurrentHashMap<>(emails) : new ConcurrentHashMap<>();
        } catch (IOException e) {
            log.warn("No emails loaded (file not found or error): {}", e.getMessage());
            return new ConcurrentHashMap<>();
        }
    }

    /**
     * Clears the data in the users file.
     */
    public static void clearUsers() {
        try (FileWriter writer = new FileWriter(USERS_FILE)) {
            writer.write("{}");
            log.info("User data cleared from '{}'", USERS_FILE);
        } catch (IOException e) {
            log.error("Failed to clear users: {}", e.getMessage(), e);
        }
    }

    /**
     * Clears the data in the emails file.
     */
    public static void clearEmails() {
        try (FileWriter writer = new FileWriter(EMAILS_FILE)) {
            writer.write("{}");
            log.info("Email data cleared from '{}'", EMAILS_FILE);
        } catch (IOException e) {
            log.error("Failed to clear emails: {}", e.getMessage(), e);
        }
    }

    /**
     * Clears the data in the users and emails files.
     */
    public static void clearFiles() {
        try (FileWriter userWriter = new FileWriter(USERS_FILE);
             FileWriter emailWriter = new FileWriter(EMAILS_FILE)) {
            userWriter.write("{}");
            emailWriter.write("{}");
            log.info("All data cleared from '{}' and '{}'", USERS_FILE, EMAILS_FILE);
        } catch (IOException e) {
            log.error("Failed to clear files: {}", e.getMessage(), e);
        }
    }
}
