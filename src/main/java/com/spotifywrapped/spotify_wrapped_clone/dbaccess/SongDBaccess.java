package com.spotifywrapped.spotify_wrapped_clone.dbaccess;

import com.spotifywrapped.spotify_wrapped_clone.dbaccess.entities.Song;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public class SongDBaccess {

    @PersistenceContext
    private EntityManager entityManager;

    public Song findByProviderTrackId(String providerTrackId) {
        return entityManager.createQuery(
                        "SELECT s FROM Song s WHERE s.providerTrackId = :providerTrackId",
                        Song.class)
                .setParameter("providerTrackId", providerTrackId)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public Song saveOrUpdateSong(String providerTrackId, String artist, String title) {
        Song song = findByProviderTrackId(providerTrackId);
        if (song == null) {
            song = new Song();
            song.setProviderTrackId(providerTrackId);
            song.setArtist(artist);
            song.setTitle(title);
            entityManager.persist(song);
            return song;
        }

        song.setArtist(artist);
        song.setTitle(title);
        return song;
    }
}
