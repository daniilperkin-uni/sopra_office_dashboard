import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { calendarApi } from '@/services/api'
import { processCalendarEvents } from '@/utils/eventUtils'

/**
 * Store for managing calendar events fetched from the backend.
 * Owns the raw events, loading/error state, and the processed events computed.
 */
export const useCalendarStore = defineStore('calendar', () => {
  const rawEvents = ref([])
  const loading = ref(false)
  const error = ref('')

  const processedEvents = computed(() => processCalendarEvents(rawEvents.value))

  async function fetchEvents(startDate, endDate) {
    loading.value = true
    error.value = ''
    try {
      const events = await calendarApi.getEvents(startDate, endDate)
      rawEvents.value = Array.isArray(events) ? events : []
    } catch (e) {
      console.error('Error fetching calendar events:', e)
      error.value = 'Failed to load calendar data.'
    } finally {
      loading.value = false
    }
  }

  function reset() {
    rawEvents.value = []
    error.value = ''
  }

  return { rawEvents, processedEvents, loading, error, fetchEvents, reset }
})
