<template>
  <button
    :type="type"
    :disabled="disabled || loading"
    :class="buttonClasses"
    @click="$emit('click', $event)"
  >
    <span v-if="loading" class="inline-flex items-center">
      <svg
        class="animate-spin -ml-1 mr-2 h-4 w-4 text-white"
        xmlns="http://www.w3.org/2000/svg"
        fill="none"
        viewBox="0 0 24 24"
      >
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
        <path
          class="opacity-75"
          fill="currentColor"
          d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
        />
      </svg>
      Processing...
    </span>
    <span v-else>
      <slot />
    </span>
  </button>
</template>

<script setup>
/**
 * Wiederverwendbare Button-Komponente, die verschiedene Varianten, Größen und Zustände unterstützt.
 * Zeigt optional einen Lade-Spinner an und deaktiviert sich selbst, um Mehrfach-Klicks zu verhindern.
 */
import { computed } from 'vue'

const props = defineProps({
  type: {
    type: String,
    default: 'button',
  },
  // Der 'validator' stellt sicher, dass Entwickler keine ungültigen Varianten übergeben (Fail-Fast).
  variant: {
    type: String,
    default: 'primary',
    validator: (value) => ['primary', 'secondary', 'danger', 'success', 'black'].includes(value),
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large'].includes(value),
  },
})

defineEmits(['click'])

/**
 * Berechnet die CSS-Klassen dynamisch basierend auf den übergebenen Props.
 * Dies ist das Herzstück der Komponente: Es übersetzt abstrakte Konzepte ("primary", "large")
 * in konkrete Tailwind-CSS-Klassen.
 */
const buttonClasses = computed(() => {
  // 1. Basis-Styling: Gilt für ALLE Buttons (rundung, transition, flexbox für zentrierung)
  const baseClasses =
    'inline-flex items-center justify-center font-medium rounded-md transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2'

  // 2. Varianten-Logik: Definiert Farben für Hintergrund, Text und Focus-Ringe
  const variantClasses = {
    primary: 'bg-primary text-white hover:bg-primary-dark focus:ring-primary',
    secondary: 'bg-gray-200 text-gray-800 hover:bg-gray-300 focus:ring-gray-500',
    danger: 'bg-red-600 text-white hover:bg-red-700 focus:ring-red-500', // Für destruktive Aktionen (Löschen)
    success: 'bg-green-600 text-white hover:bg-green-700 focus:ring-green-500',
    black: 'bg-black text-white hover:bg-gray-900 focus:ring-black',
  }

  // 3. Größen-Logik: Steuert Padding und Schriftgröße
  const sizeClasses = {
    small: 'px-3 py-1.5 text-sm',
    medium: 'px-4 py-2 text-base', // Standard
    large: 'px-6 py-3 text-lg', // Für Call-to-Actions
  }

  // 4. Zustand: Wenn disabled oder am laden, wird der Button halbtransparent und nicht klickbar
  const disabledClasses = props.disabled || props.loading ? 'opacity-50 cursor-not-allowed' : ''

  // Zusammenbau des finalen Strings: Wir nehmen die Basis, fügen die spezifische Variante/Größe hinzu
  // und ergänzen ggf. die Disabled-Klassen.
  return [
    baseClasses,
    variantClasses[props.variant],
    sizeClasses[props.size],
    disabledClasses,
  ].join(' ')
})
</script>
