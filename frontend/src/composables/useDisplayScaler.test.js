import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useDisplayScaler } from './useDisplayScaler'

// Mock Vue lifecycle hooks
vi.mock('vue', async () => {
  const actual = await vi.importActual('vue')
  return {
    ...actual,
    onMounted: (fn) => { fn() },
    onUnmounted: () => {},
  }
})

describe('useDisplayScaler', () => {
  beforeEach(() => {
    // Reset window dimensions
    Object.defineProperty(window, 'innerWidth', { writable: true, configurable: true, value: 1920 })
    Object.defineProperty(window, 'innerHeight', { writable: true, configurable: true, value: 1080 })
  })

  it('calculates scale based on window dimensions', () => {
    const { scale } = useDisplayScaler()
    // 1920/3840 = 0.5, 1080/2160 = 0.5, min = 0.5
    expect(scale.value).toBeCloseTo(0.5)
  })

  it('uses the smaller ratio when aspect ratios differ', () => {
    Object.defineProperty(window, 'innerWidth', { writable: true, configurable: true, value: 3840 })
    Object.defineProperty(window, 'innerHeight', { writable: true, configurable: true, value: 1080 })
    const { scale } = useDisplayScaler()
    // 3840/3840 = 1.0, 1080/2160 = 0.5, min = 0.5
    expect(scale.value).toBeCloseTo(0.5)
  })

  it('returns scalerStyle with correct dimensions', () => {
    const { scalerStyle } = useDisplayScaler()
    expect(scalerStyle.value.width).toBe('3840px')
    expect(scalerStyle.value.height).toBe('2160px')
    expect(scalerStyle.value.transform).toContain('translate(-50%, -50%)')
    expect(scalerStyle.value.transform).toContain('scale(')
  })

  it('scale is 1 when window matches reference resolution', () => {
    Object.defineProperty(window, 'innerWidth', { writable: true, configurable: true, value: 3840 })
    Object.defineProperty(window, 'innerHeight', { writable: true, configurable: true, value: 2160 })
    const { scale } = useDisplayScaler()
    expect(scale.value).toBe(1)
  })
})
