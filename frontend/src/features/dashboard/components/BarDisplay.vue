<template>
  <div class="bar-display bg-white rounded-lg shadow-md overflow-hidden">
    <!--
      The header displays the date and weekday formatted.
      It has a striking blue color to clearly highlight the date.
    -->
    <div class="day-header bg-gray-100 text-primary px-14 py-10 flex items-baseline justify-between gap-4">
      <h3 class="text-[68px] font-bold leading-tight">
        {{ formatDate(date) }}
      </h3>
      <p class="text-[44px] opacity-90 leading-tight">
        {{ formatWeekday(date) }}
      </p>
    </div>

    <div class="p-7">
      <!--
        This area displays parking spots as stacked horizontal bars.
        A bar is rendered for each possible parking spot (up to maxSpots).
      -->
      <div class="space-y-5">
        <div
          v-for="spot in maxSpots"
          :key="spot"
          class="horizontal-bar rounded-md shadow-sm p-7 flex items-center"
          :class="isSpotOccupied(spot) ? 'bg-red-500' : 'bg-green-500'"
        >
          <!--
            The label inside the bar shows either the name of the employee who reserved the spot,
            or the word 'frei' if the spot is available.
          -->
          <span class="text-white font-bold text-[40px] bg-white bg-opacity-20 px-10 py-3 rounded-lg">
            {{ isSpotOccupied(spot) ? getEmployeeForSpot(spot) : 'frei' }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { formatDate, formatWeekday } from '@/utils/dateUtils'

// Defines the properties the component receives from its parent.
// - date: The date for which parking occupancy is displayed.
// - entries: A list of reservations for this date.
// - maxSpots: The total number of available parking spots.
const props = defineProps({
  date: {
    type: String,
    required: true
  },
  entries: {
    type: Array,
    default: () => []
  },
  maxSpots: {
    type: Number,
    default: 5
  }
})

// A helper function that checks if a specific parking spot (based on its number) is occupied.
// A spot is considered occupied if its number is less than or equal to the number of entries.
const isSpotOccupied = (spotNumber) => {
  return spotNumber <= props.entries.length
}

// Returns the name of the employee who reserved a specific parking spot.
// The entries are assumed to be sorted, so access is possible via index.
const getEmployeeForSpot = (spotNumber) => {
  if (spotNumber <= props.entries.length) {
    return props.entries[spotNumber - 1].employeeName
  }
  return null
}
</script>

<style scoped>
/*
  Container style for the entire display with a gentle hover effect to increase interactivity.
*/
.bar-display {
  transition: transform 0.2s ease-in-out;
  font-family: 'Mulish', system-ui, sans-serif;
  transform: scaleX(0.97);
  transform-origin: center;
}

.bar-display:hover {
  transform: translateY(-2px);
}

/*
  Style for the individual horizontal bars, including a hover effect that slightly shifts the bar to the right.
*/
.horizontal-bar {
  min-height: 115px;
  transition: all 0.2s ease;
}

.horizontal-bar:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}


</style>