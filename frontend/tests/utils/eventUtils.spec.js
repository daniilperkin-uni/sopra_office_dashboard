import { describe, it, expect, vi, beforeEach } from 'vitest'
import { processCalendarEvents } from '@/utils/eventUtils'

vi.mock('@/utils/eventUtils', async () => {
  const actual = await vi.importActual('@/utils/eventUtils')
  return { ...actual }
})

describe('processCalendarEvents', () => {
  beforeEach(() => {
    vi.stubEnv('VITE_ANONYMIZE_NAMES', 'false')
  })

  it('returns empty object for null input', () => {
    expect(processCalendarEvents(null)).toEqual({})
  })

  it('returns empty object for non-array input', () => {
    expect(processCalendarEvents('not-an-array')).toEqual({})
  })

  it('returns empty object for empty array', () => {
    expect(processCalendarEvents([])).toEqual({})
  })

  it('groups events by ISO date', () => {
    const events = [
      {
        employeeName: 'Max Mustermann',
        dashboardEventDescription: '25.01.2026 Max Mustermann hat Geburtstag!',
        dashboardEventType: 'BIRTHDAY',
        dashboardEventDate: '25.01.2026',
      },
      {
        employeeName: 'Anna Schmidt',
        dashboardEventDescription: '25.01.2026 Anna Schmidt hat Geburtstag!',
        dashboardEventType: 'BIRTHDAY',
        dashboardEventDate: '25.01.2026',
      },
      {
        employeeName: 'Bob Bauer',
        dashboardEventDescription: '26.01.2026 Bob Bauer ist 5 Jahre bei itestra!',
        dashboardEventType: 'WORK_ANNIVERSARY',
        dashboardEventDate: '26.01.2026',
      },
    ]

    const result = processCalendarEvents(events)

    expect(Object.keys(result)).toHaveLength(2)
    expect(result['2026-01-25']).toHaveLength(2)
    expect(result['2026-01-26']).toHaveLength(1)
  })

  it('formats birthday events with name and cake icon', () => {
    const events = [
      {
        employeeName: 'Max Mustermann',
        dashboardEventDescription: '25.01.2026 Max Mustermann hat Geburtstag!',
        dashboardEventType: 'BIRTHDAY',
        dashboardEventDate: '25.01.2026',
      },
    ]

    const result = processCalendarEvents(events)
    const event = result['2026-01-25'][0]

    expect(event.label).toContain('Max Mustermann')
    expect(event.label).toContain('🎂')
    expect(event.color).toBe('green')
  })

  it('formats work anniversary events with years and party icon', () => {
    const events = [
      {
        employeeName: 'Anna Schmidt',
        dashboardEventDescription: 'Anna Schmidt ist 5 Jahre bei itestra!',
        dashboardEventType: 'WORK_ANNIVERSARY',
        dashboardEventDate: '15.03.2026',
      },
    ]

    const result = processCalendarEvents(events)
    const event = result['2026-03-15'][0]

    expect(event.label).toContain('Anna Schmidt')
    expect(event.label).toContain('5 Jahre')
    expect(event.label).toContain('🎉')
    expect(event.color).toBe('purple')
  })

  it('formats probation end events with checkmark icon', () => {
    const events = [
      {
        employeeName: 'Bob Bauer',
        dashboardEventDescription: 'Bob Bauer Probation Ende',
        dashboardEventType: 'PROBATION_END',
        dashboardEventDate: '10.02.2026',
      },
    ]

    const result = processCalendarEvents(events)
    const event = result['2026-02-10'][0]

    expect(event.label).toContain('Bob Bauer')
    expect(event.label).toContain('✅')
    expect(event.color).toBe('red')
  })

  it('strips leading date prefix from titles', () => {
    const events = [
      {
        employeeName: 'Max',
        dashboardEventDescription: '25.01.2026 Some Event Description',
        dashboardEventType: 'BIRTHDAY',
        dashboardEventDate: '25.01.2026',
      },
    ]

    const result = processCalendarEvents(events)
    const event = result['2026-01-25'][0]

    expect(event.title).not.toMatch(/^\d{2}\.\d{2}\.\d{4}/)
  })

  it('skips events without dashboardEventDate', () => {
    const events = [
      {
        employeeName: 'Max',
        dashboardEventDescription: 'No date event',
        dashboardEventType: 'BIRTHDAY',
      },
    ]

    const result = processCalendarEvents(events)
    expect(Object.keys(result)).toHaveLength(0)
  })

  it('anonymizes names when VITE_ANONYMIZE_NAMES is true', () => {
    vi.stubEnv('VITE_ANONYMIZE_NAMES', 'true')

    const events = [
      {
        employeeName: 'VeryLongEmployeeNameHere',
        dashboardEventDescription: 'VeryLongEmployeeNameHere hat Geburtstag!',
        dashboardEventType: 'BIRTHDAY',
        dashboardEventDate: '25.01.2026',
      },
    ]

    const result = processCalendarEvents(events)
    const event = result['2026-01-25'][0]

    expect(event.label).toContain('eNameHere')
    expect(event.label).not.toContain('VeryLongEmployeeNameHere')
  })
})
