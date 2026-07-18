import { defineStore } from 'pinia'
import { ref } from 'vue'
import { parkingApi } from '@/services/api'

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

  async function fetchOverview(silent = false) {
    if (!silent) loading.value = true
    error.value = null
    try {
      const data = await parkingApi.getOverview()
      weekData.value = Array.isArray(data) ? data : []
    } catch (e) {
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
      adminError.value = 'Buchungen konnten nicht geladen werden. Bitte versuchen Sie es später erneut.'
    } finally {
      adminLoading.value = false
    }
  }

  return {
    weekData, loading, error,
    allEntries, adminLoading, adminError,
    fetchOverview, fetchAllEntries,
  }
})
