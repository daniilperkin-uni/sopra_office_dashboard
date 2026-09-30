package de.office.dashboard.events.odooConnection;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * Client for communicating with the Odoo ERP system via XML-RPC.
 * <p>
 * This client provides methods to retrieve employee and contract data from
 * Odoo,
 * which is used to generate dashboard events. It handles authentication and
 * executes remote procedure calls using the Odoo XML-RPC API.
 * </p>
 */
@Component
public class OdooClient {

    private static final Logger log = LoggerFactory.getLogger(OdooClient.class);

    private final String url;
    private final String db;
    private final String username;
    private final String password;

    @Value("${odoo.api.work-location}")
    private String location;

    private int uid;
    private volatile boolean authenticated = false;
    private final XmlRpcClient clientCommon;
    private final XmlRpcClient clientObject;

    /**
     * Constructs a new OdooClient.
     * <p>
     * Initializes XML-RPC clients for common and object endpoints. Authentication
     * is deferred to the first API call so the application context starts even
     * if Odoo is temporarily unreachable.
     * </p>
     *
     * @param url      the base URL of the Odoo server
     * @param db       the database name
     * @param username the Odoo username for authentication
     * @param password the Odoo password for authentication
     */
    public OdooClient(@Value("${odoo.api.url}") String url,
                      @Value("${odoo.api.db}") String db,
                      @Value("${odoo.api.username}") String username,
                      @Value("${odoo.api.password}") String password) {
        log.info("OdooClient created");

        this.url = url;
        this.db = db;
        this.username = username;
        this.password = password;

        // Ohne konfigurierte URL bleibt die Odoo-Integration deaktiviert,
        // damit der Start nicht an einer leeren Basis-URL scheitert.
        if (url == null || url.isBlank()) {
            log.warn("ODOO_API_URL is not set - Odoo integration is disabled");
            clientCommon = null;
            clientObject = null;
            return;
        }

        clientCommon = buildConfigeredXmlRpcClient("/xmlrpc/2/common");
        clientObject = buildConfigeredXmlRpcClient("/xmlrpc/2/object");
    }

    private XmlRpcClient buildConfigeredXmlRpcClient(String path) {
        XmlRpcClient client = new XmlRpcClient();
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();

        try {
            URI uri = new URI(this.url + path);
            config.setServerURL(uri.toURL());
        } catch (MalformedURLException | URISyntaxException e) {
            throw new IllegalStateException("Invalid Odoo API URL: " + this.url + path, e);
        }

        client.setConfig(config);
        return client;
    }

    /**
     * Lazily authenticates with the Odoo server on the first API call.
     *
     * @throws XmlRpcException if authentication fails
     */
    private synchronized void ensureAuthenticated() throws XmlRpcException {
        if (authenticated) {
            return;
        }
        if (clientCommon == null) {
            throw new XmlRpcException("Odoo integration is not configured (ODOO_API_URL is empty)");
        }
        try {
            Object uidObject = clientCommon.execute("authenticate", new Object[]{
                    db, username, password, Map.of()
            });
            this.uid = (int) uidObject;
            this.authenticated = true;
        } catch (XmlRpcException e) {
            throw e;
        } catch (Exception e) {
            throw new XmlRpcException("Odoo authentication failed", e);
        }
    }

    /**
     * Retrieves all employees from the configured work location.
     * <p>
     * Searches for employees based on the work location filter and returns
     * their data including id, name, birthday, and work email.
     * </p>
     *
     * @return list of employee data as maps
     * @throws XmlRpcException if the Odoo API call fails
     */
    public List<Map<String, Object>> getEmployees() throws XmlRpcException {
        ensureAuthenticated();
        Object[] employeesObj = readEmployees(searchEmployees());
        List<Map<String, Object>> employees = Arrays.stream(employeesObj)
                .map(e -> (Map<String, Object>) e)
                .toList();
        return employees;
    }

    /**
     * Retrieves all contracts from Odoo.
     * <p>
     * Returns contract data including employee ID, start date, and trial end date
     * which is used for work anniversary and probation ending events.
     * </p>
     *
     * @return list of contract data as maps
     * @throws XmlRpcException if the Odoo API call fails
     */
    public List<Map<String, Object>> getContracts() throws XmlRpcException {
        ensureAuthenticated();
        Object[] contractsObj = readContracts(searchContracts());
        List<Map<String, Object>> contracts = Arrays.stream(contractsObj)
                .map(e -> (Map<String, Object>) e)
                .toList();
        return contracts;
    }

    /**
     * Searches for employee IDs matching the configured work location.
     *
     * @return list of employee IDs
     * @throws XmlRpcException if the search operation fails
     */
    private List<?> searchEmployees() throws XmlRpcException {

        Object[] employeeIdsArray = (Object[]) clientObject.execute(
                "execute_kw",
                asList(
                        db, uid, password,
                        "hr.employee", "search",
                        asList(asList(asList("work_location_id.name", "=", location))))
                        .toArray());

        List<?> employeeIds = asList(employeeIdsArray);
        return employeeIds;
    }

    /**
     * Reads employee details for the given employee IDs.
     *
     * @param employeeIds list of employee IDs to retrieve
     * @return array of employee data objects
     * @throws XmlRpcException if the read operation fails
     */
    private Object[] readEmployees(List<?> employeeIds) throws XmlRpcException {

        Object[] employeesArray = (Object[]) clientObject.execute(
                "execute_kw",
                asList(
                        db, uid, password,
                        "hr.employee", "read",
                        asList((Object) employeeIds),
                        Map.of("fields",
                                new String[]{"id", "name", "birthday",
                                        "work_email"}))
                        .toArray());

        return employeesArray;
    }

    /**
     * Searches for all contract IDs in the Odoo system.
     *
     * @return array of contract IDs
     * @throws XmlRpcException if the search operation fails
     */
    private Object[] searchContracts() throws XmlRpcException {

        Object[] contractIdsArray = (Object[]) clientObject.execute(
                "execute_kw",
                asList(
                        db, uid, password,
                        "hr.contract", "search",
                        asList(asList())).toArray());

        return contractIdsArray;
    }

    /**
     * Reads contract details for the given contract IDs.
     *
     * @param contractsIds array of contract IDs to retrieve
     * @return array of contract data objects
     * @throws XmlRpcException if the read operation fails
     */
    private Object[] readContracts(Object[] contractsIds) throws XmlRpcException {

        Object readResult = clientObject.execute(
                "execute_kw",
                asList(
                        db, uid, password,
                        "hr.contract", "read",
                        asList((Object) contractsIds),
                        Map.of("fields", new String[]{
                                "id",
                                "employee_id",
                                "date_start",
                                "trial_date_end",
                        })).toArray());
        Object[] contractsArray = (Object[]) readResult;

        return contractsArray;
    }
}
