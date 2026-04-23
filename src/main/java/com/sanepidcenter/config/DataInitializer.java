package com.sanepidcenter.config;

import com.sanepidcenter.model.InspectionType;
import com.sanepidcenter.model.OrganizationType;
import com.sanepidcenter.model.Profile;
import com.sanepidcenter.repository.InspectionTypeRepository;
import com.sanepidcenter.repository.OrganizationTypeRepository;
import com.sanepidcenter.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final OrganizationTypeRepository organizationTypeRepository;
    private final InspectionTypeRepository inspectionTypeRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedInitialData() {
        return args -> {
            if (organizationTypeRepository.count() == 0) {
                organizationTypeRepository.saveAll(List.of(
                        OrganizationType.builder().name("Предприятие общественного питания").description("Рестораны, кафе, столовые, буфеты").build(),
                        OrganizationType.builder().name("Пищевое производство").description("Заводы и цеха по производству продуктов питания").build(),
                        OrganizationType.builder().name("Медицинское учреждение").description("Больницы, поликлиники, лаборатории").build(),
                        OrganizationType.builder().name("Детское учреждение").description("Детские сады, школы, лагеря").build(),
                        OrganizationType.builder().name("Промышленное предприятие").description("Заводы, фабрики, производственные объекты").build(),
                        OrganizationType.builder().name("Торговое предприятие").description("Магазины, супермаркеты, рынки").build(),
                        OrganizationType.builder().name("Объект водоснабжения").description("Водозаборы, очистные станции, сети").build(),
                        OrganizationType.builder().name("Прочее").description("Иные объекты санитарного надзора").build()
                ));
            }

            if (inspectionTypeRepository.count() == 0) {
                inspectionTypeRepository.saveAll(List.of(
                        InspectionType.builder().name("Плановая проверка").code("PLAN").description("Проверка согласно утверждённому плану").build(),
                        InspectionType.builder().name("Внеплановая проверка").code("UNPLAN").description("Проверка по жалобам или обращениям").build(),
                        InspectionType.builder().name("Повторная проверка").code("REPEAT").description("Проверка устранения ранее выявленных нарушений").build(),
                        InspectionType.builder().name("Рейдовая проверка").code("RAID").description("Массовые проверки однотипных объектов").build(),
                        InspectionType.builder().name("Мониторинговая проверка").code("MONITOR").description("Систематический сбор данных без санкций").build()
                ));
            }

            var adminOpt = profileRepository.findByUsername("admin1");
            if (adminOpt.isEmpty()) {
                profileRepository.save(Profile.builder()
                        .id(UUID.randomUUID())
                        .username("admin1")
                        .password(passwordEncoder.encode("admin1"))
                        .fullName("Системный администратор")
                        .role("ROLE_ADMIN")
                        .phone("")
                        .position("Администратор")
                        .isActive(true)
                        .build());
            } else {
                Profile admin = adminOpt.get();
                boolean changed = false;
                if (!"ROLE_ADMIN".equals(admin.getRole())) {
                    admin.setRole("ROLE_ADMIN");
                    changed = true;
                }
                if (admin.getIsActive() == null || !admin.getIsActive()) {
                    admin.setIsActive(true);
                    changed = true;
                }
                if (changed) {
                    profileRepository.save(admin);
                }
            }
        };
    }
}
