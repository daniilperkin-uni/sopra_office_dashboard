<template>
  <div class="mb-4">
    <label
      v-if="label"
      :for="id"
      class="block text-sm font-medium text-gray-700 mb-1"
    >
      {{ label }}
    </label>
    <input
      :id="id"
      type="date"
      :value="modelValue"
      :min="minDate"
      :max="maxDate"
      :disabled="disabled"
      class="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-primary focus:border-primary sm:text-sm"
      @input="updateValue"
    >
    <p
      v-if="error"
      class="mt-1 text-sm text-red-600"
    >
      {{ error }}
    </p>
  </div>
</template>

<script setup>
/**
 * A reusable date input component that provides a standardized date picker.
 * It supports binding with `v-model`, setting minimum and maximum selectable dates,
 * disabling the input, and displaying validation errors.
 */
defineProps({
  /**
   * The unique identifier for the input element. Required for accessibility.
   */
  id: {
    type: String,
    required: true,
  },
  /**
   * The label text displayed above the input field.
   */
  label: {
    type: String,
    default: '',
  },
  /**
   * The current value of the date input (ISO 8601 format: YYYY-MM-DD).
   * Used with `v-model`.
   */
  modelValue: {
    type: String,
    default: '',
  },
  /**
   * The minimum selectable date (ISO 8601 format). Dates before this will be disabled.
   */
  minDate: {
    type: String,
    default: '',
  },
  /**
   * The maximum selectable date (ISO 8601 format). Dates after this will be disabled.
   */
  maxDate: {
    type: String,
    default: '',
  },
  /**
   * If true, the date input will be disabled and uneditable.
   */
  disabled: {
    type: Boolean,
    default: false,
  },
  /**
   * An error message to display below the input field.
   */
  error: {
    type: String,
    default: '',
  },
})

const emit = defineEmits(['update:modelValue'])

/**
 * Emits the input value to support `v-model` binding.
 * @param {Event} event - The native DOM input event.
 */
const updateValue = (event) => {
  emit('update:modelValue', event.target.value)
}
</script>
