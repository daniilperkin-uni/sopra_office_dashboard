package de.itestra.dashboard.events;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * Standalone test utility for verifying Odoo XML-RPC API connectivity and functionality.
 * <p>
 * This class contains a main method that tests the Odoo API by:
 * - Retrieving server version information
 * - Authenticating with the Odoo server
 * - Searching for employees
 * - Reading employee data
 * </p>
 * <p>
 * Note: This class contains hardcoded credentials and should only be used for testing purposes.
 * </p>
 */
public class OdooVersionTest {

    /**
     * Main method that executes a series of Odoo API calls to test connectivity.
     *
     * @param args command line arguments (not used)
     * @throws Exception if any API call fails
     */
    public static void main(String[] args) throws Exception {

        String url = "https://odoo15studentproject.dev.itestra.com";
        String db = "itestraodoo";
        String username = "admin";
        String password = "REDACTED-SECRET";

        XmlRpcClient client = new XmlRpcClient();

        // 1) Version
        XmlRpcClientConfigImpl common_config = new XmlRpcClientConfigImpl();
        common_config.setServerURL(URI.create(url + "/xmlrpc/2/common").toURL());
        client.setConfig(common_config);

        @SuppressWarnings("unchecked")
        Map<String, Object> version = (Map<String, Object>)
                client.execute("version", new Object[]{});

        System.out.println("Odoo Version: " + version.get("server_version"));
        System.out.println("Serie: " + version.get("server_serie"));
        System.out.println("Full: " + version);

        // 2) Login
        Object uidObject = client.execute("authenticate", new Object[]{
                db, username, password, Map.of()
        });

        int uid = (int) uidObject;
        System.out.println("UID = " + uid);

        // 3) Switch to OBJECT endpoint
        XmlRpcClientConfigImpl object_config = new XmlRpcClientConfigImpl();
        object_config.setServerURL(URI.create(url + "/xmlrpc/2/object").toURL());
        client.setConfig(object_config);

        // 4) Search employees
        Object[] employeeIdsArray = (Object[]) client.execute(
                "execute_kw",
                asList(
                        db, uid, password,
                        "hr.employee", "search",
                        asList(asList())
                ).toArray()
        );

        List<?> employeeIds = asList(employeeIdsArray);

        System.out.println("Employee IDs: " + employeeIds);

        // 5) Read employees
        Object[] employeesArray = (Object[]) client.execute(
                "execute_kw",
                asList(
                        db, uid, password,
                        "hr.employee", "read",
                        asList(employeeIds),
                        Map.of("fields", new String[]{"id", "name", "work_email"})
                ).toArray()
        );

        List<?> employees = asList(employeesArray);

        // 6) Print result
        System.out.println("Employees:");
        for (Object emp : employees) {
            System.out.println(emp);
        }
    }
}
