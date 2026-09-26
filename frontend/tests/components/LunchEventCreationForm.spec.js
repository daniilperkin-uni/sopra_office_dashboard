import { describe, it, expect, vi, beforeEach } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'

const mocks = vi.hoisted(() => ({
  createEvent: vi.fn(),
  getCatalogItems: vi.fn(),
  updateCatalogItem: vi.fn(),
  createCatalogItem: vi.fn(),
  updateCatalogItemActive: vi.fn(),
}))

vi.mock('@/services/lunchService', () => ({
  lunchService: {
    createEvent: mocks.createEvent,
    getCatalogItems: mocks.getCatalogItems,
    updateCatalogItem: mocks.updateCatalogItem,
    createCatalogItem: mocks.createCatalogItem,
    updateCatalogItemActive: mocks.updateCatalogItemActive,
  },
}))

import LunchEventCreationForm from '@/features/community-lunch/admin/LunchEventCreationForm.vue'

/**
 * Regression guard: the form reset after a successful submit replaced the whole
 * event model with { date, note }, dropping the location. Every event created
 * after the first was therefore POSTed without a location, while the first one
 * still carried the default 'Büro'.
 */
function submitButton(wrapper) {
  return wrapper.findAll('button').find((button) => button.text().includes('Lunch-Event anlegen'))
}

async function submitWithDate(wrapper, date) {
  await wrapper.find('input[type="date"]').setValue(date)
  await submitButton(wrapper).trigger('click')
  await flushPromises()
}

describe('LunchEventCreationForm', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mocks.getCatalogItems.mockResolvedValue([])
    mocks.createEvent.mockResolvedValue({ id: 1 })
  })

  it('keeps sending the location for every event, not only the first', async () => {
    const wrapper = mount(LunchEventCreationForm)
    await flushPromises()

    await submitWithDate(wrapper, '2099-01-02')
    await submitWithDate(wrapper, '2099-01-03')

    expect(mocks.createEvent).toHaveBeenCalledTimes(2)
    expect(mocks.createEvent.mock.calls[0][0]).toMatchObject({
      date: '2099-01-02',
      location: 'Büro',
    })
    expect(mocks.createEvent.mock.calls[1][0]).toMatchObject({
      date: '2099-01-03',
      location: 'Büro',
    })
  })

  it('clears the date and note after a successful submit', async () => {
    const wrapper = mount(LunchEventCreationForm)
    await flushPromises()

    await submitWithDate(wrapper, '2099-01-02')

    expect(wrapper.find('input[type="date"]').element.value).toBe('')
  })
})
