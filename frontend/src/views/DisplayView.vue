<template>
  <div class="display-view h-screen w-full overflow-hidden bg-black relative">
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
 * Manages automatic rotation between Calendar, Parking, Highscore, and Dashboard views.
 * Delegates scaling to useDisplayScaler and rotation to useViewRotation.
 * Data fetching is handled via Pinia stores.
 */
import { ref, onMounted, onUnmounted, computed, watch, nextTick, defineAsyncComponent } from 'vue'
import { useDisplayScaler } from '@/composables/useDisplayScaler'
import { useViewRotation } from '@/composables/useViewRotation'
import { useDisplayConfigStore } from '@/stores/useDisplayConfigStore'
import { useCalendarStore } from '@/stores/useCalendarStore'
import { useParkingStore } from '@/stores/useParkingStore'
import { useHighscoreStore } from '@/stores/useHighscoreStore'
import { formatDateISO } from '@/utils/dateUtils'

import ThreeWeekCalendar from '@/features/calendar/components/ThreeWeekCalendar.vue'
import DisplayViewHighscore from '@/views/DisplayViewHighscore.vue'
import ParkingDisplayWrapper from '@/features/parking/components/ParkingDisplayWrapper.vue'
import DashboardOverview from '@/features/dashboard/components/DashboardOverview.vue'
import WeatherDisplay from '@/features/dashboard/components/WeatherDisplay.vue'

const GameView = defineAsyncComponent(() => import('@/features/game/GameView.vue'))

const DISPLAY_VIEW_IDS = ['calendar', 'parking', 'highscore', 'dashboard', 'weather']

const viewComponentsMap = {
  calendar: ThreeWeekCalendar,
  parking: ParkingDisplayWrapper,
  highscore: DisplayViewHighscore,
  dashboard: DashboardOverview,
  weather: WeatherDisplay,
  game: GameView,
}

// --- Stores ---
const configStore = useDisplayConfigStore()
const calendarStore = useCalendarStore()
const parkingStore = useParkingStore()
const highscoreStore = useHighscoreStore()

// --- Composables ---
const { scalerStyle } = useDisplayScaler()

// --- Reactive State ---
const currentViewId = ref('dashboard')
let configPollInterval = null
let dataPollInterval = null

/**
 * Starts the 30s config + data polling loops. Paused while the tab is hidden
 * (a kiosk display tab in a background window would otherwise keep polling).
 */
function startPolling() {
  if (configPollInterval || dataPollInterval) return
  configPollInterval = setInterval(() => configStore.fetchConfig(), 30000)
  dataPollInterval = setInterval(() => {
    refreshDataForView(currentViewId.value, true)
  }, 30000)
}

function stopPolling() {
  if (configPollInterval) clearInterval(configPollInterval)
  if (dataPollInterval) clearInterval(dataPollInterval)
  configPollInterval = null
  dataPollInterval = null
}

function handleVisibilityChange() {
  if (document.hidden) {
    stopPolling()
  } else {
    // Refresh immediately on return so the display is current, then resume.
    configStore.fetchConfig()
    refreshDataForView(currentViewId.value, true)
    startPolling()
  }
}

// Template refs for calendar date-range queries
const threeWeekCalendarRef = ref(null)
const dashboardRef = ref(null)

// --- View Rotation ---
const { setupRotation } = useViewRotation({
  config: computed(() => configStore.config),
  currentViewId,
  onRotate: (view) => refreshDataForView(view),
  viewIds: DISPLAY_VIEW_IDS,
})

// --- Computed ---
const currentViewComponent = computed(() => viewComponentsMap[currentViewId.value])

const currentViewProps = computed(() => {
  if (currentViewId.value === 'calendar') {
    return {
      ref: threeWeekCalendarRef,
      baseMonday: null,
      events: calendarStore.processedEvents,
      loading: calendarStore.loading,
      error: calendarStore.error,
    }
  }
  if (currentViewId.value === 'parking') {
    return {
      weekData: parkingStore.weekData,
      loading: parkingStore.loading,
      error: parkingStore.error,
      refreshData: () => refreshDataForView('parking'),
    }
  }
  if (currentViewId.value === 'highscore') {
    return {
      highscoreData: highscoreStore.displayData,
      loading: highscoreStore.loading,
      error: highscoreStore.error,
    }
  }
  if (currentViewId.value === 'dashboard') {
    return {
      ref: dashboardRef,
      events: calendarStore.processedEvents,
      parkingData: parkingStore.weekData,
      highscoreData: highscoreStore.displayData,
      loadingState: {
        calendar: calendarStore.loading,
        parking: parkingStore.loading,
        highscore: highscoreStore.loading,
      },
    }
  }
  if (currentViewId.value === 'game') {
    return {}
  }
  return {}
})

// --- Data Fetching ---
async function refreshDataForView(view, silent = false) {
  if (view === 'game') return

  if (view === 'parking' || view === 'dashboard') {
    await parkingStore.fetchOverview(silent)
  }

  if (view === 'highscore' || view === 'dashboard') {
    await highscoreStore.fetchHighscore(silent)
  }

  if (view === 'calendar' || view === 'dashboard') {
    await nextTick()

    let calendarInstance = null
    if (view === 'calendar') {
      calendarInstance = threeWeekCalendarRef.value
    } else if (view === 'dashboard' && dashboardRef.value) {
      calendarInstance = dashboardRef.value.calendarRef
    }

    let effectiveStartDate = null
    let effectiveEndDate = null

    if (calendarInstance && typeof calendarInstance.getDisplayedDates === 'function') {
      const displayedDates = calendarInstance.getDisplayedDates()
      if (displayedDates && displayedDates.length > 0) {
        const calendarDisplayStartDate = new Date(displayedDates[0])
        const thirtyDaysAgo = new Date(calendarDisplayStartDate)
        thirtyDaysAgo.setDate(calendarDisplayStartDate.getDate() - 30)
        effectiveStartDate = formatDateISO(thirtyDaysAgo)
        effectiveEndDate = displayedDates[displayedDates.length - 1]
      }
    }

    if (!effectiveStartDate) {
      const today = new Date()
      effectiveStartDate = formatDateISO(new Date(today.getFullYear(), today.getMonth(), 1))
      effectiveEndDate = formatDateISO(new Date(today.getFullYear(), today.getMonth() + 2, 0))
    }

    await calendarStore.fetchEvents(effectiveStartDate, effectiveEndDate)
  }
}

// --- Lifecycle ---
onMounted(async () => {
  await configStore.fetchConfig()

  if (configStore.config.rotationIntervalSeconds === -1) {
    currentViewId.value = 'game'
  } else if (!configStore.config.rotationEnabled) {
    currentViewId.value = configStore.config.defaultSingleViewId || 'dashboard'
    refreshDataForView(currentViewId.value)
  } else {
    currentViewId.value = DISPLAY_VIEW_IDS[0]
    refreshDataForView(currentViewId.value)
  }

  setupRotation()

  startPolling()
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onUnmounted(() => {
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  stopPolling()
})

// --- Watchers ---
watch(threeWeekCalendarRef, (newRef) => {
  if (newRef && currentViewId.value === 'calendar') {
    refreshDataForView('calendar')
  }
})

watch(dashboardRef, (newRef) => {
  if (newRef && currentViewId.value === 'dashboard') {
    refreshDataForView('dashboard')
  }
})
</script>

<style scoped>
/* No specific styles needed here, children components manage their layout */
</style>
