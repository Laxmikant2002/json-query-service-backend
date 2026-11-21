package com.assignment.jsonqueryservice.repository;

import com.assignment.jsonqueryservice.entity.DatasetRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DatasetRecordRepository extends JpaRepository<DatasetRecord, Long> {
    List<DatasetRecord> findByDatasetNameOrderByInternalIdAsc(String datasetName);
    boolean existsByDatasetName(String datasetName);
}
