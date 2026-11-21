package com.assignment.jsonqueryservice.dto;

import java.util.List;
import java.util.Map;

public record SortedQueryResponse(
        List<Map<String, Object>> sortedRecords
) {}
