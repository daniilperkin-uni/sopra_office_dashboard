import { describe, it, expect, beforeEach, vi } from 'vitest'

const configApiMock = vi.hoisted(() => ({
  getConfig: vi.fn(),
  updateConfig: vi.fn(),
}))

vi.mock('@/services/api', () => ({ configApi: configApiMock }))

import { useDisplayConfigStore } from '@/stores/useDisplayConfigStore'
import { createPinia, setActivePinia } from 'pinia'

describe('useDisplayConfigStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('has default config', () => {
    const store = useDisplayConfigStore()
    expect(store.config.rotationEnabled).toBe(false)
    expect(store.config.defaultSingleViewId).toBe('dashboard')
    expect(store.config.rotationIntervalSeconds).toBe(15)
  })

  it('has loading ref initialized to false', () => {
    const store = useDisplayConfigStore()
    expect(store.loading).toBe(false)
  })

  it('marks the config as loaded and clears the error after a successful fetch', async () => {
    configApiMock.getConfig.mockResolvedValue({ rotationEnabled: true })
    const store = useDisplayConfigStore()

    await store.fetchConfig()

    expect(store.loaded).toBe(true)
    expect(store.error).toBe(null)
    expect(store.config.rotationEnabled).toBe(true)
  })

  it('exposes an error message when the config cannot be loaded', async () => {
    configApiMock.getConfig.mockRejectedValue(new Error('backend down'))
    const store = useDisplayConfigStore()

    await store.fetchConfig()

    expect(store.loaded).toBe(false)
    expect(store.error).toBe('Konfiguration konnte nicht geladen werden.')
  })
})
