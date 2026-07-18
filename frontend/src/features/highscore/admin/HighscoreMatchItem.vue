<template>
  <div
    class="bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg p-3 sm:p-4 shadow-sm mb-3"
  >
    <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3">
      <div class="flex-1 w-full">
        <!-- Darts Layout -->
        <template v-if="type === 'darts'">
          <div class="flex items-center space-x-2">
            <span class="font-bold text-black">{{ match.playerName }}</span>
            <span class="text-black text-sm">Ergebnis:</span>
            <span class="font-bold text-black">{{ match.dartsToFinish }} Würfe</span>
          </div>
          <div class="text-xs text-black mt-1">
            {{ formatDate(match.date) }}
          </div>
        </template>

        <!-- Kicker Layout -->
        <template v-else-if="type === 'kicker'">
          <div class="flex flex-col space-y-1">
            <div class="flex flex-col sm:flex-row sm:items-center text-sm text-black">
              <span class="font-bold text-black">
                {{ match.teamAPlayerNames?.join(' & ') }}
              </span>
              <span class="font-bold my-1 sm:my-0 sm:mx-2 text-black text-center">vs</span>
              <span class="font-bold text-black">
                {{ match.teamBPlayerNames?.join(' & ') }}
              </span>
            </div>
            <div class="flex items-center justify-between mt-2">
              <div class="flex items-center">
                <span class="text-black text-xs mr-1">Ergebnis:</span>
                <span
                  class="px-2 py-0.5 rounded text-xs font-bold uppercase bg-gray-100 text-black"
                >
                  {{ formatResult(match.matchResult) }}
                </span>
              </div>
              <span class="text-xs text-black">
                {{ formatDate(match.date) }}
              </span>
            </div>
          </div>
        </template>
      </div>

      <div class="w-full sm:w-auto flex-shrink-0 flex gap-2">
        <BaseButton
          variant="secondary"
          size="small"
          title="Bearbeiten"
          class="w-full sm:w-auto px-2"
          @click="emit('edit', { match: props.match, type: props.type })"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-4 w-4"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"
            />
          </svg>
        </BaseButton>
        <BaseButton
          variant="danger"
          size="small"
          :loading="deleting"
          title="Löschen"
          class="w-full sm:w-auto px-2"
          @click="deleteMatch"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-4 w-4"
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
        </BaseButton>
      </div>
    </div>

    <ConfirmDialog
      :is-open="showDeleteDialog"
      title="Match löschen"
      message="Bist du sicher, dass du dieses Match löschen möchtest?"
      confirm-text="Löschen"
      cancel-text="Abbrechen"
      @confirm="confirmDelete"
      @cancel="showDeleteDialog = false"
    />
  </div>
</template>

<script setup>
/**
 * Represents a single match (Darts or Kicker) in the admin history.
 * Offers the possibility to delete the match after a security confirmation.
 */
import { ref } from 'vue'
import BaseButton from '@/components/common/BaseButton.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import { formatDate } from '@/utils/dateUtils'

// Declares the match data and the type (darts/kicker).
const props = defineProps({
  match: {
    type: Object,
    required: true,
  },
  type: {
    type: String,
    required: true, // 'darts' or 'kicker'
  },
})

// Declares the event for deleting a match.
const emit = defineEmits(['delete', 'edit'])
const deleting = ref(false)
const showDeleteDialog = ref(false)

const deleteMatch = async () => {
  showDeleteDialog.value = true
}

const confirmDelete = async () => {
  showDeleteDialog.value = false
  deleting.value = true
  try {
    emit('delete', { id: props.match.id, type: props.type })
  } finally {
    deleting.value = false
  }
}

/**
 * Converts technical result codes into user-friendly texts.
 */
const formatResult = (result) => {
  if (result === 'TEAM_A_WIN') return 'Team A gewinnt'
  if (result === 'TEAM_B_WIN') return 'Team B gewinnt'
  return result
}
</script>
