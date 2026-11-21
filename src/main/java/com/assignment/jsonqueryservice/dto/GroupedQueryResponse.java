package com.assignment.jsonqueryservice.dto;

import java.util.List;
import java.util.Map;

public record GroupedQueryResponse(
        Map<String, List<Map<String, Object>>> groupedRecords
) {}
