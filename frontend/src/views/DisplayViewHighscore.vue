<template>
  <div class="highscore-view h-full bg-white dark:bg-gray-900 text-gray-900 dark:text-gray-100 flex flex-col overflow-hidden">
    <!-- Header specifically for Highscore View -->
    <header class="bg-gray-100 text-primary shadow-sm w-full border-b border-gray-200">
      <div class="px-4 py-3 flex items-center">
        <h1 class="text-7xl font-black flex-grow text-center tracking-wider">
          itestra Highscore
        </h1>
      </div>
    </header>

    <!-- Loading, Error, or Content -->
    <div
      v-if="loading"
      class="flex-grow flex justify-center items-center"
    >
      <LoadingSpinner />
    </div>
    <div
      v-else-if="error"
      class="flex-grow flex justify-center items-center p-4"
    >
      <div class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative max-w-4xl">
        <strong class="font-bold">Fehler:</strong>
        <span class="block sm:inline">{{ error }}</span>
      </div>
    </div>
    <div
      v-else
      class="flex-grow w-full px-[40px] py-[40px]"
    >
      <!-- 3-Column Grid that fills the screen -->
      <div class="grid grid-cols-[1fr_1.4fr_1fr] gap-[40px] h-full items-stretch">
        <HighscoreColumn
          title="Darts Rangliste"
          :items="highscoreData['Darts Leaderboard']"
          :is-leaderboard="true"
        />
        <HighscoreColumn
          title="Spielverlauf"
          :items="highscoreData['Match history']"
        />
        <HighscoreColumn
          title="Kicker Rangliste"
          :items="highscoreData['Kicker leaderboard']"
          :is-leaderboard="true"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * Component for the highscore large display on the dashboard.
 * Displays the Darts leaderboard, match history, and Kicker leaderboard
 * in a 4K-optimized 3-column layout.
 */
import HighscoreColumn from '@/features/highscore/components/HighscoreColumn.vue';
import LoadingSpinner from '@/components/common/LoadingSpinner.vue';

defineProps({
  // Highscore data provided by the backend (leaderboards and history).
  highscoreData: {
    type: Object,
    default: () => ({
      'Match history': [],
      'Darts Leaderboard': [],
      'Kicker leaderboard': [],
    }),
  },
  loading: {
    type: Boolean,
    default: false,
  },
  error: {
    type: String,
    default: null,
  },
});
</script>