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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AddressControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private Long createStudent(String email, String number) throws Exception {
        Map<String, Object> body = Map.of(
                "name", "Carlos",
                "phoneNumber", "+5511888887777",
                "emailAddress", email,
                "studentNumber", number
        );
        String created = mockMvc.perform(post("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) objectMapper.readValue(created, Map.class).get("id")).longValue();
    }

    @Test
    void addressLifecycle() throws Exception {
        Long personId = createStudent("addr@example.com", "STU-ADDR-001");

        Map<String, Object> addr = Map.of(
                "street", "Av. Brasil, 1000",
                "city", "Belo Horizonte",
                "state", "MG",
                "zipCode", "30000-000",
                "country", "Brasil"
        );

        String created = mockMvc.perform(post("/api/v1/persons/{pid}/addresses", personId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addr)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.city").value("Belo Horizonte"))
                .andReturn().getResponse().getContentAsString();

        Long addressId = ((Number) objectMapper.readValue(created, Map.class).get("id")).longValue();

        mockMvc.perform(get("/api/v1/persons/{pid}/addresses", personId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].city").value("Belo Horizonte"));

        Map<String, Object> updated = Map.of(
                "street", "Av. Afonso Pena, 500",
                "city", "Belo Horizonte",
                "state", "MG",
                "zipCode", "30100-000",
                "country", "Brasil"
        );
        mockMvc.perform(put("/api/v1/persons/{pid}/addresses/{aid}", personId, addressId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.street").value("Av. Afonso Pena, 500"));

        mockMvc.perform(delete("/api/v1/persons/{pid}/addresses/{aid}", personId, addressId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/persons/{pid}/addresses", personId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void create_addressForMissingPerson_returns404() throws Exception {
        Map<String, Object> addr = Map.of(
                "street", "x", "city", "x", "state", "x", "zipCode", "x", "country", "x"
        );
        mockMvc.perform(post("/api/v1/persons/{pid}/addresses", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addr)))
                .andExpect(status().isNotFound());
    }
}
