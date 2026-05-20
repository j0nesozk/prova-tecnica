package br.com.academico.crud.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StudentControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private Map<String, Object> studentPayload(String email, String number) {
        return Map.of(
                "name", "Maria Silva",
                "phoneNumber", "+5511999998888",
                "emailAddress", email,
                "studentNumber", number,
                "photo", "https://cdn.example.com/p/1.jpg",
                "addresses", List.of(Map.of(
                        "street", "Rua A, 100",
                        "city", "São Paulo",
                        "state", "SP",
                        "zipCode", "01234-567",
                        "country", "Brasil"
                ))
        );
    }

    @Test
    void fullCrudFlow() throws Exception {
        String body = objectMapper.writeValueAsString(studentPayload("flow@example.com", "STU-FLOW-001"));

        // CREATE
        String created = mockMvc.perform(post("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.addresses[0].city").value("São Paulo"))
                .andReturn().getResponse().getContentAsString();

        Number id = (Number) objectMapper.readValue(created, Map.class).get("id");

        // READ
        mockMvc.perform(get("/api/v1/students/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailAddress").value("flow@example.com"));

        // UPDATE
        Map<String, Object> updated = new HashMap<>();
        updated.put("name", "Maria Santos");
        updated.put("phoneNumber", "+5511999998888");
        updated.put("emailAddress", "flow@example.com");
        updated.put("studentNumber", "STU-FLOW-001");
        updated.put("photo", null);
        updated.put("addresses", List.of());
        mockMvc.perform(put("/api/v1/students/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Maria Santos"))
                .andExpect(jsonPath("$.addresses").isEmpty());

        // SOFT DELETE
        mockMvc.perform(delete("/api/v1/students/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/students/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISABLE"));

        // RESTORE
        mockMvc.perform(post("/api/v1/students/{id}/restore", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void create_invalidEmail_returns400() throws Exception {
        Map<String, Object> bad = Map.of(
                "name", "X",
                "phoneNumber", "+5511999998888",
                "emailAddress", "not-an-email",
                "studentNumber", "STU-BAD-001"
        );
        mockMvc.perform(post("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void create_duplicateEmail_returns409() throws Exception {
        String body = objectMapper.writeValueAsString(studentPayload("dup@example.com", "STU-DUP-001"));
        mockMvc.perform(post("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        String body2 = objectMapper.writeValueAsString(studentPayload("dup@example.com", "STU-DUP-002"));
        mockMvc.perform(post("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body2))
                .andExpect(status().isConflict());
    }

    @Test
    void findById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/students/{id}", 999999L))
                .andExpect(status().isNotFound());
    }
}
