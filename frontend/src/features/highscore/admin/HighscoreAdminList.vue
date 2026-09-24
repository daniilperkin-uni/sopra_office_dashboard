<template>
  <div class="p-4 sm:p-6">
    <h1 class="text-2xl sm:text-3xl font-extrabold text-black mb-6">
      Highscore
    </h1>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-4 sm:gap-8 mb-8">
      <!-- Darts Section -->
      <div
        class="bg-white dark:bg-gray-800 p-4 sm:p-6 rounded-lg shadow-md border-t-4 border-primary"
      >
        <h2 class="text-xl sm:text-2xl font-bold text-primary mb-4">
          Darts
        </h2>
        <p class="text-black mb-6 text-sm sm:text-base">
          Erfasse eine neue Darts-Session für einen Mitarbeiter.
        </p>
        <BaseButton
          variant="primary"
          class="w-full shadow-lg"
          @click="openForm('darts')"
        >
          Darts-Match hinzufügen
        </BaseButton>
      </div>

      <!-- Kicker Section -->
      <div
        class="bg-white dark:bg-gray-800 p-4 sm:p-6 rounded-lg shadow-md border-t-4 border-primary"
      >
        <h2 class="text-xl sm:text-2xl font-bold text-primary mb-4">
          Kicker
        </h2>
        <p class="text-black mb-6 text-sm sm:text-base">
          Erfasse ein neues Kicker-Match-Ergebnis.
        </p>
        <BaseButton
          variant="primary"
          class="w-full shadow-lg"
          @click="openForm('kicker')"
        >
          Kicker-Match hinzufügen
        </BaseButton>
      </div>
    </div>

    <div class="border-t border-gray-200 dark:border-gray-700 pt-8">
      <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-6 gap-4">
        <h2 class="text-2xl font-bold text-black">
          Spielverlauf
        </h2>
        <BaseButton
          v-if="dartsMatches.length > 3 || kickerMatches.length > 3"
          variant="danger"
          size="small"
          class="w-full sm:w-auto"
          @click="deleteNonTopMatches"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-4 w-4 mr-2"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
            />
          </svg>
          Alle nicht Top-Spiele löschen
        </BaseButton>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <!-- Darts History -->
        <div>
          <h3 class="text-xl font-semibold text-black mb-4">
            Darts-Matches
          </h3>
          <div
            v-if="dartsMatches.length === 0"
            class="text-black italic"
          >
            Keine Darts-Matches aufgezeichnet.
          </div>
          <div
            v-else
            class="space-y-2"
          >
            <HighscoreMatchItem
              v-for="match in dartsMatches"
              :key="match.id"
              :match="match"
              type="darts"
              @delete="handleDelete"
              @edit="handleEdit"
            />
          </div>
        </div>

        <!-- Kicker History -->
        <div>
          <h3 class="text-xl font-semibold text-black mb-4">
            Kicker-Matches
          </h3>
          <div
            v-if="kickerMatches.length === 0"
            class="text-black italic"
          >
            Keine Kicker-Matches aufgezeichnet.
          </div>
          <div
            v-else
            class="space-y-2"
          >
            <HighscoreMatchItem
              v-for="match in kickerMatches"
              :key="match.id"
              :match="match"
              type="kicker"
              @delete="handleDelete"
              @edit="handleEdit"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- Feedback message -->
    <div
      v-if="message"
      :class="[
        'mt-6 p-4 rounded-md',
        message.type === 'error' ? 'bg-red-100 text-red-700' : 'bg-green-100 text-green-700',
      ]"
    >
      {{ message.text }}
    </div>

    <!-- Form Modal -->
    <div
      v-if="showFormModal"
      class="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full flex items-center justify-center z-50 p-2 sm:p-4"
    >
      <div class="bg-white dark:bg-gray-800 p-4 sm:p-6 rounded-lg shadow-xl max-w-lg w-full">
        <HighscoreAdminForm
          :type="formType"
          :initial-data="initialFormData"
          @submit="handleFormSubmit"
          @cancel="closeForm"
        />
      </div>
    </div>

    <div
      v-if="loading"
      class="fixed inset-0 bg-black bg-opacity-25 flex items-center justify-center z-[60]"
    >
      <LoadingSpinner />
    </div>

    <ConfirmDialog
      :is-open="showBulkDeleteDialog"
      title="Spiele löschen"
      :message="`Möchten Sie wirklich alle ${bulkDeleteInfo.total} Spiele löschen, die keine Top-Leistungen sind?\n\n- Darts: Nur die 3 Bestleistungen bleiben erhalten.\n- Kicker: Nur Siege der aktuellen Top 3 Teams bleiben erhalten.`"
      confirm-text="Löschen"
      cancel-text="Abbrechen"
      @confirm="confirmBulkDelete"
      @cancel="showBulkDeleteDialog = false"
    />
  </div>
</template>

<script setup>
/**
 * Main component for highscore management in the admin area.
 * Allows viewing match history, adding new Darts and Kicker results,
 * and deleting existing entries.
 */
import { ref, onMounted, computed } from 'vue'
import { useHighscoreStore } from '@/stores/useHighscoreStore'
import { highscoreAdminApi } from '@/services/api'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import HighscoreAdminForm from './HighscoreAdminForm.vue'
import HighscoreMatchItem from './HighscoreMatchItem.vue'

const highscoreStore = useHighscoreStore()
const loading = ref(false)
const message = ref(null)

const showFormModal = ref(false)
const formType = ref('darts')
const initialFormData = ref(null)
const editingMatchId = ref(null)

const dartsMatches = computed(() => highscoreStore.dartsMatches)
const kickerMatches = computed(() => highscoreStore.kickerMatches)

const showBulkDeleteDialog = ref(false)
const bulkDeleteInfo = ref({ total: 0, dartsCount: 0, kickerCount: 0 })

/**
 * Deletes all matches that are not "top performances".
 * - For Darts, the 3 games with the highest scores remain.
 * - For Kicker, all wins of teams currently in the top 3 of the leaderboard remain.
 */
const deleteNonTopMatches = async () => {
  // --- DARTS LOGIC ---
  const topDartsIds = [...dartsMatches.value]
    .sort((a, b) => a.dartsToFinish - b.dartsToFinish)
    .slice(0, 3)
    .map((m) => m.id)
  const dartsToDelete = dartsMatches.value.filter((m) => !topDartsIds.includes(m.id))

  // --- KICKER LOGIC ---
  // 1. Determine top teams (same logic as in backend/display)
  const normalizeTeam = (players) => [...(players || [])].sort().join(' + ')
  const winCounts = {}

  kickerMatches.value.forEach((m) => {
    let winnerTeam = null
    if (m.matchResult === 'TEAM_A_WIN') {
      winnerTeam = normalizeTeam(m.teamAPlayerNames)
    } else if (m.matchResult === 'TEAM_B_WIN') {
      winnerTeam = normalizeTeam(m.teamBPlayerNames)
    }

    if (winnerTeam) {
      winCounts[winnerTeam] = (winCounts[winnerTeam] || 0) + 1
    }
  })

  const topTeams = Object.entries(winCounts)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 3)
    .map((entry) => entry[0])

  // 2. Determine Kicker matches to delete
  const kickerToDelete = kickerMatches.value.filter((m) => {
    let winnerTeam = null
    if (m.matchResult === 'TEAM_A_WIN') {
      winnerTeam = normalizeTeam(m.teamAPlayerNames)
    } else if (m.matchResult === 'TEAM_B_WIN') {
      winnerTeam = normalizeTeam(m.teamBPlayerNames)
    }

    // Keep the match only if it was a win for a top team
    return !winnerTeam || !topTeams.includes(winnerTeam)
  })

  const totalToDelete = dartsToDelete.length + kickerToDelete.length

  if (totalToDelete === 0) {
    message.value = {
      type: 'success',
      text: 'Es gibt keine alten Spiele zum Löschen. Die Historie ist bereits optimiert.',
    }
    setTimeout(() => {
      if (message.value) message.value = null
    }, 5000)
    return
  }

  bulkDeleteInfo.value = {
    total: totalToDelete,
    dartsCount: dartsToDelete.length,
    kickerCount: kickerToDelete.length,
  }
  pendingDartsToDelete.value = dartsToDelete
  pendingKickerToDelete.value = kickerToDelete
  showBulkDeleteDialog.value = true
}

const pendingDartsToDelete = ref([])
const pendingKickerToDelete = ref([])

const confirmBulkDelete = async () => {
  showBulkDeleteDialog.value = false
  loading.value = true
  message.value = null
  let deletedCount = 0
  let errorCount = 0

  try {
    // Delete Darts matches
    for (const match of pendingDartsToDelete.value) {
      try {
        await highscoreAdminApi.deleteDartsMatch(match.id)
        deletedCount++
      } catch (e) {
        console.error('Error deleting darts match', match.id, e)
        errorCount++
      }
    }

    // Delete Kicker matches
    for (const match of pendingKickerToDelete.value) {
      try {
        await highscoreAdminApi.deleteKickerMatch(match.id)
        deletedCount++
      } catch (e) {
        console.error('Error deleting kicker match', match.id, e)
        errorCount++
      }
    }

    if (errorCount === 0) {
      message.value = { type: 'success', text: `Erfolgreich ${deletedCount} Spiele gelöscht.` }
    } else {
      message.value = {
        type: 'error',
        text: `${deletedCount} Spiele gelöscht, aber ${errorCount} Fehler aufgetreten.`,
      }
    }

    await fetchMatches()
  } catch (err) {
    console.error('Critical error during bulk delete', err)
    message.value = {
      type: 'error',
      text: 'Ein kritischer Fehler ist beim Massenlöschen aufgetreten.',
    }
  } finally {
    loading.value = false
    setTimeout(() => {
      if (message.value) message.value = null
    }, 5000)
  }
}

/**
 * Opens the form modal for a specific game type.
 * @param {string} type - 'darts' or 'kicker'.
 */
const openForm = (type) => {
  formType.value = type
  initialFormData.value = null
  editingMatchId.value = null
  showFormModal.value = true
}

/**
 * Opens the form modal in edit mode.
 * @param {Object} param0 - { match, type }
 */
const handleEdit = ({ match, type }) => {
  formType.value = type
  initialFormData.value = match
  editingMatchId.value = match.id
  showFormModal.value = true
}

/**
 * Closes the form modal.
 */
const closeForm = () => {
  showFormModal.value = false
  initialFormData.value = null
  editingMatchId.value = null
}

/**
 * Loads all Darts and Kicker matches from the backend.
 */
const fetchMatches = async () => {
  loading.value = true
  try {
    await highscoreStore.fetchAdminMatches()
  } catch (err) {
    console.error('Failed to load matches', err)
    message.value = { type: 'error', text: 'Fehler beim Laden des Spielverlaufs.' }
  } finally {
    loading.value = false
  }
}

/**
 * Zentraler Handler für das Absenden des Match-Formulars (Erstellen oder Aktualisieren).
 * Diese Funktion empfängt das Event von der HighscoreAdminForm.
 *
 * @param {Object} param0 - Enthält den Spieltyp (darts/kicker) und die Nutzdaten (data).
 */
const handleFormSubmit = async ({ type, data }) => {
  // 1. UI-Zustand: Lade-Anzeige aktivieren und alte Nachrichten löschen.
  loading.value = true
  message.value = null

  // Hilfsvariablen für lesbare Fehlermeldungen und Modus-Erkennung.
  const matchTypeLabel = type === 'darts' ? 'Darts' : 'Kicker'
  const isEdit = !!editingMatchId.value // Wenn eine ID vorhanden ist, bearbeiten wir ein bestehendes Match.

  try {
    // 2. Weiche nach Spieltyp (Darts oder Kicker)
    if (type === 'darts') {
      if (isEdit) {
        // Fall A: Bestehendes Darts-Match aktualisieren
        await highscoreAdminApi.updateDartsEntry(editingMatchId.value, data)
      } else {
        // Fall B: Ganz neues Darts-Match anlegen
        await highscoreAdminApi.createDartsEntry(data)
      }
    } else {
      // 3. Weiche für Kicker
      if (isEdit) {
        // Fall C: Bestehendes Kicker-Match aktualisieren
        await highscoreAdminApi.updateKickerEntry(editingMatchId.value, data)
      } else {
        // Fall D: Ganz neues Kicker-Match anlegen
        await highscoreAdminApi.createKickerEntry(data)
      }
    }

    // 4. Erfolg: Nutzer benachrichtigen und UI aufräumen.
    message.value = {
      type: 'success',
      text: `${matchTypeLabel}-Match erfolgreich ${isEdit ? 'aktualisiert' : 'hinzugefügt'}!`,
    }

    closeForm() // Modal schließen und State zurücksetzen.
    await fetchMatches() // Liste im Hintergrund neu laden, damit die neuen Daten sofort erscheinen.
  } catch (err) {
    // 5. Fehlerbehandlung: Loggen und Fehlermeldung anzeigen.
    console.error('Error saving highscore entry:', err)
    message.value = {
      type: 'error',
      text: `Fehler beim Speichern des ${matchTypeLabel}-Matches.`,
    }
  } finally {
    // 6. Abschluss: Lade-Spinner deaktivieren.
    loading.value = false

    // Nachricht nach 5 Sekunden automatisch ausblenden, um die UI sauber zu halten.
    setTimeout(() => {
      if (message.value) message.value = null
    }, 5000)
  }
}

/**
 * Handles the deletion of a match.
 * @param {Object} param0 - Contains the ID and game type of the match to delete.
 */
const handleDelete = async ({ id, type }) => {
  loading.value = true
  const matchTypeLabel = type === 'darts' ? 'Darts' : 'Kicker'
  try {
    if (type === 'darts') {
      await highscoreAdminApi.deleteDartsMatch(id)
    } else {
      await highscoreAdminApi.deleteKickerMatch(id)
    }
    message.value = { type: 'success', text: `${matchTypeLabel}-Match gelöscht.` }
    await fetchMatches()
  } catch (err) {
    console.error('Failed to delete', err)
    message.value = { type: 'error', text: `Fehler beim Löschen des ${matchTypeLabel}-Matches.` }
  } finally {
    loading.value = false
    setTimeout(() => {
      if (message.value) message.value = null
    }, 5000)
  }
}

onMounted(() => {
  fetchMatches()
})
</script>
