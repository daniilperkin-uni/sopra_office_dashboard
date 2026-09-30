# itestraOfficeDashboard

[![CI](https://github.com/daniilperkin-uni/sopra_office_dashboard/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/daniilperkin-uni/sopra_office_dashboard/actions/workflows/ci.yml)

## Description

The **itestraOfficeDashboard** is a modular office management platform designed for displaying information on large 4K screens in kiosk mode with configurable view rotation. The dashboard aggregates and visualizes office information such as parking reservations, employee events, game statistics, and community lunch planning.

The system consists of two main interfaces:
- **Display Mode** (`/display`): Optimized for 4K screens, rotates between different modules (when enabled in configuration) to provide an overview of current office information
- **Admin UI** (`/admin/*`): A responsive administrative interface for managing data, accessible from desktop computers as well as mobile devices

**Key Features:**
- 🚗 Parking spot reservation management
- 📅 Calendar overview including employee birthdays, end of probation period and work anniversaries (via Odoo integration) and community lunches (via Admin UI) 
- 💬 Automated daily Mattermost notifications for upcoming employee events (birthdays, end of probation period, work anniversaries); community lunch events are announced as soon as they are created

**Additional Features:**
- 🎯 Highscore for Darts & Kicker and leaderboard 
- 🍽️ Community lunch event management with voting system
- ⚙️ Dashboard display configuration and rotation settings
- 🌦️ Weather kiosk view (Open-Meteo, display view id `weather`, `frontend/src/features/dashboard/components/WeatherDisplay.vue`)

For detailed module documentation, see [Modules Overview](#modules-overview).

## Architecture overview

- **Backend** (`backend/`): Spring Boot 4.1.1 multi-module Gradle build (Java 21, wrapper 9.7.1). `backend` is the runnable app (security, Liquibase master changelog); `module-*` hold the features (parking, events, highscore, community lunches, config) and `common` shared code. MariaDB in production, H2 in tests.
- **Frontend** (`frontend/`): Vue 3 + Vite + Tailwind SPA (Node 22) with the kiosk display (`/display`) and the admin UI (`/admin/*`); talks to the backend via `/api` (session login, CSRF token from the `XSRF-TOKEN` cookie).
- **Deployment**: Docker Compose (`backend/compose.yaml`); CI runs Gradle build and frontend lint/test/build on every push.

## My contribution

This was a university team project (SoPra). My roles and work are listed in [Team.md](Team.md).

## Table of Contents

- [Description](#description)
- [Architecture overview](#architecture-overview)
- [My contribution](#my-contribution)
- [Install using docker compose](#install-using-docker-compose)
- [Local setup](#local-setup)
  - [Prerequisites](#prerequisites)
  - [For Linux](#for-linux)
  - [For Windows and MacOS](#for-windows-and-macos)
  - [Open API specifications (Swagger-ui)](#open-api-specifications-swagger-ui)
- [Configuration](#configuration)
- [Adding modules](#adding-modules)
  - [Creating the backend](#creating-the-backend)
  - [File Structure](#file-structure)
  - [Add to `./settings.gradle`](#add-to-settingsgradle)
  - [Add to `./backend/build.gradle`](#add-to-backendbuildgradle)
  - [Optional: Use liquibase](#optional-use-liquibase)
- [Test](#test)
  - [For Linux/Mac](#for-linuxmac)
  - [For Windows](#for-windows)
- [Modules Overview](#modules-overview)
  - [module-parking](#module-parking)
  - [module-events](#module-events)
  - [module-highscore](#module-highscore)
  - [module-community-lunches](#module-community-lunches)
  - [Mattermost integration](#mattermost-integration)
  - [module-config](#module-config)
- [Troubleshooting](#troubleshooting)
- [More information](#more-information)
## Install using docker compose

```bash
$ git clone https://github.com/daniilperkin-uni/sopra_office_dashboard.git
$ cd sopra_office_dashboard/backend
$ cp .env.example .env
$ docker compose up -d # or
$ docker compose up -d --build # to force a build after applying changes
```

## Local setup

### Prerequisites

- Java Development Kit (JDK) 21
- Node.js 22 LTS
- MariaDB Server

### For Linux

```bash
$ git clone https://github.com/daniilperkin-uni/sopra_office_dashboard.git
# start the backend
$ cd sopra_office_dashboard/backend
$ touch backend/src/main/resources/application-dev.properties # and edit to your needs (example below)
$ ./gradlew bootRun --args='--spring.profiles.active=dev'
# start the frontend
$ cd ../frontend
$ npm install # only needed for the first time or after changes in the package.json
$ npm run dev
```

The Vite dev server runs on port `3000` by default (see `vite.config.js`).
The application will be accessible at `http://localhost:3000`.

Here is an example for the `backend/src/main/resources/application-dev.properties` file:

```
# backend/src/main/resources/application-dev.properties
server.port=9000

# MariaDB Datasource
spring.datasource.url=jdbc:mariadb://localhost:3306/itestraOfficeDashboard
spring.datasource.username=root
spring.datasource.password=test
```

### For Windows and MacOS
If you're using MacOS or Windows, we warmly invite you to experience the freedom and benefits of open-source operating systems. Consider these approachable options:

- **Windows Subsystem for Linux (WSL2)**: Run a full Linux environment alongside Windows—it's surprisingly straightforward and allows you to use this project seamlessly
- **Linux Distributions**: Ubuntu, Fedora, and Linux Mint are user-friendly entry points into open-source computing
- **Docker**: Containerize the project for use in Linux environments
- **Virtual Machine**: Try a Linux VM using free tools like VirtualBox to explore open-source software risk-free

We're confident that once you experience the openness, transparency, and community spirit of FOSS, you'll understand why we've made this choice.
So better you install Linux and continue with [For Linux](#for-linux)

### Open API specifications (Swagger-ui)

Without any configuration changes you can reach swagger-ui for the backend under the following link: 

http://localhost:8080/swagger-ui.html

The OpenAPI spec is generated from the code by springdoc (`/v3/api-docs`); there is no hand-maintained spec file.

## Configuration

To configure the dashboard please look at
- `./backend/.env.example` (docker compose configuration)
- `./backend/backend/src/main/resources/application.properties` (backend configuration)

**Note:** During development the anonymized employee names are very long. `VITE_ANONYMIZE_NAMES=true` reduces them to their last 10 characters for better readability on the dashboard; it defaults to `true` in dev when unset and to `false` in production builds, and can be overridden via `frontend/.env` or the `VITE_ANONYMIZE_NAMES` build arg of the compose frontend service.

**Secrets:** Database, Odoo and Mattermost credentials are read from environment variables (see `backend/.env.example` and `application.properties`); none are committed to the repository. The Odoo admin password should be rotated as a manual operation.

## Adding modules

You're warmly welcome to add new modules by your own. To enhance and extend our features.
Here is a short description what you'll have todo

### Creating the backend

Create a new Folder under `./backend/`. It's recommended to follow the naming scheme and folder-structure.
We use JPA and MariaDB as persistence layer. Modules can utilize from liquibase integration.

#### File Structure

For example if you want to implement a new module to manage community lunches:
```text
module-lunch
├── src
│   └── main
│       ├── java
│       │   └── de.itestra.lunch
│       │       ├── controller
│       │       ├── entity
│       │       ├── repository
│       │       └── service
│       └── resources
│           └── db.changelog.lunch
│               └── db.changelog-lunch-master.yaml
└── build.gradle
```

#### Add to `./settings.gradle`

We have to extend our `settings.gadle` to bring our module into scope:

```gradle
// ./settings.gradle

rootProject.name = 'backend'

// here are some other includes for modules

include 'module-lunch' // includes the lunch module

// maybe some other content
```

#### Add to `./backend/build.gradle`

In this file you'll have to add the new module so gradle knows it and is able for example to load the specific dependencies. Add

```gradle
// ./backend/build.gradle

dependencies {
    // here are some other modules
    implementation project(':module-lunch') // includes the lunch module
}
```

#### Optional: Use liquibase

To use Liquibase you have to add your module specific changelog-master-file to the overall master in `./backend/src/main/resources/db/changelog/db.changelog-master.yaml`

For our lunch example this would be:

```yaml
# ./backend/src/main/resources/db/changelog/db.changelog-master.yaml

databaseChangeLog:
# some other includes from other modules
  - include:
      file: classpath:db/changelog/lunch/db.changelog-lunch-master.yaml # include the lunch module
```
And in the `./module-lunch/src/main/resources/db/changelog/lunch/db.changelog-lunch-master.yaml` you have to specify the migrations. Use the first three digits in the migration script as a sequence number. The order of the includes is important, so take care to stick with the sequence:

```yaml
# ./module-lunch/src/main/resources/db/changelog/lunch/db.changelog-lunch-master.yaml

databaseChangeLog:
  - include:
      file: classpath:/db/changelog/lunch/001-create-lunch-events-table.sql
  - include:
      file: classpath:/db/changelog/lunch/002-another-migration.sql
```

The migration in itself consist of SQL statements.
Please keep in mind that that liquibase uses the first two commets as header, so don't forget to add them. Also add the `--rollback` comment to mark the rollback command.
In the [official Dokumentation](https://docs.liquibase.com/oss/user-guide-4-33/sql-changelog-example) you'll find additional information.

Here is an example for our first migration:

```sql
-- ./module-lunch/src/main/resources/db/changelog/lunch/001-create-lunch-events-table.sql

--liquibase formatted sql
--changeset your-name:001

CREATE TABLE lunch_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    eventName VARCHAR(255) NOT NULL,
    lunchTime DATETIME NOT NULL
);

--rollback DROP TABLE lunch_events;

```

## Test

### For Linux/Mac

#### Execute tests / full build:

```
./gradlew build
```

`./gradlew build` compiles, runs Checkstyle and the tests and assembles the deployable boot jar — this is what CI runs.
The backend suite is currently 93 tests in 7 modules (91 run, 2 skipped: the `@Disabled` `OdooClientApiTest`).

#### Open test report for module-parking

```
xdg-open module-parking/build/reports/tests/test/index.html
```

#### Open test report for module-events

```
xdg-open module-events/build/reports/tests/test/index.html
```

### For Windows

#### Execute tests:

```
gradlew.bat test
```

#### Open test report for module-parking

```
start module-parking\build\reports\tests\test\index.html
```

#### Open test report for module-events

```
start module-events\build\reports\tests\test\index.html
```

### Frontend

Run from `frontend/` with Node.js 22 (`package.json` requires `node >=22`):

```
npm run lint          # ESLint
npm run format:check  # Prettier check
npm run test          # vitest run — 10 test files / 63 tests
npm run build         # vue-tsc + vite build
```

## Modules Overview

### module-parking

The `module-parking` provides functionality for managing employee parking space reservations. Employees can manually register the days they plan to arrive by car, ensuring efficient allocation of available parking spots.

Key features include:

*   **Manual Reservations:** Employees can create, update, and delete their parking reservations for specific dates.
*   **Multi-Day Overview:** Displays reservations for the upcoming work days; the display renders 8 days (`ParkingDisplayWrapper` slices `weekData` to 8).
*   **Administrative Interface:** Provides an admin view (`/admin/parking`) for managing parking reservations.
*   **Display Integration:** Parking overview is integrated into the auto-rotating dashboard display (`/display`) for large screens.
*   **Analytics:** Aggregated parking statistics (daily occupancy, bookings per weekday, busiest day of the month) via `GET /api/parking/analytics`.

The module exposes parking data via the `/api/parking` endpoint and provides a user-friendly interface for day-to-day parking management. This helps teams coordinate parking space usage and avoid conflicts, especially in offices with limited parking availability.

### module-events

The `module-events` provides functionality for aggregating and serving upcoming company events. It integrates with the Odoo system to fetch relevant data (employees and contracts) and then processes this information to identify:

*   **Birthdays:** Upcoming birthdays of employees.
*   **Work Anniversaries:** Milestones for employee work anniversaries.
*   **Probation Endings:** Dates when employee probation periods conclude.

These events are exposed via the `/api/dashboard-events` endpoint, which returns a list of events scheduled to occur within the next 14 days. To optimize performance and reduce load on the Odoo system, the module implements:

*   **Event Caching:** Fetched events are cached internally.
*   **Scheduled Refresh:** Events are automatically refreshed daily in the background to ensure data freshness.


### module-highscore

The `module-highscore` provides game statistics and leaderboard management system. It tracks performance across two popular office games: Darts and Kicker.

Key features include:

* **Darts Statistics:** Tracks darts performances by recording how many darts a player needed to finish a game from a given starting score (e.g. 310 points). Lower dart counts indicate better performance and are ranked higher in the leaderboard.
* **Kicker Statistics:** Track kicker matches where each team consists of 1-2 players. Matches are recorded with the players and which team won. Team standings are calculated based on total wins.
* **Unified Leaderboards:** Display top 3 players/teams for both games
* **Match History:** View recent matches across both games with results and timestamps
* **Administrative Interface:** Provides an admin view (`/admin/highscores`) for managing leaderboard.
* **ELO Rankings:** Darts players are additionally ranked by an ELO rating, exposed via `GET /api/elo-rankings` and rendered by `frontend/src/features/highscore/components/EloRankingsTable.vue`.

### module-community-lunches

The `module-community-lunches` provides a community lunch event management system that enables teams to organize, vote on, and coordinate company lunch events.

Key features include:

* **Event Management:** Create and manage lunch events with dates and notes (and location in backend)
* **Event Lifecycle:** Three-stage workflow (DRAFT → OPEN → CLOSED) controlling when employees can vote
* **Meal Options:** Add options from a reusable food catalog or create custom meal labels for specific events
* **Voting System:** Employees can submit or update their meal choice for open events (one choice per employee per event)
* **Results Aggregation:** View voting results with counts per meal option, sorted by popularity
* **Food Catalog:** Maintain a reusable database of meal options with soft-delete support
* **Mattermost Integration:** Sends automated notifications in German when new events are created with meal options
* **Administrative Interface:** Provides admin view (`/admin/lunch`) for managing lunch events

The module exposes comprehensive REST APIs at `/api/community-lunches` for event and voting management, and `/api/food-catalog` for catalog administration. It uses Liquibase for database migrations and implements proper validation to ensure data integrity, helping teams coordinate lunch events efficiently.

### Mattermost integration

The Mattermost integration has no Gradle module of its own: the client and sender live in `common` (`MattermostClient`, `NotificationSender`), the daily event digest runs in `module-events` (`EventNotificationScheduler`), and `module-community-lunches` announces new lunch events through the same sender.

Key features include:

* **Automated Daily Reminders:** Sends notifications every day at 12:00 noon (cron default; server time — UTC in the Docker image) about upcoming employee events
* **Event Integration:** Fetches cached events (birthdays, work anniversaries, probation endings) from the events module
* **Next-Day Preview:** Notifies team members about events happening the next day
* **Creation Notifications:** Community lunch events are announced (German) as soon as they are created with meal options
* **Mattermost API Integration:** Uses Mattermost API v4 for posting messages to configured channels
* **German Localization:** Event descriptions are formatted in German for the target audience

It runs as a scheduled background service (`notifications.events.enabled` / `notifications.events.schedule.cron`) and reads the Mattermost API URL, token and channel ID from environment variables.

### module-config

The `module-config` provides centralized configuration management for dashboard display settings, controlling how views are presented on large screen displays.

Key features include:

* **Rotation Control:** Enable or disable automatic rotation between different dashboard views
* **Rotation Interval Management:** Configure the time interval (minimum 5 seconds) between view changes
* **View Selection:** Choose which view to display when rotation is disabled (Dashboard, Calendar, Parking, Highscore, Game; a rotation interval of `-1` boots straight into the FallingCats game view)
* **Skip Display Option:** Optionally skip the OneDisplay (dashboard) view during automatic rotation cycles
* **Administrative Interface:** Provides admin view (`/admin/config`) with German-language UI for easy configuration

The module exposes configuration via the `/api/config/display` endpoint.

## Troubleshooting

### Using docker compose my backend is not able to connect to the database

- Ensure you did not change the database connection in the `.env` file without rebuilding the container
- Try running `docker compose down -v` to remove the volume

## More information
For detailed frontend documentation, see [frontend/README.md](frontend/README.md). The (German) script of the final frontend presentation is kept in [docs/presentation.md](docs/presentation.md)
