<template>
  <div
    class="flex justify-between items-center transition-all duration-300 border rounded-xl shadow-sm px-8 py-4"
    :class="[isPlaceholder ? 'opacity-20 pointer-events-none' : '', rankColorClasses]"
    :role="isPlaceholder ? 'presentation' : 'listitem'"
    :tabindex="isPlaceholder ? -1 : 0"
    :aria-hidden="isPlaceholder"
  >
    <!-- Rank -->
    <div class="flex items-center justify-center font-black leading-none text-6xl w-20">
      <span v-if="rank === 1">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 24 24"
          fill="currentColor"
          class="w-20 h-20 opacity-90"
        >
          <path
            fill-rule="evenodd"
            d="M5.166 2.621v.858c-1.035.148-2.059.33-3.071.543a.75.75 0 00-.584.859 6.753 6.753 0 006.138 5.6 6.73 6.73 0 002.743 1.346A6.707 6.707 0 019.279 15H8.54c-1.036 0-1.875.84-1.875 1.875V19.5h-.75a2.25 2.25 0 00-2.25 2.25c0 .414.336.75.75.75h15a.75.75 0 00.75-.75 2.25 2.25 0 00-2.25-2.25h-.75v-2.625c0-1.036-.84-1.875-1.875-1.875h-.739a6.706 6.706 0 01-1.112-3.173 6.73 6.73 0 002.743-1.347 6.753 6.753 0 006.139-5.6.75.75 0 00-.585-.858 47.077 47.077 0 00-3.07-.543V2.62a.75.75 0 00-.658-.744 49.22 49.22 0 00-6.093-.377c-2.063 0-4.096.128-6.093.377a.75.75 0 00-.657.744zm0 2.629c0 1.196.312 2.32.857 3.294A5.266 5.266 0 013.16 5.337a45.6 45.6 0 012.006-.343v.256zm13.5 0v-.256c.674.1 1.343.214 2.006.343a5.265 5.265 0 01-2.863 3.207 6.72 6.72 0 00.857-3.294z"
            clip-rule="evenodd"
          />
        </svg>
      </span>
      <span v-else>{{ rank > 0 ? rank : '' }}</span>
    </div>

    <!-- Player -->
    <div class="flex flex-col text-left leading-tight flex-grow ml-4 overflow-hidden">
      <span class="font-black text-5xl truncate">
        {{ label }}
      </span>
      <span
        v-if="secondaryText"
        class="font-bold uppercase text-xl whitespace-nowrap opacity-70"
      >
        {{ secondaryText }}
      </span>
    </div>

    <!-- Score -->
    <div class="font-black text-right tabular-nums text-6xl whitespace-nowrap ml-4">
      {{ value }}
    </div>
  </div>
</template>

<script setup>
/**
 * A compact highscore card for the dashboard.
 * Displays rank, player name, and score.
 * Supports special highlights for the top 3 (Gold, Silver, Bronze).
 */
import { computed } from 'vue'

const props = defineProps({
  label: { type: String, required: true },
  value: { type: [String, Number], required: true },
  secondaryText: { type: String, default: '' },
  rank: { type: Number, default: 0 },
  isPlaceholder: { type: Boolean, default: false },
})

// Calculates the CSS classes for the background color based on the rank.
const rankColorClasses = computed(() => {
  if (props.isPlaceholder) return 'bg-gray-50 border-dashed border-gray-200 text-gray-400'

  if (props.rank === 1) {
    return 'bg-[#FFD700] border-[#FFD700] text-black shadow-[0_4px_12px_rgba(255,215,0,0.3)]'
  }
  if (props.rank === 2) {
    return 'bg-[#E0E0E0] border-[#E0E0E0] text-black'
  }
  if (props.rank === 3) {
    return 'bg-[#CD7F32] border-[#CD7F32] text-white'
  }

  return 'bg-white border-gray-100 text-gray-600'
})
</script>
