package de.office.dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Einstiegspunkt der Anwendung.
 * <p>
 * Bewusst ohne {@code TimeZone.setDefault(...)}: ein globaler JVM-Default in
 * {@code main()} gilt nur fuer den Produktionsstart und nicht fuer Tests, die
 * {@code main()} nie aufrufen - Datumsgrenzen waeren dann in Tests und
 * Produktion unterschiedlich. Die Zeitzone wird stattdessen dort gesetzt, wo
 * sie hingehoert: {@code spring.jackson.time-zone} fuer die JSON-Serialisierung
 * (application.properties) und {@code TZ} fuer den Container (compose.yaml).
 * </p>
 */
@EnableCaching
@EnableScheduling
@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
