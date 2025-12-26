package com.spotifywrapped.spotify_wrapped_clone.service;

import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SensitiveDataService {

    private final PasswordEncoder passwordEncoder;
    private final TextEncryptor textEncryptor;

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
}
