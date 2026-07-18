<template>
  <div class="booking-list">
    <div v-if="bookings.length === 0" class="text-center py-8 text-gray-500">
      Keine Reservierungen gefunden
    </div>

    <div v-else class="space-y-6">
      <!-- Serien-Reservierungen -->
      <div v-if="groupedData.recurring.length > 0" class="space-y-4">
        <h3 class="font-bold text-gray-700 uppercase text-xs border-b pb-2">
          Serien-Reservierungen
        </h3>

        <div
          v-for="group in groupedData.recurring"
          :key="group.reservation.id"
          class="border border-gray-200 rounded-lg overflow-hidden"
        >
          <div
            class="bg-gray-50 p-3 sm:p-4 flex items-center justify-between cursor-pointer hover:bg-gray-100 transition-colors"
            @click="toggleSeries(group.reservation.id)"
          >
            <div class="flex items-center gap-3">
              <span
                class="transform transition-transform duration-200"
                :class="{ 'rotate-90': expandedSeries.has(group.reservation.id) }"
              >
                ▶
              </span>
              <div>
                <h4 class="font-bold text-gray-900">
                  {{ group.reservation.employeeName }}
                </h4>
                <div class="text-sm text-gray-500">
                  {{ group.entries.length }} Einträge ({{ formatDateRange(group.entries) }})
                </div>
              </div>
            </div>

            <div class="flex items-center gap-2 shrink-0">
              <BaseButton
                variant="secondary"
                size="small"
                title="Bearbeiten"
                @click.stop="startEditSeries(group.reservation)"
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
                @click.stop="deleteSeries(group.reservation.id)"
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

          <!-- Serien-Bearbeitungsmodus -->
          <div
            v-if="editingSeriesId === group.reservation.id"
            class="bg-blue-50 p-3 sm:p-4 border-t border-gray-200"
          >
            <div class="space-y-4">
              <!-- Name -->
              <div>
                <label class="block text-xs font-medium text-gray-500 uppercase mb-1"
                  >Name der Serie</label
                >
                <input
                  v-model="seriesEditData.employeeName"
                  type="text"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                  placeholder="Name des Mitarbeiters"
                />
              </div>

              <!-- Datum -->
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <DateInput
                  id="edit-series-start"
                  v-model="seriesEditData.startDate"
                  label="Startdatum"
                />

                <div>
                  <label class="block text-xs font-medium text-gray-500 uppercase mb-1"
                    >Dauer (Monate)</label
                  >
                  <select
                    v-model.number="seriesEditData.duration"
                    class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm bg-white"
                  >
                    <option v-for="i in 6" :key="i" :value="i">
                      {{ i }} Monat{{ i > 1 ? 'e' : '' }}
                    </option>
                  </select>
                </div>
              </div>

              <!-- Wochentage -->
              <div>
                <label class="block text-xs font-medium text-gray-500 uppercase mb-2"
                  >Wochentage</label
                >
                <div class="flex flex-wrap gap-2">
                  <label
                    v-for="day in weekdayOptions"
                    :key="day.value"
                    class="inline-flex items-center bg-white border border-gray-300 rounded px-2 py-1 cursor-pointer hover:bg-gray-50"
                  >
                    <input
                      v-model="seriesEditData.daysOfWeek"
                      type="checkbox"
                      :value="day.value"
                      class="form-checkbox h-4 w-4 text-blue-600 rounded focus:ring-blue-500"
                    />
                    <span class="ml-2 text-sm text-gray-700">{{ day.label }}</span>
                  </label>
                </div>
              </div>

              <!-- Buttons -->
              <div class="flex gap-2 justify-end pt-2 border-t border-blue-100">
                <BaseButton
                  variant="secondary"
                  size="small"
                  :disabled="savingSeries"
                  @click="editingSeriesId = null"
                >
                  Abbrechen
                </BaseButton>
                <BaseButton
                  variant="primary"
                  size="small"
                  :loading="savingSeries"
                  @click="saveSeriesEdit"
                >
                  Speichern
                </BaseButton>
              </div>
            </div>
          </div>

          <div
            v-if="expandedSeries.has(group.reservation.id)"
            class="bg-gray-50/50 p-3 sm:p-4 border-t border-gray-200 space-y-2"
          >
            <BookingItem
              v-for="booking in group.entries"
              :key="booking.id"
              :booking="booking"
              @booking-deleted="$emit('booking-deleted')"
              @booking-updated="$emit('booking-updated')"
            />
          </div>
        </div>
      </div>

      <!-- Einzel-Reservierungen -->
      <div v-if="groupedData.single.length > 0" class="space-y-4">
        <h3 class="font-bold text-gray-700 uppercase text-xs border-b pb-2">
          Einzel-Reservierungen
        </h3>
        <div class="space-y-3">
          <BookingItem
            v-for="booking in groupedData.single"
            :key="booking.id"
            :booking="booking"
            @booking-deleted="$emit('booking-deleted')"
            @booking-updated="$emit('booking-updated')"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * Component for listing booking entries.
 * Groups bookings into recurring series and single reservations.
 * Supports expanding/collapsing series and editing/deleting entries.
 */
import { computed, ref } from 'vue'
import BookingItem from './BookingItem.vue'
import { parkingApi } from '@/services/api'
import { formatDate } from '@/utils/dateUtils'
import BaseButton from '@/components/common/BaseButton.vue'
import DateInput from '@/components/common/DateInput.vue'

const props = defineProps({
  bookings: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['booking-deleted', 'booking-updated'])

const expandedSeries = ref(new Set())
const editingSeriesId = ref(null)
const seriesEditData = ref({
  employeeName: '',
  startDate: '',
  endDate: '',
  daysOfWeek: [],
  skipDates: [],
})
const savingSeries = ref(false)

const weekdayOptions = [
  { value: 'MONDAY', label: 'Mo' },
  { value: 'TUESDAY', label: 'Di' },
  { value: 'WEDNESDAY', label: 'Mi' },
  { value: 'THURSDAY', label: 'Do' },
  { value: 'FRIDAY', label: 'Fr' },
]

/**
 * Groups raw booking data into recurring series and single entries.
 */
const groupedData = computed(() => {
  const single = []
  const recurringMap = new Map()

  props.bookings.forEach((b) => {
    if (b.recurringReservation) {
      const id = b.recurringReservation.id
      if (!recurringMap.has(id)) {
        recurringMap.set(id, {
          reservation: b.recurringReservation,
          entries: [],
        })
      }
      recurringMap.get(id).entries.push(b)
    } else {
      single.push(b)
    }
  })

  // Sorting by date
  single.sort((a, b) => new Date(a.date) - new Date(b.date))
  const recurring = Array.from(recurringMap.values()).map((group) => {
    group.entries.sort((a, b) => new Date(a.date) - new Date(b.date))
    return group
  })

  return { single, recurring }
})

const toggleSeries = (id) => {
  const newSet = new Set(expandedSeries.value)
  if (newSet.has(id)) {
    newSet.delete(id)
  } else {
    newSet.add(id)
  }
  expandedSeries.value = newSet
}

const startEditSeries = (reservation) => {
  editingSeriesId.value = reservation.id

  // Calculate approximate duration in months
  const start = new Date(reservation.startDate)
  const end = new Date(reservation.endDate)
  let months = (end.getFullYear() - start.getFullYear()) * 12 + (end.getMonth() - start.getMonth())
  // Clamp to 1-6
  if (months < 1) months = 1
  if (months > 6) months = 6

  seriesEditData.value = {
    employeeName: reservation.employeeName,
    startDate: reservation.startDate,
    duration: months,
    daysOfWeek: reservation.daysOfWeek,
    skipDates: reservation.skipDates || [],
  }
}

const saveSeriesEdit = async () => {
  if (!seriesEditData.value.employeeName.trim()) {
    alert('Bitte geben Sie einen Namen ein.')
    return
  }
  if (seriesEditData.value.daysOfWeek.length === 0) {
    alert('Bitte wählen Sie mindestens einen Wochentag.')
    return
  }

  savingSeries.value = true
  try {
    const { formatDateForAPI } = await import('@/utils/dateUtils')

    // Calculate new End Date based on Start Date + Duration
    const startDateObj = new Date(seriesEditData.value.startDate)
    const endDateObj = new Date(startDateObj)
    endDateObj.setMonth(endDateObj.getMonth() + seriesEditData.value.duration)

    const payload = {
      employeeName: seriesEditData.value.employeeName,
      startDate: formatDateForAPI(seriesEditData.value.startDate),
      endDate: formatDateForAPI(endDateObj),
      daysOfWeek: seriesEditData.value.daysOfWeek,
      skipDates: seriesEditData.value.skipDates.map((d) => formatDateForAPI(d)),
    }

    await parkingApi.updateRecurringReservation(editingSeriesId.value, payload)

    // Success feedback (using name)
    alert(`Serie ${seriesEditData.value.employeeName} erfolgreich aktualisiert.`)

    editingSeriesId.value = null
    emit('booking-updated')
  } catch (e) {
    console.error(e)
    const msg =
      e.response?.data?.detail ||
      e.response?.data?.message ||
      'Fehler beim Aktualisieren der Serie.'
    alert(msg)
  } finally {
    savingSeries.value = false
  }
}

const deleteSeries = async (id) => {
  if (
    !confirm(
      'Möchten Sie wirklich die gesamte Serien-Reservierung und alle zugehörigen Termine löschen?'
    )
  )
    return

  try {
    await parkingApi.deleteRecurringReservation(id)
    emit('booking-deleted')
  } catch (e) {
    console.error(e)
    const msg =
      e.response?.data?.detail || e.response?.data?.message || 'Fehler beim Löschen der Serie.'
    alert(msg)
  }
}

const formatDateRange = (entries) => {
  if (entries.length === 0) return ''
  const start = entries[0].date
  const end = entries[entries.length - 1].date
  return `${formatDate(start)} - ${formatDate(end)}`
}
</script>
