# Vlasje24

Een Nederlandse muziekchartswebsite. Wekelijks worden 24 nummers ingevoerd via een admin-panel; bezoekers kunnen de hitlijst bekijken, door de geschiedenis navigeren, artiesten en nummers opzoeken en statistieken bekijken.

Plan voor de komende gebruikersfunctionaliteit: [gebruikersaccounts en reacties](docs/accounts-en-reacties-plan.md).

## Inhoudsopgave

- [Architectuur](#architectuur)
- [Vereisten](#vereisten)
- [Lokaal draaien](#lokaal-draaien)
- [Productie deployen](#productie-deployen)
- [Admin-account aanmaken](#admin-account-aanmaken)
- [Omgevingsvariabelen](#omgevingsvariabelen)
- [API-overzicht](#api-overzicht)
- [Databaseschema](#databaseschema)
- [Databasemigraties](#databasemigraties)
- [Tests draaien](#tests-draaien)

---

## Architectuur

```
Browser
  │
  ├─ HTTPS ──► Caddy (TLS-terminatie)
  │                │
  │                ▼
  │            nginx (frontend)
  │              │       │
  │         React SPA   /api/* ──► Spring Boot (backend)
  │                                     │
  │                               MySQL (database)
  │
  └─ pma.domein.nl ──► Caddy ──► phpMyAdmin
```

| Laag | Technologie | Poort (dev) |
|---|---|---|
| Frontend | React 18 + Vite + TailwindCSS | 80 (nginx) / 5173 (Vite dev) |
| Backend | Spring Boot 3.4 / Java 21 | 8080 |
| Database | MySQL 8.0 | 3306 |
| Reverse proxy | Caddy v2 (productie) | 80 / 443 |
| DB-beheer | phpMyAdmin | 8081 |

De frontend maakt alle API-calls naar het pad `/api/v1/...` (relatief). In productie proxyt nginx dit naar de backend-container; in Vite-dev-mode handelt de Vite-proxy (`vite.config.ts`) dit af.

---

## Vereisten

- Docker Desktop (of Docker Engine + Compose v2)
- Git

Voor native ontwikkeling (optioneel):
- Java 21
- Maven 3.9+
- Node.js 20+

---

## Lokaal draaien

### 1. Eerste keer instellen

```bash
git clone <repo-url>
cd Vlasje24-Website
cp .env.example .env
```

Vul `.env` in (zie [Omgevingsvariabelen](#omgevingsvariabelen)). Voor lokaal ontwikkelen volstaan willekeurige waarden.

### 2. Starten met Docker (aanbevolen)

```bash
docker compose up --build
```

`docker-compose.override.yml` wordt automatisch meegenomen. Dit bouwt beide images lokaal en start alle services.

| Service | URL |
|---|---|
| Website | http://localhost |
| Backend API | http://localhost:8080/api/v1 |
| phpMyAdmin | http://localhost:8081 |

### 3. Starten zonder Docker (native)

Terminal 1 — backend:
```bash
cd backend
mvn spring-boot:run
```

Terminal 2 — frontend:
```bash
cd frontend
npm install
npm run dev
```

Website beschikbaar op http://localhost:5173. De Vite-proxy stuurt `/api/*` door naar `localhost:8080`.

> **Let op:** Bij native backend-start verbindt de app met `localhost:3306`. Zorg dat MySQL draait (bijv. via `docker compose up db`).

---

## Productie deployen

### Vereisten op de server
- Docker + Compose v2
- Poorten 80 en 443 open in de firewall
- DNS A-records:
  - `jouwdomein.nl` → server-IP
  - `pma.jouwdomein.nl` → server-IP (voor phpMyAdmin via HTTPS)

### Stap 1 — Images bouwen en pushen (lokaal)

```bash
docker compose -f docker-compose.yml -f docker-compose.build.yml build
docker compose -f docker-compose.yml -f docker-compose.build.yml push
```

### Stap 2 — Server inrichten (eenmalig)

```bash
# Op de server: kopieer .env.example naar .env en vul in
cp .env.example .env
nano .env
```

### Stap 3 — Opstarten op de server

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

Caddy haalt automatisch Let's Encrypt-certificaten op. Wacht een minuut en bezoek dan `https://jouwdomein.nl`.

### Updaten na een codewijziging

```bash
# Lokaal: nieuwe images bouwen en pushen
docker compose -f docker-compose.yml -f docker-compose.build.yml build
docker compose -f docker-compose.yml -f docker-compose.build.yml push

# Op de server: nieuwe images ophalen en herstarten
docker compose -f docker-compose.yml -f docker-compose.prod.yml pull
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

---

## Admin-account aanmaken

Er is nog geen registratie-UI. Een adminaccount wordt direct in de database aangemaakt. Het wachtwoord moet als BCrypt-hash worden opgeslagen.

### Hash genereren

**Via de terminal (Python):**
```bash
python3 -c "import bcrypt; print(bcrypt.hashpw(b'jouwwachtwoord', bcrypt.gensalt(10)).decode())"
# vereist: pip install bcrypt
```

**Via htpasswd:**
```bash
htpasswd -bnBC 10 "" jouwwachtwoord | tr -d ':\n'
```

### Gebruiker invoegen (via phpMyAdmin of MySQL CLI)

```sql
INSERT INTO user (username, password, email, email_verified_at, role, status)
VALUES (
  'admin',
  '$2a$10$...hier-de-gegenereerde-hash...',
  'admin@example.nl',
  UTC_TIMESTAMP(6),
  'ADMIN',
  'ACTIVE'
);
```

> De hash begint altijd met `$2a$10$` en is 60 tekens lang.

---

## Omgevingsvariabelen

Kopieer `.env.example` naar `.env` en vul alle waarden in.

| Variabele | Beschrijving |
|---|---|
| `MYSQL_ROOT_PASSWORD` | MySQL root-wachtwoord (alleen voor DB-initialisatie) |
| `MYSQL_APP_PASSWORD` | Wachtwoord voor de app-gebruiker `vlasje24_app` |
| `FLYWAY_BASELINE_ON_MIGRATE` | Alleen eenmalig `true` bij de eerste Flyway-deploy op een bestaande database; normaal `false` |
| `SESSION_TIMEOUT` | Geldigheidsduur van een inactieve sessie; standaard `14d` |
| `SESSION_COOKIE_SECURE` | In productie `true`; lokale Docker-ontwikkeling zet dit op `false` voor HTTP |
| `CORS_ALLOWED_ORIGINS` | Expliciete, kommagescheiden frontend-origins die cookies mogen meesturen |
| `DOMAIN` | Productiedomein zonder `https://` (bijv. `vlasje24.nl`) |
| `DOCKER_USERNAME` | Docker Hub gebruikersnaam (voor push/pull) |

---

## API-overzicht

Alle endpoints beginnen met `/api/v1`. Publieke data is zonder account leesbaar. Authenticatie gebruikt een server-side sessie in MySQL en een Secure/HttpOnly/SameSite=Lax-cookie. De frontend haalt via `GET /auth/csrf` een CSRF-token op en stuurt dat bij mutaties als `X-XSRF-TOKEN`; de meegeleverde API-client handelt dit automatisch af.

### Publieke endpoints

| Method | Pad | Omschrijving |
|---|---|---|
| GET | `/charts/latest` | Meest recente chart |
| GET | `/charts/{weekId}` | Specifieke chartweek |
| GET | `/charts/years` | Lijst van jaren met chartdata |
| GET | `/songs/{songId}` | Nummerdetails + charthistorie |
| GET | `/songs/top?sort=points&page=0` | Top-nummers (sort: `points`, `weeks`, `position`) |
| GET | `/artists/{artistId}` | Artiestdetails + nummers |
| GET | `/artists/top?sort=points&page=0` | Top-artiesten (sort: `points`, `hits`) |
| GET | `/search?q=query` | Zoek nummers + artiesten (max 5 elk) |
| GET | `/auth/csrf` | Initialiseer CSRF-bescherming voor mutaties |
| POST | `/auth/register` | Maak een ongeverifieerd USER-account aan |
| POST | `/auth/login` | Log in met gebruikersnaam of e-mailadres en start een sessie |

Optionele query-param: `year=2024` om te filteren op jaar.

Registratie heeft in fase 2 nog geen frontendpagina en verstuurt nog geen verificatiemail; dat volgt in fase 3 en 4 van het [accountplan](docs/accounts-en-reacties-plan.md).

### Sessie-endpoints

| Method | Pad | Omschrijving |
|---|---|---|
| GET | `/auth/me` | Geef de ingelogde veilige account-DTO terug |
| POST | `/auth/logout` | Vernietig de huidige sessie |

### Admin-endpoints (`ADMIN`-rol vereist)

| Method | Pad | Body |
|---|---|---|
| POST | `/admin/artists` | `{ "name": "..." }` |
| POST | `/admin/songs` | `{ "title", "imageUrl", "previewUrl", "artistIds": [1, 2] }` |
| POST | `/admin/charts` | `{ "date": "2024-01-01", "songIds": [1..24] }` (exact 24 nummers) |

---

## Databaseschema

```
date         (week_id PK, date)
song         (song_id PK, title, image_url, preview_url)
artist       (artist_id PK, name)
artist_of_song (song_id FK, artist_id FK, artist_order) — PK(song_id, artist_id)
chart        (week_id FK, position, song_id FK) — PK(week_id, position)
user         (user_id PK, username UNIQUE, password, email UNIQUE, role, status, verificatie/timestamps)
SPRING_SESSION + SPRING_SESSION_ATTRIBUTES (server-side browsersessies)
```

De tabel `date` bevat chartsweken; `chart` bevat de 24 posities per week. Punten worden berekend als `25 - positie` (positie 1 = 24 punten, positie 24 = 1 punt).

Flyway maakt en migreert het schema bij het starten van de backend. Hibernate valideert het resultaat daarna (`ddl-auto: validate`).

---

## Databasemigraties

Migraties staan in `backend/src/main/resources/db/migration` en hebben namen zoals:

```text
V1__initial_schema.sql
V2__expand_user_accounts.sql
V3__create_spring_session_tables.sql
```

Wijzig een toegepaste migratie nooit; voeg voor iedere wijziging een nieuwe versie toe.

### Eenmalige overstap van een bestaande database

Maak eerst een databaseback-up en start de eerste Flyway-versie van de backend eenmalig met:

```dotenv
FLYWAY_BASELINE_ON_MIGRATE=true
```

Flyway registreert het bestaande schema dan als versie 1. Controleer na de start de tabel `flyway_schema_history` en zet de variabele daarna terug op `false`. Voer de Flyway-introductie bij voorkeur uit als aparte deploy zonder andere schemawijzigingen.

---

## Tests draaien

```bash
cd backend
mvn test
```

De unit- en weblaagtests draaien zonder externe database. Repository-, migratie- en
sessie-integratietests starten via Testcontainers een tijdelijke MySQL 8-database;
daarvoor moet Docker beschikbaar zijn.
