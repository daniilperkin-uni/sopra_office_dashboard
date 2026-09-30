package de.office.dashboard.communitylunch.mapper;

import de.office.dashboard.communitylunch.entity.LunchEventOption;

/**
 * Utility mapper for extracting data from {@link LunchEventOption} entities.
 * <p>
 * Provides helper methods for resolving option labels from either catalog items or custom labels.
 * </p>
 */
public final class LunchOptionMapper {
    private LunchOptionMapper() {
    }

    /**
     * Extracts the label from a lunch event option.
     * <p>
     * Returns the catalog item label if the option is from the catalog,
     * otherwise returns the custom label.
     * </p>
     *
     * @param option the lunch event option
     * @return the option label (from catalog or custom)
     */
    public static String label(LunchEventOption option) {
        return option.getCatalogItem() != null
                ? option.getCatalogItem().getLabel()
                : option.getCustomLabel();
    }
}
