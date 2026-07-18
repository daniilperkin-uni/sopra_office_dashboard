<template>
  <div class="lunch-admin-section space-y-6 mt-6">
    <!-- Header & Toggle -->
    <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white dark:bg-gray-800 p-4 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700">
      <div>
        <h2 class="text-2xl font-bold text-gray-900 dark:text-gray-100">
          Lunch
        </h2>
      </div>
      <button
        class="flex items-center gap-2 px-4 py-2 bg-primary text-white rounded-md hover:bg-blue-600 transition-colors font-medium shadow-sm"
        @click="showCreateForm = !showCreateForm"
      >
        <span v-if="!showCreateForm">+ Lunch anlegen</span>
        <span v-else>– Einklappen</span>
      </button>
    </div>

    <!-- Creation Area (Expandable) -->
    <div
      v-if="showCreateForm"
      class="bg-gray-50 dark:bg-gray-800/50 p-6 rounded-xl border border-blue-100 dark:border-gray-700 space-y-8 animate-fade-in-down"
    >
      <div class="max-w-4xl mx-auto">
        <!-- Creation Form -->
        <LunchEventCreationForm @event-created="handleEventCreated" />
      </div>
    </div>

    <!-- Feedback Message -->
    <div
      v-if="successMessage"
      class="fixed bottom-4 right-4 bg-green-500 text-white px-6 py-3 rounded-lg shadow-lg z-50"
    >
      {{ successMessage }}
    </div>

    <!-- List View (re-rendered via :key when refresh is triggered) -->
    <LunchEventAdmin :key="lunchStore.refreshCounter" />
  </div>
</template>

<script setup>
/**
 * Main view for managing Lunch events.
 * Provides a toggle for the creation form and displays the list of existing events.
 * Uses the lunch store to trigger a refresh of the event list after creation
 * instead of manually incrementing a local counter.
 */
import { ref } from 'vue'
import { useLunchStore } from '@/stores/useLunchStore'
import LunchEventAdmin from '@/features/community-lunch/admin/LunchEventAdmin.vue'
import LunchEventCreationForm from '@/features/community-lunch/admin/LunchEventCreationForm.vue'

const lunchStore = useLunchStore()
const showCreateForm = ref(false)
const successMessage = ref('')

const handleEventCreated = () => {
  successMessage.value = 'Lunch-Event wurde erfolgreich angelegt!'
  showCreateForm.value = false
  lunchStore.refresh()
  setTimeout(() => {
    successMessage.value = ''
  }, 3000)
}
</script>

<style scoped>
.animate-fade-in-down {
  animation: fadeInDown 0.3s ease-out;
}
@keyframes fadeInDown {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
