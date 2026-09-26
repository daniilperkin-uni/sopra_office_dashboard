import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { computed, defineComponent, h, nextTick, reactive, ref } from 'vue'
import { mount } from '@vue/test-utils'
import { useViewRotation } from '@/composables/useViewRotation'

/**
 * Mounts a minimal harness component so the composable runs inside a real
 * component instance: it registers watchers and an onUnmounted hook, which Vue
 * only allows during component setup.
 * The harness calls setupRotation() once, exactly like DisplayView does on
 * mount, and renders the current view id so it can be asserted from the DOM.
 */
function mountRotation(configValues, viewIds) {
  const config = reactive({ ...configValues })
  const currentViewId = ref(viewIds[0])

  const Harness = defineComponent({
    setup() {
      const { setupRotation } = useViewRotation({
        config: computed(() => config),
        currentViewId,
        onRotate: () => {},
        viewIds,
      })
      setupRotation()
      return () => h('div', currentViewId.value)
    },
  })

  const wrapper = mount(Harness)
  return {
    wrapper,
    config,
    currentViewId,
    current: () => wrapper.text(),
  }
}

/** Advances the fake clock and lets Vue flush the resulting DOM update. */
async function tick(milliseconds) {
  vi.advanceTimersByTime(milliseconds)
  await nextTick()
}

const DEFAULT_CONFIG = {
  rotationEnabled: true,
  rotationIntervalSeconds: 15,
  skipOneDisplayInRotation: false,
  defaultSingleViewId: 'calendar',
}

describe('useViewRotation', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('rotates to the next view once the interval elapses', async () => {
    const { current } = mountRotation(DEFAULT_CONFIG, ['calendar', 'parking'])

    expect(current()).toBe('calendar')
    await tick(15000)
    expect(current()).toBe('parking')
  })

  it('applies a changed rotation interval without a page reload', async () => {
    const { config, current } = mountRotation(DEFAULT_CONFIG, ['calendar', 'parking'])

    config.rotationIntervalSeconds = 30
    await nextTick()

    await tick(15000)
    expect(current()).toBe('calendar')
    await tick(15000)
    expect(current()).toBe('parking')
  })

  it('starts rotating after rotation is enabled without a page reload', async () => {
    const { config, current } = mountRotation({ ...DEFAULT_CONFIG, rotationEnabled: false }, [
      'calendar',
      'parking',
    ])

    config.rotationEnabled = true
    await nextTick()

    await tick(15000)
    expect(current()).toBe('parking')
  })

  it('falls back to the default view after rotation is disabled without a page reload', async () => {
    const { config, current } = mountRotation(DEFAULT_CONFIG, ['calendar', 'parking'])

    await tick(15000)
    expect(current()).toBe('parking')

    config.rotationEnabled = false
    await nextTick()
    expect(current()).toBe('calendar')
  })

  it('applies a changed default view while rotation is disabled', async () => {
    const { config, current } = mountRotation({ ...DEFAULT_CONFIG, rotationEnabled: false }, [
      'calendar',
      'parking',
    ])

    config.defaultSingleViewId = 'parking'
    await nextTick()
    expect(current()).toBe('parking')
  })

  it('switches to game mode when the -1 interval is configured without a page reload', async () => {
    const { config, current } = mountRotation(DEFAULT_CONFIG, ['calendar', 'parking'])

    config.rotationIntervalSeconds = -1
    await nextTick()
    expect(current()).toBe('game')
  })

  it('clamps an unsafe interval below five seconds', async () => {
    const { current } = mountRotation({ ...DEFAULT_CONFIG, rotationIntervalSeconds: 1 }, [
      'calendar',
      'parking',
    ])

    await tick(1000)
    expect(current()).toBe('calendar')
    await tick(4000)
    expect(current()).toBe('parking')
  })
})
