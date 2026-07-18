<template>
  <div class="max-w-4xl mx-auto p-4 sm:p-6 bg-white dark:bg-gray-800 rounded-lg shadow-md mt-6">
    <h2 class="text-xl sm:text-2xl font-bold mb-6 text-black">
      Konfiguration
    </h2>

    <div
      v-if="loading"
      class="flex justify-center py-8"
    >
      <LoadingSpinner />
    </div>

    <div
      v-else-if="error"
      class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-4"
      role="alert"
    >
      <span class="block sm:inline">{{ error }}</span>
    </div>

    <form
      v-else
      class="space-y-6"
      @submit.prevent="saveConfig"
    >
      <!-- Rotation Enabled -->
      <div class="flex items-start">
        <div class="flex items-center h-5">
          <input
            id="rotationEnabled"
            v-model="config.rotationEnabled"
            type="checkbox"
            class="h-5 w-5 text-primary focus:ring-primary border-gray-300 rounded"
          >
        </div>
        <label
          for="rotationEnabled"
          class="ml-3 block text-base sm:text-lg font-medium text-black"
        >
          Automatische Ansichtsrotation aktivieren
        </label>
      </div>

      <!-- Skip OneDisplay in Rotation -->
      <div
        v-if="config.rotationEnabled"
        class="flex items-start ml-8"
      >
        <div class="flex items-center h-5">
          <input
            id="skipOneDisplay"
            v-model="config.skipOneDisplayInRotation"
            type="checkbox"
            class="h-5 w-5 text-primary focus:ring-primary border-gray-300 rounded"
          >
        </div>
        <label
          for="skipOneDisplay"
          class="ml-3 block text-sm sm:text-base font-medium text-black"
        >
          Automatische Ansichtsrotation ohne OneDisplay aktivieren
        </label>
      </div>

      <!-- Rotation Interval -->
      <div v-if="config.rotationEnabled">
        <label
          for="interval"
          class="block text-sm font-medium text-black"
        >Rotationsintervall (Sekunden)</label>
        <input
          id="interval"
          v-model.number="config.rotationIntervalSeconds"
          type="number"
          min="5"
          class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2 bg-white text-gray-900"
          required
        >
        <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
          Zeitdauer bis zum Wechsel zur nächsten Ansicht.
        </p>
      </div>

      <!-- Default View -->
      <div>
        <label
          for="defaultView"
          class="block text-sm font-medium text-black"
        >
          {{ config.rotationEnabled ? 'Standardansicht (Fallback)' : 'Aktive Ansicht' }}
        </label>
        <select
          id="defaultView"
          v-model="config.defaultSingleViewId"
          class="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2 bg-white text-gray-900"
        >
          <option value="dashboard">
            Dashboard (OneDisplay)
          </option>
          <option value="calendar">
            Kalender
          </option>
          <option value="parking">
            Parken
          </option>
          <option value="highscore">
            Highscore
          </option>
          <option value="game">
            Game
          </option>
        </select>
        <p
          v-if="!config.rotationEnabled"
          class="mt-1 text-sm text-gray-500 dark:text-gray-400"
        >
          Da die Rotation deaktiviert ist, wird diese Ansicht dauerhaft angezeigt.
        </p>
      </div>

      <div class="pt-4">
        <BaseButton
          type="submit"
          :loading="saving"
          variant="primary"
          class="w-full"
        >
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
 */
import { ref, onMounted } from 'vue';
import { configApi } from '@/services/api';
import LoadingSpinner from '@/components/common/LoadingSpinner.vue';
import BaseButton from '@/components/common/BaseButton.vue';

const loading = ref(true);
const saving = ref(false);
const error = ref(null);
const config = ref({
    rotationEnabled: true,
    skipOneDisplayInRotation: false,
    rotationIntervalSeconds: 15,
    defaultSingleViewId: 'calendar'
});

onMounted(async () => {
    try {
        const data = await configApi.getConfig();
        if (data) {
            config.value = data;
        }
    } catch {
        error.value = "Konfiguration konnte nicht geladen werden.";
    } finally {
        loading.value = false;
    }
});

const saveConfig = async () => {
    const interval = config.value.rotationIntervalSeconds;

    // Rotation interval must be at least 5 seconds to prevent browser crashes
    if (interval < 5) {
        alert("Rotationsintervall muss mindestens 5 Sekunden betragen.");
        return;
    }

    saving.value = true;
    error.value = null;
    try {
        await configApi.updateConfig(config.value);
        alert("Konfiguration erfolgreich gespeichert!");
    } catch {
        error.value = "Konfiguration konnte nicht gespeichert werden.";
    } finally {
        saving.value = false;
    }
};
</script>