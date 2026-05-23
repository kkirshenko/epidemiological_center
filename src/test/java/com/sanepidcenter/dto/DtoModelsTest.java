package com.sanepidcenter.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DtoModelsTest {

    @Test
    void adminUserCreateRequest_DefaultAndSettersShouldWork() {
        AdminUserCreateRequest request = new AdminUserCreateRequest();

        assertTrue(request.getIsActive());

        request.setUsername("admin");
        request.setPassword("secret");
        request.setFullName("Admin User");
        request.setPhone("+1 555 123 4567");
        request.setPosition("Lead");
        request.setRole("ROLE_ADMIN");
        request.setIsActive(false);

        assertEquals("admin", request.getUsername());
        assertEquals("secret", request.getPassword());
        assertEquals("Admin User", request.getFullName());
        assertEquals("+1 555 123 4567", request.getPhone());
        assertEquals("Lead", request.getPosition());
        assertEquals("ROLE_ADMIN", request.getRole());
        assertFalse(request.getIsActive());
    }

    @Test
    void adminUserUpdateRequest_DefaultAndSettersShouldWork() {
        AdminUserUpdateRequest request = new AdminUserUpdateRequest();

        assertTrue(request.getIsActive());

        request.setFullName("Updated User");
        request.setPhone("+1 555 765 4321");
        request.setPosition("Inspector");
        request.setRole("ROLE_INSPECTOR");
        request.setIsActive(false);

        assertEquals("Updated User", request.getFullName());
        assertEquals("+1 555 765 4321", request.getPhone());
        assertEquals("Inspector", request.getPosition());
        assertEquals("ROLE_INSPECTOR", request.getRole());
        assertFalse(request.getIsActive());
    }

    @Test
    void authAndLoginRequests_BuilderAndConstructorsShouldWork() {
        AuthResponse response = AuthResponse.builder()
                .token("jwt")
                .username("john")
                .role("ROLE_USER")
                .fullName("John Doe")
                .build();

        assertEquals("jwt", response.getToken());
        assertEquals("john", response.getUsername());

        LoginRequest loginRequest = new LoginRequest("john", "pass");
        assertEquals("john", loginRequest.getUsername());

        loginRequest.setPassword("new-pass");
        assertEquals("new-pass", loginRequest.getPassword());

        RegisterRequest registerRequest = new RegisterRequest();
        assertEquals("ROLE_INSPECTOR", registerRequest.getRole());
        registerRequest.setUsername("new-user");
        assertEquals("new-user", registerRequest.getUsername());
    }

    @Test
    void organizationAndProfileDtos_ConstructorsEqualsAndHashCodeShouldWork() {
        OrganizationTypeDto typeA = new OrganizationTypeDto(1, "Factory", "desc");
        OrganizationTypeDto typeB = new OrganizationTypeDto(1, "Factory", "desc");

        assertEquals(typeA, typeB);
        assertEquals(typeA.hashCode(), typeB.hashCode());

        UUID id = UUID.randomUUID();
        OrganizationDto org = new OrganizationDto(id, "Org", "O", "123", typeA,
                "Address", "City", "Director", "+1 555 111 2222", "a@b.com", 10, "HIGH", "note");

        assertEquals(id, org.getId());
        assertEquals("Org", org.getName());

        LocalDateTime now = LocalDateTime.now();
        ProfileDto profile = new ProfileDto(id, "user", "User Name", "+1", "Pos", "ROLE_USER", true, now, now);
        assertEquals("user", profile.getUsername());
        assertTrue(profile.getIsActive());
    }

    @Test
    void apiDtos_BuildersShouldPopulateAllFields() {
        UUID id = UUID.randomUUID();
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        InspectionApiDto inspection = InspectionApiDto.builder()
                .id(id)
                .organizationId(id)
                .organizationName("Org")
                .typeId(1)
                .typeName("Type")
                .inspectorId(id)
                .inspectorName("Inspector")
                .scheduledDate(today)
                .startDate(today)
                .endDate(today)
                .status("SCHEDULED")
                .result("OK")
                .findingsSummary("None")
                .recommendations("N/A")
                .actNumber("ACT-1")
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertEquals("ACT-1", inspection.getActNumber());
        assertEquals("SCHEDULED", inspection.getStatus());

        ViolationApiDto violation = ViolationApiDto.builder()
                .id(id)
                .inspectionId(id)
                .inspectionActNumber("ACT-1")
                .organizationId(id)
                .organizationName("Org")
                .code("V-001")
                .description("Desc")
                .severity("HIGH")
                .articleReference("Art 1")
                .correctionDeadline(today)
                .resolved(false)
                .resolutionNotes("Pending")
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertEquals("V-001", violation.getCode());
        assertFalse(violation.getResolved());
    }
}
