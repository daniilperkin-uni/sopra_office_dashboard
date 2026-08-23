package de.itestra.dashboard.highscore.config;

import de.itestra.dashboard.highscore.service.EloRankingAdapter;
import de.itestra.dashboard.highscore.service.EloRankingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring-Konfiguration fuer die ELO-Rangliste.
 * <p>
 * {@link EloRankingService} ist bewusst eine reine Berechnungsklasse ohne
 * Spring-Annotationen; diese Konfiguration baut die einzige Bean-Instanz
 * und versorgt sie ueber den {@link EloRankingAdapter} mit Daten.
 * </p>
 */
@Configuration
public class EloRankingConfig {

    /**
     * Erstellt den EloRankingService auf Basis des Datenbank-Adapters.
     *
     * @param adapter Adapter, der die Darts-Eintraege aus der Datenbank liefert
     * @return fertig konfigurierter EloRankingService
     */
    @Bean
    public EloRankingService eloRankingService(EloRankingAdapter adapter) {
        return new EloRankingService(adapter);
    }
}
