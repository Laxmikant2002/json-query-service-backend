package com.assignment.jsonqueryservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(
    name = "dataset_records",
    indexes = @Index(name = "idx_dataset_name", columnList = "dataset_name")
)
public class DatasetRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long internalId;

    @Column(name = "dataset_name", nullable = false, length = 100)
    private String datasetName;

    @Column(name = "record_id", length = 100)
    private String recordId;

    @Column(name = "json_payload", nullable = false, columnDefinition = "TEXT")
    private String jsonPayload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected DatasetRecord() {}

    public DatasetRecord(String datasetName, String recordId, String jsonPayload) {
        this.datasetName = datasetName;
        this.recordId = recordId;
        this.jsonPayload = jsonPayload;
        this.createdAt = Instant.now();
    }

    public Long getInternalId() { return internalId; }
    public String getDatasetName() { return datasetName; }
    public String getRecordId() { return recordId; }
    public String getJsonPayload() { return jsonPayload; }
    public Instant getCreatedAt() { return createdAt; }
}
