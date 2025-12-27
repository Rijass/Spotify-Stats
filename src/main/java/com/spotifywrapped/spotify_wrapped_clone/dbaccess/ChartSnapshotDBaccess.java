package com.spotifywrapped.spotify_wrapped_clone.dbaccess;

import com.spotifywrapped.spotify_wrapped_clone.dbaccess.entities.ChartEntry;
import com.spotifywrapped.spotify_wrapped_clone.dbaccess.entities.ChartSnapshot;
import com.spotifywrapped.spotify_wrapped_clone.dbaccess.entities.Song;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@Transactional
public class ChartSnapshotDBaccess {

    @PersistenceContext
    private EntityManager entityManager;

    public ChartSnapshot findByChartKeyAndDate(String chartKey, LocalDate chartDate) {
        return entityManager.createQuery(
                        "SELECT cs FROM ChartSnapshot cs WHERE cs.chartKey = :chartKey AND cs.chartDate = :chartDate",
                        ChartSnapshot.class)
                .setParameter("chartKey", chartKey)
                .setParameter("chartDate", chartDate)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public ChartSnapshot findLatestSnapshot(String chartKey) {
        return entityManager.createQuery(
                        "SELECT cs FROM ChartSnapshot cs WHERE cs.chartKey = :chartKey ORDER BY cs.chartDate DESC",
                        ChartSnapshot.class)
                .setParameter("chartKey", chartKey)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public ChartSnapshot createSnapshot(String chartKey, LocalDate chartDate) {
        ChartSnapshot snapshot = new ChartSnapshot();
        snapshot.setChartKey(chartKey);
        snapshot.setChartDate(chartDate);
        entityManager.persist(snapshot);
        return snapshot;
    }

    public ChartEntry addEntry(ChartSnapshot snapshot, Song song, int position) {
        ChartEntry entry = new ChartEntry();
        entry.setSnapshot(snapshot);
        entry.setSong(song);
        entry.setPosition(position);
        entityManager.persist(entry);
        return entry;
    }

    public void deleteEntriesForSnapshot(Long snapshotId) {
        entityManager.createQuery("DELETE FROM ChartEntry ce WHERE ce.snapshot.id = :snapshotId")
                .setParameter("snapshotId", snapshotId)
                .executeUpdate();
    }

    public List<ChartEntry> findEntriesForSnapshot(Long snapshotId, Integer limit) {
        var query = entityManager.createQuery(
                        "SELECT ce FROM ChartEntry ce " +
                                "JOIN FETCH ce.song s " +
                                "WHERE ce.snapshot.id = :snapshotId " +
                                "ORDER BY ce.position ASC",
                        ChartEntry.class)
                .setParameter("snapshotId", snapshotId);

        if (limit != null) {
            query.setMaxResults(limit);
        }

        return query.getResultList();
    }

    public long countEntriesForSnapshot(Long snapshotId) {
        return entityManager.createQuery(
                        "SELECT COUNT(ce) FROM ChartEntry ce WHERE ce.snapshot.id = :snapshotId",
                        Long.class)
                .setParameter("snapshotId", snapshotId)
                .getSingleResult();
    }
}