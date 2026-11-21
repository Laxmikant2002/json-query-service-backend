package com.assignment.jsonqueryservice.service;

import com.assignment.jsonqueryservice.dto.GroupedQueryResponse;
import com.assignment.jsonqueryservice.dto.InsertRecordResponse;
import com.assignment.jsonqueryservice.dto.SortedQueryResponse;
import com.assignment.jsonqueryservice.entity.DatasetRecord;
import com.assignment.jsonqueryservice.exception.DatasetNotFoundException;
import com.assignment.jsonqueryservice.exception.InvalidQueryException;
import com.assignment.jsonqueryservice.repository.DatasetRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatasetServiceImplTest {

    @Mock
    private DatasetRecordRepository repository;

    private ObjectMapper objectMapper;
    private DatasetServiceImpl datasetService;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().build();
        datasetService = new DatasetServiceImpl(repository, objectMapper);
    }

    @Test
    void insertRecordSuccess() {
        Map<String, Object> payload = Map.of(
                "id", 1,
                "name", "John Doe",
                "age", 30,
                "department", "Engineering");

        when(repository.save(any(DatasetRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InsertRecordResponse response =
                datasetService.insertRecord("employee_dataset", payload);

        assertEquals("Record added successfully", response.message());
        assertEquals("employee_dataset", response.dataset());
        assertEquals(1, response.recordId());
    }

    @Test
    void insertRecordEmptyPayloadThrowsInvalidQueryException() {
        assertThrows(InvalidQueryException.class,
                () -> datasetService.insertRecord("employee_dataset", Map.of()));
    }

    @Test
    void queryDatasetGroupByDepartmentSuccess() {
        List<DatasetRecord> records = employeeRecords();
        when(repository.findByDatasetNameOrderByInternalIdAsc("employee_dataset"))
                .thenReturn(records);

        Object result =
                datasetService.queryDataset("employee_dataset", "department", null, null);

        assertInstanceOf(GroupedQueryResponse.class, result);
        GroupedQueryResponse grouped = (GroupedQueryResponse) result;
        assertEquals(2, grouped.groupedRecords().size());
        assertEquals(2, grouped.groupedRecords().get("Engineering").size());
        assertEquals(1, grouped.groupedRecords().get("Marketing").size());
    }

    @Test
    void queryDatasetSortByAgeAscAndDescSuccess() {
        when(repository.findByDatasetNameOrderByInternalIdAsc("employee_dataset"))
                .thenReturn(employeeRecords());

        SortedQueryResponse ascResult = (SortedQueryResponse)
                datasetService.queryDataset("employee_dataset", null, "age", "asc");
        assertEquals(25, ascResult.sortedRecords().get(0).get("age"));
        assertEquals(28, ascResult.sortedRecords().get(1).get("age"));
        assertEquals(30, ascResult.sortedRecords().get(2).get("age"));

        SortedQueryResponse descResult = (SortedQueryResponse)
                datasetService.queryDataset("employee_dataset", null, "age", "desc");
        assertEquals(30, descResult.sortedRecords().get(0).get("age"));
        assertEquals(28, descResult.sortedRecords().get(1).get("age"));
        assertEquals(25, descResult.sortedRecords().get(2).get("age"));
    }

    @Test
    void queryDatasetInvalidOrderThrowsInvalidQueryException() {
        List<DatasetRecord> records = List.of(
                new DatasetRecord("employee_dataset", "1", "{\"id\":1,\"age\":30}")
        );
        when(repository.findByDatasetNameOrderByInternalIdAsc("employee_dataset"))
                .thenReturn(records);

        assertThrows(InvalidQueryException.class,
                () -> datasetService.queryDataset(
                        "employee_dataset", null, "age", "random"));
    }

    @Test
    void queryDatasetMissingDatasetThrowsDatasetNotFoundException() {
        when(repository.findByDatasetNameOrderByInternalIdAsc("unknown_ds"))
                .thenReturn(List.of());

        assertThrows(DatasetNotFoundException.class,
                () -> datasetService.queryDataset(
                        "unknown_ds", "department", null, null));
    }

    private List<DatasetRecord> employeeRecords() {
        return List.of(
                new DatasetRecord("employee_dataset", "1",
                        "{\"id\":1,\"name\":\"John Doe\",\"age\":30,\"department\":\"Engineering\"}"),
                new DatasetRecord("employee_dataset", "2",
                        "{\"id\":2,\"name\":\"Jane Smith\",\"age\":25,\"department\":\"Engineering\"}"),
                new DatasetRecord("employee_dataset", "3",
                        "{\"id\":3,\"name\":\"Alice Brown\",\"age\":28,\"department\":\"Marketing\"}")
        );
    }
}
