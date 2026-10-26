import { defineStore } from 'pinia'
import { ref } from 'vue'
import { parkingApi } from '@/services/api'
import { isRequestCanceled } from '@/utils/requestUtils'

/**
 * Store for managing parking data for both the display and admin views.
 */
export const useParkingStore = defineStore('parking', () => {
  const weekData = ref([])
  const loading = ref(false)
  const error = ref(null)

  const allEntries = ref([])
  const adminLoading = ref(false)
  const adminError = ref(null)

  /**
   * Laedt die Wochenuebersicht der Parkplaetze.
   * @param {boolean} [silent=false] - Unterdrueckt den Ladezustand beim Polling.
   * @param {AbortSignal} [signal] - Bricht die Anfrage ab, wenn die Ansicht verlassen wird.
   */
  async function fetchOverview(silent = false, signal) {
    if (!silent) loading.value = true
    error.value = null
    try {
      const data = await parkingApi.getOverview(signal)
      weekData.value = Array.isArray(data) ? data : []
    } catch (e) {
      // Ein Abbruch beim Verlassen der Ansicht ist kein Ladefehler.
      if (isRequestCanceled(e)) return
      console.error('Error fetching parking data:', e)
      if (!silent) error.value = 'Failed to load parking data.'
    } finally {
      loading.value = false
    }
  }

  async function fetchAllEntries() {
    adminLoading.value = true
    adminError.value = null
    try {
      const data = await parkingApi.getAllEntries()
      allEntries.value = Array.isArray(data) ? data : []
    } catch (e) {
      console.error('Error loading bookings:', e)
      adminError.value =
        'Buchungen konnten nicht geladen werden. Bitte versuchen Sie es später erneut.'
    } finally {
      adminLoading.value = false
    }
  }

  return {
    weekData,
    loading,
    error,
    allEntries,
    adminLoading,
    adminError,
    fetchOverview,
    fetchAllEntries,
  }
})
