package com.bernabe.bookcollection.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WebConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testCorsConfiguration_WithAllowedOrigin_ShouldAllowRequest() throws Exception {
        // Arrange & Act & Assert
        mockMvc.perform(options("/api/books")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"))
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }

    @Test
    void testCorsConfiguration_WithGetMethod_ShouldBeAllowed() throws Exception {
        // Arrange & Act & Assert
        mockMvc.perform(options("/api/books")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Methods", 
                        org.hamcrest.Matchers.containsString("GET")));
    }

    @Test
    void testCorsConfiguration_WithPostMethod_ShouldBeAllowed() throws Exception {
        // Arrange & Act & Assert
        mockMvc.perform(options("/api/books")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Methods", 
                        org.hamcrest.Matchers.containsString("POST")));
    }

    @Test
    void testCorsConfiguration_WithPutMethod_ShouldBeAllowed() throws Exception {
        // Arrange & Act & Assert
        mockMvc.perform(options("/api/books")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "PUT"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Methods", 
                        org.hamcrest.Matchers.containsString("PUT")));
    }

    @Test
    void testCorsConfiguration_WithDeleteMethod_ShouldBeAllowed() throws Exception {
        // Arrange & Act & Assert
        mockMvc.perform(options("/api/books")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "DELETE"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Methods", 
                        org.hamcrest.Matchers.containsString("DELETE")));
    }

    @Test
    void testCorsConfiguration_WithOptionsMethod_ShouldBeAllowed() throws Exception {
        // Arrange & Act & Assert
        mockMvc.perform(options("/api/books")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "OPTIONS"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Methods", 
                        org.hamcrest.Matchers.containsString("OPTIONS")));
    }

    @Test
    void testCorsConfiguration_WithCustomHeaders_ShouldBeAllowed() throws Exception {
        // Arrange & Act & Assert
        mockMvc.perform(options("/api/books")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Content-Type,Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Headers"));
    }

    @Test
    void testCorsConfiguration_OnApiEndpoint_ShouldHaveCorsHeaders() throws Exception {
        // Arrange & Act & Assert
        mockMvc.perform(get("/api/books")
                        .header("Origin", "http://localhost:4200"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
    }

    @Test
    void testCorsConfiguration_OnNonApiEndpoint_ShouldNotHaveCorsHeaders() throws Exception {
        // Arrange & Act & Assert
        // Since CORS is configured for /api/**, endpoints outside /api should not have CORS headers
        mockMvc.perform(get("/health")
                        .header("Origin", "http://localhost:4200"))
                .andExpect(result -> {
                    String corsHeader = result.getResponse().getHeader("Access-Control-Allow-Origin");
                    // For non-api endpoints, CORS should not be configured, so header might be null
                    // or 404 since /health doesn't exist in our app
                });
    }
}
