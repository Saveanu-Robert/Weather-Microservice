package com.weatherspring.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weatherspring.config.TestSecurityConfig;
import com.weatherspring.dto.CreateLocationRequest;
import com.weatherspring.dto.LocationDto;
import com.weatherspring.service.LocationService;

/** Integration tests for LocationController. */
@WebMvcTest(LocationController.class)
@Import({TestSecurityConfig.class, com.weatherspring.exception.GlobalExceptionHandler.class})
@WithMockUser(
    username = "admin",
    roles = {"USER", "ADMIN"})
class LocationControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private LocationService locationService;

  @Test
  void createLocation_WithValidData_ReturnsCreatedStatus() throws Exception {
    // Arrange
    CreateLocationRequest request =
        new CreateLocationRequest("London", "United Kingdom", 51.5074, -0.1278, "Greater London");

    LocationDto responseDto =
        new LocationDto(
            1L, // id
            "London", // name
            "United Kingdom", // country
            51.5074, // latitude
            -0.1278, // longitude
            "Greater London", // region
            null, // createdAt
            null // updatedAt
            );

    when(locationService.createLocation(any(CreateLocationRequest.class))).thenReturn(responseDto);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/locations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.name", is("London")))
        .andExpect(jsonPath("$.country", is("United Kingdom")))
        .andExpect(jsonPath("$.latitude", is(51.5074)))
        .andExpect(jsonPath("$.longitude", is(-0.1278)));

    verify(locationService).createLocation(any(CreateLocationRequest.class));
  }

  @Test
  void createLocation_WithInvalidData_ReturnsBadRequest() throws Exception {
    // Arrange
    CreateLocationRequest invalidRequest = new CreateLocationRequest("", "UK", 100.0, -200.0, null);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/locations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());

    verify(locationService, never()).createLocation(any(CreateLocationRequest.class));
  }

  @Test
  void getLocationById_WhenLocationExists_ReturnsLocation() throws Exception {
    // Arrange
    LocationDto locationDto =
        new LocationDto(
            1L, // id
            "London", // name
            "United Kingdom", // country
            51.5074, // latitude
            -0.1278, // longitude
            null, // region
            null, // createdAt
            null // updatedAt
            );

    when(locationService.getLocationById(1L)).thenReturn(locationDto);

    // Act & Assert
    mockMvc
        .perform(get("/api/locations/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.name", is("London")));

    verify(locationService).getLocationById(1L);
  }

  @Test
  void getAllLocations_ReturnsListOfLocations() throws Exception {
    // Arrange
    List<LocationDto> locations =
        Arrays.asList(
            new LocationDto(1L, "London", "UK", null, null, null, null, null),
            new LocationDto(2L, "Paris", "France", null, null, null, null, null));

    when(locationService.getAllLocations()).thenReturn(locations);

    // Act & Assert
    mockMvc
        .perform(get("/api/locations"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].name", is("London")))
        .andExpect(jsonPath("$[1].name", is("Paris")));

    verify(locationService).getAllLocations();
  }

  @Test
  void searchLocations_ReturnsMatchingLocations() throws Exception {
    // Arrange
    List<LocationDto> locations =
        Arrays.asList(new LocationDto(1L, "London", null, null, null, null, null, null));

    when(locationService.searchLocationsByName("Lon")).thenReturn(locations);

    // Act & Assert
    mockMvc
        .perform(get("/api/locations/search").param("name", "Lon"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].name", is("London")));

    verify(locationService).searchLocationsByName("Lon");
  }

  @Test
  void deleteLocation_WhenLocationExists_ReturnsNoContent() throws Exception {
    // Arrange
    doNothing().when(locationService).deleteLocation(1L);

    // Act & Assert
    mockMvc.perform(delete("/api/locations/1")).andExpect(status().isNoContent());

    verify(locationService).deleteLocation(1L);
  }

  @Test
  void searchLocations_WithBlankName_ReturnsBadRequest() throws Exception {
    // Act & Assert
    mockMvc
        .perform(get("/api/locations/search").param("name", ""))
        .andExpect(status().isBadRequest());

    verify(locationService, never()).searchLocationsByName(any());
  }

  @Test
  void searchLocations_WithNameTooLong_ReturnsBadRequest() throws Exception {
    // Arrange - create a name longer than 100 characters
    String tooLongName = "a".repeat(101);

    // Act & Assert
    mockMvc
        .perform(get("/api/locations/search").param("name", tooLongName))
        .andExpect(status().isBadRequest());

    verify(locationService, never()).searchLocationsByName(any());
  }
}
