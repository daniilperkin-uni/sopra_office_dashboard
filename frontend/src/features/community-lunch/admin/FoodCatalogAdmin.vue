<template>
  <div
    class="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-4"
  >
    <h3 class="text-lg font-bold text-gray-900 dark:text-gray-100 mb-4">
      Optionen
    </h3>

    <div class="flex gap-2 mb-4">
      <input
        v-model="newItemLabel"
        type="text"
        placeholder="Neues Gericht..."
        class="flex-grow rounded-md border-gray-300 bg-white text-gray-900 shadow-sm focus:border-primary focus:ring focus:ring-primary focus:ring-opacity-50"
        @keyup.enter="createItem"
      >
      <BaseButton
        variant="primary"
        :disabled="!newItemLabel.trim()"
        @click="createItem"
      >
        Hinzufügen
      </BaseButton>
    </div>

    <div
      v-if="loading"
      class="text-center py-4"
    >
      Lade...
    </div>

    <div
      v-else
      class="space-y-2 max-h-60 overflow-y-auto pr-2"
    >
      <div
        v-for="item in items"
        :key="item.id"
        class="flex items-center justify-between p-2 rounded hover:bg-blue-50 dark:hover:bg-blue-900/20 group transition-colors"
      >
        <!-- Read Mode -->
        <template v-if="editingId !== item.id">
          <span :class="{ 'opacity-50 line-through': !item.active }">{{ item.label }}</span>
          <div class="flex gap-2">
            <!-- Edit Button (Pencil) -->
            <button
              class="text-gray-500 hover:text-blue-600 p-1 rounded hover:bg-blue-100 dark:hover:bg-blue-900/50 transition-colors"
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
            <!-- Toggle Button -->
            <button
              class="text-xs px-2 py-1 rounded border transition-colors"
              :class="
                item.active
                  ? 'text-red-600 border-red-200 hover:bg-red-50 dark:border-red-800 dark:hover:bg-red-900/30'
                  : 'text-green-600 border-green-200 hover:bg-green-50 dark:border-green-800 dark:hover:bg-green-900/30'
              "
              @click="toggleActive(item)"
            >
              {{ item.active ? 'Deaktivieren' : 'Aktivieren' }}
            </button>
          </div>
        </template>

        <!-- Edit Mode -->
        <template v-else>
          <input
            ref="editInput"
            v-model="editLabel"
            type="text"
            class="flex-grow mr-2 rounded-md border-gray-300 shadow-sm focus:border-primary focus:ring focus:ring-primary focus:ring-opacity-50 text-sm py-1"
            @keyup.enter="saveEdit"
            @keyup.esc="cancelEdit"
          >
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
    </div>
  </div>
</template>

<script setup>
/**
 * Administration component for the food catalog.
 * Allows adding new items and toggling their active status globally.
 */
import { ref, onMounted, nextTick } from 'vue'
import { lunchService } from '@/services/lunchService'
import BaseButton from '@/components/common/BaseButton.vue'

const items = ref([])
const loading = ref(false)
const newItemLabel = ref('')

// Edit State
const editingId = ref(null)
const editLabel = ref('')
const editInput = ref(null)

/**
 * Loads all items from the food catalog.
 */
const loadItems = async () => {
  loading.value = true
  items.value = await lunchService.getCatalogItems(false)
  loading.value = false
}

/**
 * Creates a new catalog item.
 */
const createItem = async () => {
  if (!newItemLabel.value.trim()) return
  try {
    const created = await lunchService.createCatalogItem(newItemLabel.value)
    items.value.unshift(created)
    newItemLabel.value = ''
  } catch (e) {
    console.error(e)
    if (e.response && e.response.data && e.response.data.message) {
      alert(e.response.data.message)
    } else {
      alert('Ein Fehler ist aufgetreten.')
    }
  }
}

/**
 * Starts editing an item.
 */
const startEdit = (item) => {
  editingId.value = item.id
  editLabel.value = item.label
  nextTick(() => {
    if (editInput.value) {
      // If it's an array (v-for ref), take the first one or find the correct one
      // In Vue 3 setup with v-for, ref might be an array
      const inputEl = Array.isArray(editInput.value) ? editInput.value[0] : editInput.value
      inputEl?.focus()
    }
  })
}

/**
 * Cancels the current edit operation.
 */
const cancelEdit = () => {
  editingId.value = null
  editLabel.value = ''
}

/**
 * Saves the edited item.
 */
const saveEdit = async () => {
  if (!editLabel.value.trim()) return

  try {
    const updated = await lunchService.updateCatalogItem(editingId.value, editLabel.value)

    // Update local list
    const index = items.value.findIndex((i) => i.id === editingId.value)
    if (index !== -1) {
      items.value[index] = updated
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
 * Toggles active status of a catalog item.
 */
const toggleActive = async (item) => {
  const newState = !item.active
  try {
    await lunchService.updateCatalogItemActive(item.id, newState)
    item.active = newState
  } catch (e) {
    console.error(e)
  }
}

onMounted(loadItems)
</script>
