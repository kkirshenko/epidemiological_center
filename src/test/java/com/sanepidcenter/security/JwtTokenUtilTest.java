package com.sanepidcenter.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenUtilTest {

    private JwtTokenUtil jwtTokenUtil;

    @BeforeEach
    void setUp() throws Exception {
        jwtTokenUtil = new JwtTokenUtil();
        setField("secret", "U2FuRXBpZENlbnRlclNlY3JldEtleUZvckpXVFRva2VuR2VuZXJhdGlvbkFuZFZhbGlkYXRpb24yMDI0VmVyeUxvbmdTZWNyZXRLZXk=");
        setField("expiration", 86400000L);
    }

    @Test
    void generateToken_ShouldStoreNormalizedRole() {
        String token = jwtTokenUtil.generateToken("admin1", "admin");

        assertEquals("admin1", jwtTokenUtil.extractUsername(token));
        assertEquals("ROLE_ADMIN", jwtTokenUtil.extractRole(token));
        assertEquals(List.of("ROLE_ADMIN"), jwtTokenUtil.extractRoles(token));
    }

    @Test
    void normalizeRole_ShouldHandleRawAndPrefixedRoles() {
        assertEquals("ROLE_ADMIN", jwtTokenUtil.normalizeRole("admin"));
        assertEquals("ROLE_ADMIN", jwtTokenUtil.normalizeRole("ROLE_ADMIN"));
        assertEquals("ROLE_USER", jwtTokenUtil.normalizeRole(null));
    }

    private void setField(String name, Object value) throws Exception {
        Field field = JwtTokenUtil.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(jwtTokenUtil, value);
    }
}
