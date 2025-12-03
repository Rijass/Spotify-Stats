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

    public String hashPassword(String rawPassword) {
        return rawPassword == null ? null : passwordEncoder.encode(rawPassword);
    }

    public boolean passwordMatches(String rawPassword, String hashedPassword) {
        return rawPassword != null && hashedPassword != null && passwordEncoder.matches(rawPassword, hashedPassword);
    }

    public String encrypt(String value) {
        return value == null ? null : textEncryptor.encrypt(value);
    }

    public String decrypt(String encryptedValue) {
        return encryptedValue == null ? null : textEncryptor.decrypt(encryptedValue);
    }

    public String newSessionToken() {
        byte[] bytes = new byte[SESSION_TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
