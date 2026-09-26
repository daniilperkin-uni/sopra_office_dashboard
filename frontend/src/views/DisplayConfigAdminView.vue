<template>
  <div class="max-w-4xl mx-auto p-4 sm:p-6 bg-white dark:bg-gray-800 rounded-lg shadow-md mt-6">
    <h2 class="text-xl sm:text-2xl font-bold mb-6 text-black">Konfiguration</h2>

    <div v-if="configStore.loading && !configStore.loaded" class="flex justify-center py-8">
      <LoadingSpinner />
    </div>

    <div
      v-else-if="configStore.error || error"
      class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4"
      role="alert"
    >
      <span class="block sm:inline">{{ configStore.error || error }}</span>
    </div>

    <form v-else class="space-y-6" @submit.prevent="saveConfig">
      <!-- Rotation Enabled -->
      <div class="flex items-start">
        <div class="flex items-center h-5">
          <input
            id="rotationEnabled"
            v-model="configStore.config.rotationEnabled"
            type="checkbox"
            class="h-5 w-5 text-primary focus:ring-primary border-gray-300 rounded"
          />
        </div>
        <label for="rotationEnabled" class="ml-3 block text-base sm:text-lg font-medium text-black">
          Automatische Ansichtsrotation aktivieren
        </label>
      </div>

      <!-- Skip OneDisplay in Rotation -->
      <div v-if="configStore.config.rotationEnabled" class="flex items-start ml-8">
        <div class="flex items-center h-5">
          <input
            id="skipOneDisplay"
            v-model="configStore.config.skipOneDisplayInRotation"
            type="checkbox"
            class="h-5 w-5 text-primary focus:ring-primary border-gray-300 rounded"
          />
        </div>
        <label for="skipOneDisplay" class="ml-3 block text-sm sm:text-base font-medium text-black">
          Automatische Ansichtsrotation ohne OneDisplay aktivieren
        </label>
      </div>

      <!-- Rotation Interval -->
      <div v-if="configStore.config.rotationEnabled">
        <label for="interval" class="block text-sm font-medium text-black"
          >Rotationsintervall (Sekunden)</label
        >
        <input
          id="interval"
          v-model.number="configStore.config.rotationIntervalSeconds"
          type="number"
          min="-1"
          class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2 bg-white text-gray-900"
          required
        />
        <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
          Zeitdauer bis zum Wechsel zur nächsten Ansicht. Der Wert -1 aktiviert den Spielmodus.
        </p>
      </div>

      <!-- Default View -->
      <div>
        <label for="defaultView" class="block text-sm font-medium text-black">
          {{ configStore.config.rotationEnabled ? 'Standardansicht (Fallback)' : 'Aktive Ansicht' }}
        </label>
        <select
          id="defaultView"
          v-model="configStore.config.defaultSingleViewId"
          class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2 bg-white text-gray-900"
        >
          <option value="dashboard">Dashboard (OneDisplay)</option>
          <option value="calendar">Kalender</option>
          <option value="parking">Parken</option>
          <option value="highscore">Highscore</option>
          <option value="game">Game</option>
        </select>
        <p
          v-if="!configStore.config.rotationEnabled"
          class="mt-1 text-sm text-gray-500 dark:text-gray-400"
        >
          Da die Rotation deaktiviert ist, wird diese Ansicht dauerhaft angezeigt.
        </p>
      </div>

      <div class="pt-4">
        <BaseButton type="submit" :loading="saving" variant="primary" class="w-full">
          Konfiguration speichern
        </BaseButton>
      </div>
    </form>
  </div>
</template>

<script setup>
/**
 * Component for managing display configuration.
 * Enables enabling/disabling rotation, setting the interval,
 * and selecting the default view.
 * Uses the display config store so changes are shared with the DisplayView.
 */
import { ref, onMounted } from 'vue'
import { useDisplayConfigStore } from '@/stores/useDisplayConfigStore'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import BaseButton from '@/components/common/BaseButton.vue'

const configStore = useDisplayConfigStore()
const saving = ref(false)
const error = ref(null)

onMounted(async () => {
  await configStore.fetchConfig()
})

const saveConfig = async () => {
  const interval = configStore.config.rotationIntervalSeconds

  // -1 ist der dokumentierte Spielmodus-Sentinel; alles andere unter 5 Sekunden
  // bringt den Browser zum Absturz. Vorher war -1 nicht eingebbar, der
  // Spielmodus also nur durch direkte API-Aufrufe erreichbar.
  if (interval !== -1 && (Number.isNaN(interval) || interval < 5)) {
    error.value = 'Rotationsintervall muss -1 (Spielmodus) oder mindestens 5 Sekunden betragen.'
    return
  }

  saving.value = true
  error.value = null
  try {
    await configStore.updateConfig(configStore.config)
  } catch {
    error.value = 'Konfiguration konnte nicht gespeichert werden.'
  } finally {
    saving.value = false
  }
}
</script>
