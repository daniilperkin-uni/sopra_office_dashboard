<template>
  <form class="space-y-4" @submit.prevent="submitBooking">
    <div>
      <label for="employeeName" class="block text-sm font-medium text-gray-700 mb-1">
        Mitarbeitername
      </label>
      <input
        id="employeeName"
        v-model="booking.employeeName"
        type="text"
        required
        class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary focus:border-primary sm:text-sm"
        placeholder="Muster, Max"
      />
      <p v-if="errors.employeeName" class="mt-1 text-sm text-red-600">
        {{ errors.employeeName }}
      </p>
    </div>

    <DateInput
      id="bookingDate"
      v-model="booking.date"
      :label="isMultiMonthMode ? 'Startdatum' : 'Datum'"
      :min-date="minDate"
      :max-date="isMultiMonthMode ? '' : maxDate"
      :error="errors.date"
    />

    <div class="flex items-center mt-2 mb-4">
      <button
        type="button"
        class="text-sm text-primary hover:text-primary-dark underline focus:outline-none"
        @click="isMultiMonthMode = !isMultiMonthMode"
      >
        {{ isMultiMonthMode ? 'Einmalige Reservierung' : 'Dauerparker' }}
      </button>
    </div>

    <div v-if="isMultiMonthMode" class="bg-gray-50 p-4 rounded-md mb-4 border border-gray-200">
      <h3 class="text-sm font-medium text-gray-700 mb-3">Serien-Reservierung</h3>

      <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Wochentage</label>
          <div
            class="flex flex-col gap-2 bg-white border border-gray-300 rounded-md p-2 max-h-40 overflow-y-auto"
          >
            <label v-for="day in weekDays" :key="day.value" class="inline-flex items-center">
              <input
                v-model="multiMonthConfig.daysOfWeek"
                type="checkbox"
                :value="day.value"
                class="rounded border-gray-300 text-primary focus:ring-primary h-4 w-4"
              />
              <span class="ml-2 text-sm text-gray-700">{{ day.label }}</span>
            </label>
          </div>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Dauer</label>
          <select
            v-model="multiMonthConfig.duration"
            class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary focus:border-primary sm:text-sm bg-white text-gray-900"
          >
            <option v-for="m in 6" :key="m" :value="m">
              {{ m }} {{ m === 1 ? 'Monat' : 'Monate' }}
            </option>
          </select>
        </div>
      </div>
      <p class="text-xs text-gray-500 mt-2">
        Es wird ab dem Startdatum für {{ multiMonthConfig.duration }} Monat(e) an den gewählten
        Wochentagen gebucht.
      </p>
    </div>

    <!-- eslint-disable vue/no-v-html -->
    <div
      v-if="submitError"
      class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded"
      v-html="submitError"
    />
    <!-- eslint-enable vue/no-v-html -->

    <!-- Conflict Confirmation Modal -->
    <div
      v-if="showConflictModal"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50"
      @click.self="cancelBooking"
    >
      <div class="bg-white rounded-lg shadow-xl p-6 w-full max-w-md mx-4">
        <h2 class="text-xl font-bold text-gray-800 mb-4">Buchungskonflikte</h2>
        <p class="text-sm text-gray-600 mb-4">
          Folgende Tage können nicht gebucht werden. Möchten Sie die Reservierung für die restlichen
          verfügbaren Tage trotzdem erstellen?
        </p>
        <div
          class="max-h-48 overflow-y-auto bg-gray-50 p-3 rounded-md border border-gray-200 space-y-2 mb-6"
        >
          <p v-for="conflict in conflictDetails" :key="conflict.date" class="text-sm text-gray-800">
            {{ conflict.status === 'RESERVED_BEFORE' ? '⚠️' : '⛔' }} {{ conflict.date }}:
            {{ conflict.statusText }}
          </p>
        </div>
        <div class="flex justify-end gap-3">
          <BaseButton variant="secondary" @click="cancelBooking"> Verwerfen </BaseButton>
          <BaseButton variant="primary" :loading="submitting" @click="handleForceBooking">
            Trotzdem Buchen
          </BaseButton>
        </div>
      </div>
    </div>

    <BaseButton type="submit" variant="primary" :loading="submitting" class="w-full">
      {{ isMultiMonthMode ? 'Serien-Reservierung erstellen' : 'Reservierung erstellen' }}
    </BaseButton>
  </form>
</template>

<script setup>
/**
 * This Vue component provides a form for creating a new parking reservation.
 * It captures the employee name and a date, restricting the date selection to working days within the next 7 days and preventing past bookings.
 * After successful validation, the data is sent to an API and a 'booking-created' event is emitted to notify the parent component.
 * Input errors or API request errors are clearly displayed to the user.
 */

import { ref, reactive, computed } from 'vue'
import { parkingApi } from '@/services/api'
import {
  formatDateForAPI,
  isWeekend,
  getWorkingDays,
  isPastDate,
  formatDate,
} from '@/utils/dateUtils'
import BaseButton from '@/components/common/BaseButton.vue'
import DateInput from '@/components/common/DateInput.vue'

const emit = defineEmits(['booking-created'])

const booking = reactive({
  employeeName: '',
  date: new Date().toISOString().split('T')[0],
})

const isMultiMonthMode = ref(false)
const multiMonthConfig = reactive({
  daysOfWeek: [],
  duration: 1, // Months
})

// State for conflict modal
const showConflictModal = ref(false)
const conflictDetails = ref([])
const lastPayload = ref(null)

const weekDays = [
  { label: 'Montag', value: 'MONDAY' },
  { label: 'Dienstag', value: 'TUESDAY' },
  { label: 'Mittwoch', value: 'WEDNESDAY' },
  { label: 'Donnerstag', value: 'THURSDAY' },
  { label: 'Freitag', value: 'FRIDAY' },
]

const errors = reactive({
  employeeName: '',
  date: '',
})

const submitting = ref(false)
const submitError = ref('')

const minDate = computed(() => {
  return new Date().toISOString().split('T')[0]
})

const maxDate = computed(() => {
  const workingDays = getWorkingDays(new Date(), 8)
  const lastDay = workingDays[workingDays.length - 1]
  return lastDay.toISOString().split('T')[0]
})

const validateForm = () => {
  let isValid = true

  if (!booking.employeeName.trim()) {
    errors.employeeName = 'Bitte geben Sie einen Namen ein'
    isValid = false
  } else {
    errors.employeeName = ''
  }

  if (!booking.date) {
    errors.date = 'Bitte wählen Sie ein Datum'
    isValid = false
  } else if (isPastDate(booking.date)) {
    errors.date = 'Buchungen für vergangene Tage sind nicht möglich'
    isValid = false
  } else if (isWeekend(booking.date) && !isMultiMonthMode.value) {
    errors.date = 'Wochenenden sind nicht buchbar'
    isValid = false
  } else {
    if (!isMultiMonthMode.value) {
      const selected = new Date(booking.date)
      const maxAllowed = new Date(maxDate.value)
      if (selected > maxAllowed) {
        errors.date = 'Reservierungen sind nur für die nächsten 8 Arbeitstage möglich.'
        isValid = false
      } else {
        errors.date = ''
      }
    } else {
      errors.date = ''
    }
  }

  return isValid
}

/**
 * Processes the final result from the API and generates a user-friendly message.
 * @param {object} result - The result object from the API, containing created and skipped dates.
 */
function processBookingResult(result) {
  let createdCount = 0
  const skippedDetails = []

  if (result && result.dates) {
    Object.entries(result.dates).forEach(([dateStr, status]) => {
      if (status === 'OK') {
        createdCount++
      } else {
        let statusText = status
        if (status === 'FULL') statusText = 'Voll belegt'
        else if (status === 'RESERVED_BEFORE') statusText = 'Bereits reserviert'
        skippedDetails.push(`${formatDate(dateStr)} (${statusText})`)
      }
    })
  }

  const skippedCount = skippedDetails.length

  if (createdCount === 0 && skippedCount > 0) {
    const details = skippedDetails.slice(0, 5).join('<br> ') + (skippedCount > 5 ? '...' : '')
    submitError.value = `Keine Reservierungen erstellt. <br> Alle ${skippedCount} Termine waren nicht buchbar: <br> ${details}`
  } else if (skippedCount > 0) {
    const details = skippedDetails.slice(0, 3).join('<br>') + (skippedCount > 3 ? '...' : '')
    submitError.value = `Buchungsserie für ${booking.employeeName} erstellt: <br> ${createdCount} Termine gebucht. <br> ${skippedCount} Termine übersprungen: <br> ${details}`
    booking.employeeName = '' // Reset only on partial success
    emit('booking-created')
  } else {
    alert(
      `Buchungsserie für ${booking.employeeName} erfolgreich erstellt (${createdCount} Termine).`
    )
    // Reset form completely on full success
    booking.employeeName = ''
    booking.date = new Date().toISOString().split('T')[0]
    isMultiMonthMode.value = false
    multiMonthConfig.daysOfWeek = []
    emit('booking-created')
  }
}

/**
 * Handles the "Book Anyway" action from the conflict modal.
 * Makes the actual booking API call without the preview flag.
 */
async function handleForceBooking() {
  showConflictModal.value = false
  if (!lastPayload.value) return

  submitting.value = true
  submitError.value = ''

  try {
    // Make the real booking call (preview=false)
    const result = await parkingApi.createRecurringReservation(lastPayload.value, false)
    processBookingResult(result)
  } catch (error) {
    console.error('Error forcing booking:', error)
    submitError.value = 'Ein unerwarteter Fehler ist beim Buchen aufgetreten.'
  } finally {
    submitting.value = false
    lastPayload.value = null // Clear payload after use
  }
}

/**
 * Cancels the booking process from the modal.
 */
function cancelBooking() {
  showConflictModal.value = false
  lastPayload.value = null
  submitting.value = false
}

const submitBooking = async () => {
  // --- Start: Reset & Validation ---
  submitError.value = '' // 1. alte Fehlermeldung löschen
  if (!validateForm()) {
    // 2. Basis-Validierung (Name, Datum, ...)
    return // 3. Abbruch bei invalidem Formular
  }
  submitting.value = true // 4. UI: Lade-Spinner aktivieren / Doppelclick verhindern

  try {
    // --- Pfad: Multi-Month (Serie) ---
    if (isMultiMonthMode.value) {
      // 5. zusätzliche Validierung: mindestens ein Wochentag muss gewählt sein
      if (multiMonthConfig.daysOfWeek.length === 0) {
        submitError.value = 'Bitte wählen Sie mindestens einen Wochentag aus.'
        submitting.value = false
        return
      }

      // 6. Start- und Enddatum berechnen (Enddatum = Start + duration Monate)
      const startDate = new Date(booking.date)
      const endDate = new Date(startDate)
      endDate.setMonth(endDate.getMonth() + multiMonthConfig.duration)

      // 7. Payload für das Backend zusammensetzen
      const payload = {
        employeeName: booking.employeeName,
        startDate: formatDateForAPI(startDate),
        endDate: formatDateForAPI(endDate),
        daysOfWeek: multiMonthConfig.daysOfWeek,
      }

      // 8. Dry-Run: Preview-Request mit preview=true, um Konflikte zu ermitteln
      const previewResult = await parkingApi.createRecurringReservation(payload, true)

      // --- Auswertung der Preview-Antwort ---
      const conflicts = [] // 9. Konflikt-Array initialisieren
      let hasAvailability = false // 10. Flag: gibt es mindestens einen freien Termin?
      if (previewResult && previewResult.dates) {
        // 11. Für jedes Datum: OK vs. Konflikt unterscheiden
        Object.entries(previewResult.dates).forEach(([dateStr, status]) => {
          if (status !== 'OK') {
            // 12. Status-Normalisierung für bessere Anzeige
            let statusText = status
            if (status === 'FULL') statusText = 'Voll belegt'
            else if (status === 'RESERVED_BEFORE') statusText = 'Bereits reserviert'
            conflicts.push({
              date: formatDate(dateStr),
              status: status,
              statusText: statusText,
            })
          } else {
            hasAvailability = true
          }
        })
      }

      // --- Entscheidungslogik basierend auf Konflikten ---
      if (conflicts.length > 0) {
        if (hasAvailability) {
          // 13. Gemischtes Ergebnis: Zeige Modal mit Konfliktdetails (Nutzerentscheid nötig)
          conflictDetails.value = conflicts
          lastPayload.value = payload
          showConflictModal.value = true
          submitting.value = false
        } else {
          // 14. Alles belegt: Direkte Fehlermeldung mittels processBookingResult()
          processBookingResult(previewResult)
          submitting.value = false
        }
      } else {
        // 15. Keine Konflikte: Payload merken und verbindliche Buchung einleiten
        lastPayload.value = payload
        await handleForceBooking()
      }
    } else {
      // --- Pfad: Einzelbuchung ---
      const bookingData = {
        employeeName: booking.employeeName,
        date: formatDateForAPI(booking.date),
      }

      // 16. Sofort-Buchung API-Call
      await parkingApi.createEntry(bookingData)

      // 17. UI: Formular zurücksetzen
      booking.employeeName = ''
      booking.date = new Date().toISOString().split('T')[0]

      // 18. Event an Parent-Komponente, damit z.B. Liste neu geladen wird
      emit('booking-created')
      submitting.value = false
    }
  } catch (error) {
    // --- Fehlerbehandlung ---
    console.error('Error creating booking:', error)

    if (error.response && error.response.data) {
      const data = error.response.data
      const msg = data.detail || data.message || 'Ein Fehler ist aufgetreten.'

      // 19. Technische Backend-Fehlermeldungen in verständliche Deutsch-Meldungen übersetzen
      if (msg.includes('No free parking spots')) {
        submitError.value = 'Für dieses Datum sind keine freien Parkplätze mehr verfügbar.'
      } else if (msg.includes('already registered')) {
        submitError.value =
          'Dieser Mitarbeiter hat für dieses Datum bereits einen Parkplatz reserviert.'
      } else if (msg.includes('date in past')) {
        submitError.value = 'Buchungen in die Vergangenheit sind nicht möglich.'
      } else {
        submitError.value = msg
      }
    } else {
      // 20. Generische Fehlermeldung bei Netzwerk/kein Response
      submitError.value =
        'Fehler beim Erstellen der Reservierung. Bitte versuchen Sie es später erneut.'
    }
    submitting.value = false // 21. Ladezustand zurücksetzen
  }
}
</script>
