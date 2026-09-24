<template>
  <div class="h-full w-full flex flex-col bg-white">
    <!-- Header -->
    <div class="p-10 border-b-2 border-primary/10 bg-gray-100">
      <h2 class="text-7xl font-black text-primary text-center tracking-wider">
        Kalender
      </h2>
    </div>

    <div class="flex-grow flex flex-col h-full overflow-hidden">
      <!-- Day Rows -->
      <div
        v-for="(day, index) in upcomingDays"
        :key="day.dateStr"
        class="flex flex-row w-full flex-grow min-h-0"
      >
        <!-- Left Side: Date (Flat Display) -->
        <div
          class="w-44 flex flex-col justify-center items-center flex-shrink-0 text-center px-2"
          :class="index === 0 ? 'bg-primary text-white' : 'bg-gray-50 text-gray-800'"
        >
          <div class="text-7xl font-black leading-none">
            {{ day.dayDigit }}
          </div>
          <div class="text-4xl font-bold mt-1 opacity-90">
            {{ day.monthName }}
          </div>

          <div
            v-if="index === 0"
            class="mt-5 text-2xl font-black opacity-90"
          >
            Heute
          </div>
          <div
            v-else
            class="mt-3 text-2xl font-semibold opacity-50"
          >
            {{ day.weekdayShort }}
          </div>
        </div>

        <!-- Right Side: Content -->
        <div
          class="flex-1 flex flex-col justify-center px-12 py-4 bg-white"
          :class="
            index !== upcomingDays.length - 1 ? 'shadow-[inset_0_-1px_0_rgba(0,0,0,0.55)]' : ''
          "
        >
          <!-- Empty State -->
          <div
            v-if="day.events.length === 0"
            class="flex items-center text-gray-400 gap-6"
          >
            <span class="opacity-50">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                stroke-width="2"
                stroke="currentColor"
                class="w-12 h-12"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  d="M6.75 3v2.25M17.25 3v2.25M3 18.75V7.5a2.25 2.25 0 012.25-2.25h13.5A2.25 2.25 0 0121 7.5v11.25m-18 0h18M5.25 12h13.5h-13.5zm0 3.75h13.5h-13.5z"
                />
              </svg>
            </span>
            <span class="text-4xl font-bold text-gray-400">Keine Termine</span>
          </div>

          <!-- Events List -->
          <div
            v-else
            class="flex flex-col gap-8"
          >
            <div
              v-for="(event, eIndex) in day.events"
              :key="eIndex"
              class="flex items-center w-full"
            >
              <!-- Text -->
              <div class="flex flex-col justify-center">
                <span class="text-5xl font-bold text-gray-900 leading-tight">
                  {{ parseEvent(event).label }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * A calendar component for the dashboard (OneDisplay).
 * Displays the next 3 days (Today + 2) in a vertical list.
 * Optimized for large 4K displays with readable typography and icons.
 */
import { computed, ref, onMounted, watch } from 'vue'
import { lunchService } from '@/services/lunchService'

// Receives the calendar events as an object, grouped by date.
const props = defineProps({
  events: {
    type: Object,
    default: () => ({}),
  },
})

const lunchData = ref({})

/**
 * Loads lunch data for the displayed days.
 */
const fetchLunchData = async () => {
  const dates = upcomingDays.value.map((day) => day.dateStr)
  if (dates.length === 0) return

  try {
    const events = await lunchService.getCalendarLunches(dates[0], dates[dates.length - 1])
    if (!Array.isArray(events)) return

    const initialMap = {}
    for (const e of events) {
      let label = 'Lunch'
      if (e.note) {
        label += ` (${e.note})`
      }
      initialMap[e.date] = label
    }
    // Update immediately with basic info
    lunchData.value = { ...lunchData.value, ...initialMap }

    // Asynchronously update with results
    events.forEach(async (e) => {
      try {
        const resultsData = await lunchService.getResults(e.id)
        if (resultsData && Array.isArray(resultsData.results) && resultsData.results.length > 0) {
          const topOption = resultsData.results[0]
          if (topOption.count > 0) {
            let newLabel = `Lunch`
            if (e.note) {
              newLabel += ` (${e.note})`
            }
            newLabel += ` : ${topOption.label}`
            lunchData.value[e.date] = newLabel
          }
        }
      } catch (resError) {
        console.error(`Error loading lunch results for event ${e.id}`, resError)
      }
    })
  } catch (error) {
    console.error('Error loading lunch data for dashboard calendar', error)
  }
}

onMounted(() => {
  fetchLunchData()
})

// Reload when events change (often implies date changes)
watch(
  () => props.events,
  () => {
    fetchLunchData()
  },
  { deep: true }
)

// Extracts the label to display from the event object.
const parseEvent = (event) => {
  return {
    label: event.label || event.title || event.dashboardEventDescription,
  }
}

// Calculates the list of the next 3 days for display.
const upcomingDays = computed(() => {
  const days = []
  const today = new Date()

  for (let i = 0; i < 3; i++) {
    const date = new Date(today)
    date.setDate(today.getDate() + i)

    const year = date.getFullYear()
    const month = (date.getMonth() + 1).toString().padStart(2, '0')
    const dayOfMonth = date.getDate().toString().padStart(2, '0')
    const dateStr = `${year}-${month}-${dayOfMonth}`

    const dayDigit = date.getDate().toString().padStart(2, '0')
    const monthName = date.toLocaleDateString('de-DE', { month: 'short' }).replace('.', '')
    const weekdayShort = date.toLocaleDateString('de-DE', { weekday: 'short' })

    // Existing events
    const dayEvents = [...(props.events[dateStr] || [])]

    // Integrate Community Lunch
    if (lunchData.value[dateStr]) {
      dayEvents.unshift({
        label: `🍴 ${lunchData.value[dateStr]}`,
        color: 'lunch',
        isLunch: true,
      })
    }

    days.push({
      dateStr,
      dayDigit,
      monthName,
      weekdayShort,
      events: dayEvents,
    })
  }
  return days
})

// Exposes a method to retrieve the displayed dates (important for API calls).
defineExpose({
  getDisplayedDates: () => upcomingDays.value.map((day) => day.dateStr),
})
</script>
