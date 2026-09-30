package de.office.dashboard.communitylunch.repository;

import de.office.dashboard.communitylunch.entity.LunchChoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link LunchChoice} entities.
 * <p>
 * Provides data access methods for employee meal choices. Includes custom
 * queries for vote aggregation and employee choice lookup.
 * </p>
 */
public interface LunchChoiceRepository extends JpaRepository<LunchChoice, Long> {

    Optional<LunchChoice> findByEventIdAndEmployeeName(Long eventId, String employeeName);

    interface OptionCountProjection {

        Long getOptionId();

        Long getCount();
    }

    @Query("""
                select c.option.id as optionId, count(c.id) as count
                from LunchChoice c
                where c.event.id = :eventId
                group by c.option.id
                order by count(c.id) desc
            """)
    List<OptionCountProjection> countByOption(Long eventId);

    @Query("""
                select c.option.id as optionId, count(c.id) as count
                from LunchChoice c
                where c.event.id in :eventIds
                group by c.option.id
            """)
    List<OptionCountProjection> countByEventIds(List<Long> eventIds);

    List<LunchChoice> findByEventIdInAndEmployeeName(List<Long> eventIds, String employeeName);
}
