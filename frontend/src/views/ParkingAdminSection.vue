<template>
  <div class="parking-admin-section p-4 sm:p-6">
    <div class="mb-6">
      <h2 class="text-2xl sm:text-3xl font-bold text-text-dark">
        Parking
      </h2>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <div class="lg:col-span-1">
        <BaseCard title="Neue Reservierung">
          <BookingForm @booking-created="handleBookingCreated" />
        </BaseCard>
      </div>

      <div class="lg:col-span-2">
        <BaseCard title="Reservierungen">
          <FilterControls
            v-model:selected-date="selectedDate"
            v-model:search-name="searchName"
            @filter-changed="handleFilterChanged"
          />

          <div
            v-if="loading"
            class="flex justify-center items-center h-64"
          >
            <LoadingSpinner />
          </div>

          <div
            v-else-if="error"
            class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative"
          >
            <strong class="font-bold">Fehler:</strong>
            <span class="block sm:inline">{{ error }}</span>
            <BaseButton
              variant="danger"
              size="small"
              class="mt-2"
              @click="loadBookings"
            >
              Erneut versuchen
            </BaseButton>
          </div>

          <div v-else>
            <p class="text-sm text-gray-600 mb-4">
              Zeige {{ filteredBookings.length }} von {{ bookings.length }} Reservierungen
              <span
                v-if="selectedDate"
                class="font-medium"
              >
                für den {{ formatDateForDisplay(selectedDate) }}
              </span>
              <span
                v-if="searchName"
                class="font-medium"
              >
                <span v-if="selectedDate"> und </span>
                Suche nach "{{ searchName }}"
              </span>
            </p>
            <BookingList
              :bookings="filteredBookings"
              @booking-deleted="handleBookingDeleted"
              @booking-updated="handleBookingUpdated"
            />
          </div>
        </BaseCard>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * Main component for parking management in the admin area.
 * Allows creating new reservations, as well as viewing and deleting
 * existing bookings with filtering functionality.
 */
import {ref, onMounted, computed} from 'vue'
import {parkingApi} from '@/services/api'
import BaseCard from '@/components/common/BaseCard.vue'
import BaseButton from '@/components/common/BaseButton.vue'
import BookingForm from '@/features/parking/admin/BookingForm.vue'
import BookingList from '@/features/parking/admin/BookingList.vue'
import FilterControls from '@/features/parking/admin/FilterControls.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

const bookings = ref([])
const loading = ref(true)
const error = ref(null)
const selectedDate = ref('')
const searchName = ref('')

/**
 * Formats a date string (YYYY-MM-DD) for display (DD.MM.YYYY).
 */
const formatDateForDisplay = (dateString) => {
  if (!dateString) return ''
  const [year, month, day] = dateString.split('-')
  return `${day}.${month}.${year}`
}

/**
 * Compares two date strings for equality on the same calendar day.
 */
const isSameDay = (dateString1, dateString2) => {
  if (!dateString1 || !dateString2) return false
  const date1 = new Date(dateString1.includes('-') ? dateString1 : dateString1.split('.').reverse().join('-'))
  const date2 = new Date(dateString2.includes('-') ? dateString2 : dateString2.split('.').reverse().join('-'))
  return date1.toDateString() === date2.toDateString()
}

/**
 * Filters the list of bookings based on the selected date and/or employee name.
 */
const filteredBookings = computed(() => {
  return bookings.value.filter(booking => {
    const matchesDate = !selectedDate.value || isSameDay(booking.date, selectedDate.value)
    const matchesName = !searchName.value ||
      booking.employeeName.toLowerCase().includes(searchName.value.toLowerCase())

    return matchesDate && matchesName
  })
})

/**
 * Loads all parking reservations from the server.
 */
const loadBookings = async () => {
  loading.value = true
  error.value = null

  try {
    const data = await parkingApi.getAllEntries()
    bookings.value = data
  } catch (err) {
    console.error('Error loading bookings:', err)
    error.value = 'Buchungen konnten nicht geladen werden. Bitte versuchen Sie es später erneut.'
  } finally {
    loading.value = false
  }
}

const handleBookingCreated = () => {
  loadBookings()
}

const handleBookingDeleted = () => {
  loadBookings()
}

const handleBookingUpdated = () => {
  loadBookings()
}

const handleFilterChanged = () => {
  // Logic already covered by computed property.
}

onMounted(() => {
  loadBookings()
})
</script>

<style scoped>
/* Scoped styles for ParkingAdminSection if needed */
</style>