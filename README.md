# JSON Query Service

A RESTful Spring Boot application that stores dynamic JSON records in an H2 database and supports dynamic group-by and sort-by queries.

## Tech Stack

- Java 25
- Spring Boot 4.1.2
- Jackson 3
- H2
- JUnit 5 and MockMvc

## Run

```bash
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

The API runs at `http://localhost:8080`.

## Insert a Record

```bash
curl -X POST "http://localhost:8080/api/dataset/employee_dataset/record" \
  -H "Content-Type: application/json" \
  -d '{"id":1,"name":"John Doe","age":30,"department":"Engineering"}'
```

## Group Records

```bash
curl "http://localhost:8080/api/dataset/employee_dataset/query?groupBy=department"
```

## Sort Records

```bash
curl "http://localhost:8080/api/dataset/employee_dataset/query?sortBy=age&order=asc"
```
