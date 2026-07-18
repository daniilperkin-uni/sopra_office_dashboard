<template>
  <div class="h-full flex flex-col bg-white">
    <!-- Header -->
    <div class="p-10 border-b-2 border-primary/10 bg-gray-100">
      <div class="text-7xl font-black text-primary text-center tracking-wider">
        Parking
      </div>
    </div>

    <!-- List Container (Fill remaining height) -->
    <div class="flex-grow flex flex-col overflow-hidden">
      <!-- Days as Data List Items -->
      <div
        v-for="day in upcomingDays"
        :key="day.date"
        class="flex-1 flex items-center justify-between px-10 border-0 shadow-[inset_0_-1px_0_rgba(0,0,0,0.55)] last:shadow-none min-h-0"
        :class="{ 'bg-blue-50/20': isToday(day.date) }"
      >
        <!-- Date (Left) -->
        <span
          class="font-bold flex-shrink-0 w-72 text-left text-5xl whitespace-nowrap px-4 py-2 rounded-xl"
          :class="isToday(day.date) ? 'text-primary border-4 border-primary' : 'text-gray-800'"
        >
          {{ formatDateShort(day.date) }}
        </span>

        <!-- Progress Bar & Count (Right) -->
        <div class="flex items-center gap-8 flex-grow justify-end">
          <!-- Bar -->
          <div class="w-full max-w-[280px] h-7 bg-white rounded-full box-border overflow-hidden border-2 border-black/25">
            <div
              class="h-full rounded-full transition-[width] duration-700 ease-out"
              :class="getOccupancyColor(day.occupied)"
              :style="{ width: `${(day.occupied / day.total) * 100}%` }"
            />
          </div>

          <!-- Count -->
          <span
            class="font-black tabular-nums w-24 text-right text-5xl"
            :class="isToday(day.date) ? 'text-primary' : 'text-gray-500'"
          >
            {{ day.occupied }}/{{ day.total }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * A summary of parking occupancy for the dashboard.
 * Displays occupancy for the next 14 days as a list with progress bars.
 * Highlights the current day.
 */
import { computed } from 'vue';

const props = defineProps({
  weekData: {
    type: Array,
    required: true
  }
});

// Calculates the days to be displayed (from today, max. 14).
const upcomingDays = computed(() => {
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  return props.weekData
    .filter(day => {
      const date = new Date(day.date);
      return date >= today;
    })
    .map(day => ({
      date: day.date,
      occupied: day.entries.length,
      total: 5
    }))
    .slice(0, 14); // Show next 14 days to fit screen
});

// Checks if a date corresponds to today.
const isToday = (dateStr) => {
  const today = new Date();
  const date = new Date(dateStr);
  return date.getDate() === today.getDate() &&
         date.getMonth() === today.getMonth() &&
         date.getFullYear() === today.getFullYear();
};

// Formats the date shortly (e.g., "Mo, 01.01").
const formatDateShort = (dateStr) => {
  const date = new Date(dateStr);
  return date.toLocaleDateString('de-DE', { weekday: 'short', day: '2-digit', month: '2-digit' })
    .replace(/\.$/, ''); // Remove only the very last dot
};

// Determines the color of the progress bar based on occupancy.
const getOccupancyColor = (occupied) => {
  if (occupied <= 2) return 'bg-emerald-500';
  if (occupied === 3) return 'bg-yellow-500';
  if (occupied === 4) return 'bg-orange-500';
  return 'bg-red-600';
};
</script>
