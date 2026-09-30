package de.office.dashboard.security;

/**
 * Request body for the POST /api/auth/login endpoint.
 *
 * @param username the admin username
 * @param password the admin password
 */
public record LoginRequest(String username, String password) {
}
