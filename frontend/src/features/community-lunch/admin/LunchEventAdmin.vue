<template>
  <div class="lunch-event-admin space-y-8">
    <!-- User Identity Section -->
    <div
      class="bg-white dark:bg-gray-800 p-4 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 flex items-center gap-4"
    >
      <div class="font-bold text-black dark:text-gray-100">Dein Name für Abstimmungen:</div>
      <input
        v-model="userName"
        type="text"
        placeholder="Name eingeben..."
        class="flex-grow max-w-sm rounded-md border-gray-300 bg-white text-gray-900 shadow-sm focus:border-primary focus:ring focus:ring-primary focus:ring-opacity-50"
        @change="saveUserName"
      />
    </div>

    <!-- Events List -->
    <div v-if="loading" class="text-center py-12">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto" />
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
      <div
        v-for="lunch in lunches"
        :key="lunch.id"
        class="lunch-card border dark:border-gray-700 rounded-xl overflow-hidden shadow-sm bg-white dark:bg-gray-900 flex flex-col"
      >
        <!-- Card Header -->
        <div class="bg-primary text-white p-4 flex justify-between items-start">
          <div>
            <h3 class="text-xl font-bold">
              {{ formatDate(lunch.date) }}
            </h3>
            <p class="text-sm opacity-90">
              {{ lunch.note || 'Keine Notiz' }}
            </p>
          </div>
          <!-- Delete Button -->
          <button
            class="text-white hover:text-red-200 transition-colors p-1 bg-white/10 rounded hover:bg-white/20"
            title="Event löschen"
            @click.stop="deleteLunch(lunch)"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-5 w-5"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
              />
            </svg>
          </button>
        </div>

        <!-- Options List -->
        <div class="p-4 flex-grow overflow-y-auto max-h-96">
          <h4 class="text-sm font-bold text-gray-500 uppercase mb-2">Optionen</h4>

          <div class="space-y-3 mb-4">
            <div
              v-for="option in lunch.options"
              :key="option.id"
              class="relative group rounded-lg p-3 transition-all cursor-pointer"
              :class="[
                hasVotedFor(lunch, option)
                  ? 'border-2 border-primary bg-white dark:bg-gray-800 shadow-md'
                  : 'border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800 hover:border-primary',
              ]"
              @click="voteForOption(lunch, option)"
            >
              <div class="flex justify-between items-center">
                <div class="flex-grow">
                  <div
                    class="font-bold text-lg"
                    :class="
                      hasVotedFor(lunch, option)
                        ? 'text-primary dark:text-blue-400'
                        : 'text-gray-900 dark:text-gray-100'
                    "
                  >
                    {{ option.label }}
                  </div>
                  <div class="flex items-center gap-2 mt-1">
                    <span
                      class="inline-flex items-center justify-center text-xs font-semibold px-2 py-0.5 rounded"
                      :class="
                        hasVotedFor(lunch, option)
                          ? 'bg-primary text-white'
                          : 'bg-blue-100 text-blue-800 dark:bg-blue-200 dark:text-blue-900'
                      "
                    >
                      {{ option.voteCount || 0 }} Stimmen
                    </span>
                    <span
                      v-if="hasVotedFor(lunch, option)"
                      class="text-xs text-primary font-bold animate-pulse"
                    >
                      ✓ Deine Wahl
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <div
              v-if="lunch.options.length === 0"
              class="text-center text-gray-400 text-sm py-2 italic"
            >
              Noch keine Optionen hinzugefügt.
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * Main administration and voting view for lunch events.
 * Handles user identification, event display, voting, and event deletion.
 */
import { ref, onMounted, watch } from 'vue'
import { lunchService } from '@/services/lunchService'

const lunches = ref([])
const loading = ref(false)
const userName = ref('')
const myChoices = ref([]) // [{ eventId, optionId }]

/**
 * Formats a date string into a local weekday format.
 */
const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('de-DE', {
    weekday: 'short',
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  })
}

/**
 * Saves the user's name to localStorage for persistence across sessions.
 */
const saveUserName = () => {
  if (userName.value) {
    localStorage.setItem('lunchUserName', userName.value)
    fetchMyChoices()
  } else {
    myChoices.value = []
  }
}

watch(userName, (newVal) => {
  if (!newVal) {
    myChoices.value = []
  }
})

/**
 * Fetches the current user's voting choices from the backend.
 */
const fetchMyChoices = async () => {
  if (!userName.value) {
    myChoices.value = []
    return
  }
  try {
    myChoices.value = await lunchService.getMyChoices(userName.value)
  } catch (e) {
    console.error('Failed to fetch choices', e)
  }
}

/**
 * Checks if the user has voted for a specific option.
 */
const hasVotedFor = (lunch, option) => {
  return myChoices.value.some((c) => c.eventId === lunch.id && c.optionId === option.id)
}

/**
 * Submits a vote for a lunch option and refreshes local data.
 */
const voteForOption = async (lunch, option) => {
  if (!userName.value) {
    alert('Bitte gib zuerst deinen Namen oben ein!')
    return
  }
  try {
    await lunchService.voteForOption(lunch.id, option.id, userName.value)
    await refreshEvent(lunch)
    await fetchMyChoices()
  } catch (e) {
    console.error(e)
    alert('Fehler beim Abstimmen')
  }
}

/**
 * Loads all upcoming lunch events and their current results.
 */
const loadData = async () => {
  loading.value = true
  const storedName = localStorage.getItem('lunchUserName')
  if (storedName) {
    userName.value = storedName
  }

  try {
    const lunchesData = await lunchService.getUpcomingLunches(30)
    if (userName.value) {
      await fetchMyChoices()
    }

    // Initialize list with basic data immediately
    lunches.value = lunchesData.map((lunch) => ({
      ...lunch,
      options: lunch.options.map((opt) => ({ ...opt, voteCount: 0 })),
    }))
    loading.value = false // Show the list now

    // Asynchronously update with results
    lunches.value.forEach(async (lunch) => {
      try {
        const resultsData = await lunchService.getResults(lunch.id)
        if (resultsData && resultsData.results) {
          lunch.options = lunch.options.map((opt) => {
            const result = resultsData.results.find((r) => r.optionId === opt.id)
            return { ...opt, voteCount: result ? result.count : 0 }
          })
        }
      } catch (e) {
        console.error(`Error loading results for event ${lunch.id}`, e)
      }
    })
  } catch (e) {
    console.error(e)
    loading.value = false
  }
}

/**
 * Refreshes data for a single event to avoid full page reloads.
 */
const refreshEvent = async (lunch) => {
  try {
    const updatedEvent = await lunchService.getEvent(lunch.id)
    const resultsData = await lunchService.getResults(lunch.id)

    if (updatedEvent && resultsData) {
      lunch.options = updatedEvent.options.map((opt) => {
        const result = resultsData.results.find((r) => r.optionId === opt.id)
        return { ...opt, voteCount: result ? result.count : 0 }
      })
      lunch.location = updatedEvent.location
      lunch.note = updatedEvent.note
      lunch.status = updatedEvent.status
    }
  } catch (e) {
    console.error('Error refreshing event', e)
  }
}

/**
 * Deletes a lunch event after user confirmation.
 */
const deleteLunch = async (lunch) => {
  if (!confirm(`Möchtest du das Lunch-Event am ${formatDate(lunch.date)} wirklich löschen?`)) return

  try {
    await lunchService.deleteEvent(lunch.id)
    lunches.value = lunches.value.filter((l) => l.id !== lunch.id)
  } catch (e) {
    console.error('Failed to delete event', e)
    alert('Fehler beim Löschen des Events.')
  }
}

onMounted(loadData)
</script>
