<template>
  <div
    id="app"
    class="min-h-screen flex flex-col bg-neutral-bg"
  >
    <!-- Header with navigation (only visible when not in display mode) -->
    <header
      v-if="showNavigation"
      class="bg-gray-100 shadow-md"
    >
      <div class="container mx-auto px-4 py-3 flex flex-col sm:flex-row items-center">
        <!-- Left spacer to balance the nav on the right -->
        <div class="hidden sm:block flex-1" />

        <h1 class="text-3xl sm:text-7xl font-black text-primary text-center">
          itestra Dashboard
        </h1>

        <nav class="flex-1 flex justify-center sm:justify-end space-x-2 sm:space-x-4 mt-4 sm:mt-0">
          <router-link
            to="/display"
            class="px-3 py-2 rounded-md text-sm font-medium text-gray-500 hover:text-primary transition-colors"
            :class="{ '!text-primary': $route.path === '/display' }"
          >
            Anzeige
          </router-link>
          <router-link
            to="/admin/parking"
            class="px-3 py-2 rounded-md text-sm font-medium text-gray-500 hover:text-primary transition-colors"
            :class="{ '!text-primary': $route.path.startsWith('/admin') }"
          >
            Verwaltung
          </router-link>
          <button
            v-if="showNavigation && $route.path.startsWith('/admin')"
            class="px-3 py-2 rounded-md text-sm font-medium text-gray-500 hover:text-red-500 transition-colors"
            @click="handleLogout"
          >
            Abmelden
          </button>
        </nav>
      </div>
    </header>

    <!-- Main Content -->
    <main
      class="flex-grow flex flex-col"
      :class="{'container mx-auto px-4 py-8': showNavigation}"
    >
      <router-view />
    </main>
  </div>
</template>

<script setup>
/**
 * Main application component.
 * Defines the basic layout including header and router view.
 * Hides navigation in display mode (/display).
 */
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { authService } from '@/services/authService'

const route = useRoute()
const router = useRouter()

// Show navigation only if we are not on the display page
const showNavigation = computed(() => {
  return route.path !== '/display'
})

const handleLogout = async () => {
  await authService.logout()
  router.push('/display')
}
</script>

<style>
/* Global color definitions (Fallback/Supplement to Tailwind) */
.bg-primary {
  background-color: #009ee2;
}

/* Hover status for Primary (slightly darker) */
.bg-primary-dark {
  background-color: #008bc7;
}
</style>