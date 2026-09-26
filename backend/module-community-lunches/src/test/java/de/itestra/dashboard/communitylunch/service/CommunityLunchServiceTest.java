package de.itestra.dashboard.communitylunch.service;

import de.itestra.dashboard.common.services.MattermostNotificationSender;
import de.itestra.dashboard.communitylunch.dto.choice.request.LunchChoiceRequest;
import de.itestra.dashboard.communitylunch.entity.CommunityLunchEvent;
import de.itestra.dashboard.communitylunch.entity.LunchChoice;
import de.itestra.dashboard.communitylunch.entity.LunchEventOption;
import de.itestra.dashboard.communitylunch.entity.LunchStatus;
import de.itestra.dashboard.communitylunch.repository.CommunityLunchEventRepository;
import de.itestra.dashboard.communitylunch.repository.FoodCatalogItemRepository;
import de.itestra.dashboard.communitylunch.repository.LunchChoiceRepository;
import de.itestra.dashboard.communitylunch.repository.LunchEventOptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the voting rules in {@link CommunityLunchService}.
 * <p>
 * The service is the largest in the project and had no test at all, which is
 * why the status guard behind {@code choose()} went unverified.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CommunityLunchServiceTest {

    @Mock
    private CommunityLunchEventRepository communityLunchEventRepository;

    @Mock
    private FoodCatalogItemRepository foodCatalogItemRepository;

    @Mock
    private LunchEventOptionRepository lunchEventOptionRepository;

    @Mock
    private LunchChoiceRepository lunchChoiceRepository;

    @Mock
    private MattermostNotificationSender mattermostNotificationSender;

    @Mock
    private LunchNotificationFormatter lunchNotificationFormatter;

    @InjectMocks
    private CommunityLunchService service;

    private CommunityLunchEvent event(Long id, LunchStatus status) {
        CommunityLunchEvent event = mock(CommunityLunchEvent.class);
        when(event.getId()).thenReturn(id);
        when(event.getStatus()).thenReturn(status);
        return event;
    }

    @Test
    void choose_rejectsVotingWhenEventIsStillADraft() {
        CommunityLunchEvent draft = event(7L, LunchStatus.DRAFT);
        when(communityLunchEventRepository.findById(7L)).thenReturn(Optional.of(draft));

        LunchChoiceRequest request = new LunchChoiceRequest(1L, "Max Mustermann");

        assertThrows(IllegalStateException.class, () -> service.choose(7L, request));
        verify(lunchChoiceRepository, never()).save(any(LunchChoice.class));
    }

    @Test
    void choose_rejectsVotingWhenEventIsClosed() {
        CommunityLunchEvent closed = event(7L, LunchStatus.CLOSED);
        when(communityLunchEventRepository.findById(7L)).thenReturn(Optional.of(closed));

        LunchChoiceRequest request = new LunchChoiceRequest(1L, "Max Mustermann");

        assertThrows(IllegalStateException.class, () -> service.choose(7L, request));
        verify(lunchChoiceRepository, never()).save(any(LunchChoice.class));
    }

    @Test
    void choose_recordsTheTrimmedChoiceWhenEventIsOpen() {
        CommunityLunchEvent openEvent = event(7L, LunchStatus.OPEN);

        LunchEventOption option = mock(LunchEventOption.class);
        when(option.getEvent()).thenReturn(openEvent);
        when(option.isActive()).thenReturn(true);

        when(communityLunchEventRepository.findById(7L)).thenReturn(Optional.of(openEvent));
        when(lunchEventOptionRepository.findById(1L)).thenReturn(Optional.of(option));
        when(lunchChoiceRepository.findByEventIdAndEmployeeName(7L, "Max Mustermann")).thenReturn(Optional.empty());

        service.choose(7L, new LunchChoiceRequest(1L, "  Max Mustermann  "));

        ArgumentCaptor<LunchChoice> captor = ArgumentCaptor.forClass(LunchChoice.class);
        verify(lunchChoiceRepository).save(captor.capture());
        assertEquals("Max Mustermann", captor.getValue().getEmployeeName());
    }

    @Test
    void choose_failsWhenTheEventDoesNotExist() {
        when(communityLunchEventRepository.findById(99L)).thenReturn(Optional.empty());

        LunchChoiceRequest request = new LunchChoiceRequest(1L, "Max Mustermann");

        assertThrows(NoSuchElementException.class, () -> service.choose(99L, request));
    }

    @Test
    void choose_failsWhenTheOptionBelongsToAnotherEvent() {
        CommunityLunchEvent openEvent = event(7L, LunchStatus.OPEN);
        CommunityLunchEvent otherEvent = event(8L, LunchStatus.OPEN);

        LunchEventOption option = mock(LunchEventOption.class);
        when(option.getEvent()).thenReturn(otherEvent);

        when(communityLunchEventRepository.findById(7L)).thenReturn(Optional.of(openEvent));
        when(lunchEventOptionRepository.findById(1L)).thenReturn(Optional.of(option));

        LunchChoiceRequest request = new LunchChoiceRequest(1L, "Max Mustermann");

        assertThrows(IllegalArgumentException.class, () -> service.choose(7L, request));
        verify(lunchChoiceRepository, never()).save(any(LunchChoice.class));
    }
}
