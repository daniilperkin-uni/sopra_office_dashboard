package de.itestra.dashboard.communitylunch.service;

import de.itestra.dashboard.common.services.MattermostNotificationSender;
import de.itestra.dashboard.communitylunch.dto.choice.request.LunchChoiceRequest;
import de.itestra.dashboard.communitylunch.dto.choice.response.LunchChoiceResponse;
import de.itestra.dashboard.communitylunch.dto.choice.response.LunchResultsResponse;
import de.itestra.dashboard.communitylunch.dto.event.request.CreateCommunityLunchEventRequest;
import de.itestra.dashboard.communitylunch.dto.event.request.UpdateCommunityLunchEventRequest;
import de.itestra.dashboard.communitylunch.dto.event.response.CommunityLunchEventResponse;
import de.itestra.dashboard.communitylunch.dto.option.request.AddCustomOptionRequest;
import de.itestra.dashboard.communitylunch.dto.option.request.AddFromCatalogRequest;
import de.itestra.dashboard.communitylunch.entity.*;
import de.itestra.dashboard.communitylunch.mapper.CommunityLunchEventMapper;
import de.itestra.dashboard.communitylunch.mapper.LunchOptionMapper;
import de.itestra.dashboard.communitylunch.mapper.LunchResultsMapper;
import de.itestra.dashboard.communitylunch.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing community lunch events, meal options, and employee voting.
 * <p>
 * Rules:
 * </p>
 * <ul>
 *   <li>Events must have unique dates (one event per day)</li>
 *   <li>Voting only allowed when event status is OPEN (not DRAFT or CLOSED)</li>
 *   <li>One choice per employee per event</li>
 *   <li>Options can be either from catalog or custom</li>
 *   <li>Mattermost notifications sent on event creation</li>
 *   <li>Cascade delete: deleting event removes all options and choices</li>
 * </ul>
 */
@Service
public class CommunityLunchService {

    private CommunityLunchEventRepository communityLunchEventRepository;
    private FoodCatalogItemRepository foodCatalogItemRepository;
    private LunchEventOptionRepository lunchEventOptionRepository;
    private LunchChoiceRepository lunchChoiceRepository;
    private MattermostNotificationSender mattermostNotificationSender;
    private LunchNotificationFormatter lunchNotificationFormatter;

    /**
     * Constructs a new CommunityLunchService.
     *
     * @param communityLunchEventRepository repository for lunch events
     * @param foodCatalogItemRepository repository for food catalog items
     * @param lunchEventOptionRepository repository for lunch event options
     * @param lunchChoiceRepository repository for employee choices
     * @param mattermostNotificationSender sender for Mattermost notifications
     * @param lunchNotificationFormatter formatter for notification messages
     */
    public CommunityLunchService(
            CommunityLunchEventRepository communityLunchEventRepository,
            FoodCatalogItemRepository foodCatalogItemRepository,
            LunchEventOptionRepository lunchEventOptionRepository,
            LunchChoiceRepository lunchChoiceRepository,
            MattermostNotificationSender mattermostNotificationSender,
            LunchNotificationFormatter lunchNotificationFormatter) {
        this.communityLunchEventRepository = communityLunchEventRepository;
        this.foodCatalogItemRepository = foodCatalogItemRepository;
        this.lunchEventOptionRepository = lunchEventOptionRepository;
        this.lunchChoiceRepository = lunchChoiceRepository;
        this.mattermostNotificationSender = mattermostNotificationSender;
        this.lunchNotificationFormatter = lunchNotificationFormatter;
    }

    /**
     * Retrieves upcoming community lunch events within the specified number of days.
     *
     * @param days number of days to look ahead from today (inclusive)
     * @return list of upcoming events sorted by date
     */
    @Transactional(readOnly = true)
    public List<CommunityLunchEventResponse> getUpcomingCommunityLunches(int days) {
        LocalDate from = LocalDate.now();
        LocalDate to = from.plusDays(days);

        return communityLunchEventRepository.findWithOptionsByDateBetween(from, to).stream()
                .map(CommunityLunchEventMapper::toResponse)
                .toList();
    }

    /**
     * Retrieves all community lunch events between two dates (inclusive).
     *
     * @param from start date (inclusive)
     * @param to end date (inclusive)
     * @return list of events in the date range sorted by date
     * @throws IllegalArgumentException if to is before from
     */
    @Transactional(readOnly = true)
    public List<CommunityLunchEventResponse> getCommunityLunchesBetween(LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("to must be >= from");
        }
        return communityLunchEventRepository.findWithOptionsByDateBetween(from, to).stream()
                .map(CommunityLunchEventMapper::toResponse)
                .toList();
    }

    /**
     * Retrieves a single community lunch event by ID.
     *
     * @param eventId unique identifier of the event
     * @return the event with all its options
     * @throws NoSuchElementException if event not found
     */
    @Transactional(readOnly = true)
    public CommunityLunchEventResponse getCommunityLunch(Long eventId) {
        CommunityLunchEvent event = communityLunchEventRepository.findWithOptionsById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Event not found: " + eventId));
        return CommunityLunchEventMapper.toResponse(event);
    }

    /**
     * Creates a new community lunch event.
     * <p>
     * Optionally adds initial meal options from the catalog. Sends Mattermost notification
     * to the channel if initial options are provided. Trims location text for consistency.
     * </p>
     *
     * @param request the event creation request containing date, location, note, and optional initial catalog item IDs
     * @return the newly created event
     * @throws IllegalArgumentException if an event already exists for the specified date
     */
    @Transactional
    public CommunityLunchEventResponse createCommunityLunch(CreateCommunityLunchEventRequest request) {
        if (communityLunchEventRepository.existsByDate(request.date())) {
            throw new IllegalArgumentException("Event already exists for date: " + request.date());
        }

        CommunityLunchEvent event = new CommunityLunchEvent();
        event.setDate(request.date());
        event.setLocation(request.location().trim());
        event.setNote(request.note());

        communityLunchEventRepository.save(event);

        if (request.initialCatalogItemIds() != null && !request.initialCatalogItemIds().isEmpty()) {
            List<FoodCatalogItem> itemsToAdd = foodCatalogItemRepository.findAllById(request.initialCatalogItemIds());
            for (FoodCatalogItem item : itemsToAdd) {
                LunchEventOption option = new LunchEventOption();
                option.setCatalogItem(item);
                option.validate();
                event.addOption(option);
            }
            communityLunchEventRepository.save(event);

            String message = lunchNotificationFormatter.formatLunchCreationNotification(event);
            mattermostNotificationSender.sendToChannel(message);
        }

        return CommunityLunchEventMapper.toResponse(event);
    }

    /**
     * Updates an existing community lunch event.
     *
     * @param eventId unique identifier of the event to update
     * @param request the update request containing new values
     * @return the updated event
     * @throws NoSuchElementException if event not found
     * @throws IllegalArgumentException if new date conflicts with existing event or status is invalid
     */
    @Transactional
    public CommunityLunchEventResponse updateCommunityLunch(Long eventId, UpdateCommunityLunchEventRequest request) {
        CommunityLunchEvent event = communityLunchEventRepository.findWithOptionsById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Event not found: " + eventId));

        if (request.date() != null && !request.date().equals(event.getDate())) {
            if (communityLunchEventRepository.existsByDate(request.date())) {
                throw new IllegalArgumentException("Another event already exists for date: " + request.date());
            }
            event.setDate(request.date());
        }

        event.setLocation(request.location().trim());

        if (request.note() != null) {
            event.setNote(request.note());
        }

        try {
            event.setStatus(LunchStatus.valueOf(request.status().trim().toUpperCase()));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid status: " + request.status() + " (expected DRAFT/OPEN/CLOSED)");
        }

        return CommunityLunchEventMapper.toResponse(event);
    }

    /**
     * Updates only the status of a community lunch event.
     * <p>
     * Status controls whether voting is allowed (OPEN) or not (DRAFT/CLOSED).
     * </p>
     *
     * @param eventId unique identifier of the event
     * @param status new status (DRAFT, OPEN, or CLOSED)
     * @return the updated event
     * @throws NoSuchElementException if event not found
     * @throws NullPointerException if status is null
     */
    @Transactional
    public CommunityLunchEventResponse updateCommunityLunchStatus(Long eventId, LunchStatus status) {
        Objects.requireNonNull(status, "status required");

        CommunityLunchEvent event = communityLunchEventRepository.findWithOptionsById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Event not found: " + eventId));

        event.setStatus(status);

        return CommunityLunchEventMapper.toResponse(event);
    }

    /**
     * Adds meal options from the food catalog to an event.
     *
     * @param eventId unique identifier of the event
     * @param request request containing catalog item IDs to add
     * @return the updated event with new options
     * @throws NoSuchElementException if event not found
     * @throws IllegalArgumentException if no valid catalog items found
     */
    @Transactional
    public CommunityLunchEventResponse addOptionsFromCatalog(Long eventId, AddFromCatalogRequest request) {
        CommunityLunchEvent event = communityLunchEventRepository.findWithOptionsById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Event not found: " + eventId));

        List<FoodCatalogItem> itemsToAdd = foodCatalogItemRepository.findAllById(request.catalogItemIds());
        if (itemsToAdd.isEmpty()) {
            throw new IllegalArgumentException("No catalog items found");
        }

        Set<Long> existingCatalogIds = event.getOptions().stream()
                .map(LunchEventOption::getCatalogItem)
                .filter(Objects::nonNull)
                .map(FoodCatalogItem::getId)
                .collect(Collectors.toSet());

        for (FoodCatalogItem item : itemsToAdd) {
            if (existingCatalogIds.contains(item.getId())) {
                continue;
            }

            LunchEventOption option = new LunchEventOption();
            option.setCatalogItem(item);
            option.validate();

            event.addOption(option);
        }

        return CommunityLunchEventMapper.toResponse(event);
    }

    /**
     * Adds a custom meal option to an event.
     * <p>
     * Optionally saves the custom option to the food catalog for future reuse.
     * If saving to catalog and an item with the same label already exists,
     * it will be reused instead of creating a duplicate.
     * </p>
     *
     * @param eventId unique identifier of the event
     * @param request request containing the custom option label and save-to-catalog flag
     * @return the updated event with the new option
     * @throws NoSuchElementException if event not found
     * @throws IllegalArgumentException if option with same label already exists in event
     */
    @Transactional
    public CommunityLunchEventResponse addCustomOption(Long eventId, AddCustomOptionRequest request) {
        CommunityLunchEvent event = communityLunchEventRepository.findWithOptionsById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Event not found: " + eventId));

        String label = request.label().trim();

        boolean exists = event.getOptions().stream()
                .map(LunchOptionMapper::label)
                .anyMatch(l -> l.equalsIgnoreCase(label));
        if (exists) {
            throw new IllegalArgumentException("Option already exists for this event");
        }

        FoodCatalogItem catalogItem = null;
        if (request.saveToDefaultCatalog()) {
            catalogItem = foodCatalogItemRepository.findByLabelIgnoreCase(label)
                    .orElseGet(() -> {
                        FoodCatalogItem created = new FoodCatalogItem();
                        created.setLabel(label);
                        return foodCatalogItemRepository.save(created);
                    });
        }

        LunchEventOption option = new LunchEventOption();
        if (catalogItem != null) {
            option.setCatalogItem(catalogItem);
        } else {
            option.setCustomLabel(label);
        }
        option.validate();

        event.addOption(option);

        return CommunityLunchEventMapper.toResponse(event);
    }

    /**
     * Activates or deactivates a meal option.
     * <p>
     * Inactive options are hidden from voting. This allows temporarily removing
     * options without deleting them.
     * </p>
     *
     * @param eventId unique identifier of the event
     * @param optionId unique identifier of the option
     * @param active whether the option should be active
     * @return the updated event
     * @throws NoSuchElementException if event or option not found
     */
    @Transactional
    public CommunityLunchEventResponse updateOptionActive(Long eventId, Long optionId, boolean active) {
        CommunityLunchEvent event = communityLunchEventRepository.findWithOptionsById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Event not found: " + eventId));

        LunchEventOption option = event.getOptions().stream()
                .filter(o -> Objects.equals(o.getId(), optionId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Option not found in event: " + optionId));

        option.setActive(active);

        return CommunityLunchEventMapper.toResponse(event);
    }

    /**
     * Permanently removes a meal option from an event.
     *
     * @param eventId unique identifier of the event
     * @param optionId unique identifier of the option to delete
     * @throws NoSuchElementException if event or option not found
     */
    @Transactional
    public void deleteOption(Long eventId, Long optionId) {
        CommunityLunchEvent event = communityLunchEventRepository.findWithOptionsById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Event not found: " + eventId));

        LunchEventOption option = event.getOptions().stream()
                .filter(o -> Objects.equals(o.getId(), optionId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Option not found in event: " + optionId));

        event.getOptions().remove(option);
    }

    /**
     * Permanently deletes a community lunch event.
     *
     * @param eventId unique identifier of the event to delete
     * @throws NoSuchElementException if event not found
     */
    @Transactional
    public void deleteCommunityLunch(Long eventId) {
        if (!communityLunchEventRepository.existsById(eventId)) {
            throw new NoSuchElementException("Event not found: " + eventId);
        }
        communityLunchEventRepository.deleteById(eventId);
    }

    /**
     * Records or updates an employee's meal choice for an event.
     * <p>
     * If the employee already has a choice for this event,
     * it will be updated to the new option. Voting is only allowed when the event status
     * is OPEN (not DRAFT or CLOSED). Validates that the chosen option belongs to the event
     * and is active.
     * </p>
     *
     * @param eventId unique identifier of the event
     * @param request request containing the selected option ID and employee name
     * @throws NoSuchElementException if event or option not found
     * @throws IllegalStateException if voting is not open (event status is not OPEN)
     * @throws IllegalArgumentException if option doesn't belong to event or is inactive
     */
    @Transactional
    public void choose(Long eventId, LunchChoiceRequest request) {
        CommunityLunchEvent event = communityLunchEventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Event not found: " + eventId));

        if (event.getStatus() != LunchStatus.OPEN) {
            throw new IllegalStateException("Voting is only allowed when the event is OPEN (current: " + event.getStatus() + ")");
        }

        LunchEventOption option = lunchEventOptionRepository.findById(request.optionId())
                .orElseThrow(() -> new NoSuchElementException("Option not found: " + request.optionId()));

        if (!Objects.equals(option.getEvent().getId(), event.getId())) {
            throw new IllegalArgumentException("Option does not belong to event");
        }
        if (!option.isActive()) {
            throw new IllegalArgumentException("Option is inactive");
        }

        String employee = request.employeeName().trim();

        LunchChoice choice = lunchChoiceRepository.findByEventIdAndEmployeeName(eventId, employee)
                .orElseGet(LunchChoice::new);

        choice.setEvent(event);
        choice.setOption(option);
        choice.setEmployeeName(employee);

        lunchChoiceRepository.save(choice);
    }

    /**
     * Retrieves all meal choices made by a specific employee for upcoming events.
     * <p>
     * Looks ahead 365 days from today.
     * Returns empty list if no upcoming events or no choices found.
     * </p>
     *
     * @param employeeName name of the employee
     * @return list of the employee's choices for upcoming events
     */
    @Transactional(readOnly = true)
    public List<LunchChoiceResponse> getChoicesByEmployee(String employeeName) {
        LocalDate from = LocalDate.now();
        List<Long> upcomingEventIds = communityLunchEventRepository
                .findWithOptionsByDateBetween(from, from.plusDays(365))
                .stream().map(CommunityLunchEvent::getId).toList();

        if (upcomingEventIds.isEmpty()) {
            return List.of();
        }

        return lunchChoiceRepository.findByEventIdInAndEmployeeName(upcomingEventIds, employeeName.trim())
                .stream()
                .map(c -> new LunchChoiceResponse(c.getEvent().getId(), c.getOption().getId(), c.getEmployeeName()))
                .toList();
    }

    /**
     * Retrieves aggregated voting results for an event
     *
     * @param eventId unique identifier of the event
     * @return voting results with vote counts per option
     * @throws NoSuchElementException if event not found
     */
    @Transactional(readOnly = true)
    public LunchResultsResponse results(Long eventId) {
        CommunityLunchEvent event = communityLunchEventRepository.findWithOptionsById(eventId)
                .orElseThrow(() -> new NoSuchElementException("Event not found: " + eventId));

        Map<Long, Long> counts = new HashMap<>();
        for (LunchChoiceRepository.OptionCountProjection projection : lunchChoiceRepository.countByOption(eventId)) {
            counts.put(projection.getOptionId(), projection.getCount());
        }

        return LunchResultsMapper.toResponse(event, counts);
    }
}
