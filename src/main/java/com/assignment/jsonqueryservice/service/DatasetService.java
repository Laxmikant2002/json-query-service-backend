package com.assignment.jsonqueryservice.service;

import com.assignment.jsonqueryservice.dto.InsertRecordResponse;

import java.util.Map;

public interface DatasetService {
    InsertRecordResponse insertRecord(String datasetName, Map<String, Object> record);
    Object queryDataset(String datasetName, String groupBy, String sortBy, String order);
}
