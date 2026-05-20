package br.com.academico.crud.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProfessorControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void createReadDeleteFlow() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "Dr. João",
                "phoneNumber", "+5511777776666",
                "emailAddress", "joao.prof@example.com",
                "salary", 8500.00
        );

        String created = mockMvc.perform(post("/api/v1/professors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.salary").value(8500.00))
                .andReturn().getResponse().getContentAsString();

        Number id = (Number) objectMapper.readValue(created, Map.class).get("id");

        mockMvc.perform(get("/api/v1/professors/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(delete("/api/v1/professors/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/professors/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISABLE"));
    }

    @Test
    void createWithNegativeSalary_returns400() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "Dr. João",
                "phoneNumber", "+5511777776666",
                "emailAddress", "neg@example.com",
                "salary", -100.00
        );
        mockMvc.perform(post("/api/v1/professors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }
}
