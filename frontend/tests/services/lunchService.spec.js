import { describe, it, expect, vi, beforeEach } from 'vitest'

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

import { lunchService } from '@/services/lunchService'

/**
 * Regression guard: addOptionFromCatalog sent { catalogItemId } (a single
 * value) while the backend's AddFromCatalogRequest requires the
 * @NotEmpty List<...> catalogItemIds, so every call was rejected with HTTP 400.
 */
describe('lunchService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    apiClientMock.post.mockResolvedValue({ data: { id: 1 } })
  })

  it('sends the catalogItemIds list the backend request expects', async () => {
    await lunchService.addOptionFromCatalog(10, 42)

    expect(apiClientMock.post).toHaveBeenCalledWith('/community-lunches/10/options/from-catalog', {
      catalogItemIds: [42],
    })
  })
})
