import { describe, it, expect, beforeEach } from 'vitest'
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
})
