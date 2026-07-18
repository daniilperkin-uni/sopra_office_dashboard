package de.itestra.dashboard.communitylunch.service;

import de.itestra.dashboard.communitylunch.entity.FoodCatalogItem;
import de.itestra.dashboard.communitylunch.repository.FoodCatalogItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FoodCatalogServiceTest {

    private static final Logger log = LoggerFactory.getLogger(FoodCatalogServiceTest.class);

    @Mock
    private FoodCatalogItemRepository repository;

    @InjectMocks
    private FoodCatalogService service;

    @Test
    void getCatalogItems_shouldReturnItems() {
        FoodCatalogItem item = new FoodCatalogItem();
        item.setLabel("Pizza");
        item.setActive(true);

        when(repository.findAll()).thenReturn(List.of(item));

        var result = service.getCatalogItems(false);

        log.info("RESULT: " + result);

        assertEquals(1, result.size());
        assertEquals("Pizza", result.get(0).label());
    }
}
