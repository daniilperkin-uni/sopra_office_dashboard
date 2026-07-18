<template>
  <div
    class="highscore-card rounded-[20px] flex justify-between items-center transition-all duration-200"
    :class="[
      isPlaceholder ? 'opacity-20 pointer-events-none' : 'shadow-[0_18px_36px_rgba(0,0,0,0.16)]',
      'py-[48px] px-[56px] gap-8', // Base styles (promoted from 4K)
      rank > 0 ? 'min-h-[300px]' : 'min-h-[180px]',
      rankColorClasses
    ]"
    :role="isPlaceholder ? 'presentation' : 'listitem'"
    :tabindex="isPlaceholder ? -1 : 0"
    :aria-hidden="isPlaceholder"
  >
    <div class="flex flex-col text-left leading-tight">
      <span 
        class="font-semibold"
        :class="rank > 0 ? 'text-[100px]' : 'text-[50px]'"
      >
        {{ label }}
      </span>
      <span 
        v-if="secondaryText" 
        class="opacity-75"
        :class="rank > 0 ? 'text-[68px]' : 'text-[34px]'"
      >
        {{ secondaryText }}
      </span>
    </div>
    <span 
      class="font-black"
      :class="rank > 0 ? 'text-[83px]' : 'text-[64px]'"
    >
      {{ value }}
    </span>
  </div>
</template>

<script setup>
import { computed } from 'vue';

/**
 * Represents a single card within a highscore column.
 * Displays a name (label), the achieved score (points/wins), and optional secondary text.
 * Supports special colors for ranks 1 (Gold), 2 (Silver), and 3 (Bronze).
 */
const props = defineProps({
  label: {
    type: String,
    default: '',
  },
  value: {
    type: [String, Number],
    default: '',
  },
  secondaryText: {
    type: String,
    default: '',
  },
  isPlaceholder: {
    type: Boolean,
    default: false,
  },
  rank: {
    type: Number,
    default: 0, // 0 means no special rank (e.g. in match history)
  }
});

/**
 * Determines CSS classes based on rank.
 */
const rankColorClasses = computed(() => {
  if (props.rank === 1) {
    return 'bg-[#FFD700] text-black shadow-[0_0_30px_rgba(255,215,0,0.3)]'; // Gold
  } else if (props.rank === 2) {
    return 'bg-[#E0E0E0] text-black'; // Silver (slightly lighter than standard)
  } else if (props.rank === 3) {
    return 'bg-[#CD7F32] text-white'; // Bronze
  }
  
  // Standard style
  return 'bg-green-100 dark:bg-green-700 text-green-800 dark:text-green-100';
});
</script>

<style scoped>
.highscore-card:focus-visible {
  outline: 4px solid rgba(0, 123, 255, 0.22);
  outline-offset: 2px;
}
</style>