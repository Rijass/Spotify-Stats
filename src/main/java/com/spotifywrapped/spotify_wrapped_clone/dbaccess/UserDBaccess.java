package com.spotifywrapped.spotify_wrapped_clone.dbaccess;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;

import com.spotifywrapped.spotify_wrapped_clone.dbaccess.entities.User;

@Repository
@Transactional
public class UserDBaccess {

    @PersistenceContext
    private EntityManager entityManager;

    public User createUser(User user) {
        entityManager.persist(user);
        return user;
    }

    public User findUserById(Long id) {
        return entityManager.find(User.class, id);
    }

    public User updateUser(Long id, User userUpdates) {
        User existingUser = entityManager.find(User.class, id);

        if (existingUser == null) {
            return null;
        }

        if (userUpdates.getUsername() != null) {
            existingUser.setUsername(userUpdates.getUsername());
        }

        if (userUpdates.getEmail() != null) {
            existingUser.setEmail(userUpdates.getEmail());
        }

        if (userUpdates.getPassword() != null) {
            existingUser.setPassword(userUpdates.getPassword());
        }

        if (userUpdates.getSessionToken() != null) {
            existingUser.setSessionToken(userUpdates.getSessionToken());
        }

        if (userUpdates.getSessionExpiresAt() != null) {
            existingUser.setSessionExpiresAt(userUpdates.getSessionExpiresAt());
        }

        if (userUpdates.getSpotifyRefreshToken() != null) {
            existingUser.setSpotifyRefreshToken(userUpdates.getSpotifyRefreshToken());
        }

        if (userUpdates.getSpotifyAccessToken() != null) {
            existingUser.setSpotifyAccessToken(userUpdates.getSpotifyAccessToken());
        }

        if (userUpdates.getSpotifyAccessTokenExpiresAt() != null) {
            existingUser.setSpotifyAccessTokenExpiresAt(userUpdates.getSpotifyAccessTokenExpiresAt());
        }

        return existingUser;
    }

    public boolean deleteUser(Long id) {
        User existingUser = entityManager.find(User.class, id);

        if (existingUser == null) {
            return false;
        }

        entityManager.remove(existingUser);
        return true;
    }

    public User findByUsernameOrEmail(String identifier) {
        return entityManager.createQuery(
                        "SELECT u FROM User u WHERE u.username = :identifier OR u.email = :identifier",
                        User.class)
                .setParameter("identifier", identifier)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public void updateSession(Long id, String encryptedSessionToken, Instant sessionExpiresAt) {
        User existingUser = entityManager.find(User.class, id);

        if (existingUser == null) {
            return;
        }

        existingUser.setSessionToken(encryptedSessionToken);
        existingUser.setSessionExpiresAt(sessionExpiresAt);
    }

    public List<User> findActiveSessions(Instant now) {
        return entityManager.createQuery(
                        "SELECT u FROM User u WHERE u.sessionToken IS NOT NULL AND u.sessionExpiresAt IS NOT NULL AND u.sessionExpiresAt > :now",
                        User.class)
                .setParameter("now", now)
                .getResultList();
    }

    public User findBySessionToken(String encryptedSessionToken, Instant now) {
        return entityManager.createQuery(
                        "SELECT u FROM User u WHERE u.sessionToken = :token AND u.sessionExpiresAt > :now",
                        User.class)
                .setParameter("token", encryptedSessionToken)
                .setParameter("now", now)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public void updateSpotifyRefreshToken(Long userId, String encryptedRefreshToken) {
        User existingUser = entityManager.find(User.class, userId);
        if (existingUser == null) {
            return;
        }

        existingUser.setSpotifyRefreshToken(encryptedRefreshToken);
    }

    public void updateSpotifyTokens(Long userId, String encryptedRefreshToken, String encryptedAccessToken, Instant accessTokenExpiresAt) {
        User existingUser = entityManager.find(User.class, userId);
        if (existingUser == null) {
            return;
        }

        if (encryptedRefreshToken != null) {
            existingUser.setSpotifyRefreshToken(encryptedRefreshToken);
        }

        existingUser.setSpotifyAccessToken(encryptedAccessToken);
        existingUser.setSpotifyAccessTokenExpiresAt(accessTokenExpiresAt);
    }

}