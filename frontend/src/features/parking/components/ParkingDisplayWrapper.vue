<template>
  <div class="flex flex-col min-h-screen bg-neutral-bg">
    <!-- Header specifically for Parking View -->
    <header class="bg-gray-100 text-primary shadow-sm w-full border-b border-gray-200">
      <div class="px-4 py-3 flex items-center">
        <h1 class="text-7xl font-black flex-grow text-center tracking-wider">itestra Parking</h1>
      </div>
    </header>

    <div class="flex-grow p-6">
      <div v-if="loading" class="flex justify-center items-center h-full">
        <LoadingSpinner />
      </div>
      <div
        v-else-if="error"
        class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative"
      >
        <strong class="font-bold">Fehler:</strong>
        <span class="block sm:inline">{{ error }}</span>
        <BaseButton variant="danger" class="mt-2" @click="refreshData">
          Erneut versuchen
        </BaseButton>
      </div>
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
        <BarDisplay
          v-for="day in weekData.slice(0, 8)"
          :key="day.date"
          :date="day.date"
          :entries="day.entries"
          :max-spots="5"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * Container component for the parking display on the dashboard.
 * It handles loading and error states and renders a BarDisplay component for each day
 * of the week.
 */
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import BarDisplay from '@/features/dashboard/components/BarDisplay.vue'
import BaseButton from '@/components/common/BaseButton.vue'

defineProps({
  weekData: {
    type: Array,
    required: true,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  error: {
    type: String,
    default: null,
  },
  refreshData: {
    type: Function,
    required: true,
  },
})
</script>
