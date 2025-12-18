package com.spotifywrapped.spotify_wrapped_clone.service;

import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class SensitiveDataService {

    private static final int SESSION_TOKEN_BYTES = 32;

    private final PasswordEncoder passwordEncoder;
    private final TextEncryptor textEncryptor;
    private final SecureRandom secureRandom = new SecureRandom();

    public SensitiveDataService(PasswordEncoder passwordEncoder, TextEncryptor textEncryptor) {
        this.passwordEncoder = passwordEncoder;
        this.textEncryptor = textEncryptor;
    }

    // === PASSWORDS ===

    public String hashPassword(String rawPassword) {
        return rawPassword == null ? null : passwordEncoder.encode(rawPassword);
    }

    public boolean passwordMatches(String rawPassword, String hashedPassword) {
        return rawPassword != null && hashedPassword != null && passwordEncoder.matches(rawPassword, hashedPassword);
    }


    // === SPOTIFY REFRESH TOKEN  ===

    public String encrypt(String value) {
        return value == null ? null : textEncryptor.encrypt(value);
    }

    public String decrypt(String encryptedValue) {
        return encryptedValue == null ? null : textEncryptor.decrypt(encryptedValue);
    }


    // === SESSION TOKEN GENERATION ===

    public String newSessionToken() {
        byte[] bytes = new byte[SESSION_TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }


    // === SESSION TOKEN HASHING ===

    public String hashToken(String rawToken) {
        return passwordEncoder.encode(rawToken); // BCrypt
    }

    public boolean tokenMatches(String rawToken, String hashedToken) {
        return rawToken != null && hashedToken != null && passwordEncoder.matches(rawToken, hashedToken);
    }
}
