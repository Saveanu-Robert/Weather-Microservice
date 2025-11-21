package com.weatherspring.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weatherspring.dto.CreateLocationRequest;
import com.weatherspring.dto.LocationDto;
import com.weatherspring.service.LocationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for LocationController.
 */
@WebMvcTest(LocationController.class)
class LocationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LocationService locationService;

    @Test
    void createLocation_WithValidData_ReturnsCreatedStatus() throws Exception {
        // Arrange
        CreateLocationRequest request = CreateLocationRequest.builder()
                .name("London")
                .country("United Kingdom")
                .latitude(51.5074)
                .longitude(-0.1278)
                .region("Greater London")
                .build();

        LocationDto responseDto = LocationDto.builder()
                .id(1L)
                .name("London")
                .country("United Kingdom")
                .latitude(51.5074)
                .longitude(-0.1278)
                .region("Greater London")
                .build();

        when(locationService.createLocation(any(CreateLocationRequest.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/locations")
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
        CreateLocationRequest invalidRequest = CreateLocationRequest.builder()
                .name("")
                .country("UK")
                .latitude(100.0)
                .longitude(-200.0)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(locationService, never()).createLocation(any(CreateLocationRequest.class));
    }

    @Test
    void getLocationById_WhenLocationExists_ReturnsLocation() throws Exception {
        // Arrange
        LocationDto locationDto = LocationDto.builder()
                .id(1L)
                .name("London")
                .country("United Kingdom")
                .latitude(51.5074)
                .longitude(-0.1278)
                .build();

        when(locationService.getLocationById(1L)).thenReturn(locationDto);

        // Act & Assert
        mockMvc.perform(get("/api/locations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("London")));

        verify(locationService).getLocationById(1L);
    }

    @Test
    void getAllLocations_ReturnsListOfLocations() throws Exception {
        // Arrange
        List<LocationDto> locations = Arrays.asList(
                LocationDto.builder().id(1L).name("London").country("UK").build(),
                LocationDto.builder().id(2L).name("Paris").country("France").build()
        );

        when(locationService.getAllLocations()).thenReturn(locations);

        // Act & Assert
        mockMvc.perform(get("/api/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("London")))
                .andExpect(jsonPath("$[1].name", is("Paris")));

        verify(locationService).getAllLocations();
    }

    @Test
    void searchLocations_ReturnsMatchingLocations() throws Exception {
        // Arrange
        List<LocationDto> locations = Arrays.asList(
                LocationDto.builder().id(1L).name("London").build()
        );

        when(locationService.searchLocationsByName("Lon")).thenReturn(locations);

        // Act & Assert
        mockMvc.perform(get("/api/locations/search")
                        .param("name", "Lon"))
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
        mockMvc.perform(delete("/api/locations/1"))
                .andExpect(status().isNoContent());

        verify(locationService).deleteLocation(1L);
    }
}
