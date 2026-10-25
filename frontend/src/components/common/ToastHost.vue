<template>
  <Teleport to="body">
    <div class="toast-host" role="status" aria-live="polite">
      <TransitionGroup name="toast">
        <div
          v-for="toast in toasts"
          :key="toast.id"
          class="toast"
          :class="`toast--${toast.type}`"
          :data-testid="`toast-${toast.type}`"
        >
          <span class="toast__message">{{ toast.message }}</span>
          <button
            class="toast__close"
            type="button"
            aria-label="Meldung schließen"
            @click="dismiss(toast.id)"
          >
            &times;
          </button>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<script setup>
/**
 * Rendert die Meldungen aus useToast() fest oben rechts.
 * aria-live="polite" sorgt dafür, dass Screenreader neue Meldungen vorlesen,
 * ohne die aktuelle Aktion zu unterbrechen.
 */
import { useToast } from '@/composables/useToast'

const { toasts, dismiss } = useToast()
</script>

<style scoped>
.toast-host {
  position: fixed;
  top: 1rem;
  right: 1rem;
  z-index: 100;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  max-width: min(26rem, calc(100vw - 2rem));
  pointer-events: none;
}

.toast {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 0.75rem 0.875rem;
  border-radius: 0.5rem;
  border-left: 4px solid #009ee2;
  background: #ffffff;
  color: #1f2937;
  box-shadow: 0 10px 25px rgba(15, 23, 42, 0.18);
  font-size: 0.875rem;
  line-height: 1.35;
  pointer-events: auto;
}

.toast--success {
  border-left-color: #15803d;
}

.toast--error {
  border-left-color: #b91c1c;
}

.toast__message {
  flex: 1;
  white-space: pre-line;
}

.toast__close {
  flex: none;
  border: 0;
  background: transparent;
  color: #6b7280;
  font-size: 1.1rem;
  line-height: 1;
  cursor: pointer;
  padding: 0 0.125rem;
}

.toast__close:hover {
  color: #111827;
}

.toast-enter-active,
.toast-leave-active {
  transition:
    opacity 180ms ease,
    transform 180ms ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateX(0.75rem);
}

@media (prefers-reduced-motion: reduce) {
  .toast-enter-active,
  .toast-leave-active {
    transition: none;
  }
}
</style>
