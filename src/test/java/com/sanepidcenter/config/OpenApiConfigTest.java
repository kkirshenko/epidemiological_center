package com.sanepidcenter.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiConfigTest {

    @Test
    void securitySchemeAnnotation_ShouldHaveExpectedValues() {
        SecurityScheme scheme = OpenApiConfig.class.getAnnotation(SecurityScheme.class);
        assertNotNull(scheme);
        assertEquals("bearerAuth", scheme.name());
        assertEquals(SecuritySchemeType.HTTP, scheme.type());
        assertEquals("bearer", scheme.scheme());
        assertEquals("JWT", scheme.bearerFormat());
        assertEquals(SecuritySchemeIn.HEADER, scheme.in());
    }
}
