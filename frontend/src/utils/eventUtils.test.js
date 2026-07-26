import { describe, it, expect } from 'vitest'
import { processCalendarEvents } from './eventUtils'

describe('processCalendarEvents', () => {
  it('returns empty object for null/undefined input', () => {
    expect(processCalendarEvents(null)).toEqual({})
    expect(processCalendarEvents(undefined)).toEqual({})
    expect(processCalendarEvents('not an array')).toEqual({})
  })

  it('returns empty object for empty array', () => {
    expect(processCalendarEvents([])).toEqual({})
  })

  it('groups events by ISO date', () => {
    const events = [
      { dashboardEventDate: '01.01.2026', dashboardEventType: 'BIRTHDAY', dashboardEventDescription: 'Test', employeeName: 'John Doe' },
      { dashboardEventDate: '01.01.2026', dashboardEventType: 'WORK_ANNIVERSARY', dashboardEventDescription: '5 Jahre', employeeName: 'Jane Smith' },
      { dashboardEventDate: '02.01.2026', dashboardEventType: 'BIRTHDAY', dashboardEventDescription: 'Test2', employeeName: 'Bob' },
    ]
    const result = processCalendarEvents(events)
    expect(Object.keys(result)).toHaveLength(2)
    expect(result['2026-01-01']).toHaveLength(2)
    expect(result['2026-01-02']).toHaveLength(1)
  })

  it('transforms BIRTHDAY events with emoji and name', () => {
    const events = [
      { dashboardEventDate: '15.03.2026', dashboardEventType: 'BIRTHDAY', dashboardEventDescription: 'Birthday', employeeName: 'Alice' },
    ]
    const result = processCalendarEvents(events)
    const entry = result['2026-03-15'][0]
    expect(entry.label).toContain('🎂')
    expect(entry.label).toContain('Alice')
    expect(entry.color).toBe('green')
  })

  it('transforms WORK_ANNIVERSARY events', () => {
    const events = [
      { dashboardEventDate: '10.06.2026', dashboardEventType: 'WORK_ANNIVERSARY', dashboardEventDescription: '5 Jahre', employeeName: 'Bob Builder' },
    ]
    const result = processCalendarEvents(events)
    const entry = result['2026-06-10'][0]
    expect(entry.label).toContain('🎉')
    expect(entry.color).toBe('purple')
  })

  it('transforms PROBATION_END events', () => {
    const events = [
      { dashboardEventDate: '20.09.2026', dashboardEventType: 'PROBATION_END', dashboardEventDescription: 'End', employeeName: 'Charlie' },
    ]
    const result = processCalendarEvents(events)
    const entry = result['2026-09-20'][0]
    expect(entry.label).toContain('✅')
    expect(entry.color).toBe('red')
  })

  it('skips events without a date', () => {
    const events = [
      { dashboardEventType: 'BIRTHDAY', dashboardEventDescription: 'No date', employeeName: 'Test' },
    ]
    const result = processCalendarEvents(events)
    expect(Object.keys(result)).toHaveLength(0)
  })

  it('skips events with malformed date', () => {
    const events = [
      { dashboardEventDate: 'invalid', dashboardEventType: 'BIRTHDAY', dashboardEventDescription: 'Bad', employeeName: 'Test' },
    ]
    const result = processCalendarEvents(events)
    expect(Object.keys(result)).toHaveLength(0)
  })

  it('strips leading date from titles', () => {
    const events = [
      { dashboardEventDate: '01.01.2026', dashboardEventType: 'OTHER', dashboardEventDescription: '01.01.2026 Some Event', employeeName: 'Test' },
    ]
    const result = processCalendarEvents(events)
    const entry = result['2026-01-01'][0]
    expect(entry.title).not.toMatch(/^\d{2}\.\d{2}\.\d{4}/)
  })
})
