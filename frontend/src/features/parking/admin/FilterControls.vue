<template>
  <div class="filter-controls mb-4">
    <div class="flex flex-col sm:flex-row gap-4">
      <div class="flex-1">
        <label for="nameFilter" class="block text-sm font-medium text-gray-700 mb-1">
          Nach Name suchen
        </label>
        <input
          id="nameFilter"
          v-model="localSearchName"
          type="text"
          placeholder="Name eingeben..."
          class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary focus:border-primary sm:text-sm"
          @input="handleNameInput"
        />
      </div>

      <div class="flex-1">
        <label for="dateFilter" class="block text-sm font-medium text-gray-700 mb-1">
          Nach Datum filtern
        </label>
        <input
          id="dateFilter"
          v-model="localSelectedDate"
          type="date"
          class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary focus:border-primary sm:text-sm"
          @change="handleDateChange"
        />
      </div>

      <div class="flex items-end">
        <BaseButton variant="secondary" class="w-full sm:w-auto" @click="clearFilter">
          Filter löschen
        </BaseButton>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * Provides controls for filtering parking reservations.
 * Enables searching by employee name, selecting a specific date, or resetting filters.
 */
import { ref, watch } from 'vue'
import BaseButton from '@/components/common/BaseButton.vue'

// Defines the props passed from the parent component.
const props = defineProps({
  selectedDate: {
    type: String,
    default: '',
  },
  searchName: {
    type: String,
    default: '',
  },
})

// Declares the events this component can emit to the parent component.
const emit = defineEmits(['update:selectedDate', 'update:searchName', 'filter-changed'])

// Local reactive states for the inputs to avoid direct prop mutation.
const localSelectedDate = ref(props.selectedDate)
const localSearchName = ref(props.searchName)

// Synchronizes the local values when the props change externally.
watch(
  () => props.selectedDate,
  (newValue) => {
    localSelectedDate.value = newValue
  }
)

watch(
  () => props.searchName,
  (newValue) => {
    localSearchName.value = newValue
  }
)

// Called when the user types in the name search field.
const handleNameInput = () => {
  emit('update:searchName', localSearchName.value)
  emit('filter-changed')
}

// Called when the user selects a new date.
const handleDateChange = () => {
  emit('update:selectedDate', localSelectedDate.value)
  emit('filter-changed')
}

// Resets all filters and emits the changes.
const clearFilter = () => {
  localSelectedDate.value = ''
  localSearchName.value = ''
  emit('update:selectedDate', '')
  emit('update:searchName', '')
  emit('filter-changed')
}
</script>

<style scoped>
.filter-controls {
  background: #f9fafb;
  padding: 1rem;
  border-radius: 0.5rem;
  border: 1px solid #e5e7eb;
}
</style>
