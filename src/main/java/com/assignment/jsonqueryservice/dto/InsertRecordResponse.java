package com.assignment.jsonqueryservice.dto;

public record InsertRecordResponse(
        String message,
        String dataset,
        Object recordId
) {}
