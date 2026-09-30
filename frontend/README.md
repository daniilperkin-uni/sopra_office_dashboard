# Frontend Application

This is the frontend application for the Office Dashboard, built with **Vue.js 3** and **Vite**, utilizing a **Feature-Driven Architecture**.

## Running the Application

To run the frontend application locally:

1.  **Install Dependencies:**
    ```bash
    npm install
    ```
2.  **Start Development Server:**
    ```bash
    npm run dev
    ```
    The application will be accessible at `http://localhost:3000`.

## Display View (Kiosk Mode)

The main display is located at `/display`. It is designed for high-resolution screens (4K) but includes a **Smart Scaler** that automatically fits the content to any screen size (laptops, monitors) while preserving the 4K layout.

*   **Configuration**: The display rotation and view settings are now managed dynamically via the **Admin Panel**.
    *   Navigate to `/admin/config` to toggle rotation, set the interval, and choose the default view.
    *   Changes are saved to the backend and automatically picked up by the Display View (polling every 30s).

### Available Views
1.  **Dashboard (OneDisplay)**: An all-in-one view with a 4-4-4 column split.
    *   **Calendar**: 3-day agenda (today + 2). Current day is highlighted with a **solid blue background**.
    *   **Parking**: **14-day occupancy overview** with color-coded progress bars. Today's date has a distinct **blue outline**.
    *   **Highscore**: Compact Dart & Kicker leaderboards. Top 3 ranks feature **Gold, Silver, and Bronze** backgrounds.
2.  **Calendar**: Full-screen 2-week outlook with "Today's Focus" overflow modes.
3.  **Parking**: Full-screen current week's parking occupancy (**8 working days**).
4.  **Highscore**: Full-screen Darts & Kicker Top 3 leaderboards + Match History.
5.  **Weather**: Full-screen weather forecast (display view id `weather`) fetched from Open-Meteo without an API key; the location is hardcoded in `WeatherDisplay.vue` (Stuttgart).

## Admin Panels

The application provides dedicated admin interfaces for managing data. All admin routes require the session login (`/login`); the API is CSRF-protected via the `XSRF-TOKEN` cookie (Axios sends it as the `X-XSRF-TOKEN` header automatically).

*   **Overview**: Navigate to `/admin` for the main admin landing page.
*   **Parking Admin** (`/admin/parking`): 
    *   Manage parking reservations (single days and recurring series); occupancy capacity is served by the backend (`maximumParkingSpots`).
    *   **Search by Name**: Filter reservations by employee name and date.
    *   **Series Management**: Recurring reservations are now grouped by series in the list.
        *   **Expand/Collapse**: View individual dates within a series.
        *   **Delete Series**: Remove an entire recurring reservation and all its future entries with one click.
    *   **Multi-day Selection**: Create recurring bookings for multiple specific weekdays (e.g., "Mon & Wed") for up to 6 months.
*   **Highscore Admin** (`/admin/highscores`):
    *   Add new Darts (Würfe / `totalThrows`) and Kicker (2vs2, 1v1, etc.) match results.
    *   View detailed match history.
    *   **Delete** incorrect entries directly from the history list.
    *   **Bulk Delete**: Option to clean up history by removing all "non-top" matches (keeping only the top 3).
*   **Display Config** (`/admin/config`):
    *   **Centralized Control**: Toggle rotation, set interval, and select default view.
    *   **Selective Rotation**: Option to exclude the "OneDisplay" view from the rotation cycle.
*   **Community Lunch Admin** (`/admin/lunch`):
    *   **Event Management**: Create and manage office lunch events.
    *   **Food Catalog**: Maintain a reusable list of dishes.
    *   **Voting System**: Click-to-vote interface with name persistence (localStorage).
    *   **Calendar Integration**: Winning dishes and event notes are automatically displayed in the office calendar.

## Project Structure

The codebase follows a **Feature-Driven Architecture** to ensure modularity and maintainability:

-   `src/features/`: Contains all domain-specific logic and components.
    -   `calendar/`: Event display and logic.
    -   `parking/`: Reservation system and occupancy views.
    -   `highscore/`: Leaderboards, admin forms, and match history.
    -   `community-lunch/`: Lunch event management and voting.
    -   `dashboard/`: Shared dashboard widgets — contains only `components/`; the Display Config admin lives in `src/views/DisplayConfigAdminView.vue`.
    -   `game/`: Easter-egg game (FallingCats).
-   `src/components/common/`: Generic UI primitives (Buttons, Cards, Spinners).
-   `src/views/`: Top-level page layouts — `DisplayView.vue` (kiosk), `LoginView.vue`, `AdminView.vue` plus the admin sections (`ParkingAdminSection`, `HighscoreAdminView`, `LunchAdminSection`, `DisplayConfigAdminView`).
-   `src/services/`: API clients (`api.js`, `authService.js`, `lunchService.js`).
-   `src/stores/`, `src/composables/`, `src/utils/`, `src/router/`: Pinia stores, reusable composition logic (view rotation, display scaler), date/event helpers and the routes; the Vitest suites live in `tests/` and next to pure functions in `src/`.