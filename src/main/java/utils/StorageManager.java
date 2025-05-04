package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import exception.FailedToLoadException;
import exception.FailedToSaveException;
import lombok.extern.slf4j.Slf4j;
import model.Email;
import model.User;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The StorageManager class is responsible for managing the storage of data in the application.
 * It provides methods to save, retrieve, and delete data from the storage into a file.
 */
@Slf4j
public class StorageManager {

    private static final String USERS_FILE = "data/users.json";
    private static final String EMAILS_FILE = "data/emails.json";
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setPrettyPrinting()
            .create();

    /**
     * Saves the given map of users to a JSON file.
     *
     * @param users The map of users to save
     * @throws FailedToSaveException if the file cannot be written
     */
    public static void saveUsers(Map<String, User> users) throws FailedToSaveException {
        try (FileWriter writer = new FileWriter(USERS_FILE)) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            throw new FailedToSaveException("Failed to save users to " + USERS_FILE, e);
        }
    }

    /**
     * Saves the given map of emails to a JSON file.
     *
     * @param emails The map of emails to save
     * @throws FailedToSaveException if the file cannot be written
     */
    public static void saveEmails(Map<String, Email> emails) throws FailedToSaveException {
        try (FileWriter writer = new FileWriter(EMAILS_FILE)) {
            gson.toJson(emails, writer);
        } catch (IOException e) {
            throw new FailedToSaveException("Failed to save emails to " + EMAILS_FILE, e);
        }
    }

    /**
     * Saves the given map of users and emails to their respective JSON files.
     *
     * @param users  The map of users to save
     * @param emails The map of emails to save
     * @throws FailedToSaveException if the files cannot be written
     */
    public static void saveUsersAndEmails(Map<String, User> users, Map<String, Email> emails) throws FailedToSaveException {
        saveUsers(users);
        saveEmails(emails);
    }

    /**
     * Loads the map of users from a JSON file as concurrent hash map.
     *
     * @return The map of users
     * @throws FailedToLoadException if the file cannot be read
     */
    public static Map<String, User> loadUsers() throws FailedToLoadException {
        try (FileReader reader = new FileReader(USERS_FILE)) {
            Type type = new TypeToken<Map<String, User>>() {}.getType();
            Map<String, User> users = gson.fromJson(reader, type);
            if (users == null||users.isEmpty()) {
                return new ConcurrentHashMap<>();
            } else {
                return new ConcurrentHashMap<>(users);
            }
        } catch (IOException e) {
            throw new FailedToLoadException("Failed to load users from " + USERS_FILE, e);
        }
    }

    /**
     * Loads the map of emails from a JSON file.
     *
     * @return The map of emails
     * @throws FailedToLoadException if the file cannot be read
     */
    public static Map<String, Email> loadEmails() throws FailedToLoadException {
        try (FileReader reader = new FileReader(EMAILS_FILE)) {
            Type type = new TypeToken<Map<String, Email>>() {}.getType();
            Map<String, Email> emails = gson.fromJson(reader, type);
            if (emails == null||emails.isEmpty()) {
                return new ConcurrentHashMap<>();
            } else {
                return new ConcurrentHashMap<>(emails);
            }
        } catch (IOException e) {
            throw new FailedToLoadException("Failed to load emails from " + EMAILS_FILE, e);
        }
    }

    /**
     * Clears the data in the users file.
     *
     * @throws FailedToSaveException if the file cannot be written
     */
    public static void clearUsers() throws FailedToSaveException {
        try (FileWriter writer = new FileWriter(USERS_FILE)) {
            writer.write("{}");
        } catch (IOException e) {
            throw new FailedToSaveException("Failed to clear users in " + USERS_FILE, e);
        }
    }

    /**
     * Clears the data in the emails file.
     *
     * @throws FailedToSaveException if the file cannot be cleared
     */
    public static void clearEmails() throws FailedToSaveException {
        try (FileWriter writer = new FileWriter(EMAILS_FILE)) {
            writer.write("{}");
        } catch (IOException e) {
            throw new FailedToSaveException("Failed to clear emails in " + EMAILS_FILE, e);
        }
    }

    /**
     * Clears the data in the users and emails files.
     *
     * @throws FailedToSaveException if the files cannot be cleared
     */
    public static void clearFiles() throws FailedToSaveException {
        try (FileWriter userWriter = new FileWriter(USERS_FILE);
             FileWriter emailWriter = new FileWriter(EMAILS_FILE)) {
            userWriter.write("{}");
            emailWriter.write("{}");
        } catch (IOException e) {
            throw new FailedToSaveException("Failed to clear both files", e);
        }
    }
}
