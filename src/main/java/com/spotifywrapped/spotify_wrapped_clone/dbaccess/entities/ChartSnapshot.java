package com.spotifywrapped.spotify_wrapped_clone.dbaccess.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chart_snapshots", uniqueConstraints = {
        @UniqueConstraint(name = "uq_chart_key_date", columnNames = {"chart_key", "chart_date"})
})
public class ChartSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "chart_key", nullable = false, length = 100)
    private String chartKey;

    @Column(name = "chart_date", nullable = false)
    private LocalDate chartDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "snapshot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChartEntry> entries = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChartKey() {
        return chartKey;
    }

    public void setChartKey(String chartKey) {
        this.chartKey = chartKey;
    }

    public LocalDate getChartDate() {
        return chartDate;
    }

    public void setChartDate(LocalDate chartDate) {
        this.chartDate = chartDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<ChartEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<ChartEntry> entries) {
        this.entries = entries;
    }
}
