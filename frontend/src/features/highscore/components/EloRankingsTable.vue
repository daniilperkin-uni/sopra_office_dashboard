<template>
  <div class="elo-rankings card">
    <h2 class="section-title">
      Darts ELO-Rangliste
    </h2>

    <div
      v-if="isLoading"
      class="loading"
    >
      Lade Rangliste...
    </div>
    <p
      v-else-if="error"
      class="error-message"
    >
      Rangliste konnte nicht geladen werden: {{ error }}
    </p>
    <p
      v-else-if="rankings.length === 0"
      class="empty"
    >
      Noch keine Darts-Spiele erfasst - die Rangliste erscheint nach dem ersten Match.
    </p>

    <table
      v-else
      class="rankings-table"
    >
      <thead>
        <tr>
          <th>#</th>
          <th>Spieler</th>
          <th>ELO</th>
          <th>Siege</th>
          <th>Niederlagen</th>
          <th>Aktuelle Serie</th>
          <th>Beste Serie</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="(entry, index) in rankings"
          :key="entry.playerName"
          :class="{ 'top-row': index === 0 }"
        >
          <td>{{ index + 1 }}</td>
          <td>{{ entry.playerName }}</td>
          <td class="elo-cell">
            {{ entry.elo }}
          </td>
          <td>{{ entry.wins }}</td>
          <td>{{ entry.losses }}</td>
          <td>
            <span
              v-if="entry.currentStreak > 0"
              class="streak-badge"
            >
              {{ entry.currentStreak }} Sieg{{ entry.currentStreak === 1 ? '' : 'e' }}
            </span>
            <span v-else>-</span>
          </td>
          <td>{{ entry.bestStreak }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
/**
 * Zeigt die ELO-Rangliste der Darts-Spiele an.
 *
 * Die Daten kommen aus dem reinen EloRankingService, der den gespeicherten
 * Spielverlauf on-demand aggregiert; diese Komponente konsumiert nur den
 * Endpoint und rendert die Tabelle.
 */
import { onMounted, ref } from 'vue'
import { highscoreAdminApi } from '@/services/api'

const rankings = ref([])
const isLoading = ref(true)
const error = ref(null)

/**
 * Laedt die Rangliste vom Backend.
 */
async function fetchRankings() {
  isLoading.value = true
  error.value = null
  try {
    rankings.value = await highscoreAdminApi.getEloRankings()
  } catch (err) {
    error.value = err.message || 'Unbekannter Fehler'
  } finally {
    isLoading.value = false
  }
}

onMounted(fetchRankings)
</script>

<style scoped>
.elo-rankings {
  padding: var(--space-lg);
}

.section-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: var(--space-md);
  color: var(--color-text);
}

.rankings-table {
  width: 100%;
  border-collapse: collapse;
}

.rankings-table th,
.rankings-table td {
  border-bottom: 1px solid var(--color-border);
  padding: var(--space-sm) var(--space-md);
  text-align: left;
}

.rankings-table th {
  font-weight: 600;
  color: var(--color-text-light);
  font-size: 0.85rem;
}

.top-row .elo-cell {
  color: #009ee2;
  font-weight: 700;
}

.elo-cell {
  font-weight: 600;
}

.streak-badge {
  background-color: rgba(126, 211, 33, 0.15);
  color: #4a8a10;
  border-radius: 999px;
  padding: 2px 10px;
  font-size: 0.8rem;
  font-weight: 600;
}

.loading,
.empty,
.error-message {
  text-align: center;
  padding: var(--space-lg);
  color: var(--color-text-light);
}

.error-message {
  color: var(--color-error, #d0021b);
}
</style>
