import { describe, it, expect, vi, beforeEach } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'

const mocks = vi.hoisted(() => ({
  getUpcomingLunches: vi.fn(),
  getResults: vi.fn(),
  getEvent: vi.fn(),
  getMyChoices: vi.fn(),
  voteForOption: vi.fn(),
  updateStatus: vi.fn(),
  deleteEvent: vi.fn(),
}))

vi.mock('@/services/lunchService', () => ({ lunchService: mocks }))

import LunchEventAdmin from '@/features/community-lunch/admin/LunchEventAdmin.vue'

/**
 * Regression guard: neue Lunch-Events werden als DRAFT angelegt, und das
 * Backend laesst Stimmen nur bei OPEN zu. Die Oberflaeche hatte keinen
 * Ausloeser fuer den Statuswechsel, also war die Abstimmung fuer jedes hier
 * erzeugte Event unerreichbar.
 */
function upcomingDraft() {
  return {
    id: 7,
    date: '2099-01-02',
    location: 'Büro',
    note: 'Testessen',
    status: 'DRAFT',
    options: [{ id: 11, label: 'Pizza' }],
  }
}

async function mountAdmin() {
  const wrapper = mount(LunchEventAdmin)
  await flushPromises()
  return wrapper
}

describe('LunchEventAdmin', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
    mocks.getUpcomingLunches.mockResolvedValue([upcomingDraft()])
    mocks.getResults.mockResolvedValue({ results: [] })
    mocks.getMyChoices.mockResolvedValue([])
  })

  it('offers a control that opens a draft event for voting', async () => {
    mocks.updateStatus.mockResolvedValue({ id: 7, status: 'OPEN' })

    const wrapper = await mountAdmin()

    expect(wrapper.get('[data-testid="lunch-status"]').text()).toBe('Entwurf')

    const toggle = wrapper.get('[data-testid="toggle-voting"]')
    expect(toggle.text()).toBe('Öffnen')

    await toggle.trigger('click')
    await flushPromises()

    expect(mocks.updateStatus).toHaveBeenCalledWith(7, 'OPEN')
    expect(wrapper.get('[data-testid="lunch-status"]').text()).toBe('Abstimmung offen')
    expect(wrapper.get('[data-testid="toggle-voting"]').text()).toBe('Schließen')
  })

  it('does not send a vote while the event is still a draft', async () => {
    localStorage.setItem('lunchUserName', 'Alice')

    const wrapper = await mountAdmin()

    await wrapper.get('.lunch-card .cursor-pointer').trigger('click')
    await flushPromises()

    expect(mocks.voteForOption).not.toHaveBeenCalled()
  })
})
