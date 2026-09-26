import { defineStore } from 'pinia'
import { ref } from 'vue'
import { configApi } from '@/services/api'

/**
 * Store for managing the dashboard display configuration.
 * Owns the config state and polling logic so DisplayView and DisplayConfigAdmin
 * can share it without prop drilling or duplicate fetches.
 */
export const useDisplayConfigStore = defineStore('displayConfig', () => {
  const config = ref({
    rotationEnabled: false,
    skipOneDisplayInRotation: false,
    rotationIntervalSeconds: 15,
    defaultSingleViewId: 'dashboard',
  })
  const loading = ref(false)
  const loaded = ref(false)
  const error = ref(null)

  async function fetchConfig() {
    loading.value = true
    try {
      const remoteConfig = await configApi.getConfig()
      if (remoteConfig) {
        config.value = { ...config.value, ...remoteConfig }
      }
      loaded.value = true
      error.value = null
    } catch (e) {
      console.error('Failed to load config', e)
      // The admin view renders an error banner from this ref; without it a
      // failed load was indistinguishable from a successful one.
      error.value = 'Konfiguration konnte nicht geladen werden.'
    } finally {
      loading.value = false
    }
  }

  async function updateConfig(newConfig) {
    loading.value = true
    try {
      const updated = await configApi.updateConfig(newConfig)
      config.value = { ...config.value, ...updated }
      return updated
    } catch (e) {
      console.error('Failed to update config', e)
      throw e
    } finally {
      loading.value = false
    }
  }

  return { config, loading, loaded, error, fetchConfig, updateConfig }
})
