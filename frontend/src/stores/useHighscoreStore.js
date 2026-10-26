import { defineStore } from 'pinia'
import { ref } from 'vue'
import { highscoreApi, highscoreAdminApi } from '@/services/api'
import { isRequestCanceled } from '@/utils/requestUtils'

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

  /**
   * Laedt die Highscore-Daten fuer die Anzeige.
   * @param {boolean} [silent=false] - Unterdrueckt den Ladezustand beim Polling.
   * @param {AbortSignal} [signal] - Bricht die Anfrage ab, wenn die Ansicht verlassen wird.
   */
  async function fetchHighscore(silent = false, signal) {
    if (!silent) loading.value = true
    error.value = null
    try {
      const data = await highscoreApi.getHighscore(signal)
      displayData.value = {
        'Match history': data['Match history'] || [],
        'Darts Leaderboard': data['Darts Leaderboard'] || [],
        'Kicker leaderboard': data['Kicker leaderboard'] || [],
      }
    } catch (e) {
      // Ein Abbruch beim Verlassen der Ansicht ist kein Ladefehler.
      if (isRequestCanceled(e)) return
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
