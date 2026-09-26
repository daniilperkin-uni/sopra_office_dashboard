package de.itestra.dashboard.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Boots the application the way production does: an empty database, Liquibase
 * as the only schema source and {@code spring.jpa.hibernate.ddl-auto=validate}
 * (see application.properties).
 * <p>
 * Regression guard for the Liquibase/JPA drift: the parking, events and config
 * changelogs did not create everything their entities mapped, so a fresh
 * database failed Hibernate validation and the backend never started. Every
 * module test disables Liquibase and uses {@code create-drop}, which is why the
 * regression stayed invisible. This test only passes when the changelog chain
 * creates a schema that the entity mappings accept.
 * </p>
 */
@SpringBootTest(classes = de.itestra.dashboard.BackendApplication.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:liquibase-schema;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.liquibase.enabled=true",
        "spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.yaml"
})
class LiquibaseSchemaBootstrapTest {

    @Test
    void contextLoadsAgainstAMigratedEmptyDatabase() {
        // Starting the context runs every Liquibase changeset and then asks
        // Hibernate to validate the resulting schema against the entities.
    }
}
