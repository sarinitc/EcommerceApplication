package org.example.ecommerceapplication.customer.service;

import org.example.ecommerceapplication.profile.service.ProfileService;
import org.example.ecommerceapplication.user.repository.UserRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/** Opt-in audit: all database connections are read-only; Flyway validates without migrating. */
@EnabledIfEnvironmentVariable(named = "RUN_USERS_DB_AUDIT", matches = "true")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.hikari.read-only=true",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.sql.init.mode=never",
        "spring.flyway.clean-disabled=true",
        "spring.mail.username=audit@example.invalid",
        "spring.mail.password=unused",
        "logging.level.org.hibernate.SQL=OFF",
        "logging.level.org.hibernate.orm.jdbc.bind=OFF"
})
@Import(UsersDatabaseAuditIT.ReadOnlyFlyway.class)
class UsersDatabaseAuditIT {
    @TestConfiguration
    static class ReadOnlyFlyway {
        @Bean
        FlywayMigrationStrategy auditFlywayStrategy() {
            return Flyway::validate;
        }
    }

    @Autowired UserRepository users;
    @Autowired ProfileService profiles;
    @Autowired AdminCustomerService customers;
    @Autowired Flyway flyway;
    @Autowired JdbcTemplate jdbc;

    @Test
    @Transactional(readOnly = true)
    void startsWithValidatedSchemaAndLoadsExistingProfilesAndCustomers() {
        assertEquals("on", jdbc.queryForObject("SHOW transaction_read_only", String.class));
        assertEquals(0, flyway.info().pending().length);
        var existing = users.findAll();
        assertFalse(existing.isEmpty());
        for (var user : existing) {
            var profile = profiles.getMyProfile(user.getEmail());
            assertEquals(user.getId(), profile.getUserId());
            assertEquals(user.getProfileImage(), profile.getProfileImage());
            assertEquals(user.isVerified(), profile.isVerified());
            assertNotNull(user.getCreatedAt());
        }
        assertNotNull(customers.getCustomers(0, 100, null));
    }
}
