import { defineStore } from 'pinia'
import { ref } from 'vue'
import { lunchService } from '@/services/lunchService'

/**
 * Store for managing community lunch events.
 * Replaces the :key force-remount pattern in LunchAdminSection.
 */
export const useLunchStore = defineStore('lunch', () => {
  const events = ref([])
  const loading = ref(false)
  const error = ref(null)

  const refreshCounter = ref(0)

  async function fetchUpcomingLunches(days = 30) {
    loading.value = true
    try {
      const data = await lunchService.getUpcomingLunches(days)
      events.value = Array.isArray(data) ? data : []
    } catch {
      error.value = 'Lunch-Events konnten nicht geladen werden.'
    } finally {
      loading.value = false
    }
  }

  async function fetchCalendarLunches(from, to) {
    try {
      const data = await lunchService.getCalendarLunches(from, to)
      events.value = Array.isArray(data) ? data : []
    } catch {
      error.value = 'Lunch-Events konnten nicht geladen werden.'
    }
  }

  function refresh() {
    refreshCounter.value++
  }

  return {
    events, loading, error, refreshCounter,
    fetchUpcomingLunches, fetchCalendarLunches, refresh,
  }
})
