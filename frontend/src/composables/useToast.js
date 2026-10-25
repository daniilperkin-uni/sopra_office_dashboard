import { reactive } from 'vue'

/**
 * Kleiner globaler Toast-Store.
 *
 * Vorher haben die Admin-Komponenten native alert()-Dialoge benutzt: die sind
 * blockierend, sehen wie ein Browser-Dialog aus und lassen sich nicht
 * gestalten. Der Store hält die Meldungen modulweit, damit jede Komponente
 * toasts auslösen kann, ohne Props durchzureichen.
 *
 * Verwendung:
 *   const toast = useToast()
 *   toast.error('Fehler beim Speichern.')
 *   toast.success('Serie gespeichert.')
 */
const state = reactive({ items: [] })
let seq = 0

const DEFAULT_TIMEOUT = 5000
const ERROR_TIMEOUT = 8000

function push(message, type, timeout) {
  if (message === undefined || message === null || message === '') return null
  const id = ++seq
  state.items.push({ id, message: String(message), type })
  if (timeout > 0) {
    setTimeout(() => dismiss(id), timeout)
  }
  return id
}

function dismiss(id) {
  const index = state.items.findIndex((item) => item.id === id)
  if (index !== -1) state.items.splice(index, 1)
}

export function useToast() {
  return {
    toasts: state.items,
    info: (message, timeout = DEFAULT_TIMEOUT) => push(message, 'info', timeout),
    success: (message, timeout = DEFAULT_TIMEOUT) => push(message, 'success', timeout),
    // Fehler bleiben länger stehen: sie enthalten oft den Grund aus der API.
    error: (message, timeout = ERROR_TIMEOUT) => push(message, 'error', timeout),
    dismiss,
  }
}
