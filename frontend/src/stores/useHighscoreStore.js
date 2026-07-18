import { defineStore } from 'pinia'
import { ref } from 'vue'
import { highscoreApi, highscoreAdminApi } from '@/services/api'

/**
 * Store for managing highscore data for both display and admin views.
 */
export const useHighscoreStore = defineStore('highscore', () => {
  const displayData = ref({
    'Match history': [],
    'Darts Leaderboard': [],
    'Kicker leaderboard': [],
  })
  const loading = ref(false)
  const error = ref(null)

  const dartsMatches = ref([])
  const kickerMatches = ref([])
  const adminLoading = ref(false)
  const adminError = ref(null)

  async function fetchHighscore(silent = false) {
    if (!silent) loading.value = true
    error.value = null
    try {
      const data = await highscoreApi.getHighscore()
      displayData.value = {
        'Match history': data['Match history'] || [],
        'Darts Leaderboard': data['Darts Leaderboard'] || [],
        'Kicker leaderboard': data['Kicker leaderboard'] || [],
      }
    } catch (e) {
      console.error('Error fetching highscore data:', e)
      if (!silent) error.value = 'Failed to load highscore data.'
    } finally {
      loading.value = false
    }
  }

  async function fetchAdminMatches() {
    adminLoading.value = true
    adminError.value = null
    try {
      const [darts, kicker] = await Promise.all([
        highscoreAdminApi.getDartsMatches(),
        highscoreAdminApi.getKickerMatches(),
      ])
      dartsMatches.value = Array.isArray(darts) ? darts : []
      kickerMatches.value = Array.isArray(kicker) ? kicker : []
    } catch (e) {
      console.error('Error fetching admin matches:', e)
      adminError.value = 'Spiele konnten nicht geladen werden.'
    } finally {
      adminLoading.value = false
    }
  }

  return {
    displayData,
    loading,
    error,
    dartsMatches,
    kickerMatches,
    adminLoading,
    adminError,
    fetchHighscore,
    fetchAdminMatches,
  }
})
