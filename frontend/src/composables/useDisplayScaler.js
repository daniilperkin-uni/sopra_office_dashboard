import { ref, computed, onMounted, onUnmounted } from 'vue'

/**
 * Composable for the global "Fit to Screen" scaling strategy.
 * The dashboard always renders at 3840x2160 (4K) and scales down via CSS
 * transforms to fit any screen size while preserving the 4K layout.
 *
 * @returns {Object} scalerStyle (computed style object) and scale (reactive ref)
 */
export function useDisplayScaler() {
  const REFERENCE_WIDTH = 3840
  const REFERENCE_HEIGHT = 2160

  const scale = ref(1)

  const updateScale = () => {
    const widthRatio = window.innerWidth / REFERENCE_WIDTH
    const heightRatio = window.innerHeight / REFERENCE_HEIGHT
    scale.value = Math.min(widthRatio, heightRatio)
  }

  const scalerStyle = computed(() => ({
    width: `${REFERENCE_WIDTH}px`,
    height: `${REFERENCE_HEIGHT}px`,
    transform: `translate(-50%, -50%) scale(${scale.value})`,
  }))

  onMounted(() => {
    updateScale()
    window.addEventListener('resize', updateScale)
  })

  onUnmounted(() => {
    window.removeEventListener('resize', updateScale)
  })

  return { scalerStyle, scale }
}
