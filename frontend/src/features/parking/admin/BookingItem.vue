<template>
  <div
    class="booking-item bg-white border border-gray-200 rounded-lg p-3 sm:p-4 shadow-sm relative"
  >
    <div
      v-if="!isEditing"
      class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3"
    >
      <div class="flex-1">
        <h4 class="font-bold text-gray-900">
          {{ booking.employeeName }}
        </h4>
        <p class="text-sm text-gray-600 mt-1">
          {{ formatDate(booking.date) }} ({{ formatWeekday(booking.date) }})
        </p>
        <p v-if="booking.note" class="text-sm text-gray-500 mt-1">
          {{ booking.note }}
        </p>
      </div>

      <div class="flex gap-2 shrink-0">
        <BaseButton
          v-if="!booking.recurringReservation"
          variant="secondary"
          size="small"
          title="Bearbeiten"
          @click="startEdit"
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
          title="Löschen"
          :loading="deleting"
          @click="deleteBooking"
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

    <!-- Bearbeitungsmodus -->
    <div v-else class="space-y-4">
      <div>
        <label class="block text-xs font-medium text-gray-500 uppercase mb-1">Name</label>
        <input
          v-model="editData.employeeName"
          type="text"
          class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
          placeholder="Name des Mitarbeiters"
        />
      </div>

      <div v-if="!booking.recurringReservation">
        <DateInput id="edit-date" v-model="editData.date" label="Datum" />
      </div>

      <div class="flex gap-2 pt-2">
        <BaseButton variant="primary" size="small" :loading="saving" @click="saveEdit">
          Speichern
        </BaseButton>
        <BaseButton variant="secondary" size="small" :disabled="saving" @click="isEditing = false">
          Abbrechen
        </BaseButton>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * Displays a single parking reservation item.
 * Allows editing (Name/Date) and deleting the reservation.
 * For recurring reservations, only the name can be edited directly here.
 */

import { ref } from 'vue'
import { parkingApi } from '@/services/api'
import { formatDate, formatWeekday, formatDateForAPI } from '@/utils/dateUtils'
import BaseButton from '@/components/common/BaseButton.vue'
import DateInput from '@/components/common/DateInput.vue'

const props = defineProps({
  booking: {
    type: Object,
    required: true,
  },
})

const emit = defineEmits(['booking-deleted', 'booking-updated'])

const deleting = ref(false)
const isEditing = ref(false)
const saving = ref(false)
const editData = ref({ employeeName: '', date: '' })

const startEdit = () => {
  editData.value = {
    employeeName: props.booking.employeeName,
    date: props.booking.date,
  }
  isEditing.value = true
}

const saveEdit = async () => {
  if (!editData.value.employeeName.trim()) {
    alert('Bitte geben Sie einen Namen ein.')
    return
  }

  saving.value = true

  try {
    const payload = {
      employeeName: editData.value.employeeName,
      date: formatDateForAPI(editData.value.date),
    }
    await parkingApi.updateEntry(props.booking.id, payload)
    isEditing.value = false
    emit('booking-updated')
  } catch (error) {
    console.error('Error updating booking:', error)
    const msg =
      error.response?.data?.detail ||
      error.response?.data?.message ||
      'Fehler beim Speichern der Änderungen.'
    alert(msg)
  } finally {
    saving.value = false
  }
}

const deleteBooking = async () => {
  if (
    !confirm(`Möchten Sie die Reservierung für ${props.booking.employeeName} wirklich löschen?`)
  ) {
    return
  }

  deleting.value = true

  try {
    await parkingApi.deleteEntry(props.booking.id)
    emit('booking-deleted')
  } catch (error) {
    console.error('Error deleting booking:', error)
    const msg =
      error.response?.data?.detail ||
      error.response?.data?.message ||
      'Fehler beim Löschen der Reservierung.'
    alert(msg)
  } finally {
    deleting.value = false
  }
}
</script>
