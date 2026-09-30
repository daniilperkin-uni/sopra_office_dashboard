package de.office.dashboard.parking.scheduler;

import de.office.dashboard.parking.service.ParkingService;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduler responsible for automated parking entry maintenance tasks.
 * <p>
 * This scheduler runs periodic tasks to keep the parking system clean and
 * up-to-date. Currently, it handles automatic deletion of old parking entries
 * to prevent the database from accumulating historical data.
 * </p>
 */
@Service
public class ParkingScheduler {
    private final ParkingService parkingService;

    /**
     * Constructs the scheduler with required parking service dependency.
     *
     * @param parkingService the service used to perform parking operations
     */
    public ParkingScheduler(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    /**
     * Automatically deletes parking entries from yesterday.
     * <p>
     * This scheduled task runs daily to remove parking
     * entries from the previous day. This keeps the database clean by removing
     * historical parking data that is no longer needed.
     * </p>
     * <p>
     * Default schedule: Daily at 12:00 (noon).
     * Can be customized via {@code past.parking.entries.remove.cron} property.
     * Cron expression: "0 0 0 * * *" (second, minute, hour, day, month, day of week)
     * </p>
     */
    @Scheduled(cron = "${past.parking.entries.remove.cron:0 0 0 * * *}")
    @Transactional
    public void deleteYesterdayEntriesAutomatically() {
        parkingService.deleteYesterdayEntries();
    }
}

