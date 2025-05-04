package utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SecurityUtils {

    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;

    /**
     * Generates a random salt string.
     */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes a password using PBKDF2 with HMAC-SHA256 with the provided salt.
     *
     * @param password The plain-text password
     * @param salt     The salt to use
     * @return The hashed password (Base64 encoded)
     */
    public static String hashPassword(String password, String salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                Base64.getDecoder().decode(salt),
                ITERATIONS,
                KEY_LENGTH
        );
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * checks that a raw password matches the hashed one using salt
     *
     * @param rawPassword     The password input
     * @param salt            The salt stored with the user
     * @param expectedHash    The stored hashed password
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String rawPassword, String salt, String expectedHash)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        String hashed = hashPassword(rawPassword, salt);
        return hashed.equals(expectedHash);
    }
}
