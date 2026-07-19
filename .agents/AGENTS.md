# Agent Guidelines for itestraOfficeDashboard

This file contains rules and guidelines for AI agents working on the `itestraOfficeDashboard` project.

## 1. Tech Stack
- **Backend**: Java 21, Spring Boot 3.5.7, MariaDB, Liquibase, Gradle Multi-Module (in `backend/`).
- **Frontend**: Vue.js 3, Vite, Tailwind CSS, Pinia, Vue Router (in `frontend/`).
- **Deployment**: Docker Compose.

## 2. Coding Conventions & Language
- **Language Requirements**: All source code comments and JSDoc MUST be written in **German**. This is a strict and documented project requirement.
- **Frontend Architecture**: Follow the existing **Feature-Driven Architecture** (`frontend/src/features/`). Each feature (`calendar`, `parking`, `highscore`, `community-lunch`, `dashboard`) should encapsulate its own components and logic. Use generic components from `src/components/common/`.
- **Backend Architecture**: The backend uses a modular approach (`module-events`, `module-parking`, etc.). When adding new modules, ensure they are properly registered in `settings.gradle`, `backend/build.gradle`, and the master Liquibase changelog.

## 3. UI/UX Rules
- **4K Display Mode**: The Display view (`/display`) is optimized for 4K (3840x2160) screens. It uses a global "Smart Scaler" (CSS transforms) to fit smaller screens. Always design UI components to scale well in 4K by default rather than using complex responsive media queries.
- **Corporate Identity**: The primary corporate identity (CI) color is `#009ee2`. Use this for branding and primary highlights.
- **Aesthetics**: Follow the modern, premium aesthetic guidelines. Ensure high-contrast typography for large screen readability.

## 4. Testing & Running
- **Frontend Local Dev**: Runs on `http://localhost:3000` via `npm run dev`. Proxy is configured for `/api` to route to the backend.
- **Backend Local Dev**: Runs on port `8080` (or `9000` via `application-dev.properties`). Use `./gradlew bootRun --args='--spring.profiles.active=dev'`.

## 5. Documentation
- When creating new features, always update `README.md`, `frontend/README.md`, and `GEMINI.md` to reflect the changes.
- **GEMINI.md** acts as a technical changelog and architectural document. Keep its resolution history updated.
