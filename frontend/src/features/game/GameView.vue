<template>
  <div class="main-layout">
    <!-- Left Column: Falling Cats (33%) -->
    <div class="column cats-column">
      <FallingCats />
    </div>

    <!-- Middle Column: Flappy Bird Game (33%) -->
    <div ref="wrapperRef" class="column game-wrapper">
      <div
        class="game-container"
        :style="{ transform: `scale(${scale})` }"
        @click="jump"
        @mousedown.prevent
      >
        <!-- Parallax Background -->
        <div class="background-scroll" />
        <div class="ground-scroll" />

        <!-- UI: Score -->
        <div class="score">
          {{ score }}
        </div>

        <!-- Game World -->
        <div
          class="bird-wrapper"
          :style="{
            top: birdY + 'px',
            left: CONSTANTS.BIRD_X + 'px',
            transform: `rotate(${rotation}deg)`,
          }"
        >
          <div class="bird-visual">
            <div class="eye" />
            <div class="wing" />
            <div class="beak" />
          </div>
        </div>

        <!-- Pipes -->
        <div v-for="pipe in pipes" :key="pipe.id">
          <div
            class="pipe top"
            :style="{
              left: pipe.x + 'px',
              height: pipe.topHeight + 'px',
              width: CONSTANTS.PIPE_WIDTH + 'px',
            }"
          >
            <div class="pipe-cap top-cap" />
          </div>
          <div
            class="pipe bottom"
            :style="{
              left: pipe.x + 'px',
              height: pipe.bottomHeight + 'px',
              width: CONSTANTS.PIPE_WIDTH + 'px',
            }"
          >
            <div class="pipe-cap bottom-cap" />
          </div>
        </div>

        <!-- Game Over Screen -->
        <transition name="fade">
          <div v-if="gameOver" class="game-over-overlay">
            <div class="game-over-box">
              <h1>GAME OVER</h1>
              <div class="score-card">
                <span class="label">SCORE</span>
                <span class="value">{{ score }}</span>
              </div>
              <div class="score-card best">
                <span class="label">BEST</span>
                <span class="value">{{ highScore }}</span>
              </div>
              <button class="restart-btn" @click.stop="resetGame">PLAY AGAIN</button>
            </div>
          </div>
        </transition>

        <!-- Start Screen Hint -->
        <div v-if="!gameStarted && !gameOver" class="start-hint">PRESS SPACE OR CLICK</div>
      </div>
    </div>

    <!-- Right Column: YouTube (33%) -->
    <div class="column video-sidebar">
      <iframe
        src="https://www.youtube-nocookie.com/embed/eRXE8Aebp7s?autoplay=1&mute=1&loop=1&playlist=eRXE8Aebp7s"
        title="Subway Surfers"
        frameborder="0"
        allow="
          accelerometer;
          autoplay;
          clipboard-write;
          encrypted-media;
          gyroscope;
          picture-in-picture;
          web-share;
        "
        referrerpolicy="strict-origin-when-cross-origin"
        allowfullscreen
      />
    </div>
  </div>
</template>

<script setup>
/**
 * The main view for the "Secret Game" (Easter Egg).
 * Combines three columns: Falling Cats, Flappy Bird Clone, and YouTube Stream.
 * This view is only displayed if the rotation interval is set to -1.
 */
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import FallingCats from './FallingCats.vue'

// --- CONSTANTS & CONFIGURATION ---
const CONSTANTS = {
  GAME_WIDTH: 400,
  GAME_HEIGHT: 600,
  GRAVITY: 0.3,
  JUMP_STRENGTH: -5.5,
  ROTATION_JUMP: -25,
  ROTATION_FALL_MAX: 90,
  ROTATION_SPEED: 1.8,

  PIPE_SPEED: 1.5,
  PIPE_SPAWN_RATE: 2800,
  PIPE_WIDTH: 60,
  PIPE_GAP: 240,
  PIPE_MIN_HEIGHT: 50,

  BIRD_X: 60,
  BIRD_SIZE: 34,
  HITBOX_PADDING: 14,

  GROUND_HEIGHT: 20,
}

// --- STATE ---
const birdY = ref(CONSTANTS.GAME_HEIGHT / 2)
const velocity = ref(0)
const rotation = ref(0)
const pipes = ref([])
const score = ref(0)
const highScore = ref(0)
const gameOver = ref(false)
const gameStarted = ref(false)
const scale = ref(1)
const wrapperRef = ref(null)

let gameLoop = null
let pipeGenerator = null
let pipeIdCounter = 0

// --- PHYSICS & LOGIC ---

/**
 * Updates the game scale to fit the window size.
 */
const updateScale = () => {
  if (!wrapperRef.value) return
  const h = wrapperRef.value.clientHeight
  const w = wrapperRef.value.clientWidth

  const scaleX = w / CONSTANTS.GAME_WIDTH
  const scaleY = h / CONSTANTS.GAME_HEIGHT
  scale.value = Math.min(scaleX, scaleY)
}

/**
 * Makes the bird jump. Starts the game if not already started.
 */
const jump = () => {
  if (gameOver.value) return
  if (!gameStarted.value) startGame()
  velocity.value = CONSTANTS.JUMP_STRENGTH
  rotation.value = CONSTANTS.ROTATION_JUMP
}

/**
 * Starts the game loop and pipe generator.
 */
const startGame = () => {
  gameStarted.value = true
  loop()
  pipeGenerator = setInterval(spawnPipe, CONSTANTS.PIPE_SPAWN_RATE)
}

/**
 * Creates a new pair of pipes with random height.
 */
const spawnPipe = () => {
  if (gameOver.value || !gameStarted.value) return

  const availableHeight = CONSTANTS.GAME_HEIGHT - CONSTANTS.PIPE_GAP - CONSTANTS.GROUND_HEIGHT
  const maxTop = availableHeight - CONSTANTS.PIPE_MIN_HEIGHT
  const minTop = CONSTANTS.PIPE_MIN_HEIGHT

  const topHeight = Math.floor(Math.random() * (maxTop - minTop + 1)) + minTop
  const bottomHeight =
    CONSTANTS.GAME_HEIGHT - CONSTANTS.PIPE_GAP - topHeight - CONSTANTS.GROUND_HEIGHT

  pipes.value.push({
    id: pipeIdCounter++,
    x: CONSTANTS.GAME_WIDTH,
    topHeight: topHeight,
    bottomHeight: bottomHeight,
  })
}

/**
 * Checks for collisions between bird and pipes or ground/ceiling.
 * @returns {boolean} True on collision
 */
const checkCollision = () => {
  const pad = CONSTANTS.HITBOX_PADDING
  const bx1 = CONSTANTS.BIRD_X + pad
  const bx2 = CONSTANTS.BIRD_X + CONSTANTS.BIRD_SIZE - pad
  const by1 = birdY.value + pad
  const by2 = birdY.value + CONSTANTS.BIRD_SIZE - pad

  if (by2 >= CONSTANTS.GAME_HEIGHT - CONSTANTS.GROUND_HEIGHT || by1 <= 0) return true

  for (const pipe of pipes.value) {
    const px1 = pipe.x
    const px2 = pipe.x + CONSTANTS.PIPE_WIDTH

    if (bx2 > px1 && bx1 < px2) {
      if (by1 < pipe.topHeight) return true
      const bottomPipeVisualTop = CONSTANTS.GAME_HEIGHT - pipe.bottomHeight
      if (by2 > bottomPipeVisualTop) return true
    }
  }
  return false
}

/**
 * The main game loop. Updates physics, positions, and checks collisions.
 */
const loop = () => {
  if (gameOver.value) return

  velocity.value += CONSTANTS.GRAVITY
  birdY.value += velocity.value

  if (velocity.value < 0) {
    // jump logic
  } else {
    rotation.value += CONSTANTS.ROTATION_SPEED
    if (rotation.value > CONSTANTS.ROTATION_FALL_MAX) rotation.value = CONSTANTS.ROTATION_FALL_MAX
  }

  pipes.value.forEach((pipe) => (pipe.x -= CONSTANTS.PIPE_SPEED))

  if (pipes.value.length > 0) {
    const firstPipe = pipes.value[0]
    if (firstPipe.x + CONSTANTS.PIPE_WIDTH < CONSTANTS.BIRD_X && !firstPipe.passed) {
      score.value++
      firstPipe.passed = true
    }
    if (firstPipe.x < -CONSTANTS.PIPE_WIDTH) {
      pipes.value.shift()
    }
  }

  if (checkCollision()) {
    endGame()
    return
  }

  gameLoop = requestAnimationFrame(loop)
}

/**
 * Ends the game and saves the highscore.
 */
const endGame = () => {
  gameOver.value = true
  gameStarted.value = false
  if (score.value > highScore.value) highScore.value = score.value
  clearInterval(pipeGenerator)
  cancelAnimationFrame(gameLoop)
}

/**
 * Resets the game to the initial state.
 */
const resetGame = () => {
  birdY.value = CONSTANTS.GAME_HEIGHT / 2
  velocity.value = 0
  rotation.value = 0
  pipes.value = []
  score.value = 0
  gameOver.value = false
  gameStarted.value = false
}

const handleInput = (e) => {
  if (e.type === 'keydown' && e.code !== 'Space') return
  if (gameOver.value) return
  jump()
}

onMounted(async () => {
  await nextTick()
  updateScale()
  window.addEventListener('resize', updateScale)
  window.addEventListener('keydown', handleInput)

  const saved = localStorage.getItem('flappyHighScore')
  if (saved) highScore.value = parseInt(saved)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateScale)
  window.removeEventListener('keydown', handleInput)
  clearInterval(pipeGenerator)
  cancelAnimationFrame(gameLoop)
  localStorage.setItem('flappyHighScore', highScore.value.toString())
})
</script>

<style scoped>
/* --- Layout --- */
/* Füllt den 4K-Container (3840x2160) des Kiosk-Scalers. Viewport-Einheiten
   (100vw/100vh) würden nur die Fenstergröße abdecken; der Rest des
   Canvas bliebe leer. */
.main-layout {
  width: 100%;
  height: 100%;
  display: flex;
  background-color: #000;
  overflow: hidden;
}

.column {
  flex: 1; /* Equal width 33% */
  height: 100%;
  overflow: hidden;
  position: relative;
  border-left: 2px solid #333;
}
.column:first-child {
  border-left: none;
}

/* Left: Cats */
.cats-column {
  background: #111;
}

/* Middle: Game */
.game-wrapper {
  background-color: #222;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* Right: YouTube */
.video-sidebar {
  background: black;
  display: flex;
  justify-content: center;
  align-items: center;
}

.video-sidebar iframe {
  transform: scale(3); /* Zoom */
  transform-origin: center center;
  width: 100%;
  height: 100%;
}

/* --- Game Container --- */
.game-container {
  position: relative;
  width: 400px;
  height: 600px;
  background: linear-gradient(#4ec0ca, #9adbd9);
  overflow: hidden;
  box-shadow: 0 0 50px rgba(0, 0, 0, 0.5);
  transform-origin: center center;
  cursor: pointer;
}

/* ... (Keep rest of game styles identical) ... */
.background-scroll {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 200%;
  height: 200px;
  background-image:
    radial-gradient(circle 20px at 20% 50%, white 100%, transparent 100%),
    radial-gradient(circle 30px at 25% 60%, white 100%, transparent 100%),
    radial-gradient(circle 20px at 30% 50%, white 100%, transparent 100%),
    radial-gradient(circle 25px at 60% 30%, white 100%, transparent 100%),
    radial-gradient(circle 40px at 68% 40%, white 100%, transparent 100%),
    radial-gradient(circle 25px at 76% 30%, white 100%, transparent 100%);
  background-repeat: repeat-x;
  opacity: 0.8;
  animation: scrollClouds 20s linear infinite;
}
.ground-scroll {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 200%;
  height: 20px;
  background: repeating-linear-gradient(-45deg, #ded895, #ded895 10px, #cbb968 10px, #cbb968 20px);
  border-top: 2px solid #555;
  animation: scrollGround 2s linear infinite;
  z-index: 5;
}
@keyframes scrollClouds {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(-50%);
  }
}
@keyframes scrollGround {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(-50px);
  }
}

.bird-wrapper {
  position: absolute;
  width: 34px;
  height: 24px;
  z-index: 10;
  transform-origin: 50% 50%;
  will-change: transform, top;
}
.bird-visual {
  width: 100%;
  height: 100%;
  background: #f4d03f;
  border: 2px solid #000;
  border-radius: 50% / 60% 60% 40% 40%;
  position: relative;
  box-shadow: inset -2px -2px 0 rgba(0, 0, 0, 0.1);
}
.eye {
  position: absolute;
  top: 2px;
  right: 6px;
  width: 10px;
  height: 10px;
  background: white;
  border: 2px solid #000;
  border-radius: 50%;
}
.eye::after {
  content: '';
  position: absolute;
  top: 2px;
  right: 1px;
  width: 3px;
  height: 3px;
  background: #000;
  border-radius: 50%;
}
.wing {
  position: absolute;
  top: 12px;
  left: 4px;
  width: 14px;
  height: 8px;
  background: white;
  border: 2px solid #000;
  border-radius: 50% 50% 0 0;
}
.beak {
  position: absolute;
  top: 12px;
  right: -6px;
  width: 8px;
  height: 6px;
  background: #e67e22;
  border: 2px solid #000;
  border-radius: 0 50% 50% 0;
}

.pipe {
  position: absolute;
  background: linear-gradient(
    90deg,
    #55aa55 0%,
    #88cc88 15%,
    #55aa55 40%,
    #227722 95%,
    #004400 100%
  );
  border: 2px solid #000;
  box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.3);
}
.pipe-cap {
  position: absolute;
  left: -4px;
  width: calc(100% + 8px);
  height: 24px;
  background: linear-gradient(
    90deg,
    #55aa55 0%,
    #88cc88 15%,
    #55aa55 40%,
    #227722 95%,
    #004400 100%
  );
  border: 2px solid #000;
}
.pipe.top .pipe-cap {
  bottom: 0;
}
.pipe.bottom .pipe-cap {
  top: 0;
}
.pipe.top {
  top: 0;
}
.pipe.bottom {
  bottom: 0;
}

.score {
  position: absolute;
  top: 40px;
  width: 100%;
  text-align: center;
  font-size: 50px;
  font-weight: 900;
  color: white;
  z-index: 20;
  text-shadow:
    -2px -2px 0 #000,
    2px -2px 0 #000,
    -2px 2px 0 #000,
    2px 2px 0 #000;
  pointer-events: none;
}
.start-hint {
  position: absolute;
  bottom: 120px;
  width: 100%;
  text-align: center;
  font-size: 20px;
  font-weight: bold;
  color: white;
  text-shadow: 1px 1px 0 #000;
  animation: bounce 1s infinite alternate;
}
@keyframes bounce {
  from {
    transform: translateY(0);
  }
  to {
    transform: translateY(-10px);
  }
}

.game-over-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 50;
  backdrop-filter: blur(4px);
}
.game-over-box {
  background: #ded895;
  border: 4px solid #e67e22;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.4);
}
.game-over-box h1 {
  margin: 0 0 20px 0;
  color: #e67e22;
  font-size: 32px;
  text-shadow: 2px 2px 0 #000;
  -webkit-text-stroke: 1px #000;
}
.score-card {
  display: flex;
  justify-content: space-between;
  background: #cbb968;
  padding: 8px 12px;
  margin-bottom: 8px;
  border: 2px solid #a49345;
  border-radius: 4px;
  font-weight: bold;
}
.score-card.best {
  margin-bottom: 20px;
}
.restart-btn {
  background: #4ec0ca;
  border: 2px solid #fff;
  color: white;
  padding: 10px 20px;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  box-shadow: 0 4px 0 #2a8a92;
  transition:
    transform 0.1s,
    box-shadow 0.1s;
}
.restart-btn:active {
  transform: translateY(4px);
  box-shadow: 0 0 0 #2a8a92;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
