import { describe, it, expect, vi } from 'vitest'
import { parkingApi } from '@/services/api'

vi.mock('axios', () => {
  return {
    default: {
      create: vi.fn(() => ({
        get: vi.fn(),
        post: vi.fn(),
        put: vi.fn(),
        delete: vi.fn(),
        patch: vi.fn(),
        interceptors: {
          response: {
            use: vi.fn(),
          },
        },
      })),
    },
  }
})

describe('parkingApi', () => {
  it('has getAllEntries method', () => {
    expect(typeof parkingApi.getAllEntries).toBe('function')
  })

  it('has getOverview method', () => {
    expect(typeof parkingApi.getOverview).toBe('function')
  })

  it('has createEntry method', () => {
    expect(typeof parkingApi.createEntry).toBe('function')
  })

  it('has deleteEntry method', () => {
    expect(typeof parkingApi.deleteEntry).toBe('function')
  })

  it('has updateEntry method', () => {
    expect(typeof parkingApi.updateEntry).toBe('function')
  })

  it('has createRecurringReservation method', () => {
    expect(typeof parkingApi.createRecurringReservation).toBe('function')
  })

  it('has deleteRecurringReservation method', () => {
    expect(typeof parkingApi.deleteRecurringReservation).toBe('function')
  })

  it('has updateRecurringReservation method', () => {
    expect(typeof parkingApi.updateRecurringReservation).toBe('function')
  })
})
