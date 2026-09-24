<template>
  <div
    class="parking-spot p-2 rounded-md text-center transition-all duration-200"
    :class="spotClasses"
  >
    <div class="text-xs font-medium mb-1">Platz {{ spotNumber }}</div>
    <div v-if="occupied" class="text-xs truncate">
      {{ employee }}
    </div>
    <div v-else class="text-xs opacity-70">Frei</div>
  </div>
</template>

<script setup>
/**
 * Represents a single parking spot and changes its appearance based on occupancy status.
 * Displays the spot number and optionally the name of the employee who reserved it.
 */
import { computed } from 'vue'

// Defines the properties of the parking spot: number, occupancy status, and employee name.
const props = defineProps({
  spotNumber: {
    type: Number,
    required: true,
  },
  occupied: {
    type: Boolean,
    default: false,
  },
  employee: {
    type: String,
    default: '',
  },
})

// Dynamically assigns CSS classes to display the parking spot as red (occupied) or green (free).
const spotClasses = computed(() => {
  if (props.occupied) {
    return 'bg-red-100 text-red-800 border border-red-200'
  }
  return 'bg-green-100 text-green-800 border border-green-200'
})
</script>
