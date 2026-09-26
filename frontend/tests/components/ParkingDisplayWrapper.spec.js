import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ParkingDisplayWrapper from '@/features/parking/components/ParkingDisplayWrapper.vue'
import BarDisplay from '@/features/dashboard/components/BarDisplay.vue'

/**
 * Regression guard: the rotating /display parking view hardcoded a capacity of
 * five spots, ignoring the per-day capacity the API reports
 * (ParkingEntriesForDayResponse.totalSpots). The dashboard widget already used
 * the reported value.
 */
function mountWrapper(weekData) {
  return mount(ParkingDisplayWrapper, {
    props: {
      weekData,
      loading: false,
      error: null,
      refreshData: () => {},
    },
  })
}

describe('ParkingDisplayWrapper', () => {
  it('renders each day with the capacity reported by the API', () => {
    const wrapper = mountWrapper([
      { date: '2026-01-02', entries: [], totalSpots: 8 },
      { date: '2026-01-05', entries: [], totalSpots: 3 },
    ])

    const bars = wrapper.findAllComponents(BarDisplay)
    expect(bars).toHaveLength(2)
    expect(bars[0].props('maxSpots')).toBe(8)
    expect(bars[1].props('maxSpots')).toBe(3)
  })

  it('falls back to five spots when the API does not report a capacity', () => {
    const wrapper = mountWrapper([{ date: '2026-01-02', entries: [] }])

    expect(wrapper.findComponent(BarDisplay).props('maxSpots')).toBe(5)
  })
})
