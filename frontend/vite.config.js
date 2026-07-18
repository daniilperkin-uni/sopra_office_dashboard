import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// Exportiert die Vite-Konfiguration. 'defineConfig' bietet TypeScript-Unterstützung und Autovervollständigung.
export default defineConfig({
  // Plugins, die Vite während des Build- und Entwicklungsprozesses verwenden soll.
  // 'vue()' ist essenziell für die Verarbeitung von Vue Single-File Components (.vue Dateien).
  plugins: [vue()],

  // Konfiguriert, wie Module (Dateien) aufgelöst werden.
  resolve: {
    // Definiert Aliase, die als Abkürzungen für Import-Pfade verwendet werden können.
    alias: {
      // Erstellt ein Alias '@', der auf das 'src'-Verzeichnis zeigt.
      // Dies erlaubt Importe wie 'import MyComponent from "@/components/MyComponent.vue"'
      // anstelle von relativen Pfaden wie '../../../components/MyComponent.vue'.
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },

  // Konfiguriert den Vite-Entwicklungsserver.
  server: {
    // Setzt den Port, auf dem der Entwicklungsserver laufen soll.
    port: 3000,

    // Definiert Proxy-Regeln, um bestimmte Anfragen an einen anderen Server weiterzuleiten.
    // Dies ist nützlich, um CORS-Probleme während der Entwicklung zu umgehen.
    proxy: {
      // Leitet alle Anfragen, die mit '/api' beginnen, an das Backend weiter.
      '/api': {
        // Die Ziel-URL des Backend-Servers.
        target: 'http://localhost:8080',
        // Ändert den 'Origin'-Header der Anfrage auf den des Zielservers. Das ist wichtig für CORS.
        changeOrigin: true
      }
    }
  }
})