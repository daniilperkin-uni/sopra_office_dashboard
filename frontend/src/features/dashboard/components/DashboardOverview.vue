<template>
  <div class="h-full w-full bg-neutral-bg box-border overflow-hidden font-sans text-gray-700 p-8">
    <!-- Main Grid Layout: 3 Columns (50% / 25% / 25%) -->
    <div class="grid grid-cols-12 gap-8 h-full">
      <!-- Column 1: Calendar (approx 33% - 4/12) -->
      <div class="col-span-4 h-full relative">
        <div class="h-full w-full rounded-2xl bg-white shadow-lg overflow-hidden relative">
          <DashboardCalendar ref="calendarRef" :events="events" class="h-full w-full" />
        </div>
        <!-- Loading Overlay for Calendar -->
        <div
          v-if="loadingState.calendar"
          class="absolute inset-0 bg-white bg-opacity-80 flex items-center justify-center z-10 rounded-2xl"
        >
          <LoadingSpinner />
        </div>
      </div>

      <!-- Column 2: Parking (approx 33% - 4/12) -->
      <div class="col-span-4 h-full relative">
        <div class="h-full w-full rounded-2xl bg-white shadow-lg overflow-hidden">
          <DashboardParkingSummary :week-data="parkingData" class="h-full w-full" />
        </div>
        <div
          v-if="loadingState.parking"
          class="absolute inset-0 bg-white bg-opacity-80 flex items-center justify-center rounded-2xl z-10"
        >
          <LoadingSpinner />
        </div>
      </div>

      <!-- Column 3: Highscore (approx 33% - 4/12) -->
      <div class="col-span-4 flex flex-col gap-4 4k:gap-8 h-full min-h-0">
        <!-- Dart Leaderboard -->
        <div class="flex-1 min-h-0">
          <DashboardHighscoreColumn
            title="Darts"
            :items="highscoreData['Darts Leaderboard']"
            :is-leaderboard="true"
            class="h-full w-full"
          />
        </div>

        <!-- Kicker Leaderboard -->
        <div class="flex-1 min-h-0 relative">
          <DashboardHighscoreColumn
            title="Kicker"
            :items="highscoreData['Kicker leaderboard']"
            :is-leaderboard="true"
            class="h-full w-full"
          />
          <div
            v-if="loadingState.highscore"
            class="absolute inset-0 bg-white bg-opacity-80 flex items-center justify-center rounded-lg z-10"
          >
            <LoadingSpinner />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * The central Dashboard (OneDisplay) widget.
 * Combines Calendar, Parking Overview, and Highscore Leaderboards in a single view.
 * Uses a 3-column layout (or grid) to display all information at a glance.
 * Layout is fixed for 4K display, as it is wrapped in a global scaler.
 */
import { ref } from 'vue'
import DashboardCalendar from '@/features/dashboard/components/DashboardCalendar.vue'
import DashboardParkingSummary from '@/features/dashboard/components/DashboardParkingSummary.vue'
import DashboardHighscoreColumn from '@/features/dashboard/components/DashboardHighscoreColumn.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

// Receives all necessary data for the sub-components.
defineProps({
  events: {
    type: Object,
    default: () => ({}),
  },
  parkingData: {
    type: Array,
    default: () => [],
  },
  highscoreData: {
    type: Object,
    default: () => ({
      'Match history': [],
      'Darts Leaderboard': [],
      'Kicker leaderboard': [],
    }),
  },
  loadingState: {
    type: Object,
    default: () => ({
      calendar: false,
      parking: false,
      highscore: false,
    }),
  },
})

const calendarRef = ref(null)

// Enables access to the calendar reference from outside (e.g., for date calculations).
defineExpose({
  calendarRef,
})
</script>
