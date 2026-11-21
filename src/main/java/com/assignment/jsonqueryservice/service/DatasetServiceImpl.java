package com.assignment.jsonqueryservice.service;

import com.assignment.jsonqueryservice.dto.GroupedQueryResponse;
import com.assignment.jsonqueryservice.dto.InsertRecordResponse;
import com.assignment.jsonqueryservice.dto.SortedQueryResponse;
import com.assignment.jsonqueryservice.entity.DatasetRecord;
import com.assignment.jsonqueryservice.exception.DatasetNotFoundException;
import com.assignment.jsonqueryservice.exception.InvalidQueryException;
import com.assignment.jsonqueryservice.repository.DatasetRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DatasetServiceImpl implements DatasetService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE_REF =
            new TypeReference<>() {};

    private final DatasetRecordRepository repository;
    private final ObjectMapper objectMapper;

    public DatasetServiceImpl(DatasetRecordRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public InsertRecordResponse insertRecord(String datasetName, Map<String, Object> recordPayload) {
        validateDatasetName(datasetName);

        if (recordPayload == null || recordPayload.isEmpty()) {
            throw new InvalidQueryException("Request body must be a non-empty JSON object.");
        }

        Object rawId = recordPayload.get("id");
        String recordIdStr = rawId != null ? String.valueOf(rawId) : null;
        String serializedJson = objectMapper.writeValueAsString(recordPayload);

        DatasetRecord saved = repository.save(
                new DatasetRecord(datasetName.trim(), recordIdStr, serializedJson));

        Object responseRecordId = rawId != null ? rawId : saved.getInternalId();
        return new InsertRecordResponse(
                "Record added successfully",
                datasetName.trim(),
                responseRecordId
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Object queryDataset(String datasetName, String groupBy, String sortBy, String order) {
        validateDatasetName(datasetName);

        boolean hasGroupBy = groupBy != null && !groupBy.isBlank();
        boolean hasSortBy = sortBy != null && !sortBy.isBlank();

        if (!hasGroupBy && !hasSortBy) {
            throw new InvalidQueryException(
                    "At least one query operation parameter ('groupBy' or 'sortBy') must be provided.");
        }
        if (hasGroupBy && hasSortBy) {
            throw new InvalidQueryException(
                    "Provide either 'groupBy' or 'sortBy' per query request, not both simultaneously.");
        }

        List<DatasetRecord> entities =
                repository.findByDatasetNameOrderByInternalIdAsc(datasetName.trim());
        if (entities.isEmpty()) {
            throw new DatasetNotFoundException("No records found for dataset: " + datasetName);
        }

        List<Map<String, Object>> records = entities.stream()
                .map(entity -> objectMapper.readValue(
                        entity.getJsonPayload(), MAP_TYPE_REF))
                .toList();

        if (hasGroupBy) {
            return performGroupBy(records, groupBy.trim());
        }
        return performSortBy(records, sortBy.trim(), order);
    }

    private GroupedQueryResponse performGroupBy(
            List<Map<String, Object>> records, String groupByField) {
        Map<String, List<Map<String, Object>>> grouped = records.stream()
                .collect(Collectors.groupingBy(
                        record -> {
                            Object value = record.get(groupByField);
                            return value != null ? String.valueOf(value) : "UNKNOWN";
                        },
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        return new GroupedQueryResponse(grouped);
    }

    private SortedQueryResponse performSortBy(
            List<Map<String, Object>> records, String sortByField, String order) {
        String normalizedOrder = order == null || order.isBlank()
                ? "asc"
                : order.trim().toLowerCase(Locale.ROOT);

        if (!normalizedOrder.equals("asc") && !normalizedOrder.equals("desc")) {
            throw new InvalidQueryException(
                    "Invalid 'order' parameter: '" + order
                            + "'. Allowed values are 'asc' or 'desc'.");
        }

        Comparator<Map<String, Object>> comparator =
                (left, right) -> compareFieldValues(
                        left.get(sortByField), right.get(sortByField));
        if ("desc".equals(normalizedOrder)) {
            comparator = comparator.reversed();
        }

        List<Map<String, Object>> sorted = records.stream()
                .sorted(comparator)
                .toList();
        return new SortedQueryResponse(sorted);
    }

    private int compareFieldValues(Object left, Object right) {
        if (left == null && right == null) return 0;
        if (left == null) return 1;
        if (right == null) return -1;

        if (left instanceof Number leftNumber && right instanceof Number rightNumber) {
            return new BigDecimal(leftNumber.toString())
                    .compareTo(new BigDecimal(rightNumber.toString()));
        }
        if (left instanceof Boolean leftBoolean && right instanceof Boolean rightBoolean) {
            return Boolean.compare(leftBoolean, rightBoolean);
        }
        return String.valueOf(left).compareToIgnoreCase(String.valueOf(right));
    }

    private void validateDatasetName(String datasetName) {
        if (datasetName == null || datasetName.isBlank()) {
            throw new InvalidQueryException("Dataset name must not be blank.");
        }
    }
}
