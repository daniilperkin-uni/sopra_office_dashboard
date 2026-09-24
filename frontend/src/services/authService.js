import apiClient from './api'

/**
 * Service for authentication: login, logout, and checking the current user.
 * Stores the auth state in a module-level ref so components can react to it.
 */
import { ref, computed } from 'vue'

const authChecked = ref(false)
const currentUser = ref(null)

export const authService = {
  isAuthenticated: computed(() => currentUser.value !== null),
  authChecked: computed(() => authChecked.value),

  async login(username, password) {
    // CSRF-Cookie vorab holen, damit der Login-POST den X-XSRF-TOKEN-Header mitsendet
    await apiClient.get('/auth/csrf')
    const response = await apiClient.post('/auth/login', { username, password })
    currentUser.value = response.data
    return response.data
  },

  async logout() {
    await apiClient.post('/auth/logout')
    currentUser.value = null
  },

  async checkAuth() {
    try {
      const response = await apiClient.get('/auth/me')
      currentUser.value = response.data
    } catch {
      currentUser.value = null
    } finally {
      authChecked.value = true
    }
    return currentUser.value
  },
}
