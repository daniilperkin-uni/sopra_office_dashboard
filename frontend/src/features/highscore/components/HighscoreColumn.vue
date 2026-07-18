<template>
  <div
    class="flex flex-col p-[60px] bg-white dark:bg-gray-800 rounded-3xl shadow-2xl h-full w-full"
  >
    <h3 id="column-title" class="text-[58px] font-bold mb-8 text-primary text-center">
      {{ title }}
    </h3>
    <div
      class="flex-grow flex flex-col justify-between space-y-[24px]"
      role="list"
      aria-labelledby="column-title"
    >
      <HighscoreCard
        v-for="(item, index) in displayItems"
        :key="index"
        :label="item.label"
        :value="item.value"
        :secondary-text="item.secondaryText"
        :is-placeholder="item.isPlaceholder"
        :rank="isLeaderboard ? index + 1 : 0"
        class="flex-grow"
      />
    </div>
  </div>
</template>

<script setup>
/**
 * A column for displaying highscore data (e.g., leaderboards or match history).
 * Renders a list of HighscoreCard components and limits the display to the
 * top 3 entries for leaderboards or 7 for history.
 */
import { computed } from 'vue'
import HighscoreCard from './HighscoreCard.vue'

// Defines the title of the column and the list of entries to display.
const props = defineProps({
  title: {
    type: String,
    required: true,
  },
  items: {
    type: Array,
    default: () => [],
  },
  isLeaderboard: {
    type: Boolean,
    default: false,
  },
})

/**
 * Calculates the list of entries to display.
 * - Leaderboard: Top 3
 * - History/Others: Top 7
 */
const displayItems = computed(() => {
  const limit = props.isLeaderboard ? 3 : 7
  return props.items.slice(0, limit)
})
</script>
