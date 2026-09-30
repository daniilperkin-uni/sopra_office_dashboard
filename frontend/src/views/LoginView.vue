<template>
  <div class="min-h-screen flex items-center justify-center bg-neutral-bg px-4">
    <div class="max-w-md w-full bg-white rounded-2xl shadow-lg p-8">
      <h1 class="text-3xl font-black text-primary text-center mb-2">Office Dashboard</h1>
      <p class="text-center text-gray-500 mb-8">Admin-Login</p>

      <form class="space-y-4" @submit.prevent="handleSubmit">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1"> Benutzername </label>
          <input
            v-model="username"
            type="text"
            autocomplete="username"
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-primary text-gray-900 bg-white"
            :disabled="loading"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1"> Passwort </label>
          <input
            v-model="password"
            type="password"
            autocomplete="current-password"
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-primary text-gray-900 bg-white"
            :disabled="loading"
          />
        </div>

        <div
          v-if="error"
          class="bg-red-100 border border-red-400 text-red-700 px-4 py-2 rounded text-sm"
        >
          {{ error }}
        </div>

        <button
          type="submit"
          :disabled="loading || !username || !password"
          class="w-full py-3 bg-primary text-white font-semibold rounded-lg hover:bg-primary-dark transition-colors disabled:opacity-50"
        >
          {{ loading ? 'Anmeldung...' : 'Anmelden' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { authService } from '@/services/authService'

const router = useRouter()
const route = useRoute()

const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

const handleSubmit = async () => {
  loading.value = true
  error.value = ''
  try {
    await authService.login(username.value, password.value)
    const redirect = route.query.redirect || '/admin/parking'
    router.push(redirect)
  } catch (err) {
    if (err.response && err.response.status === 401) {
      error.value = 'Benutzername oder Passwort ist falsch.'
    } else {
      error.value = 'Anmeldung fehlgeschlagen. Bitte später erneut versuchen.'
    }
  } finally {
    loading.value = false
  }
}
</script>
