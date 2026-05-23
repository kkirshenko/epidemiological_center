package com.sanepidcenter.model;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EntityDefaultsAndLifecycleTest {

    @Test
    void profile_DefaultsAndEnsureId_ShouldWork() throws Exception {
        Profile profile = Profile.builder()
                .username("user")
                .password("pass")
                .fullName("Test User")
                .phone("123")
                .position("Inspector")
                .build();

        assertEquals("ROLE_INSPECTOR", profile.getRole());
        assertTrue(profile.getIsActive());
        assertNull(profile.getId());

        invokeEnsureId(profile, Profile.class);
        assertNotNull(profile.getId());

        UUID existing = profile.getId();
        invokeEnsureId(profile, Profile.class);
        assertEquals(existing, profile.getId());
    }

    @Test
    void organizationInspectionViolation_DefaultsAndEnsureId_ShouldWork() throws Exception {
        OrganizationType orgType = OrganizationType.builder().name("Type").description("Desc").build();
        Organization org = Organization.builder()
                .name("Org")
                .shortName("O")
                .type(orgType)
                .address("Addr")
                .city("City")
                .directorName("Dir")
                .phone("123")
                .email("a@b.com")
                .notes("notes")
                .build();
        assertEquals(0, org.getEmployeeCount());
        assertEquals("medium", org.getRiskCategory());
        assertTrue(org.getIsActive());
        assertNotNull(org.getInspections());
        invokeEnsureId(org, Organization.class);
        assertNotNull(org.getId());

        InspectionType inspectionType = InspectionType.builder().name("Planned").code("PL").description("Desc").build();
        Profile inspector = Profile.builder().username("i").password("p").fullName("Inspector").phone("1").position("pos").build();
        Inspection inspection = Inspection.builder()
                .organization(org)
                .type(inspectionType)
                .inspector(inspector)
                .scheduledDate(LocalDate.now())
                .findingsSummary("summary")
                .recommendations("recs")
                .build();
        assertEquals("planned", inspection.getStatus());
        assertEquals("pending", inspection.getResult());
        assertNotNull(inspection.getViolations());
        invokeEnsureId(inspection, Inspection.class);
        assertNotNull(inspection.getId());

        Violation violation = Violation.builder()
                .inspection(inspection)
                .code("V-1")
                .description("desc")
                .articleReference("art")
                .resolutionNotes("notes")
                .build();
        assertEquals("minor", violation.getSeverity());
        assertFalse(violation.getResolved());
        invokeEnsureId(violation, Violation.class);
        assertNotNull(violation.getId());
    }

    private void invokeEnsureId(Object target, Class<?> type) throws Exception {
        Method method = type.getDeclaredMethod("ensureId");
        method.setAccessible(true);
        method.invoke(target);
    }
}
