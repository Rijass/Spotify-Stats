package com.spotifywrapped.spotify_wrapped_clone.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class CryptoConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public TextEncryptor textEncryptor(
            @Value("${app.crypto.password}") String cryptoPassword,
            @Value("${app.crypto.salt}") String cryptoSalt) {

        return Encryptors.text(cryptoPassword, cryptoSalt);
    }
}
