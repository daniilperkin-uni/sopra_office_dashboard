import { describe, it, expect } from 'vitest'
import {
  formatDate,
  formatDateISO,
  formatDateForAPI,
  formatWeekday,
  isWeekend,
  getWorkingDays,
  isPastDate,
} from '@/utils/dateUtils'

/**
 * Unit tests for the shared date helpers. The module was listed as a test gap
 * in the audit even though every calendar view depends on it.
 */
describe('dateUtils', () => {
  describe('formatDate', () => {
    it('formats an ISO date as DD.MM.YYYY', () => {
      expect(formatDate('2026-01-25')).toBe('25.01.2026')
    })

    it('formats a [year, month, day] array', () => {
      expect(formatDate([2026, 1, 25])).toBe('25.01.2026')
    })

    it('returns an empty string for missing or invalid input', () => {
      expect(formatDate(null)).toBe('')
      expect(formatDate(undefined)).toBe('')
      expect(formatDate('not-a-date')).toBe('')
    })
  })

  describe('formatDateISO', () => {
    it('formats a date as YYYY-MM-DD', () => {
      expect(formatDateISO(new Date(2026, 0, 25))).toBe('2026-01-25')
    })

    it('pads single-digit months and days', () => {
      expect(formatDateISO(new Date(2026, 8, 5))).toBe('2026-09-05')
    })

    it('returns an empty string for missing input', () => {
      expect(formatDateISO(null)).toBe('')
    })
  })

  describe('formatDateForAPI', () => {
    it('formats a date as DD-MM-YYYY', () => {
      expect(formatDateForAPI(new Date(2026, 0, 25))).toBe('25-01-2026')
    })

    it('returns an empty string for invalid input', () => {
      expect(formatDateForAPI('not-a-date')).toBe('')
    })
  })

  describe('formatWeekday', () => {
    it('names the weekday in German', () => {
      expect(formatWeekday('2026-01-26')).toBe('Montag')
    })

    it('returns an empty string for invalid input', () => {
      expect(formatWeekday(null)).toBe('')
    })
  })

  describe('isWeekend', () => {
    it('is true on Saturday and Sunday', () => {
      expect(isWeekend('2026-01-24')).toBe(true)
      expect(isWeekend('2026-01-25')).toBe(true)
    })

    it('is false on a working day', () => {
      expect(isWeekend('2026-01-26')).toBe(false)
    })
  })

  describe('getWorkingDays', () => {
    it('skips weekends and returns the requested number of days', () => {
      const days = getWorkingDays('2026-01-23', 3) // Friday
      expect(days).toHaveLength(3)
      expect(formatDateISO(days[0])).toBe('2026-01-23') // Friday
      expect(formatDateISO(days[1])).toBe('2026-01-26') // Monday
      expect(formatDateISO(days[2])).toBe('2026-01-27') // Tuesday
    })

    it('returns an empty list when no days are requested', () => {
      expect(getWorkingDays('2026-01-23', 0)).toEqual([])
    })
  })

  describe('isPastDate', () => {
    it('is true for yesterday and false for tomorrow', () => {
      const today = new Date()
      const yesterday = new Date(today)
      yesterday.setDate(today.getDate() - 1)
      const tomorrow = new Date(today)
      tomorrow.setDate(today.getDate() + 1)

      expect(isPastDate(yesterday)).toBe(true)
      expect(isPastDate(tomorrow)).toBe(false)
    })

    it('is false for missing input', () => {
      expect(isPastDate(null)).toBe(false)
    })
  })
})
