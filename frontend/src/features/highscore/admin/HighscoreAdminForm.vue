<template>
  <form
    class="p-4 sm:p-6 bg-white dark:bg-gray-800 rounded-lg shadow-lg space-y-4 sm:space-y-6"
    @submit.prevent="handleSubmit"
  >
    <h2 class="text-xl sm:text-2xl font-bold mb-4 text-primary">
      {{
        initialData
          ? 'Match bearbeiten'
          : type === 'darts'
            ? 'Neues Darts-Match hinzufügen'
            : 'Neues Kicker-Match hinzufügen'
      }}
    </h2>

    <!-- DARTS FORM -->
    <div
      v-if="type === 'darts'"
      class="space-y-4"
    >
      <div>
        <label
          for="playerName"
          class="block text-sm font-medium text-black"
        >Spielername</label>
        <input
          id="playerName"
          v-model="dartsData.playerName"
          type="text"
          class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2 bg-white text-gray-900 focus:ring-primary focus:border-primary"
          required
        >
      </div>
      <div>
        <label
          for="points"
          class="block text-sm font-medium text-black"
        >Würfe</label>
        <input
          id="points"
          v-model.number="dartsData.dartsToFinish"
          type="number"
          min="1"
          class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2 bg-white text-gray-900 focus:ring-primary focus:border-primary"
          required
        >
      </div>
    </div>

    <!-- KICKER FORM -->
    <div
      v-else-if="type === 'kicker'"
      class="space-y-4"
    >
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div>
          <h3 class="font-bold text-black mb-2 text-sm">
            Team A
          </h3>
          <div class="space-y-2">
            <input
              v-model="kickerData.teamAPlayer1"
              type="text"
              placeholder="Spieler 1"
              class="block w-full border border-gray-300 rounded-md shadow-sm p-2 bg-white text-gray-900 text-sm"
              required
            >
            <input
              v-model="kickerData.teamAPlayer2"
              type="text"
              placeholder="Spieler 2 (optional)"
              class="block w-full border border-gray-300 rounded-md shadow-sm p-2 bg-white text-gray-900 text-sm"
            >
          </div>
        </div>
        <div>
          <h3 class="font-bold text-black mb-2 text-sm">
            Team B
          </h3>
          <div class="space-y-2">
            <input
              v-model="kickerData.teamBPlayer1"
              type="text"
              placeholder="Spieler 1"
              class="block w-full border border-gray-300 rounded-md shadow-sm p-2 bg-white text-gray-900 text-sm"
              required
            >
            <input
              v-model="kickerData.teamBPlayer2"
              type="text"
              placeholder="Spieler 2 (optional)"
              class="block w-full border border-gray-300 rounded-md shadow-sm p-2 bg-white text-gray-900 text-sm"
            >
          </div>
        </div>
      </div>
      <div>
        <label class="block text-sm font-medium text-black mb-2">Ergebnis</label>
        <select
          v-model="kickerData.matchResult"
          class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2 bg-white text-gray-900"
          required
        >
          <option value="TEAM_A_WIN">
            Team A gewinnt
          </option>
          <option value="TEAM_B_WIN">
            Team B gewinnt
          </option>
        </select>
      </div>
    </div>

    <div class="flex flex-col sm:flex-row justify-end gap-3 mt-6">
      <BaseButton
        variant="secondary"
        class="order-2 sm:order-1"
        @click="emit('cancel')"
      >
        Abbrechen
      </BaseButton>
      <BaseButton
        type="submit"
        variant="primary"
        class="order-1 sm:order-2"
      >
        Eintrag {{ initialData ? 'speichern' : 'hinzufügen' }}
      </BaseButton>
    </div>
  </form>
</template>

<script setup>
/**
 * Form for recording new match results for Darts or Kicker.
 * Depending on the selected type, different fields (single player for Darts,
 * 2vs2 teams for Kicker) are displayed.
 */
import { reactive, watch } from 'vue'
import BaseButton from '@/components/common/BaseButton.vue'

// Defines the type of game (darts or kicker).
const props = defineProps({
  type: {
    type: String,
    required: true,
  },
  initialData: {
    type: Object,
    default: null,
  },
})

// Declares events for submitting form data or cancelling.
const emit = defineEmits(['submit', 'cancel'])

// Reactive data structure for Darts results.
const dartsData = reactive({
  playerName: '',
  dartsToFinish: 0,
})

// Reactive data structure for Kicker results.
const kickerData = reactive({
  teamAPlayer1: '',
  teamAPlayer2: '',
  teamBPlayer1: '',
  teamBPlayer2: '',
  matchResult: 'TEAM_A_WIN',
})

// Watch for changes in initialData to pre-fill the form
watch(
  () => props.initialData,
  (newData) => {
    if (newData) {
      if (props.type === 'darts') {
        dartsData.playerName = newData.playerName || newData.player || '' // Handle inconsistency if any
        dartsData.dartsToFinish = newData.dartsToFinish || newData.points || 0
      } else if (props.type === 'kicker') {
        // Map team lists to individual fields
        const tA = newData.teamAPlayers || newData.teamAPlayerNames || []
        const tB = newData.teamBPlayers || newData.teamBPlayerNames || []

        kickerData.teamAPlayer1 = tA[0] || ''
        kickerData.teamAPlayer2 = tA[1] || ''
        kickerData.teamBPlayer1 = tB[0] || ''
        kickerData.teamBPlayer2 = tB[1] || ''
        kickerData.matchResult = newData.matchResult || 'TEAM_A_WIN'
      }
    } else {
      // Reset form
      if (props.type === 'darts') {
        dartsData.playerName = ''
        dartsData.dartsToFinish = 0
      } else {
        kickerData.teamAPlayer1 = ''
        kickerData.teamAPlayer2 = ''
        kickerData.teamBPlayer1 = ''
        kickerData.teamBPlayer2 = ''
        kickerData.matchResult = 'TEAM_A_WIN'
      }
    }
  },
  { immediate: true }
)

/**
 * Verarbeitet das Absenden des Formulars und übergibt die validierten Daten
 * an die Elternkomponente. Passt das Datenformat je nach Spieltyp an.
 */
function handleSubmit() {
  let payload

  // === FALL A: DARTS ===
  if (props.type === 'darts') {
    // Validierung: Es macht keinen Sinn, mit 0 oder weniger Würfen zu gewinnen.
    if (dartsData.dartsToFinish < 1) {
      alert('Die Anzahl der Würfe muss mindestens 1 sein.')
      return // Abbruch bei ungültigen Daten
    }
    // Einfaches Kopieren des Objekts, da die Struktur bereits passt.
    payload = { ...dartsData }

    // === FALL B: KICKER ===
  } else {
    // Daten-Transformation: Das Formular hat flache Felder (Spieler 1, Spieler 2),
    // aber das Backend erwartet Arrays für die Teams.

    // Wir sammeln die Namen und filtern leere Eingaben heraus (trim() entfernt Leerzeichen).
    // So wird aus [ "Max", "" ] automatisch [ "Max" ] -> ermöglicht 1vs1, 1vs2 oder 2vs2.
    const teamA = [kickerData.teamAPlayer1, kickerData.teamAPlayer2].filter(
      (p) => p && p.trim() !== ''
    )

    const teamB = [kickerData.teamBPlayer1, kickerData.teamBPlayer2].filter(
      (p) => p && p.trim() !== ''
    )

    // Konstruktion des Payloads für das Backend
    payload = {
      teamAPlayers: teamA,
      teamBPlayers: teamB,
      matchResult: kickerData.matchResult,
    }
  }

  // Event an die Eltern-Komponente senden (diese kümmert sich um den API-Call)
  emit('submit', { type: props.type, data: payload })
}
</script>
