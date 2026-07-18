<template>
  <div
    class="flex flex-col bg-white rounded-2xl shadow-md border border-gray-200 h-full w-full overflow-hidden"
  >
    <!-- Header -->
    <div class="bg-gray-100 p-8 border-b border-gray-200">
      <h3 id="column-title" class="font-black text-primary text-7xl text-center leading-none">
        {{ title }}
      </h3>
    </div>

    <!-- List -->
    <div
      class="flex-grow flex flex-col justify-evenly p-4 gap-4"
      role="list"
      aria-labelledby="column-title"
    >
      <DashboardHighscoreCard
        v-for="(item, index) in displayItems"
        :key="index"
        :label="item.label"
        :value="item.value"
        :secondary-text="item.secondaryText"
        :is-placeholder="item.isPlaceholder"
        :rank="isLeaderboard ? index + 1 : 0"
        class="flex-1"
      />
    </div>
  </div>
</template>

<script setup>
/**
 * A column for displaying a highscore leaderboard on the dashboard.
 * Renders a list of DashboardHighscoreCards.
 * Automatically limits the display to the top 3 (Leaderboard) or top 7 (Other).
 */
import { computed } from 'vue'
import DashboardHighscoreCard from './DashboardHighscoreCard.vue'

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

// Limits the number of displayed items.
const displayItems = computed(() => {
  const limit = props.isLeaderboard ? 3 : 7
  return props.items.slice(0, limit)
})
</script>
