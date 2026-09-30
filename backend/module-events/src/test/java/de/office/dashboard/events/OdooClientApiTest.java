package de.office.dashboard.events;

import de.office.dashboard.events.odooConnection.OdooClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration test for OdooClient API operations.
 * <p>
 * Tests the connectivity and basic functionality of the OdooClient by verifying
 * that it can authenticate and retrieve data from the Odoo system.
 * </p>
 * <p>
 * Disabled by default — requires real Odoo credentials in the environment.
 * To run locally, set the ODOO_API_* environment variables and remove @Disabled.
 * </p>
 */
@Disabled("Requires real Odoo credentials — set ODOO_API_* env vars and remove @Disabled to run locally")
@SpringBootTest(classes = de.office.dashboard.BackendApplication.class)
class OdooClientApiTest {

    @Autowired
    private OdooClient client;

    /**
     * Tests that the OdooClient can authenticate and retrieve employee data.
     * Verifies that the returned employee list is not null.
     */
    @Test
    void odooApi_shouldAuthenticateAndReturnEmployees() throws Exception {
        List<Map<String, Object>> employees = client.getEmployees();

        assertNotNull(employees, "Employees list must not be null");
    }

    /**
     * Tests that the OdooClient can authenticate and retrieve contract data.
     * Verifies that the returned contract list is not null.
     */
    @Test
    void odooApi_shouldAuthenticateAndReturnContracts() throws Exception {
        List<Map<String, Object>> contracts = client.getContracts();

        assertNotNull(contracts, "Contracts list must not be null");
    }
}
