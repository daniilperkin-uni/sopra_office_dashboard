# 📘 itestra Smart Office Dashboard (Frontend)

## 1. Projektübersicht & Ziele (5 Min)

### Einleitung
Herzlich willkommen zur Vorstellung des **itestra Smart Office Dashboards**. Wir betrachten heute das Frontend-System, das als zentrales Informations-Hub in unserem Büro dient. Dieses Projekt ist nicht nur eine einfache Webseite, sondern eine hybride Lösung, die zwei sehr unterschiedliche Anforderungen in einer einzigen Codebase vereint.

Das Dashboard läuft 24/7 auf einem großen 4K-Display im Eingangsbereich (Kiosk-Mode) und dient gleichzeitig als Administrations-Tool auf den Laptops der Office Manager. Unser Ziel war es, eine robuste, wartungsarme und visuell ansprechende Lösung zu schaffen, die den "Pulse" des Büros visualisiert.

### Die User Personas & Szenarien

Wir haben zwei Hauptnutzergruppen identifiziert, deren Bedürfnisse oft im Widerspruch stehen, was die Entwicklung besonders spannend macht:

1.  **Der Office Manager (Admin)**:
    *   **Kontext**: Sitzt am Schreibtisch, nutzt Maus und Tastatur, hat Zeit für Interaktion.
    *   **Bedürfnis**: Effizienz und Kontrolle. Er muss schnell Parkplätze für Gäste blocken, das Mittagessen organisieren oder fehlerhafte Highscore-Einträge korrigieren.
    *   **Anforderung an UI**: Dichte Informationsdarstellung, Formulare, Validierung, Feedback-Messages (Toasts).

2.  **Der Mitarbeiter (Viewer)**:
    *   **Kontext**: Läuft am TV im Vorbeigehen vorbei (z.B. auf dem Weg zur Kaffeemaschine).
    *   **Bedürfnis**: Information "at a glance". Er hat maximal 3 Sekunden Zeit, um zu erfassen: "Gibt es heute Pizza?", "Ist Parkplatz 5 frei?" oder "Wer führt beim Kicker?".
    *   **Anforderung an UI**: Maximaler Kontrast, riesige Schriftarten (für 4K optimiert), keine Interaktionselemente, automatische Rotation der Inhalte.

### Die Technische Herausforderung: Dual-Mode UI
Die größte Herausforderung war es, diese beiden Welten zu vereinen, ohne zwei separate Apps zu bauen.
*   **Kiosk Mode**: Muss passiv sein. Er darf nie "stecken bleiben" (z.B. in einem Modal). Er muss sich selbst heilen, wenn das Internet kurz weg ist.
*   **Admin Mode**: Muss reaktiv sein. Er braucht Authentifizierung (bzw. geschützte Routen) und komplexes State-Management für Formulare.
*   **Stabilität**: "Set and Forget". Die App läuft oft wochenlang ohne Refresh. Memory Leaks oder instabile Verbindungen sind hier fatal.

### Kerntechnologien
Wir haben uns bewusst für einen modernen Stack entschieden:
*   **Vue 3 (Composition API)**: Der Gamechanger für uns. Anders als bei der Options API können wir Logik (z.B. "Wie hole ich Parkdaten?") perfekt von der UI-Komponente trennen und wiederverwenden.
*   **Tailwind CSS**: Ermöglicht uns extrem schnelles Styling direkt im Markup. Besonders wichtig für den Admin-Bereich, wo wir Konsistenz brauchen, und den Display-Bereich, wo wir pixelgenaue Kontrolle über Layouts benötigen.
*   **Vite**: Das Build-Tool der Wahl. Der Hot-Module-Replacement (HMR) ist so schnell, dass Änderungen quasi in Echtzeit auf dem Screen landen – essentiell für das Feintuning von Animationen.
*   **Docker & Nginx**: Unser Deployment-Standard. Wir bauen ein statisches Artefakt, das von einem leichtgewichtigen Nginx ausgeliefert wird. Das garantiert, dass die App lokal exakt so läuft wie auf dem Produktionsserver.

---

## 2. Architektur: Feature-Driven Design (5 Min)

### Das Problem mit "Technischer Schichtung"
In vielen Frontend-Projekten sieht man Ordner wie `src/components`, `src/services` oder `src/views`. Am Anfang wirkt das ordentlich. Aber sobald das Projekt wächst, entsteht "Spaghetti-Code".
*   Beispiel: Um zu verstehen, wie das "Parking"-Feature funktioniert, muss man in 5 verschiedenen Ordnern suchen.
*   Wenn man das "Parking"-Feature löschen will, muss man chirurgisch Code aus globalen Dateien entfernen.

### Unsere Lösung: Feature-Driven Architecture
Wir strukturieren unseren Code strikt nach **Fachlichkeit (Business Domains)**. Ein Blick in `src/features/` verrät sofort, was die App *tut*, nicht nur woraus sie technisch besteht.

**Verzeichnisstruktur im Detail:** `src/features/`
```plaintext
src/features/
├── parking/
│   ├── components/  # BookingForm.vue (Nur hier verwendet!)
│   ├── admin/       # Admin-spezifische Views
│   └── utils/       # Logik: Was ist ein "Wochenende"?
├── highscore/
│   ├── components/  # DartsCard.vue, KickerCard.vue
│   └── services/    # Eigener API-Service für Highscores
└── community-lunch/ # Das neueste Modul
```

### Vorteile in der Praxis
1.  **Isolation & Sicherheit**: Wenn wir am neuen `community-lunch` Feature arbeiten, können wir zu 100% sicher sein, dass wir das `parking` Modul nicht kaputt machen. Es gibt keine geteilten "Global States", die versehentlich überschrieben werden.
2.  **Team-Skalierung**: Entwickler A baut das Lunch-Feature, Entwickler B refactored das Parking-Admin-Panel. Da sie in komplett getrennten Ordnern arbeiten, gibt es beim Merge fast nie Konflikte.
3.  **Kognitive Last**: Ein neuer Entwickler muss nicht die ganze App verstehen. Wir sagen: "Schau dir den Ordner `features/highscore` an". Das ist alles, was er wissen muss, um einen Bug im Highscore zu fixen.

---

## 3. Deep Dive: DisplayView & Orchestrierung (7 Min)

Kommen wir zum Herzstück der Kiosk-Anwendung: Die `DisplayView.vue`.
Man könnte denken, eine Slideshow ist einfach. Aber eine Slideshow, die *intelligent* Daten lädt und wochenlang stabil läuft, ist komplex.

### A. Dynamisches View-Management
Statt harter `if-else` Blöcke nutzen wir eine Map, um die Komponenten zu verwalten. Das macht das System erweiterbar. 
Wenn wir morgen eine "Wetter-View" bauen wollen, importieren wir sie und fügen sie einfach der `viewComponentsMap` hinzu. 
Das `<component :is="...">` Tag von Vue übernimmt den Rest und tauscht den Inhalt dynamisch aus.

```javascript
const viewComponentsMap = {
  calendar: ThreeWeekCalendar, // Die Kalender-Logik ist hier gekapselt
  parking: ParkingDisplayWrapper,
  highscore: DisplayViewHighscore,
  dashboard: DashboardOverview, // Unser "OneDisplay" Screen
  weather: WeatherDisplay, // Kiosk-View für das Wetter
};
```

### B. Intelligentes Daten-Polling & Date-Detection
Hier wird es smart. Wir laden nicht einfach blind *alle* Daten alle 30 Sekunden neu.
Die `DisplayView` fungiert als Orchestrator. Wenn die Ansicht rotiert (z.B. auf den Kalender), fragt sie die Komponente: "Welchen Zeitraum zeigst du gerade an?".

Warum ist das wichtig? Der Kalender zeigt je nach Wochentag unterschiedliche Zeiträume (Wochenende überspringen etc.). Die View holt sich diese Daten (`getDisplayedDates`) und lädt dann *nur* die Events für genau diesen Zeitraum vom Backend. Das spart Bandbreite und verhindert, dass wir Events laden, die gar nicht sichtbar sind.

```javascript
const refreshDataForView = async (view, silent = false) => {
  // Wir prüfen: Braucht die aktuelle View Kalenderdaten?
  if (view === 'calendar' || view === 'dashboard') {
    await nextTick(); // Warten, bis Vue die Komponente gerendert hat
    
    // Wir greifen auf die Instanz der Kind-Komponente zu
    let calendarInstance = view === 'calendar' ? threeWeekCalendarRef.value : dashboardRef.value?.calendarRef;
    
    let start = null, end = null;
    // Feature Detection: Hat die Komponente die Methode 'getDisplayedDates'?
    if (calendarInstance?.getDisplayedDates) {
      const dates = calendarInstance.getDisplayedDates();
      // Wir laden einen Puffer, damit beim Blättern keine Lücken entstehen
      start = formatDateISO(new Date(new Date(dates[0]).setDate(new Date(dates[0]).getDate() - 30)));
      end = dates[dates.length - 1];
    }
    // Gezielter API-Call
    const events = await calendarApi.getEvents(start, end);
    calendarEvents.value = events; // Reaktivität update UI automatisch
  }
};
```

### C. Resilience & Self-Healing ("Der Watchdog")
Was passiert, wenn der Browser über Nacht ein Update macht oder das Netzwerk kurz weg ist?
Im `onMounted` Hook starten wir unsere "Lebenserhaltungssysteme".
1.  **Config Polling**: Alle 30 Sekunden prüfen wir, ob der Admin die Rotationsgeschwindigkeit geändert hat. Das passiert im Hintergrund, ohne die UI zu blockieren.
2.  **Data Polling**: Unabhängig von der Rotation aktualisieren wir die Daten der *aktuellen* Ansicht. So sieht man z.B. einen neuen Parkplatz-Blocker sofort, auch wenn die View gerade nicht wechselt.

```javascript
onMounted(async () => {
  updateScale(); // Visuelle Anpassung (dazu gleich mehr)
  window.addEventListener('resize', updateScale);
  
  await fetchConfig(); // Initiale Konfiguration laden
  setupRotation(); // Den Rotations-Timer starten
  
  // Die Herzschlag-Funktionen
    configPollInterval = setInterval(fetchConfig, 30000);
  setInterval(() => refreshDataForView(currentViewId.value, true), 30000);
});
```

---

## 4. UI Engine: 4K "Smart Scaling" (4 Min)

Ein riesiges Problem bei Kiosk-Displays ist die Auflösung. Entwickelt wird auf einem Laptop (1920x1080), aber die App läuft auf 4K (3840x2160).
Standard-CSS (`rem`, `em`, Media Queries) stößt hier an Grenzen. Wenn wir Schriftgrößen einfach verdoppeln, zerschießt es oft das Layout komplexer Grids.

### Der "Fit to Screen" Ansatz
Wir haben uns für einen radikalen Ansatz entschieden: Wir bauen die UI *immer* nativ für 4K.
In unserem CSS sind alle Breiten, Höhen und Schriftgrößen fest für 3840x2160 Pixel ausgelegt.
Wenn die App auf einem kleineren Screen läuft (z.B. meinem Laptop hier), "schrumpfen" wir einfach die gesamte App.

### Der Scaler-Algorithmus
Die Mathematik dahinter ist simpel aber effektiv. Wir berechnen das Verhältnis zwischen dem Browserfenster und unserer Zielauflösung (4K). Wir nehmen den kleineren Faktor (Breite oder Höhe), um sicherzustellen, dass immer alles sichtbar ist ("Contain"-Strategie).

```javascript
const REFERENCE_WIDTH = 3840;
const REFERENCE_HEIGHT = 2160;

const updateScale = () => {
  const widthRatio = window.innerWidth / REFERENCE_WIDTH;
  const heightRatio = window.innerHeight / REFERENCE_HEIGHT;
  // Math.min garantiert, dass nichts abgeschnitten wird
  scale.value = Math.min(widthRatio, heightRatio);
};
```

### Die CSS-Implementierung
Das Geniale daran: Wir nutzen CSS Transforms (`scale`). Das ist extrem performant, da es von der GPU (Grafikkarte) berechnet wird. Der Browser muss das Layout (Reflow) nicht neu berechnen, er "zoomt" nur das fertige Bild. Das sorgt für butterweiche Performance, selbst auf schwächerer Hardware.

```html
<div 
  class="origin-center absolute top-1/2 left-1/2"
  :style="{ 
     width: '3840px',   // Wir tun so, als hätten wir immer 4K Platz
     height: '2160px',
     transform: `translate(-50%, -50%) scale(${scale})` // Hier passiert die Magie
  }"
>
  <component :is="currentViewComponent" />
</div>
```

---

## 5. Feature Deep Dive: Community Lunch (5 Min)

Lassen Sie uns kurz das neueste Feature anschauen: **Community Lunch**.
Hier war die UX-Herausforderung: Wie zeigen wir komplexe Abstimmungsergebnisse an, ohne dass die UI einfriert?

### Non-Blocking Async Loading
Wenn wir Kalender, Parkplätze und jetzt auch noch Lunch-Daten laden, darf die UI nicht warten.
Wir nutzen ein "Optimistic UI" Pattern.
1.  Wir laden erst die Liste der Lunch-Events (Metadaten). Das geht schnell.
2.  Wir zeigen sofort "Lunch" im Kalender an.
3.  *Erst danach* laden wir für jedes Event asynchron die Abstimmungsdetails (Wer hat was gewählt?).
4.  Sobald die Daten da sind, aktualisieren wir den Text reaktiv.

Das Ergebnis: Die Seite wirkt sofort "da", Details ploppen Sekundenbruchteile später rein. Das fühlt sich für den Nutzer viel schneller an als ein großer Ladebalken.

```javascript
const fetchLunchData = async () => {
  // 1. Hole alle Termine (schneller Request)
  const events = await lunchService.getCalendarLunches(start, end);
  
  // 2. Platzhalter sofort anzeigen
  lunchData.value = events.map(e => ({ ...e, label: 'Lunch' }));
  
  // 3. Details faul nachladen (Lazy Loading)
  events.forEach(async (e) => {
    const res = await lunchService.getResults(e.id);
    if (res?.results?.[0]) {
      // Reaktives Update: Der Text ändert sich von "Lunch" zu "Lunch: Pizza"
      lunchData.value[e.date] = `Lunch: ${res.results[0].label}`;
    }
  });
};
```

---

## 6. Kommunikation & Deployment (4 Min)

Zum Schluss: Wie kommt der Code auf den Fernseher?

### Docker & Reproduzierbarkeit
Wir verpacken das Frontend in einen Docker-Container. Das Build-Artefakt (HTML/JS/CSS) wird in ein Nginx-Image kopiert.
Warum? Weil es alle "Es läuft aber auf meinem Rechner"-Probleme eliminiert. Der Container auf dem Entwickler-Laptop verhält sich identisch zum Container im Büro-Netzwerk.

### Der Nginx als Reverse Proxy
Wir nutzen Nginx nicht nur als Webserver, sondern auch als Router.
Das Problem: Unsere Frontend-App (Port 80) muss mit dem Backend (Port 8080) sprechen. Browser blockieren das oft wegen CORS (Cross-Origin Resource Sharing).
Die Lösung: Nginx nimmt alle Anfragen an `/api` entgegen und leitet sie *intern* im Docker-Netzwerk an das Backend weiter. Für den Browser sieht es so aus, als käme alles vom selben Server.

```nginx
server {
    listen 80;
    
    # Docker DNS Resolver (damit wir Container-Namen wie 'backend' nutzen können)
    resolver 127.0.0.11 valid=30s;
    
    # API-Calls weiterleiten
    location /api {
        set $upstream_backend backend;
        proxy_pass http://$upstream_backend:8080;
    }
    
    # Single Page Application (SPA) Routing
    location / {
        root /usr/share/nginx/html;
        # WICHTIG: Wenn eine Datei nicht existiert (z.B. /admin/parking),
        # leite immer auf index.html um. Vue Router übernimmt dann.
        try_files $uri $uri/ /index.html;
    }
}
```

---

## Zusammenfassung & Fragen

Wir haben heute gesehen, wie das **itestra Smart Office Dashboard** durch:
1.  **Feature-Driven Architecture** wartbar bleibt,
2.  **Intelligente Orchestrierung** stabil läuft,
3.  **Smart Scaling** auf jedem Display gut aussieht und
4.  **Modernes Tooling** (Docker/Nginx) zuverlässig deployt wird.

Vielen Dank für Ihre Aufmerksamkeit. Ich stehe nun für Fragen zur Verfügung!