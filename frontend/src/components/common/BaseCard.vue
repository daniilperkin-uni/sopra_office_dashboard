<template>
  <div class="bg-white shadow-md rounded-lg overflow-hidden">
    <div v-if="title" class="bg-primary text-white px-4 sm:px-6 py-3 sm:py-4">
      <h3 class="text-base sm:text-lg font-semibold">
        {{ title }}
      </h3>
    </div>
    <div class="p-4 sm:p-6">
      <slot />
    </div>
  </div>
</template>

<script setup>
/**
 * A reusable card component that provides a consistent visual container for content.
 * It can optionally display a title in a distinct header section.
 */
defineProps({
  /**
   * The title to display in the card's header. If not provided, no header will be rendered.
   */
  title: {
    type: String,
    default: '',
  },
})
</script>
