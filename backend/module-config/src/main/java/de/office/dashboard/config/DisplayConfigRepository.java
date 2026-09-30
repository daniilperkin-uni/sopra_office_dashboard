package de.office.dashboard.config;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DisplayConfigRepository extends JpaRepository<DisplayConfigEntity, Long> {
}
