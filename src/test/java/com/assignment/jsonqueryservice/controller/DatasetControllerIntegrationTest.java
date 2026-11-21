package com.assignment.jsonqueryservice.controller;

import com.assignment.jsonqueryservice.repository.DatasetRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DatasetControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DatasetRecordRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void endToEndInsertGroupByAndSortByWorkflow() throws Exception {
        String record1 =
                "{ \"id\": 1, \"name\": \"John Doe\", \"age\": 30, \"department\": \"Engineering\" }";
        String record2 =
                "{ \"id\": 2, \"name\": \"Jane Smith\", \"age\": 25, \"department\": \"Engineering\" }";
        String record3 =
                "{ \"id\": 3, \"name\": \"Alice Brown\", \"age\": 28, \"department\": \"Marketing\" }";

        mockMvc.perform(post("/api/dataset/employee_dataset/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(record1))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Record added successfully"))
                .andExpect(jsonPath("$.dataset").value("employee_dataset"))
                .andExpect(jsonPath("$.recordId").value(1));

        mockMvc.perform(post("/api/dataset/employee_dataset/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(record2))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/dataset/employee_dataset/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(record3))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/dataset/employee_dataset/query")
                        .param("groupBy", "department"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupedRecords.Engineering", hasSize(2)))
                .andExpect(jsonPath("$.groupedRecords.Marketing", hasSize(1)));

        mockMvc.perform(get("/api/dataset/employee_dataset/query")
                        .param("sortBy", "age")
                        .param("order", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sortedRecords", hasSize(3)))
                .andExpect(jsonPath("$.sortedRecords[0].name").value("Jane Smith"))
                .andExpect(jsonPath("$.sortedRecords[1].name").value("Alice Brown"))
                .andExpect(jsonPath("$.sortedRecords[2].name").value("John Doe"));
    }

    @Test
    void queryDatasetMissingParametersReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/dataset/employee_dataset/query"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void queryDatasetUnknownDatasetReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/dataset/non_existent/query")
                        .param("groupBy", "department"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}
