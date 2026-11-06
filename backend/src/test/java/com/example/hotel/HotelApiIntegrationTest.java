package com.example.hotel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HotelApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void customerAndStaffBookingFlow() throws Exception {
        mvc.perform(get("/api/staff/customers")).andExpect(status().is4xxClientError());
        JsonNode publicRooms = read(mvc.perform(get("/api/rooms")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        long roomId = publicRooms.path("content").get(0).path("id").asLong();
        long secondRoomId = publicRooms.path("content").get(1).path("id").asLong();
        assertThat(publicRooms.path("totalElements").asInt()).isPositive();

        String email = "guest@example.com";
        JsonNode createdCustomer = read(mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("customerFullName", "Test Guest",
                        "telephone", "0901234567", "emailAddress", email, "password", "Guest@12345"))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        long customerId = createdCustomer.path("id").asLong();
        String customerToken = login(email, "Guest@12345");
        String staffToken = login("staff@hotel.local", "Staff@12345");
        mvc.perform(get("/api/staff/customers").header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/customers/me").header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(customerId));

        LocalDate start = LocalDate.now().plusDays(2);
        LocalDate end = start.plusDays(3);
        String createBody = json.writeValueAsString(Map.of("rooms", new Object[] {
                Map.of("roomId", roomId, "startDate", start.toString(), "endDate", end.toString())}));
        JsonNode createdBooking = read(mvc.perform(post("/api/bookings/me")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON).content(createBody))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        long bookingId = createdBooking.path("id").asLong();
        assertThat(createdBooking.path("totalPrice").decimalValue()).isPositive();
        assertThat(createdBooking.path("details").size()).isEqualTo(1);

        mvc.perform(post("/api/bookings/me").header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON).content(createBody))
                .andExpect(status().isConflict());

        LocalDate groupStart = end.plusDays(10);
        LocalDate groupEnd = groupStart.plusDays(2);
        String multiRoomBody = json.writeValueAsString(Map.of("rooms", new Object[] {
                Map.of("roomId", roomId, "startDate", groupStart.toString(), "endDate", groupEnd.toString()),
                Map.of("roomId", secondRoomId, "startDate", groupStart.toString(), "endDate", groupEnd.toString())}));
        JsonNode groupBooking = read(mvc.perform(post("/api/bookings/me")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON).content(multiRoomBody))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        assertThat(groupBooking.path("details").size()).isEqualTo(2);
        assertThat(groupBooking.path("totalPrice").decimalValue()).isEqualByComparingTo(
                groupBooking.path("details").get(0).path("actualPrice").decimalValue()
                        .add(groupBooking.path("details").get(1).path("actualPrice").decimalValue()));

        mvc.perform(put("/api/staff/bookings/{id}", bookingId)
                .header("Authorization", "Bearer " + staffToken)
                .contentType(MediaType.APPLICATION_JSON).content("{\"bookingStatus\":\"CONFIRMED\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.bookingStatus").value("CONFIRMED"));

        LocalDate movedStart = end.plusDays(2);
        LocalDate movedEnd = movedStart.plusDays(2);
        String moveBody = json.writeValueAsString(Map.of("rooms", new Object[] {
                Map.of("roomId", roomId, "startDate", movedStart.toString(), "endDate", movedEnd.toString())}));
        mvc.perform(put("/api/staff/bookings/{id}", bookingId)
                .header("Authorization", "Bearer " + staffToken)
                .contentType(MediaType.APPLICATION_JSON).content(moveBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.details[0].startDate").value(movedStart.toString()));

        mvc.perform(delete("/api/staff/rooms/{id}", roomId).header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/rooms/{id}", roomId)).andExpect(status().isNotFound());
        mvc.perform(get("/api/staff/rooms/{id}", roomId).header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.roomStatus").value("INACTIVE"));

        String disable = json.writeValueAsString(Map.of("customerFullName", "Test Guest",
                "telephone", "0901234567", "emailAddress", email, "customerStatus", "INACTIVE"));
        mvc.perform(put("/api/staff/customers/{id}", customerId).header("Authorization", "Bearer " + staffToken)
                .contentType(MediaType.APPLICATION_JSON).content(disable)).andExpect(status().isOk());
        mvc.perform(get("/api/customers/me").header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    private String login(String email, String password) throws Exception {
        String response = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return read(response).path("token").asText();
    }

    private JsonNode read(String body) throws Exception { return json.readTree(body); }
}
