import { onUnmounted } from 'vue'

/**
 * Composable for managing the automatic view rotation in kiosk mode.
 *
 * @param {Object} options
 * @param {import('vue').Ref<Object>} options.config - reactive display config
 * @param {import('vue').Ref<string>} options.currentViewId - reactive current view ID
 * @param {Function} options.onRotate - callback when view rotates (e.g. refresh data)
 * @param {string[]} options.viewIds - array of view IDs that can be rotated
 */
export function useViewRotation({ config, currentViewId, onRotate, viewIds }) {
  let viewRotationInterval = null

  function setupRotation() {
    if (viewRotationInterval) {
      clearInterval(viewRotationInterval)
      viewRotationInterval = null
    }

    // Special case: game mode
    if (config.value.rotationIntervalSeconds === -1) {
      currentViewId.value = 'game'
      return
    }

    // Reset from game mode if config changed
    if (currentViewId.value === 'game' && config.value.rotationIntervalSeconds !== -1) {
      currentViewId.value = config.value.defaultSingleViewId || 'dashboard'
    }

    if (config.value.rotationEnabled) {
      let safeInterval = config.value.rotationIntervalSeconds
      if (safeInterval < 5) {
        console.warn(`Unsafe rotation interval ${safeInterval}s detected. Defaulting to 5s.`)
        safeInterval = 5
      }

      viewRotationInterval = setInterval(() => {
        let availableViews = viewIds
        if (config.value.skipOneDisplayInRotation) {
          availableViews = viewIds.filter(id => id !== 'dashboard')
        }

        let currentIndex = availableViews.indexOf(currentViewId.value)
        const nextIndex = (currentIndex + 1) % availableViews.length
        currentViewId.value = availableViews[nextIndex]
        onRotate(currentViewId.value)
      }, safeInterval * 1000)
    } else {
      if (config.value.defaultSingleViewId && viewIds.includes(config.value.defaultSingleViewId)) {
        if (currentViewId.value !== config.value.defaultSingleViewId) {
          currentViewId.value = config.value.defaultSingleViewId
          onRotate(currentViewId.value)
        }
      }
    }
  }

  onUnmounted(() => {
    if (viewRotationInterval) clearInterval(viewRotationInterval)
  })

  return { setupRotation }
}
