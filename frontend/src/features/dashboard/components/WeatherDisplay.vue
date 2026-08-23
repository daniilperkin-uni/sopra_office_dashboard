<template>
  <div
    class="h-full w-full bg-neutral-bg box-border overflow-hidden font-sans text-white p-8 flex flex-col items-center justify-center relative"
  >
    <div v-if="loading" class="flex flex-col items-center gap-4">
      <LoadingSpinner />
      <p class="text-white/70">Wetter wird geladen…</p>
    </div>

    <div v-else-if="error" class="flex flex-col items-center gap-3 text-center px-8">
      <span class="text-5xl">🌐</span>
      <p class="text-white/70 text-lg">
        Wetter derzeit nicht verfügbar. Der Kiosk sorgt weiter für sich.
      </p>
    </div>

    <div v-else-if="current" class="flex flex-col items-center gap-2 text-center">
      <p class="text-white/60 text-xl tracking-wide">{{ locationLabel }}</p>
      <p class="text-7xl font-light leading-none">{{ current.temperature }}°C</p>
      <p class="text-3xl">{{ iconFor(current.weathercode) }}</p>
      <p class="text-white/80 text-lg">{{ labelFor(current.weathercode) }}</p>
      <div class="flex gap-6 mt-2 text-white/70">
        <span>↓ {{ today.min }}°</span>
        <span>↑ {{ today.max }}°</span>
      </div>
    </div>

    <div v-else class="text-white/60 text-lg">Keine Wetterdaten.</div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

/**
 * Open-Meteo is a free weather API that requires no API key. The city is
 * configured by editing WEATHER_LOCATION below (latitude/longitude); the
 * kiosk stays honest offline by always falling back to an empty state
 * instead of fabricated data.
 */
const WEATHER_LOCATION = {
  lat: 48.7109,
  lon: 9.1327,
  name: 'Stuttgart',
}

const WMO_LABELS = {
  0: 'Klar',
  1: 'Überwiegend klar',
  2: 'Teilweise bewölkt',
  3: 'Bedeckt',
  45: 'Neblig',
  48: 'Gefrierender Nebel',
  51: 'Leichter Nieselregen',
  53: 'Nieselregen',
  55: 'Starker Nieselregen',
  61: 'Leichter Regen',
  63: 'Regen',
  65: 'Starker Regen',
  71: 'Leichter Schneefall',
  73: 'Schneefall',
  75: 'Starker Schneefall',
  80: 'Regenschauer',
  81: 'Kräftige Schauer',
  82: 'Gewitterschauer',
  95: 'Gewitter',
  96: 'Gewitter mit Hagel',
  99: 'Gewitter mit Hagel',
}

const WMO_ICONS = {
  0: '☀️',
  1: '🌤️',
  2: '⛅',
  3: '☁️',
  45: '🌫️',
  48: '🌫️',
  51: '🌦️',
  53: '🌦️',
  55: '🌦️',
  61: '🌧️',
  63: '🌧️',
  65: '🌧️',
  71: '🌨️',
  73: '🌨️',
  75: '🌨️',
  80: '🌦️',
  81: '🌧️',
  82: '⛈️',
  95: '⛈️',
  96: '⛈️',
  99: '⛈️',
}

const loading = ref(true)
const error = ref(false)
const current = ref(null)
const today = ref({ min: '–', max: '–' })
const locationLabel = `${WEATHER_LOCATION.name}`

let refreshTimer = null

async function fetchWeather() {
  const url =
    'https://api.open-meteo.com/v1/forecast' +
    `?latitude=${WEATHER_LOCATION.lat}&longitude=${WEATHER_LOCATION.lon}` +
    '&current_weather=true&daily=temperature_2m_max,temperature_2m_min&timezone=auto'

  try {
    const res = await fetch(url)
    if (!res.ok) throw new Error(`weather request ${res.status}`)
    const data = await res.json()
    if (!data.current_weather) throw new Error('malformed weather payload')
    current.value = data.current_weather
    today.value = {
      min: Math.round(data.daily.temperature_2m_min[0]),
      max: Math.round(data.daily.temperature_2m_max[0]),
    }
    error.value = false
  } catch {
    error.value = true
    current.value = null
  } finally {
    loading.value = false
  }
}

function iconFor(code) {
  return WMO_ICONS[code] ?? '🌡️'
}

function labelFor(code) {
  return WMO_LABELS[code] ?? 'Unbekannt'
}

onMounted(() => {
  fetchWeather()
  refreshTimer = setInterval(fetchWeather, 30 * 60 * 1000) // refresh every 30 min
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
})
</script>
