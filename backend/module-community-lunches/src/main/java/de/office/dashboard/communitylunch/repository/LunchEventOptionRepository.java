package de.office.dashboard.communitylunch.repository;

import de.office.dashboard.communitylunch.entity.LunchEventOption;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link LunchEventOption} entities.
 * <p>
 * Provides basic CRUD operations for lunch event meal options.
 * </p>
 */
public interface LunchEventOptionRepository extends JpaRepository<LunchEventOption, Long> {
}
