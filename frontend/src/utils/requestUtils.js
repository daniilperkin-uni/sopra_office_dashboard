/**
 * Hilfsfunktionen fuer HTTP-Anfragen, die von mehreren Stores genutzt werden.
 */

/**
 * Prueft, ob ein Fehler von einem Abbruch (AbortSignal) stammt.
 *
 * Abgebrochene Anfragen sind kein Fehlerfall: Sie entstehen zum Beispiel, wenn
 * eine Ansicht waehrend einer laufenden Anfrage verlassen wird. Solche Fehler
 * duerfen weder ein Fehlerbanner noch einen Fehler-Toast ausloesen.
 *
 * @param {unknown} error - Der abgefangene Fehler.
 * @returns {boolean} True, wenn die Anfrage abgebrochen wurde.
 */
export function isRequestCanceled(error) {
  if (!error || typeof error !== 'object') return false
  if (error.code === 'ERR_CANCELED') return true
  return error.name === 'CanceledError' || error.name === 'AbortError'
}
