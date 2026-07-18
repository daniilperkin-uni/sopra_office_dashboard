# GEMINI.md - technical project Documentation

## 1. Project Overview

The **itestraOfficeDashboard** is a modular, web-based dashboard designed for high-resolution office displays. It provides real-time visibility into office events, parking availability, and competitive highscores.

### Key Components:
*   **Backend**: Java 21 (Spring Boot 3.5.7) with a modular architecture.
    *   Internal Port: `8080`
    *   External Port (Docker): `8099`
*   **Database**: MariaDB with Liquibase migrations.
*   Frontend: Vue.js 3 (Vite, TypeScript, Tailwind CSS) using a **Feature-Driven Architecture**.
    *   Internal Port: `80` (Nginx)
    *   External Port (Docker): `8098`
*   **Deployment**: Docker-compose orchestrated environment.

---

## 2. Directory Structure

```
C:\git-repositories\team_i5\
├───backend\                        # Java Spring Boot Backend (Gradle Multi-Module)
│   ├───backend\                    # Main Application Entrypoint & Orchestration
│   ├───common\                     # Shared utilities
│   ├───module-community-lunches\       # Community Lunch Logic
├───module-config\              # Dynamic Display Configuration Logic
│   ├───module-events\              # Event Management (Odoo Integration)
│   ├───module-highscore\           # Highscore Logic (Darts/Kicker)
│   ├───module-parking\             # Parking Management Logic
│   ├───compose.yaml                # Docker Compose Configuration
│   └───Dockerfile                  # Backend Dockerfile
├───frontend\                       # Vue.js 3 Frontend
│   ├───public\                     # Static Assets (Logos, etc.)
│   ├───src\
│   │   ├───assets\                 # Global Styles (main.css)
│   │   ├───components\             # Shared Components (common UI elements)
│   │   ├───features\
│   │   │   ├───calendar\           # Calendar View Logic
│   │   │   ├───community-lunch\    # Community Lunch Logic
│   │   │   ├───dashboard\          # Dashboard Widgets (OneDisplay Mode)
│   │   │   ├───highscore\          # Highscore View & Admin Logic
│   │   │   └───parking\            # Parking View & Admin Logic
│   │   ├───services\               # API Clients (axios)
│   │   └───views\                  # Main Page Views (Display, Admin)
│   ├───nginx.conf                  # Nginx Configuration
│   └───Dockerfile                  # Frontend Dockerfile
└───GEMINI.md                       # Project Documentation
```

---

## 3. Core Architecture

### 3.1 Backend Modules
The backend is split into logical modules to ensure separation of concerns:
*   **module-events**: Integrates with external systems (Odoo) to track birthdays, anniversaries, and probation ends.
*   **module-parking**: Manages office parking reservations with occupancy tracking.
*   **module-highscore**: Tracks competitive metrics for office activities (Darts, Kicker, etc.).
*   **module-community-lunches**: Manages shared office meals, food catalogs, and voting results.
*   **module-config**: Manages dynamic display settings (rotation speed, active views).

### 3.2 Frontend: Feature-Driven Architecture
The frontend follows a **Feature-Driven Architecture** to improve maintainability:
*   Each feature (`calendar`, `parking`, `highscore`) contains its own components and logic.
*   **Dashboard Feature**: A new `dashboard` feature consolidates widgets for the "OneDisplay" mode.
*   `DisplayView.vue` acts as the main orchestrator, cycling through feature views or displaying the static dashboard based on backend configuration.

---

## 4. Key Features & Implementation Highlights

### 4.1 Dashboard (OneDisplay) - *New!*
*   **Static Overview**: A consolidated "At-a-Glance" view displaying Calendar, Parking, and Highscores simultaneously.
*   **Smart Scaler**: Implemented a global "Fit to Screen" scaling strategy. The dashboard always renders at **3840x2160** (4K) and scales down via CSS transforms to fit any screen size (laptops, monitors) without layout shifts.
*   **Layout**: 
    *   **Equal 3-Column Grid (4-4-4 split)**:
        *   **Left**: Calendar (Today + 2 Days). Current day highlighted with **solid blue background**.
        *   **Middle**: Parking Summary (14-day overview). Today's date highlighted with **blue outline**.
        *   **Right**: Compact Highscore Leaderboards. Top 3 ranks use **Gold, Silver, and Bronze** backgrounds.
*   **Visual Polish**: Consistent corporate identity (CI) headers, high-contrast typography, and 4K-optimized sizing as default.

### 4.2 Advanced Calendar Display
*   **Two-Week View**: Optimized for large screens, showing current and upcoming office events.
*   **Dynamic Overflow Mode**:
    *   **Standard Overflow**: If >2 events occur on a day, the view splits to show a "Today's Focus" list on the right.
    *   **Super Overflow**: If >16 events occur on a day, the view transforms into a **Full-Screen Grid** (2-column layout) to display all events clearly.
*   **Dashboard Widget**: A specialized `DashboardCalendar` component optimized for the OneDisplay layout.

### 4.3 Kiosk Mode (View Rotation)
*   **Automated Cycling**: The dashboard can automatically rotate between the Calendar, Parking, Highscore, and Dashboard views.
*   **Configuration**: Rotation intervals and active views are managed via the **Admin Panel** (`/admin/config`).
*   **Default View**: Now defaults to the static "Dashboard (OneDisplay)" view.

### 4.4 Competitive Highscore System
*   **Unified Overview**: A consolidated view of Darts and Kicker top performers.
*   **Match Recording**: Supports specialized scoring for Darts (points) and Kicker (2vs2 teams).
*   **Compact Widget**: A streamlined version for the Dashboard view with reduced padding and optimized font sizes.

---

## 5. Resolution History (Changelog)

### **Jan 25, 2026 Updates (Scaling & Polish)**
*   **Scaling Strategy (Fit to Screen)**:
    *   Implemented a **Global Resolution Scaler** for the Display View.
    *   The application now wraps the entire Dashboard, Calendar, Parking, and Highscore views in a **3840x2160** container.
    *   Uses CSS transforms to dynamically scale the content down to fit the browser window, ensuring a perfect "4K experience" on laptops and smaller monitors.
    *   Refactored all dashboard components to use 4K text sizes and dimensions by default, removing complex responsive media queries.
*   **Parking Module**:
    *   **Search Feature**: Added a "Search by Name" input to the Parking Admin panel, allowing quick filtering of reservations by employee.
    *   **Visuals**: Highlighting "Today's" date in the OneDisplay Parking summary with a distinct blue outline and rounded border.
*   **Highscore Module**:
    *   **Podium Visuals**: Updated both the Dashboard widget and the full Highscore Display to feature **Gold (#FFD700)**, **Silver (#E0E0E0)**, and **Bronze (#CD7F32)** backgrounds for ranks 1, 2, and 3.
    *   **Text Contrast**: Adjusted text colors (black for Gold/Silver, white for Bronze) to ensure readability.
*   **Calendar Visuals**:
    *   **Current Day**: Highlighting "Today" in the OneDisplay Calendar with a solid corporate blue (`bg-primary`) background and white text.

### **Jan 22, 2026 Updates (Community Lunch & Stability)**
*   **Community Lunch Module**:
    *   **Feature Release**: Implemented full Community Lunch management including event creation, food cataloging, and a collaborative voting system.
    *   **Voting UX**: Re-designed the admin view to support a "one-click" voting system where dishes act as buttons.
    *   **Persistence**: Added a "Save to Catalog" option for new dishes and implemented `localStorage` for user names to streamline repeat voting.
    *   **Calendar Integration**: Updated both `ThreeWeekCalendar` and `DashboardCalendar` to display lunch events with notes and the current top-voted dish (e.g., "Lunch (Note) : Pizza").
*   **Backend & Build Fixes**:
    *   **Compiler Config**: Added `-parameters` flag to all subprojects in `build.gradle` to fix `IllegalArgumentException` in library modules caused by missing parameter names in bytecode.
    *   **CORS Update**: Expanded `allowedMethods` in `CorsConfig` to include `PUT` and `PATCH`, resolving 403 Forbidden errors on voting and status updates.
*   **UI/UX Refinement**:
    *   **Input Styling**: Standardized all lunch admin input fields to use a white background with dark text for better visibility and consistency with the parking module.

### **Jan 21, 2026 Updates (Highscore & Build Fixes)**
*   **Highscore Module**:
    *   **Domain Alignment**: Renamed `points` to `totalThrows` (backend) / `dartsToFinish` (frontend) for Darts entries to better reflect the game mechanics (less is better).
    *   **Visuals**: Updated all labels from "Punkte" to "Würfe" and "Gesamtpunkte" to "Gesamtwürfe".
    *   **Sorting**: Adjusted leaderboard and history sorting for Darts to be ascending (fewest throws first).
    *   **Bulk Delete**: Updated "Delete Non-Top Matches" logic to respect the new sorting order.
*   **Build System**:
    *   **Gradle Configuration**: Disabled `bootJar` task for library modules (`common`, `module-config`, `module-events`, `module-parking`) to prevent build failures, ensuring only the main `backend` application produces an executable JAR.

### **Jan 17, 2026 Updates (Recurring Reservations & Schema Fixes)**
*   **Parking Module (Series Management)**:
    *   **Grouped Series View**: The Parking Admin list now groups recurring reservations, allowing users to expand/collapse series details.
    *   **Delete Series**: Added functionality to delete an entire recurring series (and all future entries) in one click.
    *   **All-or-Nothing Logic**: Recurring reservations now fail atomically if any date in the series is blocked, preventing partial "Swiss cheese" bookings.
    *   **Multi-Day Selection**: The Admin form now supports selecting multiple specific weekdays (e.g., "Mon & Wed") for a series.
    *   **Error Feedback**: Backend now returns detailed error messages listing specific conflicted dates, which are localized and displayed in German on the frontend.
    *   **Weekend Block**: Backend strictly blocks Single Booking creation on weekends (Sat/Sun).
*   **Highscore Module**:
    *   **Schema Migration**: Fixed a critical Liquibase schema mismatch by dropping legacy columns and creating correct `kicker_match_player` tables.
    *   **Variable Team Sizes**: Updated Frontend Admin to support 1v1, 1v2, and 2v1 matches (2nd player optional).
*   **Display & Visuals**:
    *   **Birthday Styling**: Updated OneDisplay Birthday events to use a Green Star icon (distinct from Purple Star for Work Anniversary).
    *   **View Limits**: Configured specific data limits per view:
        *   **OneDisplay Parking**: Shows next **14 days**.
        *   **Parking Display**: Shows next **8 working days**.
    *   **Frontend Stability**: Fixed `TypeError: filter is not a function` crash by adding robust array checks for API responses.

### **Jan 16, 2026 Updates (Doc Alignment & Backend Fixes)**
*   **Documentation Consistency**:
    *   Formally documented the project's coding convention: All source code comments and JSDoc must be in **German**.
*   **Frontend Refactoring**: Refactored all components and utility files in `frontend/src` to include descriptive German comments, following the `BaseButton.vue` standard.
*   **Backend Stability**: 
    *   Fixed critical compilation errors in `module-parking` caused by missing DTOs and Enums.
    *   Restored `DateStatus`, `RecurringParkingEntryRequest`, and `RecurringParkingResult` to ensure successful builds.

### **Jan 12, 2026 Updates (Parking & Admin UX)**
*   **Multi-month Parking Reservations**:
    *   Implemented a new "Series-Reservierung" feature in the Parking Admin panel.
    *   Allows users to book a specific weekday (Mon-Fri) for a duration of **1 to 6 months** in advance.
    *   Automated batch creation of individual entries via the frontend, with a summary report on success/failure counts.
    *   Relaxed the standard 8-day future limit for these series to enable long-term planning.
*   **UI/UX Improvements**:
    *   Dynamic labels for date inputs (Startdatum vs. Datum).
    *   Fixed `DateInput` prop handling for browser-level date restrictions (`minDate`/`maxDate`).

### **Jan 11, 2026 Updates (Dashboard & Polish)**
*   **OneDisplay Dashboard Optimizations**:
    *   **Layout**: Transitioned to an equal **3-column grid** (4-4-4 split) to give more space to Parking and Highscores.
    *   **Calendar Widget**: 
        *   Renamed header from "Agenda" to "**KALENDER**".
        *   Synchronized event formatting with the main calendar (using anonymized short names and corrected grammar).
    *   **Parking Widget**:
        *   Extended view to **14 working days** (previously 10).
        *   Enhanced UI with larger text, distinct progress bar outlines, and a **light blue highlight** for the current day.
        *   Updated color logic: 1-2 (Green), 3 (Yellow), 4 (Orange), 5 (Red).
    *   **Highscore Widget**: Increased player names and scores by ~30% for improved 4K visibility.
*   **Display Configuration**:
    *   Implemented "**Automatische Ansichtsrotation ohne OneDisplay aktivieren**" (Skip OneDisplay in rotation).
    *   Added backend support for `skipOneDisplayInRotation` in `DisplayConfig`.
*   **Full Calendar Polish**:
    *   Increased all text sizes by ~5% for better readability.
    *   Implemented "Shrink-to-fit" rows (empty days take less vertical space).
    *   Added light blue highlight for the current day square.
*   **Bug Fixes**:
    *   Fixed `ReferenceError: subtitle is not defined` in `eventUtils.js`.
    *   Fixed progress bar color flickering by restricting transitions to the `width` property.

### **Jan 10, 2026 Updates (Dashboard Refactoring)**
*   **OneDisplay Dashboard**:
    *   Implemented a new static dashboard view (`DashboardOverview.vue`) integrating all key information.
    *   **Layout**: 3-column split (Calendar 50%, Parking 25%, Highscore 25%).
    *   **Widgets**:
        *   **Calendar**: New `DashboardCalendar` component showing Today + 2 Days in a horizontal row layout. Improved event formatting (Title/Subtitle) and fixed raw ID display.
        *   **Parking**: New `ParkingSummary` component with visual progress bars (Green/Yellow/Orange/Red) and right-aligned occupancy text.
        *   **Highscore**: Compact leaderboards for Dart and Kicker (Top 3), removing match history to save space.
*   **Configuration**:
    *   Added "Dashboard (OneDisplay)" as a selectable default view in the Admin Config panel.
    *   Updated `DisplayView.vue` to support the dashboard as a 4th slide and default view.
*   **Visual Enhancements**:
    *   **Headers**: Standardized all widget headers to Uppercase, Bold, Corporate Blue (#009ee2), Left-Aligned with a separator.
    *   **Typography**: Significantly increased font sizes for 4K readability across all dashboard widgets.
    *   **Colors**: Refined Parking progress bar colors and Calendar day background logic.

### **Jan 07, 2026 Updates**
*   **Highscore Optimization**:
    *   **Leaderboard Limit**: Restricted the Darts and Kicker leaderboards to display only the **Top 3** entries (previously Top 5) for a more focused view.
    *   **Visual Enhancements**: Increased font sizes for player names and totals by 100% and point values by 30% on leaderboard cards to improve readability on large displays.
    *   **Admin Tools**: Added a bulk delete function to the Highscore Admin to clean up "non-top" matches (keeping only the top 3 performances).
*   **Corporate Identity & UI Refinement**:
    *   **Unified Color Scheme**: Replaced all instances of various blue shades with the specific corporate color `#009ee2`.
    *   **Kicker Branding**: Updated the Kicker section in the Admin panel to align with the new blue color scheme (previously green).
    *   **Admin UX**: Standardized input fields in Highscore and Config admin panels to use a white background for better contrast and consistency.

### **Jan 03, 2026 Updates (Revision)**
*   Documentation Correction**: Verified and updated port mappings in documentation to match `compose.yaml`.
    *   Backend: `8099:8080`
    *   Frontend: `8098:80`
*   **Database Persistence**: Confirmed `spring.jpa.hibernate.ddl-auto` is set to `update` to prevent data loss on restart.
*   **Health Checks**: Confirmed MariaDB health check uses standard TCP check (Note: `mysqladmin` check is not currently implemented).

---

## 6. Developer Handover & Operation

### 6.1 Local Development
1.  **Backend**: Run `./gradlew bootRun` from the `backend` folder.
2.  **Frontend**: Run `npm install` and `npm run dev` from the `frontend` folder.
3.  **Docker**: Use `docker-compose -f backend/compose.yaml up --build` for a full system spin-up.

### 6.2 Troubleshooting
*   **Database**: `spring.jpa.hibernate.ddl-auto` in `application.properties` is set to `update`.
*   **Frontend Access**: The application is available at `http://localhost:8098` (Docker) or via the Vite dev server port (usually `http://localhost:5173`) when running locally.
*   **Backend Access**: API is available at `http://localhost:8099` (Docker).