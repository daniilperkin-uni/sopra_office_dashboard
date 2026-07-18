package de.itestra.dashboard.events.odooConnection;

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
    private final XmlRpcClient clientCommon;
    private final XmlRpcClient clientObject;

    /**
     * Constructs a new OdooClient and establishes connection to the Odoo system.
     * <p>
     * Initializes XML-RPC clients for common and object endpoints, and
     * authenticates
     * with the Odoo server using the provided credentials.
     * </p>
     *
     * @param url      the base URL of the Odoo server
     * @param db       the database name
     * @param username the Odoo username for authentication
     * @param password the Odoo password for authentication
     * @throws Exception if connection or authentication fails
     */
    public OdooClient(@Value("${odoo.api.url}") String url,
                      @Value("${odoo.api.db}") String db,
                      @Value("${odoo.api.username}") String username,
                      @Value("${odoo.api.password}") String password) throws Exception {
        log.info("OdooClient created");

        this.url = url;
        this.db = db;
        this.username = username;
        this.password = password;

        clientCommon = buildConfigeredXmlRpcClient("/xmlrpc/2/common");
        clientObject = buildConfigeredXmlRpcClient("/xmlrpc/2/object");

        authenticate();
    }

    private XmlRpcClient buildConfigeredXmlRpcClient(String path) {
        XmlRpcClient client = new XmlRpcClient();
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();

        try {
            URI uri = new URI(this.url + path);
            config.setServerURL(uri.toURL());
        } catch (MalformedURLException | URISyntaxException e) {
            log.error(e.getMessage());
        }

        client.setConfig(config);
        return client;
    }

    /**
     * Authenticates with the Odoo server and stores the user ID.
     *
     * @throws Exception if authentication fails
     */
    private void authenticate() throws Exception {
        Object uidObject = clientCommon.execute("authenticate", new Object[]{
                db, username, password, Map.of()
        });
        this.uid = (int) uidObject;
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
