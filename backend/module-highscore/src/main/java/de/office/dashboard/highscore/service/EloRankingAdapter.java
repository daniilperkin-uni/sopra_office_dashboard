package de.office.dashboard.highscore.service;

import de.office.dashboard.highscore.entity.DartsEntry;
import de.office.dashboard.highscore.repository.DartsEntryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Dünner Spring-Adapter, der {@link EloRankingService} an die Datenbank
 * anbindet.
 * <p>
 * Die Ranglisten-Berechnung selbst bleibt eine reine Klasse ohne
 * Spring-Abhängigkeit; dieser Adapter liefert lediglich die gespeicherten
 * Darts-Einträge in chronologischer Reihenfolge (älteste zuerst) über das
 * {@link DartsEntryReader}-Interface nach.
 * </p>
 */
@Service
public class EloRankingAdapter implements EloRankingService.DartsEntryReader {

    private final DartsEntryRepository dartsEntryRepository;

    /**
     * Konstruktor für die Abhängigkeitsinjektion.
     *
     * @param dartsEntryRepository Repository für Darts-Einträge
     */
    public EloRankingAdapter(DartsEntryRepository dartsEntryRepository) {
        this.dartsEntryRepository = dartsEntryRepository;
    }

    /**
     * Liefert alle Darts-Einträge in chronologischer Reihenfolge (älteste zuerst).
     *
     * @return Liste aller Darts-Einträge
     */
    @Override
    public List<EloRankingService.DartsEntryView> findAllOrdered() {
        List<DartsEntry> entries = dartsEntryRepository.findAll(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.ASC, "createdAt"));
        return entries.stream()
                .map(entry -> new EloRankingService.DartsEntryView(
                        entry.getPlayerName(), entry.getDartsToFinish()))
                .toList();
    }
}
