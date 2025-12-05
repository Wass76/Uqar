package com.Uqar.test;

import com.Uqar.utils.restExceptionHanding.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TokenExpiredResponseTest {
    
    @Test
    public void testTokenExpiredResponseFormat() throws Exception {
        // Simulate the response format from the filter using ApiException
        ApiException apiException = new ApiException(
                "Token expired",
                HttpStatus.UNAUTHORIZED,
                LocalDateTime.now()
        );
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String jsonResponse = objectMapper.writeValueAsString(apiException);
        
        // Verify the response contains expected fields
        assertTrue(jsonResponse.contains("Token expired"));
        assertTrue(jsonResponse.contains("UNAUTHORIZED"));
        assertTrue(jsonResponse.contains("localDateTime"));
        
        // Verify it's valid JSON and can be parsed back
        ApiException parsedResponse = objectMapper.readValue(jsonResponse, ApiException.class);
        assertEquals("Token expired", parsedResponse.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, parsedResponse.getStatus());
        assertNotNull(parsedResponse.getLocalDateTime());
    }
}
