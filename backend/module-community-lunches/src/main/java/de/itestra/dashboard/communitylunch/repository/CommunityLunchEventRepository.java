package de.itestra.dashboard.communitylunch.repository;

import de.itestra.dashboard.communitylunch.entity.CommunityLunchEvent;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link CommunityLunchEvent} entities.
 * <p>
 * Provides data access methods for community lunch events. 
 * </p>
 */
public interface CommunityLunchEventRepository extends JpaRepository<CommunityLunchEvent, Long> {

    boolean existsByDate(LocalDate date);

    @EntityGraph(attributePaths = {"options", "options.catalogItem"})
    Optional<CommunityLunchEvent> findWithOptionsById(Long id);

    @EntityGraph(attributePaths = {"options", "options.catalogItem"})
    List<CommunityLunchEvent> findWithOptionsByDateBetweenOrderByDateAsc(LocalDate from, LocalDate to);

    default List<CommunityLunchEvent> findWithOptionsByDateBetween(LocalDate from, LocalDate to) {
        return findWithOptionsByDateBetweenOrderByDateAsc(from, to);
    }
}
