package com.spotifywrapped.spotify_wrapped_clone.dbaccess;

import com.spotifywrapped.spotify_wrapped_clone.dbaccess.entities.SpotifyToken;
import com.spotifywrapped.spotify_wrapped_clone.dbaccess.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
@Transactional
public class SpotifyTokenDBaccess {

    @PersistenceContext
    private EntityManager entityManager;

    public SpotifyToken findByUserId(Long userId) {
        return entityManager.createQuery(
                        "SELECT t FROM SpotifyToken t WHERE t.user.id = :userId",
                        SpotifyToken.class)
                .setParameter("userId", userId)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public SpotifyToken createOrUpdateToken(Long userId, String refreshToken, String accessToken, Instant accessTokenExpiresAt) {
        SpotifyToken token = findByUserId(userId);
        if (token == null) {
            User user = entityManager.find(User.class, userId);
            if (user == null) {
                return null;
            }
            token = new SpotifyToken();
            token.setUser(user);
            entityManager.persist(token);
            user.setSpotifyToken(token);
        }

        if (refreshToken != null) {
            token.setRefreshToken(refreshToken);
        }
        if (accessToken != null) {
            token.setAccessToken(accessToken);
        }
        if (accessTokenExpiresAt != null) {
            token.setAccessTokenExpiresAt(accessTokenExpiresAt);
        }

        return token;
    }
}
