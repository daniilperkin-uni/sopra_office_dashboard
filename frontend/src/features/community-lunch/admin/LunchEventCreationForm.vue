<template>
  <div
    class="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700"
  >
    <h3 class="text-xl font-bold mb-4 text-gray-900 dark:text-gray-100">
      Neues Lunch-Event anlegen
    </h3>
    <div class="flex flex-col gap-4">
      <div class="flex flex-wrap gap-4 items-end">
        <div class="flex-grow max-w-xs">
          <label class="block text-sm font-medium text-black mb-1">Datum</label>
          <input
            v-model="newEvent.date"
            type="date"
            class="w-full rounded-md border-gray-300 bg-white text-gray-900 shadow-sm focus:border-primary focus:ring focus:ring-primary focus:ring-opacity-50"
            :class="{ 'border-red-500 focus:border-red-500 focus:ring-red-500': errors.date }"
          />
          <p v-if="errors.date" class="mt-1 text-sm text-red-600">
            {{ errors.date }}
          </p>
        </div>
        <div class="flex-grow">
          <label class="block text-sm font-medium text-black mb-1">Notiz</label>
          <input
            v-model="newEvent.note"
            type="text"
            placeholder="z.B. Geburtstagsfeier"
            class="w-full rounded-md border-gray-300 bg-white text-gray-900 shadow-sm focus:border-primary focus:ring focus:ring-primary focus:ring-opacity-50"
          />
        </div>
      </div>

      <!-- Dish Selection / Catalog -->
      <div class="border-t border-gray-100 dark:border-gray-700 pt-4">
        <div class="flex justify-between items-center mb-4">
          <label class="text-lg font-bold text-gray-900 dark:text-gray-100">Optionen</label>
          <button
            class="text-xs text-blue-600 hover:text-blue-800 dark:text-blue-400 dark:hover:text-blue-300 underline focus:outline-none"
            @click="showDeactivatedFirst = !showDeactivatedFirst"
          >
            {{ showDeactivatedFirst ? 'Aktive zuerst anzeigen' : 'Deaktivierte zuerst anzeigen' }}
          </button>
        </div>

        <!-- Add New Item -->
        <div class="flex gap-2 mb-4">
          <input
            v-model="newItemLabel"
            type="text"
            placeholder="Neue Option hinzufügen..."
            class="flex-grow rounded-md border-gray-300 bg-white text-gray-900 shadow-sm focus:border-primary focus:ring focus:ring-primary focus:ring-opacity-50"
            @keyup.enter="createCatalogItem"
          />
          <BaseButton variant="primary" :disabled="!newItemLabel.trim()" @click="createCatalogItem">
            Hinzufügen
          </BaseButton>
        </div>

        <div v-if="loadingCatalog" class="text-center py-4 text-gray-500 italic">
          Lade Optionen...
        </div>
        <div
          v-else
          class="space-y-2 max-h-72 overflow-y-auto pr-2 bg-gray-50 dark:bg-gray-900/50 p-2 rounded-lg border border-gray-100 dark:border-gray-800"
        >
          <div
            v-for="item in sortedCatalogItems"
            :key="item.id"
            class="flex items-center justify-between p-2 rounded bg-white dark:bg-gray-800 border border-gray-100 dark:border-gray-700 hover:shadow-sm transition-all group"
          >
            <template v-if="editingId !== item.id">
              <span
                class="text-gray-900 dark:text-gray-100 font-medium"
                :class="{ 'opacity-50 line-through': !item.active }"
              >
                {{ item.label }}
              </span>

              <div class="flex items-center gap-2">
                <button
                  class="text-xs px-3 py-1.5 rounded border transition-colors font-semibold"
                  :class="
                    item.active
                      ? 'text-red-600 border-red-200 hover:bg-red-50 dark:text-red-400 dark:border-red-900/50 dark:hover:bg-red-900/20'
                      : 'text-green-600 border-green-200 hover:bg-green-50 dark:text-green-400 dark:border-green-900/50 dark:hover:bg-green-900/20'
                  "
                  @click="toggleActive(item)"
                >
                  {{ item.active ? 'Deaktivieren' : 'Aktivieren' }}
                </button>

                <!-- Edit Button (Pencil) to the RIGHT -->
                <button
                  class="text-gray-400 hover:text-blue-600 p-1 rounded hover:bg-blue-50 dark:hover:bg-blue-900/30 transition-colors"
                  title="Bearbeiten"
                  @click="startEdit(item)"
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
                </button>
              </div>
            </template>

            <template v-else>
              <input
                ref="editInput"
                v-model="editLabel"
                type="text"
                class="flex-grow mr-2 rounded-md border-gray-300 shadow-sm focus:border-primary focus:ring focus:ring-primary focus:ring-opacity-50 text-sm py-1"
                @keyup.enter="saveEdit"
                @keyup.esc="cancelEdit"
              />
              <div class="flex gap-2">
                <button
                  class="text-green-600 hover:text-green-800"
                  title="Speichern"
                  @click="saveEdit"
                >
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    class="h-5 w-5"
                    viewBox="0 0 20 20"
                    fill="currentColor"
                  >
                    <path
                      fill-rule="evenodd"
                      d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                      clip-rule="evenodd"
                    />
                  </svg>
                </button>
                <button
                  class="text-red-600 hover:text-red-800"
                  title="Abbrechen"
                  @click="cancelEdit"
                >
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    class="h-5 w-5"
                    viewBox="0 0 20 20"
                    fill="currentColor"
                  >
                    <path
                      fill-rule="evenodd"
                      d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
                      clip-rule="evenodd"
                    />
                  </svg>
                </button>
              </div>
            </template>
          </div>

          <div
            v-if="catalogItems.length === 0"
            class="text-sm text-gray-500 italic text-center py-8"
          >
            Noch keine Optionen vorhanden.
          </div>
        </div>
      </div>

      <div class="flex justify-center pt-4 border-t border-gray-100 dark:border-gray-700">
        <BaseButton
          variant="primary"
          size="large"
          class="w-full sm:w-auto font-bold shadow-md hover:shadow-lg transform active:scale-95"
          :disabled="!newEvent.date || !!errors.date || loading"
          :loading="loading"
          @click="createEvent"
        >
          Lunch-Event anlegen
        </BaseButton>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * Form for creating new community lunch events.
 * Manages event details and initial catalog item selection.
 */
import { ref, onMounted, computed, nextTick, reactive, watch } from 'vue'
import { lunchService } from '@/services/lunchService'
import { isPastDate } from '@/utils/dateUtils'
import BaseButton from '@/components/common/BaseButton.vue'

const emit = defineEmits(['event-created'])

const loading = ref(false)
const loadingCatalog = ref(false)
const catalogItems = ref([])
const newItemLabel = ref('')
const showDeactivatedFirst = ref(false)

// Edit State
const editingId = ref(null)
const editLabel = ref('')
const editInput = ref(null)

const newEvent = ref({
  date: '',
  location: 'Büro',
  note: '',
})

const errors = reactive({
  date: '',
})

const validateForm = () => {
  let isValid = true
  if (newEvent.value.date && isPastDate(newEvent.value.date)) {
    errors.date = 'Lunch-Events können nicht in der Vergangenheit erstellt werden.'
    isValid = false
  } else {
    errors.date = ''
  }
  return isValid
}

watch(
  () => newEvent.value.date,
  () => {
    validateForm()
  }
)

/**
 * Starts editing a catalog item.
 */
const startEdit = (item) => {
  editingId.value = item.id
  editLabel.value = item.label
  nextTick(() => {
    const el = Array.isArray(editInput.value) ? editInput.value[0] : editInput.value
    el?.focus()
  })
}

/**
 * Cancels editing.
 */
const cancelEdit = () => {
  editingId.value = null
  editLabel.value = ''
}

/**
 * Saves the edited label.
 */
const saveEdit = async () => {
  if (!editLabel.value.trim()) return
  try {
    const updated = await lunchService.updateCatalogItem(editingId.value, editLabel.value)
    // Update local state
    const idx = catalogItems.value.findIndex((i) => i.id === editingId.value)
    if (idx !== -1) {
      catalogItems.value[idx] = updated
    }
    cancelEdit()
  } catch (e) {
    console.error(e)
    if (e.response && e.response.status === 409) {
      alert('Dieses Gericht existiert bereits.')
    } else {
      alert('Fehler beim Speichern.')
    }
  }
}

/**
 * Sorts catalog items based on active status and label.
 */
const sortedCatalogItems = computed(() => {
  return [...catalogItems.value].sort((a, b) => {
    const aActive = a.active ? 1 : 0
    const bActive = b.active ? 1 : 0

    if (showDeactivatedFirst.value) {
      return aActive - bActive || a.label.localeCompare(b.label)
    } else {
      return bActive - aActive || a.label.localeCompare(b.label)
    }
  })
})

/**
 * Loads all items from the food catalog.
 */
const loadCatalog = async () => {
  loadingCatalog.value = true
  try {
    catalogItems.value = await lunchService.getCatalogItems(false)
  } catch (e) {
    console.error('Failed to load catalog', e)
  } finally {
    loadingCatalog.value = false
  }
}

/**
 * Creates a new catalog item and adds it to the list.
 */
const createCatalogItem = async () => {
  if (!newItemLabel.value.trim()) return
  try {
    const created = await lunchService.createCatalogItem(newItemLabel.value)
    catalogItems.value.unshift(created)
    newItemLabel.value = ''
  } catch (e) {
    console.error(e)
    if (e.response && e.response.status === 409) {
      alert('Diese Option existiert bereits (aktiv oder inaktiv).')
    } else {
      alert('Fehler beim Erstellen der Option.')
    }
  }
}

/**
 * Toggles the active status of a catalog item.
 */
const toggleActive = async (item) => {
  const newState = !item.active
  item.active = newState
  try {
    await lunchService.updateCatalogItemActive(item.id, newState)
  } catch (e) {
    console.error(e)
    item.active = !newState
    alert('Fehler beim Aktualisieren des Status.')
  }
}

/**
 * Submits the form to create a new lunch event.
 */
const createEvent = async () => {
  if (!validateForm()) {
    return
  }

  loading.value = true
  try {
    const activeItemIds = catalogItems.value.filter((item) => item.active).map((item) => item.id)

    const payload = {
      ...newEvent.value,
      initialCatalogItemIds: activeItemIds,
    }
    await lunchService.createEvent(payload)

    newEvent.value = { date: '', note: '' }
    emit('event-created')
  } catch (e) {
    console.error(e)
    alert('Fehler beim Erstellen des Events')
  } finally {
    loading.value = false
  }
}

onMounted(loadCatalog)
</script>
