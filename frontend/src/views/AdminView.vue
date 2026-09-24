<template>
  <div class="admin-view bg-neutral-bg min-h-screen">
    <div class="bg-gray-100 py-6 border-b border-gray-200">
      <h1 class="text-4xl font-black text-primary tracking-wider text-center">Admin Bereich</h1>
    </div>

    <nav class="border-b border-gray-200">
      <ul class="flex justify-center space-x-8 px-4">
        <li>
          <router-link
            to="/admin/parking"
            class="py-4 px-1 border-b-2 font-medium text-lg whitespace-nowrap"
            :class="[
              $route.path === '/admin/parking'
                ? 'border-primary text-primary dark:text-blue-400'
                : 'border-transparent text-gray-500 hover:text-primary hover:border-primary dark:text-gray-400 dark:hover:text-blue-400',
            ]"
          >
            Parken
          </router-link>
        </li>
        <li>
          <router-link
            to="/admin/highscores"
            class="py-4 px-1 border-b-2 font-medium text-base sm:text-lg whitespace-nowrap"
            :class="[
              $route.path === '/admin/highscores'
                ? 'border-primary text-primary dark:text-blue-400'
                : 'border-transparent text-gray-500 hover:text-primary hover:border-primary dark:text-gray-400 dark:hover:text-blue-400',
            ]"
          >
            Highscores
          </router-link>
        </li>
        <li>
          <router-link
            to="/admin/lunch"
            class="py-4 px-1 border-b-2 font-medium text-base sm:text-lg whitespace-nowrap"
            :class="[
              $route.path === '/admin/lunch'
                ? 'border-primary text-primary dark:text-blue-400'
                : 'border-transparent text-gray-500 hover:text-primary hover:border-primary dark:text-gray-400 dark:hover:text-blue-400',
            ]"
          >
            Lunch
          </router-link>
        </li>
        <li>
          <router-link
            to="/admin/config"
            class="py-4 px-1 border-b-2 font-medium text-base sm:text-lg whitespace-nowrap"
            :class="[
              $route.path === '/admin/config'
                ? 'border-primary text-primary dark:text-blue-400'
                : 'border-transparent text-gray-500 hover:text-primary hover:border-primary dark:text-gray-400 dark:hover:text-blue-400',
            ]"
          >
            Konfiguration
          </router-link>
        </li>
      </ul>
    </nav>

    <router-view />
  </div>
</template>

<script setup>
/**
 * Main view for the administration area.
 * Provides navigation between Parking Management,
 * Highscore Management, and Display Configuration.
 */
// No specific script logic required as it is a wrapper for router-view
</script>

<style scoped>
.admin-view {
  padding: 1rem;
}
</style>
