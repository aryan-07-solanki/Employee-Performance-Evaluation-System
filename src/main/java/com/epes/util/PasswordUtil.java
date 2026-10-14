package com.epes.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Helper for hashing passwords with SHA-256, so plain passwords are never stored.
 * The output matches MySQL's SHA2(password, 256) used in schema.sql.
 */
public final class PasswordUtil {

    private PasswordUtil() { }

    /**
     * Converts a plain password into a 64-character hexadecimal SHA-256 hash.
     *
     * @param plain the password typed by the user
     * @return lowercase hex hash
     */
    public static String hash(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is always available in Java, so this should never happen
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
