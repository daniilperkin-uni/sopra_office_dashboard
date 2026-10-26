import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { calendarApi } from '@/services/api'
import { processCalendarEvents } from '@/utils/eventUtils'
import { isRequestCanceled } from '@/utils/requestUtils'

/**
 * Store for managing calendar events fetched from the backend.
 * Owns the raw events, loading/error state, and the processed events computed.
 */
export const useCalendarStore = defineStore('calendar', () => {
  const rawEvents = ref([])
  const loading = ref(false)
  const error = ref('')

  const processedEvents = computed(() => processCalendarEvents(rawEvents.value))

  /**
   * Laedt die Kalender-Events fuer einen Zeitraum.
   * @param {string} [startDate] - Startdatum (YYYY-MM-DD).
   * @param {string} [endDate] - Enddatum (YYYY-MM-DD).
   * @param {AbortSignal} [signal] - Bricht die Anfrage ab, wenn die Ansicht verlassen wird.
   */
  async function fetchEvents(startDate, endDate, signal) {
    loading.value = true
    error.value = ''
    try {
      const events = await calendarApi.getEvents(startDate, endDate, signal)
      rawEvents.value = Array.isArray(events) ? events : []
    } catch (e) {
      // Ein Abbruch beim Verlassen der Ansicht ist kein Ladefehler.
      if (isRequestCanceled(e)) return
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
