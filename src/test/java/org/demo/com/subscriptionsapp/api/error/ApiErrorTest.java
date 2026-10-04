package org.demo.com.subscriptionsapp.api.error;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiErrorTest {

    @Test
    void notFoundCarriesTheEntityAndId() {
        NotFoundException exception = NotFoundException.of("User", 9L);

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found: 9", exception.getReason());
    }

    @Test
    void badRequestCarriesTheSuppliedReason() {
        BadRequestException exception = new BadRequestException("User is not eligible for tier SILVER");

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("User is not eligible for tier SILVER", exception.getReason());
    }
}
