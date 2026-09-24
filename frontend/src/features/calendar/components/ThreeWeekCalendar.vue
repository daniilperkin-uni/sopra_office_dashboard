<template>
  <div class="modern-calendar-container flex flex-col h-full bg-neutral-bg">
    <!-- Header specifically for Calendar View -->
    <header class="bg-gray-100 text-primary shadow-sm w-full border-b border-gray-200">
      <div class="px-4 py-3 flex items-center">
        <h1 class="text-7xl font-black flex-grow text-center tracking-wider">
          itestra Kalender
        </h1>
      </div>
    </header>

    <div
      class="calendar-grid"
      :class="{
        'overflow-mode': isOverflowMode && !isSuperOverflowMode,
        'super-overflow-mode': isSuperOverflowMode,
      }"
    >
      <!-- Standard & Split Overflow View -->
      <template v-if="!isSuperOverflowMode">
        <!-- Sub-Header Row for Weeks and Notes Label -->
        <template v-if="calendarRows.length > 0">
          <div
            class="header-cell weekday-label"
            style="grid-column: weekday; grid-row: 1"
          >
            Tag
          </div>
          <div
            class="header-cell week-header is-week-0-header"
            style="grid-column: week1; grid-row: 1"
          >
            {{ weekMonthNames[0] }}
          </div>
          <div
            class="header-cell notes-header is-week-0-header"
            style="grid-column: notes1; grid-row: 1"
          >
            Ereignisse
          </div>
          <!-- Default Headers for Week 2 -->
          <div
            v-if="!isOverflowMode"
            class="header-cell week-header"
            style="grid-column: week2; grid-row: 1"
          >
            {{ weekMonthNames[1] }}
          </div>
          <div
            v-if="!isOverflowMode"
            class="header-cell notes-header"
            style="grid-column: notes2; grid-row: 1"
          >
            Ereignisse
          </div>
          <!-- Header for Overflow Mode -->
          <div
            v-if="isOverflowMode"
            class="header-cell notes-header"
            style="grid-column: today-overflow; grid-row: 1"
          >
            Ereignisse heute
          </div>
        </template>

        <template
          v-for="(row, rowIndex) in calendarRows"
          :key="rowIndex"
        >
          <!-- Weekday Label Column -->
          <div
            class="weekday-label"
            :style="{ 'grid-row': rowIndex + 2 }"
          >
            {{ row.weekdayLabel }}
          </div>

          <!-- Day Cells for each Row -->

          <template
            v-for="day in row.days"
            :key="day.isoDate"
          >
            <div
              v-if="!isOverflowMode || day.weekIndex === 0"
              :class="[
                'day-cell',
                {
                  'is-today': day.isToday,
                  'is-weekend': day.dayOfWeekIndex === 5 || day.dayOfWeekIndex === 6,
                  'is-week-0': day.weekIndex === 0,
                  'is-week-1': day.weekIndex === 1,
                  'opacity-50': isDatePast(day.date),
                },
              ]"
              :style="{ 'grid-column': getDayNumberColumn(day), 'grid-row': rowIndex + 2 }"
              :aria-label="day.ariaLabel"
            >
              <span class="day-number">{{ day.dayNumber }}</span>
            </div>
          </template>

          <!-- Notes Column for Week 1 -->

          <div
            class="notes-cell"
            :style="{ 'grid-column': 'notes1', 'grid-row': rowIndex + 2 }"
          >
            <template
              v-for="day in row.days.filter((d) => d.weekIndex === 0)"
              :key="day.isoDate"
            >
              <template v-if="day.notes.length > 0 && !(isOverflowMode && day.isToday)">
                <div
                  v-for="(note, nIndex) in day.notes.slice(0, 4)"
                  :key="nIndex"
                  class="event-note"
                  :style="{ backgroundColor: getEventStyles(note.color).noteBg }"
                  :aria-label="note.label"
                >
                  <span
                    class="event-text"
                    :style="{ color: getEventStyles(note.color).noteText }"
                  >{{ note.label }}</span>
                </div>

                <div
                  v-if="day.notes.length > 4"
                  class="event-note-more"
                  :aria-label="`Zeige weitere Ereignisse für ${day.isoDate}`"
                >
                  + {{ day.notes.length - 4 }} weitere
                </div>
              </template>
            </template>
          </div>

          <!-- Notes Column for Week 2 (only in default mode) -->

          <div
            v-if="!isOverflowMode"
            class="notes-cell"
            :style="{ 'grid-column': 'notes2', 'grid-row': rowIndex + 2 }"
          >
            <template
              v-for="day in row.days.filter((d) => d.weekIndex === 1)"
              :key="day.isoDate"
            >
              <template v-if="day.notes.length > 0">
                <div
                  v-for="(note, nIndex) in day.notes.slice(0, 4)"
                  :key="nIndex"
                  class="event-note"
                  :style="{ backgroundColor: getEventStyles(note.color).noteBg }"
                  :aria-label="note.label"
                >
                  <span
                    class="event-text"
                    :style="{ color: getEventStyles(note.color).noteText }"
                  >{{ note.label }}</span>
                </div>

                <div
                  v-if="day.notes.length > 4"
                  class="event-note-more"
                  :aria-label="`Zeige weitere Ereignisse für ${day.isoDate}`"
                >
                  + {{ day.notes.length - 4 }} weitere
                </div>
              </template>
            </template>
          </div>
        </template>

        <!-- Overflow Column for Today's Events (Split View) -->

        <div
          v-if="isOverflowMode && overflowDay"
          class="today-overflow-container"
          style="grid-column: today-overflow; grid-row: 2 / span 7"
        >
          <div
            v-for="(note, nIndex) in overflowDay.notes"
            :key="nIndex"
            class="event-note"
            :style="{ backgroundColor: getEventStyles(note.color).noteBg }"
          >
            <span
              class="event-icon"
              :style="{
                backgroundColor: getEventStyles(note.color).iconBg,
                border: getEventStyles(note.color).iconBorder,
              }"
              aria-hidden="true"
            />

            <span
              class="event-text"
              :style="{ color: getEventStyles(note.color).noteText }"
            >{{
              note.label
            }}</span>
          </div>
        </div>
      </template>

      <!-- Super Overflow View (Full Screen) -->

      <div
        v-if="isSuperOverflowMode && overflowDay"
        class="super-overflow-container"
      >
        <h2
          class="header-cell notes-header"
          style="margin-bottom: 2rem"
        >
          Heute: {{ overflowDay.notes.length }} Ereignisse
        </h2>

        <div class="super-events-grid">
          <div
            v-for="(note, nIndex) in overflowDay.notes"
            :key="nIndex"
            class="event-note"
            :style="{ backgroundColor: getEventStyles(note.color).noteBg }"
          >
            <span
              class="event-icon"
              :style="{
                backgroundColor: getEventStyles(note.color).iconBg,
                border: getEventStyles(note.color).iconBorder,
              }"
              aria-hidden="true"
            />

            <span
              class="event-text"
              :style="{ color: getEventStyles(note.color).noteText }"
            >{{
              note.label
            }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * A calendar component that displays events across a two-week view (current and next week).
 * It is optimized for 4K displays and features dynamic overflow modes to handle a large number of events.
 * The component fetches and displays various types of events, including calendar and community lunch events.
 */
import { computed, ref, onMounted, watch } from 'vue'
import { lunchService } from '@/services/lunchService'

/**
 * Props defined for the ThreeWeekCalendar component.
 */
const props = defineProps({
  /**
   * Optional: The starting Monday for the calendar view. If null, it defaults to the current week's Monday.
   * Can be a Date object or an ISO string (YYYY-MM-DD).
   */
  baseMonday: {
    type: [Date, String],
    default: null,
  },
  /**
   * An object containing calendar events, where keys are ISO date strings (YYYY-MM-DD)
   * and values are arrays of event objects for that day.
   */
  events: {
    type: Object,
    default: () => ({}),
  },
})

/**
 * Defines a mapping of event color keys to their respective CSS style properties
 * for background, text, and icon.
 * Used to ensure consistent styling across different event types.
 */
const COLOR_MAPPING = {
  green: {
    noteBg: 'rgba(16, 185, 129, 0.1)',
    noteText: '#10b981',
    iconBg: '#10b981',
    iconBorder: 'transparent',
  },
  purple: {
    noteBg: 'rgba(11, 79, 240, 0.1)',
    noteText: '#009ee2',
    iconBg: '#009ee2',
    iconBorder: 'transparent',
  },
  default: {
    noteBg: 'rgba(44, 62, 80, 0.05)',
    noteText: '#2c3e50',
    iconBg: 'transparent',
    iconBorder: '1px solid rgba(60, 60, 60, 0.5)',
  },
  lunch: {
    noteBg: 'rgba(255, 152, 0, 0.1)',
    noteText: '#e67e22',
    iconBg: '#e67e22',
    iconBorder: 'transparent',
  },
}

/**
 * Returns the style configuration for an event based on its color name.
 * @param {string} colorName - The name of the color (e.g., 'green', 'purple', 'lunch').
 * @returns {object} An object containing `noteBg`, `noteText`, `iconBg`, and `iconBorder` CSS properties.
 */
const getEventStyles = (colorName) => {
  const styles = COLOR_MAPPING[colorName] || COLOR_MAPPING.default
  return {
    noteBg: styles.noteBg,
    noteText: styles.noteText,
    iconBg: styles.iconBg,
    iconBorder: styles.iconBorder,
  }
}

/**
 * Array of short weekday names for display.
 */
const WEEKDAY_NAMES = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
/**
 * Number of weeks to display in the calendar.
 */
const NUM_WEEKS = 2
/**
 * Number of days in a standard week.
 */
const DAYS_PER_WEEK = 7

/**
 * Determines the grid column name for a given day based on its week index.
 * @param {object} day - The day object.
 * @returns {string} 'week1' or 'week2'.
 */
const getDayNumberColumn = (day) => {
  return day.weekIndex === 0 ? 'week1' : 'week2'
}

/**
 * Formats a Date object into an ISO 8601 string (YYYY-MM-DD).
 * @param {Date} date - The date to format.
 * @returns {string} The formatted date string.
 */
const formatIsoDate = (date) => {
  const year = date.getFullYear()
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  return `${year}-${month}-${day}`
}

/**
 * Parses various date formats (Date object, ISO string) into a standardized Date object
 * with time set to midnight (00:00:00).
 * @param {Date|string} dateInput - The date input to parse.
 * @returns {Date} A new Date object representing the date at midnight.
 */
const parseDate = (dateInput) => {
  if (dateInput instanceof Date) {
    return new Date(dateInput.getFullYear(), dateInput.getMonth(), dateInput.getDate())
  }
  if (typeof dateInput === 'string') {
    const parts = dateInput.split('-').map(Number)
    // Month is 0-indexed in JavaScript Date objects
    return new Date(parts[0], parts[1] - 1, parts[2])
  }
  return new Date()
}

/**
 * Calculates the most recent Monday (start of the week) for a given date.
 * @param {Date} date - The reference date.
 * @returns {Date} A new Date object representing the Monday of that week.
 */
const getMostRecentMonday = (date) => {
  const day = date.getDay()
  // Adjust to ensure Monday is the start of the week (0 for Sunday, 1 for Monday, etc.)
  const diff = date.getDate() - day + (day === 0 ? -6 : 1)
  return new Date(date.getFullYear(), date.getMonth(), diff)
}

/**
 * Checks if two Date objects represent the same day (ignoring time).
 * @param {Date} d1 - The first date.
 * @param {Date} d2 - The second date.
 * @returns {boolean} True if they are the same day, false otherwise.
 */
const isSameDay = (d1, d2) => {
  return (
    d1.getFullYear() === d2.getFullYear() &&
    d1.getMonth() === d2.getMonth() &&
    d1.getDate() === d2.getDate()
  )
}

/**
 * Computed property that determines the Monday of the first displayed week.
 * It uses the `baseMonday` prop or defaults to the current week's Monday.
 */
const firstMonday = computed(() => {
  const today = new Date()
  today.setHours(0, 0, 0, 0) // Normalize today to start of day
  const inputDate = props.baseMonday ? parseDate(props.baseMonday) : today
  return getMostRecentMonday(inputDate)
})

/**
 * Generates a comprehensive dataset for all displayed days across the two weeks.
 * Each day includes its date, events, and various flags for styling and logic.
 */
const displayedDaysData = computed(() => {
  const daysData = []
  const today = new Date()
  today.setHours(0, 0, 0, 0) // Normalize today to start of day

  for (let week = 0; week < NUM_WEEKS; week++) {
    for (let dayOfWeek = 0; dayOfWeek < DAYS_PER_WEEK; dayOfWeek++) {
      const currentDate = new Date(firstMonday.value)
      currentDate.setDate(firstMonday.value.getDate() + week * DAYS_PER_WEEK + dayOfWeek)

      const isCurrentDay = isSameDay(currentDate, today)
      const isoDate = formatIsoDate(currentDate)
      const dayEventsArray = props.events[isoDate]

      // Combine calendar events with lunch events
      const notes =
        dayEventsArray && dayEventsArray.length > 0
          ? dayEventsArray.map((e) => ({ label: e.label, ...e }))
          : []

      if (lunchData.value[isoDate]) {
        notes.unshift({
          label: `🍴 ${lunchData.value[isoDate]}`,
          color: 'lunch',
        })
      }

      // Create unique event indicators based on color (e.g., for visual cues)
      const eventIndicators = Array.from(new Set(notes.map((note) => note.color))).map((color) => ({
        color: color || 'default',
      }))

      daysData.push({
        date: currentDate,
        isoDate: isoDate,
        dayNumber: currentDate.getDate(),
        dayOfWeekIndex: dayOfWeek,
        weekIndex: week,
        isToday: isCurrentDay,
        notes: notes,
        eventIndicators: eventIndicators,
        ariaLabel: `${WEEKDAY_NAMES[dayOfWeek]} ${currentDate.getDate()} ${currentDate.toLocaleString('en-US', { month: 'long' })} ${currentDate.getFullYear()}`,
      })
    }
  }
  return daysData
})

/**
 * Organizes the `displayedDaysData` into rows, one for each weekday.
 * This structure simplifies rendering the calendar grid by rows.
 */
const calendarRows = computed(() => {
  const rows = []
  for (let dayOfWeek = 0; dayOfWeek < DAYS_PER_WEEK; dayOfWeek++) {
    const rowDays = displayedDaysData.value.filter((day) => day.dayOfWeekIndex === dayOfWeek)
    rows.push({
      weekdayLabel: WEEKDAY_NAMES[dayOfWeek],
      days: rowDays,
    })
  }
  return rows
})

/**
 * Provides the month names for the displayed weeks.
 * Used in the calendar header to indicate the month of each week.
 */
const weekMonthNames = computed(() => {
  const monthNames = []
  for (let week = 0; week < NUM_WEEKS; week++) {
    const daysInWeek = displayedDaysData.value.filter((day) => day.weekIndex === week)
    if (daysInWeek.length === 0) {
      monthNames.push('')
      continue
    }
    // Get month name in English
    monthNames.push(daysInWeek[0].date.toLocaleString('en-US', { month: 'long' }))
  }
  return monthNames
})

/**
 * Computed property that identifies if "today" has more than 4 events,
 * triggering the split-view or super-overflow mode for today's events.
 */
const overflowDay = computed(() =>
  displayedDaysData.value.find((day) => day.isToday && day.notes.length > 4)
)

/**
 * Computed property indicating if the calendar should render in "overflow mode" (split view).
 */
const isOverflowMode = computed(() => !!overflowDay.value)
/**
 * Computed property indicating if the calendar should render in "super overflow mode" (full-screen grid).
 */
const isSuperOverflowMode = computed(
  () => !!overflowDay.value && overflowDay.value.notes.length > 16
)

/**
 * Checks if a given date is in the past relative to today.
 * @param {Date} date - The date to check.
 * @returns {boolean} True if the date is in the past, false otherwise.
 */
const isDatePast = (date) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0) // Normalize today to start of day
  return date < today
}

/**
 * Reactive reference to store fetched lunch data, keyed by ISO date string.
 */
const lunchData = ref({})

/**
 * Fetches community lunch data for the currently displayed calendar range.
 * It also processes the results to determine the top-voted dish for each lunch event.
 */
const fetchLunchData = async () => {
  if (displayedDaysData.value.length === 0) return

  const startDate = displayedDaysData.value[0].isoDate
  const endDate = displayedDaysData.value[displayedDaysData.value.length - 1].isoDate

  try {
    const events = await lunchService.getCalendarLunches(startDate, endDate)
    if (!Array.isArray(events)) return

    const initialMap = {}
    for (const e of events) {
      let label = 'Lunch'
      if (e.note) {
        label += ` (${e.note})`
      }
      initialMap[e.date] = label
    }
    // Immediately update the UI with basic lunch info
    lunchData.value = { ...lunchData.value, ...initialMap }

    // Asynchronously fetch and update with voting results
    events.forEach(async (e) => {
      try {
        const resultsData = await lunchService.getResults(e.id)
        if (resultsData && Array.isArray(resultsData.results) && resultsData.results.length > 0) {
          const topOption = resultsData.results[0]
          if (topOption.count > 0) {
            // Rebuild the label with the result and update the specific date
            let newLabel = `Lunch`
            if (e.note) {
              newLabel += ` (${e.note})`
            }
            newLabel += ` : ${topOption.label}`
            lunchData.value[e.date] = newLabel
          }
        }
      } catch (resError) {
        console.error(`Error loading results for lunch ${e.id}`, resError)
      }
    })
  } catch (error) {
    console.error('Error fetching lunch data for calendar', error)
  }
}

/**
 * Lifecycle hook: Called after the component has mounted.
 * Initiates fetching of lunch data.
 */
onMounted(() => {
  fetchLunchData()
})

/**
 * Watcher: Triggers `fetchLunchData` whenever the `firstMonday` (start of the displayed week) changes,
 * ensuring lunch data is up-to-date for the visible calendar range.
 */
watch(
  () => firstMonday.value,
  () => {
    fetchLunchData()
  }
)

/**
 * Exposes internal methods or properties to the parent component using `ref`.
 * Allows parent components (e.g., DisplayView) to access displayed dates.
 */
defineExpose({
  getDisplayedDates: () => displayedDaysData.value.map((day) => day.isoDate),
})
</script>

<style scoped>
/* Styles remain unchanged as they are purely visual/structural */
.modern-calendar-container {
  width: 100%;
  max-width: 100%;
  overflow: hidden;
  background: #ffffff;
  color: #2c3e50;
  margin: 0;
}

.calendar-grid {
  background: #ffffff;
  padding: 1rem 3rem 3rem 3rem;
  display: grid;
  grid-template-columns: [weekday] 200px [week1] 300px [notes1] 1.5fr [week2] 300px [notes2] 1.5fr;
  grid-template-rows: auto repeat(7, auto);
  gap: 2rem;
}

.header-cell {
  background-color: #f8f9fa;
  font-weight: 700;
  font-size: 2.7rem;
  color: #2c3e50;
  text-align: center;
  padding: 15px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.weekday-label {
  background-color: transparent;
  font-weight: 700;
  font-size: 2.7rem;
  color: #2c3e50;
  justify-content: flex-start;
  padding-left: 10px;
  grid-column: weekday;
}

.day-cell {
  background-color: #f5f7fa;
  border: 1px solid rgba(60, 60, 60, 0.12);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease-in-out;
  position: relative;
  min-height: 0;
}

.day-number {
  font-size: 3.85rem;
  font-weight: 500;
  color: #2c3e50;
  z-index: 1;
}

.day-cell.is-today {
  background-color: #e6f7ff;
  border: 1px solid #009ee2;
}

.notes-cell {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-start;
  justify-content: flex-start;
  background-color: transparent;
  padding-top: 10px;
}

.event-note {
  background-color: #f3f4f6;
  color: #2c3e50;
  padding: 8px 14px;
  border-radius: 12px;
  font-size: 2.95rem;
  transition:
    background-color 0.2s ease,
    transform 0.2s ease;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
}

.event-note-more {
  background: none;
  font-size: 2.95rem;
  color: #009ee2;
  padding: 8px 14px;
  transition: color 0.2s ease;
}

.calendar-grid.overflow-mode {
  grid-template-columns: [weekday] 80px [week1] 0.5fr [notes1] 1.5fr [today-overflow] 2.1fr;
}

.today-overflow-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-start;
  justify-content: flex-start;
  background-color: #f8f9fa;
  padding: 1rem;
  border-radius: 12px;
  border: 1px solid rgba(60, 60, 60, 0.12);
}

.super-events-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
  align-content: start;
}
</style>
