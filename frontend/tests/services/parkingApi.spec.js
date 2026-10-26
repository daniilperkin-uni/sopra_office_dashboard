import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'

// Der Axios-Mock liegt in vi.hoisted, damit der gepatchte Client schon vor dem
// Import von '@/services/api' existiert und die Aufrufe im Test pruefbar sind.
const { apiClientMock, createMock } = vi.hoisted(() => {
  const client = {
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
  }

  return { apiClientMock: client, createMock: vi.fn(() => client) }
})

vi.mock('axios', () => ({
  default: {
    create: createMock,
  },
}))

import { parkingApi } from '@/services/api'

// Konfiguration des zentralen Clients: entsteht beim Import, deshalb hier
// einmalig festhalten (beforeEach leert die Aufrufhistorie der Mocks).
const clientOptions = createMock.mock.calls[0]?.[0]

describe('parkingApi', () => {
  let consoleErrorSpy

  beforeEach(() => {
    vi.clearAllMocks()
    consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

    // Standardantworten; einzelne Tests ueberschreiben sie bei Bedarf.
    apiClientMock.get.mockResolvedValue({ data: [] })
    apiClientMock.post.mockResolvedValue({ data: { id: 1 } })
    apiClientMock.put.mockResolvedValue({ data: { id: 1 } })
    apiClientMock.delete.mockResolvedValue({ data: null })
  })

  afterEach(() => {
    consoleErrorSpy.mockRestore()
  })

  it('creates the shared axios client with a request timeout', () => {
    // Ohne Timeout bleibt eine nie beantwortete Anfrage unendlich offen:
    // Lade-Flags bleiben true und auf /display startet das Polling nie.
    expect(clientOptions.timeout).toBe(10000)
    expect(clientOptions.baseURL).toBe('/api')
  })

  it('loads all entries from /entries and returns the payload', async () => {
    const entries = [{ id: 1, employeeName: 'Ada' }]
    apiClientMock.get.mockResolvedValue({ data: entries })

    await expect(parkingApi.getAllEntries()).resolves.toEqual(entries)
    expect(apiClientMock.get).toHaveBeenCalledTimes(1)
    expect(apiClientMock.get).toHaveBeenCalledWith('/entries')
  })

  it('loads the occupancy overview from /overview and forwards the abort signal', async () => {
    const overview = [{ date: '2026-01-02', entries: [], totalSpots: 8 }]
    apiClientMock.get.mockResolvedValue({ data: overview })
    const controller = new AbortController()

    await expect(parkingApi.getOverview(controller.signal)).resolves.toEqual(overview)
    expect(apiClientMock.get).toHaveBeenCalledWith('/overview', { signal: controller.signal })
  })

  it('posts a new entry to /entries and returns the created entry', async () => {
    const payload = { employeeName: 'Ada', date: '2026-01-02' }
    const created = { id: 7, ...payload }
    apiClientMock.post.mockResolvedValue({ data: created })

    await expect(parkingApi.createEntry(payload)).resolves.toEqual(created)
    expect(apiClientMock.post).toHaveBeenCalledTimes(1)
    expect(apiClientMock.post).toHaveBeenCalledWith('/entries', payload)
  })

  it('updates an entry via PUT /entries/:id and returns the updated entry', async () => {
    const payload = { employeeName: 'Grace', date: '2026-01-05' }
    const updated = { id: 7, ...payload }
    apiClientMock.put.mockResolvedValue({ data: updated })

    await expect(parkingApi.updateEntry(7, payload)).resolves.toEqual(updated)
    expect(apiClientMock.put).toHaveBeenCalledWith('/entries/7', payload)
  })

  it('deletes an entry via DELETE /entries/:id and reports success', async () => {
    await expect(parkingApi.deleteEntry(7)).resolves.toBe(true)
    expect(apiClientMock.delete).toHaveBeenCalledTimes(1)
    expect(apiClientMock.delete).toHaveBeenCalledWith('/entries/7')
  })

  it('posts a recurring reservation to /recurring without preview by default', async () => {
    const payload = { employeeName: 'Ada', weekdays: ['MO'], startDate: '2026-01-02' }
    const result = { created: 4 }
    apiClientMock.post.mockResolvedValue({ data: result })

    await expect(parkingApi.createRecurringReservation(payload)).resolves.toEqual(result)
    expect(apiClientMock.post).toHaveBeenCalledWith('/recurring', payload, {
      params: { preview: false },
    })
  })

  it('asks for a preview when creating a recurring reservation in preview mode', async () => {
    const payload = { employeeName: 'Ada', weekdays: ['MO'], startDate: '2026-01-02' }

    await parkingApi.createRecurringReservation(payload, true)

    expect(apiClientMock.post).toHaveBeenCalledWith('/recurring', payload, {
      params: { preview: true },
    })
  })

  it('deletes a recurring series via DELETE /recurring/:id and reports success', async () => {
    await expect(parkingApi.deleteRecurringReservation(9)).resolves.toBe(true)
    expect(apiClientMock.delete).toHaveBeenCalledWith('/recurring/9')
  })

  it('updates a recurring series via PUT /recurring/:id', async () => {
    const payload = { employeeName: 'Grace' }
    const updated = { id: 9, ...payload }
    apiClientMock.put.mockResolvedValue({ data: updated })

    await expect(parkingApi.updateRecurringReservation(9, payload)).resolves.toEqual(updated)
    expect(apiClientMock.put).toHaveBeenCalledWith('/recurring/9', payload)
  })

  it('rethrows backend errors instead of swallowing them', async () => {
    const error = new Error('backend down')
    apiClientMock.post.mockRejectedValue(error)

    await expect(parkingApi.createEntry({ employeeName: 'Ada' })).rejects.toThrow('backend down')
    expect(consoleErrorSpy).toHaveBeenCalled()
  })
})
