import { describe, it, expect, vi } from 'vitest'

const calls = []
vi.mock('@/services/api', () => ({
  default: {
    get: vi.fn(async (url) => {
      calls.push(`GET ${url}`)
      return { data: {} }
    }),
    post: vi.fn(async (url) => {
      calls.push(`POST ${url}`)
      return { data: { username: 'admin', role: 'ADMIN' } }
    }),
  },
}))

import { authService } from '@/services/authService'

describe('authService.login', () => {
  it('primes the CSRF cookie before posting the credentials', async () => {
    const user = await authService.login('admin', 'secret')
    expect(calls).toEqual(['GET /auth/csrf', 'POST /auth/login'])
    expect(user.username).toBe('admin')
    expect(authService.isAuthenticated.value).toBe(true)
  })
})
