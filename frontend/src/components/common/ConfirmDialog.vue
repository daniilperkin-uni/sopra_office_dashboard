<template>
  <Teleport to="body">
    <div
      v-if="isOpen"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
      @click.self="handleCancel"
    >
      <div class="bg-white rounded-lg shadow-xl max-w-md w-full p-6">
        <h3 class="text-lg font-bold text-gray-900 mb-2">
          {{ title }}
        </h3>
        <p class="text-gray-600 mb-6">
          {{ message }}
        </p>
        <div class="flex justify-end gap-3">
          <BaseButton variant="secondary" @click="handleCancel">
            {{ cancelText }}
          </BaseButton>
          <BaseButton :variant="danger ? 'danger' : 'primary'" @click="handleConfirm">
            {{ confirmText }}
          </BaseButton>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
/**
 * Reusable confirmation dialog for destructive actions.
 * Prevents accidental data loss by requiring explicit confirmation.
 */
import BaseButton from './BaseButton.vue'

defineProps({
  isOpen: {
    type: Boolean,
    default: false,
  },
  title: {
    type: String,
    default: 'Bitte bestätigen',
  },
  message: {
    type: String,
    default: 'Sind Sie sicher, dass Sie diese Aktion ausführen möchten?',
  },
  confirmText: {
    type: String,
    default: 'Bestätigen',
  },
  cancelText: {
    type: String,
    default: 'Abbrechen',
  },
  danger: {
    type: Boolean,
    default: true,
  },
})

const emit = defineEmits(['confirm', 'cancel'])

const handleConfirm = () => emit('confirm')
const handleCancel = () => emit('cancel')
</script>
