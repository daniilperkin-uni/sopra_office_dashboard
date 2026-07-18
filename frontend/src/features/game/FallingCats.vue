<template>
  <div class="falling-cats-container">
    <div
      v-for="cat in activeCats"
      :key="cat.id"
      class="falling-cat"
      :style="{
        left: cat.x + '%',
        width: cat.size + 'px',
        height: cat.size + 'px',
        animationDuration: cat.duration + 's',
        animationDelay: cat.delay + 's',
        backgroundImage: `url(${cat.src})`,
      }"
    />
  </div>
</template>

<script setup>
/**
 * A fun component ("Easter Egg") that animates falling cat images.
 * Used in the secret game mode.
 * Generates random cats at random positions with different speeds.
 */
import { ref, onMounted, onUnmounted } from 'vue'

const CAT_IMAGES = [
  '/cats/cat2.jpg',
  '/cats/cat3.jpg',
  '/cats/cat5.jpg',
  '/cats/cat 6.jpg',
  '/cats/cat7.jpg',
  '/cats/cat8.jpg',
  '/cats/catgif1.gif',
  '/cats/catgif2.gif',
  '/cats/catgif3.gif',
  '/cats/catgif4.gif',
  '/cats/catgif5.gif',
  '/cats/catgif6.gif',
]

const activeCats = ref([])
let intervalId = null
let idCounter = 0

// Creates a new cat with random properties.
const spawnCat = () => {
  const id = idCounter++
  const src = CAT_IMAGES[Math.floor(Math.random() * CAT_IMAGES.length)]
  const x = Math.random() * 60 // 0-60% to avoid overflow
  const duration = 4 + Math.random() * 5 // Slower, heavier fall
  const delay = 0

  // Random huge sizes for 4K visibility (Increased by 15%: ~230px to ~520px)
  const size = 230 + Math.random() * 290

  activeCats.value.push({ id, src, x, duration, delay, size })

  // Limits the number of active cats to conserve performance.
  if (activeCats.value.length > 30) {
    activeCats.value.shift()
  }
}

onMounted(() => {
  for (let i = 0; i < 3; i++) spawnCat()
  intervalId = setInterval(spawnCat, 1200) // Spawn every 1.2s
})

onUnmounted(() => {
  if (intervalId) clearInterval(intervalId)
})
</script>

<style scoped>
.falling-cats-container {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
  background-color: #111;
}

.falling-cat {
  position: absolute;
  top: -500px; /* Start well above screen given larger size */
  background-size: cover;
  background-position: center;
  border-radius: 15px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
  border: 2px solid rgba(255, 255, 255, 0.2); /* Visibility aid */
  animation-name: fall;
  animation-timing-function: linear;
  animation-iteration-count: infinite;
  z-index: 10;
}

@keyframes fall {
  0% {
    transform: translateY(0) rotate(0deg);
  }
  100% {
    transform: translateY(150vh) rotate(360deg);
  }
}
</style>
