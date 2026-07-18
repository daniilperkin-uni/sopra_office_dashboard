import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// Load professional fonts (matching itestra branding)
import '@fontsource/mulish/300.css'
import '@fontsource/mulish/400.css'
import '@fontsource/mulish/600.css'
import '@fontsource/mulish/800.css'

import './assets/main.css'

/**
 * Main application entry point.
 * Initializes the Vue app with Pinia state management and the router.
 */
const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
