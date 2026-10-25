import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import ToastHost from '@/components/common/ToastHost.vue'
import { useToast } from '@/composables/useToast'

/**
 * Die Admin-Oberflächen haben früher native alert()-Dialoge benutzt: blockierend,
 * nicht gestaltbar und für Screenreader nicht als Statusmeldung erkennbar.
 * Diese Tests pinnen den Ersatz: Meldungen erscheinen im ToastHost, tragen den
 * richtigen Typ und lassen sich wieder schließen.
 *
 * Der Host rendert per <Teleport to="body">, deshalb wird im document.body
 * gesucht und nicht im Wrapper.
 */
const findToast = (type) => document.body.querySelector(`[data-testid="toast-${type}"]`)
const allToasts = () => [...document.body.querySelectorAll('.toast')]
const mountHost = () => mount(ToastHost, { attachTo: document.body })

describe('ToastHost', () => {
  let toast

  beforeEach(() => {
    vi.useFakeTimers()
    toast = useToast()
    toast.toasts.splice(0, toast.toasts.length)
  })

  afterEach(() => {
    toast.toasts.splice(0, toast.toasts.length)
    vi.useRealTimers()
    document.body.innerHTML = ''
  })

  it('renders a success message pushed through the composable', async () => {
    mountHost()

    toast.success('Serie gespeichert.')
    await nextTick()

    const item = findToast('success')
    expect(item).not.toBeNull()
    expect(item.textContent).toContain('Serie gespeichert.')
  })

  it('marks errors separately so they are distinguishable by type', async () => {
    mountHost()

    toast.error('Fehler beim Löschen der Serie.')
    await nextTick()

    expect(findToast('error')).not.toBeNull()
    expect(findToast('success')).toBeNull()
  })

  it('announces messages politely to assistive technology', async () => {
    mountHost()

    toast.info('Bitte einen Namen eingeben.')
    await nextTick()

    const host = document.body.querySelector('[role="status"]')
    expect(host).not.toBeNull()
    expect(host.getAttribute('aria-live')).toBe('polite')
  })

  it('dismisses a message when its close button is clicked', async () => {
    mountHost()

    toast.error('Fehler beim Speichern.')
    await nextTick()
    expect(allToasts()).toHaveLength(1)

    document.body.querySelector('.toast__close').click()
    await nextTick()

    expect(allToasts()).toHaveLength(0)
  })

  it('removes a message automatically after its timeout', async () => {
    mountHost()

    toast.success('Kurz sichtbar.', 1000)
    await nextTick()
    expect(allToasts()).toHaveLength(1)

    vi.advanceTimersByTime(1100)
    await nextTick()

    expect(allToasts()).toHaveLength(0)
  })
})
