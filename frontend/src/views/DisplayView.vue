<template>
  <div class="display-view h-screen w-full overflow-hidden bg-black relative">
    <!-- Scaler Container: Fixed at 4K resolution, scales down to fit window -->
    <div
      class="origin-center overflow-hidden bg-white absolute top-1/2 left-1/2 shadow-2xl"
      :style="scalerStyle"
    >
      <component
        :is="currentViewComponent"
        v-bind="currentViewProps"
        class="h-full w-full"
      />
    </div>
  </div>
</template>

<script setup>
/**
 * The central display component for Kiosk mode.
 * It manages the automatic rotation between Calendar, Parking, Highscore, and Dashboard views.
 * It is also responsible for fetching configuration and display data regularly.
 * Implements a global "Fit to Screen" scaler to render all views at 4K resolution.
 */
import { ref, onMounted, onUnmounted, computed, watch, nextTick, defineAsyncComponent } from 'vue';
/**
 * API service for parking-related operations.
 */
import { parkingApi, calendarApi, highscoreApi, configApi } from '@/services/api';
/**
 * Utility function to format a date object into an ISO string (YYYY-MM-DD).
 */
import { formatDateISO } from '@/utils/dateUtils';
/**
 * Utility function to process and normalize calendar events.
 */
import { processCalendarEvents } from '@/utils/eventUtils';

// Components
/**
 * Calendar component displaying a three-week view of events.
 */
import ThreeWeekCalendar from '@/features/calendar/components/ThreeWeekCalendar.vue';
/**
 * Component for displaying highscore leaderboards in a dedicated view.
 */
import DisplayViewHighscore from '@/views/DisplayViewHighscore.vue';
/**
 * Wrapper component for displaying parking occupancy information.
 */
import ParkingDisplayWrapper from '@/features/parking/components/ParkingDisplayWrapper.vue';
/**
 * Consolidated dashboard overview component (OneDisplay mode).
 */
import DashboardOverview from '@/features/dashboard/components/DashboardOverview.vue';

const GameView = defineAsyncComponent(() => import('@/features/game/GameView.vue'));

/**
 * Array of view IDs that can be rotated through.
 */
const DISPLAY_VIEW_IDS = ['calendar', 'parking', 'highscore', 'dashboard'];

/**
 * Maps view IDs to their corresponding Vue components.
 */
const viewComponentsMap = {
  calendar: ThreeWeekCalendar,
  parking: ParkingDisplayWrapper,
  highscore: DisplayViewHighscore,
  dashboard: DashboardOverview,
  game: GameView,
};

// --- Reactive State ---
/**
 * Reactive state for display configuration, fetched from the backend.
 * Controls rotation behavior, intervals, and default views.
 */
const config = ref({
  rotationEnabled: false, // Default to false as per request to "Disable automatic cycling" initially
  skipOneDisplayInRotation: false,
  rotationIntervalSeconds: 15,
  defaultSingleViewId: 'dashboard' // Default to our new "All-in-One" view
});

/**
 * Reactive state holding the ID of the currently active view.
 */
const currentViewId = ref('dashboard');
/**
 * Timer ID for the view rotation interval.
 */
let viewRotationInterval = null;
/**
 * Timer ID for polling the configuration from the backend.
 */
let configPollInterval = null;
/**
 * Reactive state for the scaling factor applied to the entire display.
 */
const scale = ref(1);

/**
 * Reference width for the 4K display resolution.
 */
const REFERENCE_WIDTH = 3840;
/**
 * Reference height for the 4K display resolution.
 */
const REFERENCE_HEIGHT = 2160;

// Data specific to each view that is managed by DisplayView.vue
/**
 * Template ref for the `ThreeWeekCalendar` component instance.
 */
const threeWeekCalendarRef = ref(null);
/**
 * Template ref for the `DashboardOverview` component instance.
 * Used to access nested components or methods within the dashboard.
 */
const dashboardRef = ref(null);

/**
 * Reactive state for fetched calendar events.
 */
const calendarEvents = ref([]);
/**
 * Reactive state for fetched parking data.
 */
const parkingWeekData = ref([]);
/**
 * Loading state for parking data.
 */
const parkingLoading = ref(true);
/**
 * Error message for parking data fetching.
 */
const parkingError = ref(null);
/**
 * Loading state for calendar data.
 */
const calendarLoading = ref(true);
/**
 * Error message for calendar data fetching.
 */
const calendarError = ref('');
/**
 * Reactive state for fetched highscore data, including leaderboards and match history.
 */
const highscoreData = ref({
  'Match history': [],
  'Darts Leaderboard': [],
  'Kicker leaderboard': [],
});
/**
 * Loading state for highscore data.
 */
const highscoreLoading = ref(true);
/**
 * Error message for highscore data fetching.
 */
const highscoreError = ref(null);

/**
 * Updates the global scale factor based on the current window size
 * to ensure the 4K content fits the available screen space.
 */
const updateScale = () => {
  const widthRatio = window.innerWidth / REFERENCE_WIDTH;
  const heightRatio = window.innerHeight / REFERENCE_HEIGHT;

  // Choose the smaller ratio to ensure the content fits entirely on screen (contain)
  scale.value = Math.min(widthRatio, heightRatio);
};

/**
 * Computed styles for the scaler container.
 * Uses `translate(-50%, -50%)` combined with absolute positioning (`top-1/2 left-1/2`)
 * to perfectly center the container regardless of its scaled size.
 */
const scalerStyle = computed(() => ({
  width: `${REFERENCE_WIDTH}px`,
  height: `${REFERENCE_HEIGHT}px`,
  transform: `translate(-50%, -50%) scale(${scale.value})`,
}));

/**
 * Computed property for processed calendar events, utilizing a utility function.
 */
const processedEvents = computed(() => {
  return processCalendarEvents(calendarEvents.value);
});

/**
 * Computed property that returns the component corresponding to the `currentViewId`.
 */
const currentViewComponent = computed(() => viewComponentsMap[currentViewId.value]);

/**
 * Computed property that returns the props to be passed to the `currentViewComponent`.
 * Props are dynamically determined based on the active view.
 */
const currentViewProps = computed(() => {
  if (currentViewId.value === 'calendar') {
    return {
      ref: threeWeekCalendarRef,
      baseMonday: null, // Calendar determines its own base Monday
      events: processedEvents.value,
      loading: calendarLoading.value,
      error: calendarError.value,
    };
  } else if (currentViewId.value === 'parking') {
    return {
      weekData: parkingWeekData.value,
      loading: parkingLoading.value,
      error: parkingError.value,
      refreshData: () => refreshDataForView('parking'), // Exposes a refresh method to the child
    };
  } else if (currentViewId.value === 'highscore') {
    return {
      highscoreData: highscoreData.value,
      loading: highscoreLoading.value,
      error: highscoreError.value,
    };
  } else if (currentViewId.value === 'dashboard') {
    return {
      ref: dashboardRef,
      events: processedEvents.value,
      parkingData: parkingWeekData.value,
      highscoreData: highscoreData.value,
      loadingState: {
        calendar: calendarLoading.value,
        parking: parkingLoading.value,
        highscore: highscoreLoading.value
            }
          };
        } else if (currentViewId.value === 'game') {
            return {};
        }
        return {};
      });
      
      /**
       * Aktualisiert die Daten für eine spezifische Ansicht (oder mehrere, wenn es das Dashboard ist).        
       * Steuert Lade-Indikatoren und behandelt Fehler, damit die UI stabil bleibt.
       * 
       * @param {string} view - Die ID der aktiven Ansicht (z.B. 'calendar', 'parking', 'highscore', 'dashboard').
       * @param {boolean} silent - Wenn true, werden keine Ladekreisel angezeigt (für Hintergrund-Updates).    
        */
      const refreshDataForView = async (view, silent = false) => {
        if (view === 'game') return; // No data needed for game
      
        // === BLOCK 1: PARKPLATZ DATEN ===
        // Wir laden Parkdaten, wenn die aktive Ansicht "parking" ist ODER wir im "dashboard" sind (wo alles zu sehen ist).
        if (view === 'parking' || view === 'dashboard') {    // UI-Feedback: Spinner einschalten, aber nur, wenn es kein leises Hintergrund-Update ist.
    if (!silent) parkingLoading.value = true;
    // Alte Fehler löschen, damit wir frisch starten.
    parkingError.value = null;
    
    try {
      // API-Call: Hole die Übersicht vom Backend. 'await' wartet, bis die Antwort da ist.
      const parkingData = await parkingApi.getOverview();
      
      // Sicherheits-Check: Wir stellen sicher, dass wir wirklich ein Array speichern, 
      // auch wenn das Backend null oder Quatsch zurückgibt. Verhindert Crashes im Template.
      parkingWeekData.value = Array.isArray(parkingData) ? parkingData : [];
      
    } catch (err) {
      // Fehlerbehandlung: Loggen für Entwickler...
      console.error('Error fetching parking data:', err);
      // ...und Nachricht für den User setzen (wieder nur, wenn nicht silent).
      if (!silent) parkingError.value = 'Failed to load parking data.';
      
    } finally {
      // Aufräumen: Egal ob Erfolg oder Fehler, der Lade-Spinner muss weg.
      parkingLoading.value = false;
    }
  }

  // === BLOCK 2: HIGHSCORE DATEN ===
  // Ähnliche Logik: Laden für 'highscore' View oder 'dashboard'.
  if (view === 'highscore' || view === 'dashboard') {
    if (!silent) highscoreLoading.value = true;
    highscoreError.value = null;
    
    try {
      // API-Call: Highscore-Statistiken abrufen.
      const data = await highscoreApi.getHighscore();
      
      // Daten-Mapping: Wir verteilen die Rohdaten in ein strukturiertes Objekt,
      // das die UI erwartet. Fallbacks (|| []) verhindern Fehler bei leeren Daten.
      highscoreData.value = {
        'Match history': data['Match history'] || [],
        'Darts Leaderboard': data['Darts Leaderboard'] || [],
        'Kicker leaderboard': data['Kicker leaderboard'] || [],
      };
      
    } catch (err) {
      console.error('Error fetching highscore data:', err);
      if (!silent) highscoreError.value = 'Failed to load highscore data.';
    } finally {
      highscoreLoading.value = false;
    }
  }

  // === BLOCK 3: KALENDER DATEN (Der komplexe Teil) ===
  if (view === 'calendar' || view === 'dashboard') {
    if (!silent) calendarLoading.value = true;
    calendarError.value = null;

    // WICHTIG: 'await nextTick()' pausiert kurz, bis Vue das DOM aktualisiert hat.
    // Das brauchen wir, weil wir gleich auf 'refs' (Elemente im HTML) zugreifen wollen,
    // die vielleicht gerade erst sichtbar geworden sind.
    await nextTick();

    try {
      // Wir suchen die aktive Kalender-Komponente, um sie zu fragen: "Was zeigst du an?"
      let calendarInstance = null;
      
      if (view === 'calendar') {
        // Fall A: Die normale Vollbild-Kalenderansicht ist aktiv.
        calendarInstance = threeWeekCalendarRef.value;
      } else if (view === 'dashboard' && dashboardRef.value) {
        // Fall B: Das Dashboard ist aktiv. Der Kalender steckt hier tiefer verschachtelt
        // in einer Unter-Komponente. Wir greifen durch.
        calendarInstance = dashboardRef.value.calendarRef;
      }

      let effectiveStartDate = null;
      let effectiveEndDate = null;

      // Feature Detection: Wir prüfen, ob wir die Kalender-Instanz gefunden haben 
      // UND ob sie die Methode 'getDisplayedDates' hat.
      if (calendarInstance && typeof calendarInstance.getDisplayedDates === 'function') {
        // Wir holen uns die Liste aller aktuell sichtbaren Tage (z.B. ["2026-01-27", "2026-01-28"...])
        const displayedDates = calendarInstance.getDisplayedDates();
        
        if (displayedDates && displayedDates.length > 0) {
          // Startdatum ermitteln
          const calendarDisplayStartDate = new Date(displayedDates[0]);
          
          // SMART LOGIC: Wir berechnen ein Datum 30 Tage VOR dem sichtbaren Start.
          // Grund: Events, die letzten Monat begannen und lange dauern, könnten noch sichtbar sein.
          const thirtyDaysAgo = new Date(calendarDisplayStartDate);
          thirtyDaysAgo.setDate(calendarDisplayStartDate.getDate() - 30);
          
          // Formatieren für die API (YYYY-MM-DD)
          effectiveStartDate = formatDateISO(thirtyDaysAgo);
          // Enddatum ist einfach der letzte sichtbare Tag.
          effectiveEndDate = displayedDates[displayedDates.length - 1];
        }
      }

      // Fallback-Strategie: Falls irgendwas schief ging (Komponente nicht bereit, Ref leer),
      // raten wir einfach einen sinnvollen Zeitraum (Aktueller Monat bis Ende nächster Monat).
      if (!effectiveStartDate) {
        const today = new Date();
        // Erster Tag des aktuellen Monats
        effectiveStartDate = formatDateISO(new Date(today.getFullYear(), today.getMonth(), 1));
        // Letzter Tag des übernächsten Monats (Trick: Tag 0 vom über-übernächsten Monat ist der letzte vom vorigen)
        effectiveEndDate = formatDateISO(new Date(today.getFullYear(), today.getMonth() + 2, 0));
      }

      // Endlich: Der API-Call mit dem präzise berechneten Zeitraum.
      const fetchedCalendarEvents = await calendarApi.getEvents(effectiveStartDate, effectiveEndDate);
      // Speichern der Events in den reaktiven State -> UI aktualisiert sich.
      calendarEvents.value = fetchedCalendarEvents;
      
    } catch (err) {
      console.error('Error fetching calendar events:', err);
      if (!silent) calendarError.value = 'Failed to load calendar data.';
    } finally {
      calendarLoading.value = false;
    }
  }
};

/**
 * Sets up the interval for automatic view rotation based on the current configuration.
 * Clears any existing interval before setting a new one.
 */
const setupRotation = () => {
    if (viewRotationInterval) {
        clearInterval(viewRotationInterval);
        viewRotationInterval = null;
    }

    // Special Case: Game Mode if interval is -1
    if (config.value.rotationIntervalSeconds === -1) {
        currentViewId.value = 'game';
        return;
    }

    // If we were in game mode but config changed, reset to default or first available
    if (currentViewId.value === 'game' && config.value.rotationIntervalSeconds !== -1) {
         currentViewId.value = config.value.defaultSingleViewId || 'dashboard';
    }

    if (config.value.rotationEnabled) {
        // SAFETY: Ensure interval is at least 5 seconds to prevent rapid cycling
        let safeInterval = config.value.rotationIntervalSeconds;
        if (safeInterval < 5) {
            console.warn(`Unsafe rotation interval ${safeInterval}s detected. Defaulting to 5s.`);
            safeInterval = 5;
        }

        viewRotationInterval = setInterval(() => {
            let availableViews = DISPLAY_VIEW_IDS;
            if (config.value.skipOneDisplayInRotation) {
                 // Filter out 'dashboard' if configured to skip it during rotation
                 availableViews = DISPLAY_VIEW_IDS.filter(id => id !== 'dashboard');
            }

            // Find current index in the filtered list (or fallback to -1 if current view is filtered out)
            let currentIndex = availableViews.indexOf(currentViewId.value);

            const nextIndex = (currentIndex + 1) % availableViews.length;
            currentViewId.value = availableViews[nextIndex];
            refreshDataForView(currentViewId.value);
        }, safeInterval * 1000);
    } else {
        // Ensure we switch to the default view if rotation is disabled
        if (config.value.defaultSingleViewId && DISPLAY_VIEW_IDS.includes(config.value.defaultSingleViewId)) {
             if (currentViewId.value !== config.value.defaultSingleViewId) {
                currentViewId.value = config.value.defaultSingleViewId;
                refreshDataForView(currentViewId.value);
             }
        }
    }
};

/**
 * Retrieves the current display configuration (intervals, views) from the server.
 * Updates the local config state and restarts rotation if settings have changed.
 */
const fetchConfig = async () => {
    try {
        const remoteConfig = await configApi.getConfig();
        if (remoteConfig) {
            // Check if rotation settings changed to restart interval
            const changed = JSON.stringify(config.value) !== JSON.stringify(remoteConfig);
            // We want to force 'dashboard' as default if rotation is disabled,
            // unless the server config explicitly says otherwise (though currently server config might not know about 'dashboard')
            // For now, we mix the remote config with our local defaults logic
            config.value = { ...config.value, ...remoteConfig };

            if (changed) {
                setupRotation();
            }
        }
    } catch (e) {
        console.error("Failed to load config", e);
    }
};

/**
 * Vue lifecycle hook: Called after the component has mounted.
 * Initializes scaling, fetches configuration, sets up initial view,
 * and starts data refresh/config polling intervals.
 */
onMounted(async () => {
  updateScale();
  // Add event listener for window resize to adjust scaling
  window.addEventListener('resize', updateScale);

  await fetchConfig(); // Fetch initial configuration

  // Set initial view logic based on rotation settings
  if (config.value.rotationIntervalSeconds === -1) {
      currentViewId.value = 'game';
  } else if (!config.value.rotationEnabled) {
      // If rotation is disabled, show the configured default single view
      currentViewId.value = config.value.defaultSingleViewId || 'dashboard';
      refreshDataForView(currentViewId.value);
  } else {
      // If rotation is enabled, start with the first view in the rotation sequence
       currentViewId.value = DISPLAY_VIEW_IDS[0];
       refreshDataForView(currentViewId.value);
  }

  setupRotation(); // Start view rotation based on fetched config

  // Poll for config changes every 30 seconds
  configPollInterval = setInterval(fetchConfig, 30000);

  // Refresh data for the current view every 30 seconds (silent refresh)
  setInterval(() => {
    // Only refresh data for the current view to avoid errors with unmounted components
    refreshDataForView(currentViewId.value, true);
  }, 30000);
});

/**
 * Vue lifecycle hook: Called before the component is unmounted.
 * Cleans up event listeners and intervals to prevent memory leaks.
 */
onUnmounted(() => {
  window.removeEventListener('resize', updateScale);
  if (viewRotationInterval) clearInterval(viewRotationInterval);
  if (configPollInterval) clearInterval(configPollInterval);
});

/**
 * Watcher: Observes `threeWeekCalendarRef` to ensure calendar data is fetched
 * once the calendar component is mounted and accessible.
 */
watch(threeWeekCalendarRef, (newRef) => {
  if (newRef && currentViewId.value === 'calendar') {
    refreshDataForView('calendar');
  }
});

/**
 * Watcher: Observes `dashboardRef` to ensure dashboard data is fetched
 * once the dashboard component is mounted and accessible.
 */
watch(dashboardRef, (newRef) => {
  if (newRef && currentViewId.value === 'dashboard') {
    // When dashboard mounts, we trigger a refresh.
    // The calendar inside it might take a tick to be ready, handled in refreshDataForView
    refreshDataForView('dashboard');
  }
});

</script>

<style scoped>
/* No specific styles needed here, children components will manage their layout */
</style>
