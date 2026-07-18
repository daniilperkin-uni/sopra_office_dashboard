package de.itestra.dashboard.communitylunch.repository;

import de.itestra.dashboard.communitylunch.entity.LunchEventOption;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link LunchEventOption} entities.
 * <p>
 * Provides basic CRUD operations for lunch event meal options.
 * </p>
 */
public interface LunchEventOptionRepository extends JpaRepository<LunchEventOption, Long> {
}
