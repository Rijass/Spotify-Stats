package com.spotifywrapped.spotify_wrapped_clone.db_tests;

import com.spotifywrapped.spotify_wrapped_clone.dbaccess.SpotifyTokenDBaccess;
import com.spotifywrapped.spotify_wrapped_clone.dbaccess.UserDBaccess;
import com.spotifywrapped.spotify_wrapped_clone.dbaccess.entities.SpotifyToken;
import com.spotifywrapped.spotify_wrapped_clone.dbaccess.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({SpotifyTokenDBaccess.class, UserDBaccess.class})
class SpotifyTokenDBaccessTest {

    @Autowired
    private SpotifyTokenDBaccess spotifyTokenDBaccess;

    @Autowired
    private UserDBaccess userDBaccess;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void createOrUpdateTokenReturnsNullWhenUserIsMissing() {
        SpotifyToken token = spotifyTokenDBaccess.createOrUpdateToken(
                9999L,
                "refresh",
                "access",
                Instant.now().truncatedTo(ChronoUnit.MICROS)
        );

        assertThat(token).isNull();
    }

    @Test
    void createOrUpdateTokenPersistsForExistingUser() {
        User user = userDBaccess.createUser(buildUser("testuser", "user@example.com", "pw"));
        entityManager.flush();
        entityManager.clear();

        Instant expiry = Instant.now()
                .plusSeconds(3600)
                .truncatedTo(ChronoUnit.MICROS);

        SpotifyToken token = spotifyTokenDBaccess.createOrUpdateToken(
                user.getId(),
                "refresh-123",
                "access-123",
                expiry
        );
        entityManager.flush();
        entityManager.clear();

        SpotifyToken reloaded = entityManager.find(SpotifyToken.class, token.getId());

        assertThat(reloaded).isNotNull();
        assertThat(reloaded.getUser().getId()).isEqualTo(user.getId());
        assertThat(reloaded.getRefreshToken()).isEqualTo("refresh-123");
        assertThat(reloaded.getAccessToken()).isEqualTo("access-123");
        assertThat(reloaded.getAccessTokenExpiresAt()).isEqualTo(expiry);
    }

    @Test
    void createOrUpdateTokenUpdatesExistingWithoutOverwritingNullValues() {
        User user = userDBaccess.createUser(buildUser("another", "another@example.com", "pw"));
        entityManager.flush();
        entityManager.clear();

        Instant initialExpiry = Instant.now()
                .plusSeconds(1000)
                .truncatedTo(ChronoUnit.MICROS);

        SpotifyToken initial = spotifyTokenDBaccess.createOrUpdateToken(
                user.getId(),
                "refresh-a",
                "access-a",
                initialExpiry
        );
        entityManager.flush();
        entityManager.clear();

        Instant newExpiry = Instant.now()
                .plusSeconds(2000)
                .truncatedTo(ChronoUnit.MICROS);

        SpotifyToken updated = spotifyTokenDBaccess.createOrUpdateToken(
                user.getId(),
                null,
                "access-b",
                newExpiry
        );
        entityManager.flush();
        entityManager.clear();

        SpotifyToken reloaded = entityManager.find(SpotifyToken.class, updated.getId());

        assertThat(updated.getId()).isEqualTo(initial.getId());
        assertThat(reloaded.getRefreshToken()).isEqualTo("refresh-a");
        assertThat(reloaded.getAccessToken()).isEqualTo("access-b");
        assertThat(reloaded.getAccessTokenExpiresAt()).isEqualTo(newExpiry);
    }

    private User buildUser(String username, String email, String password) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }
}