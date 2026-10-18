# Vlasje24 — Agent Instructions

Nederlandse muziekchartswebsite. Wekelijks worden 24 nummers ingevoerd; bezoekers zien de hitlijst, kunnen navigeren door de geschiedenis en statistieken bekijken.

## Tech stack

| Laag | Technologie |
|---|---|
| Backend | Spring Boot 3.4.4, Java 21, Spring Security + Spring Session JDBC, JPA/Hibernate, MySQL |
| Frontend | React 18, TypeScript 5, Vite 6, TailwindCSS 3, TanStack Query v5, React Router v6, Recharts |
| Database | MySQL 8.0 + Flyway-migraties |
| Infra | Docker Compose, Caddy v2 (HTTPS), nginx (SPA + API-proxy) |

## Projectstructuur

```
backend/src/main/java/nl/vlasje24/
├── Main.java                          # Entry point
├── config/
│   ├── SecurityConfig.java            # CORS, sessies, CSRF, BCrypt, endpoint-rechten
│   └── WebConfig.java                 # Leeg placeholder
├── controller/                        # REST-endpoints (dunne laag, delegeert naar services)
├── domain/                            # JPA-entiteiten
├── dto/                               # Java records (request/response)
├── exception/                         # NotFoundException + GlobalExceptionHandler (→ 404)
├── repository/                        # Spring Data JPA interfaces met JPQL
├── security/
│   ├── AccountPrincipal.java          # Veilige, serialiseerbare sessieprincipal
│   ├── AppUserDetailsService.java     # Accounts en rollen uit de database
│   └── AuthRateLimiter.java           # Basislimieten voor login en registratie
└── service/                           # Businesslogica

frontend/src/
├── App.tsx                            # Routes
├── api/client.ts                      # Alle fetch-calls, BASE_URL = '/api/v1'
├── types/api.ts                       # TypeScript-interfaces die overeenkomen met backend-DTOs
├── hooks/
│   └── useAudioPlayer.ts              # Globale singleton audiospeler (Context)
├── pages/                             # Één component per route
└── components/
    ├── layout/                        # AppShell (header + nav), SearchBar
    ├── chart/                         # ChartEntryRow, ChartHeader, ExitedSongs, Top*EntryRow
    ├── shared/                        # ArtistLinks
    ├── song/                          # PositionChart (Recharts)
    └── admin/                         # CreateArtistForm, CreateSongForm, CreateChartForm
```

## Essentiële domeinkennis

### Puntensysteem
Een nummer op positie `p` verdient `25 - p` punten per week. Positie 1 = 24 punten, positie 24 = 1 punt. Dit wordt nergens opgeslagen — altijd live berekend via JPQL `SUM(25 - ce.id.position)`.

### Samengestelde sleutels
- `ChartEntryId`: `(weekId, position)` — een chart heeft altijd precies 24 entries
- `ArtistOfSongId`: `(songId, artistId)` — met `artistOrder` (Byte) voor weergavevolgorde
- Deze composite keys zijn `@Embeddable` en worden meegegeven aan de `@EmbeddedId`-annotatie op de entiteit

### Tabel `date` = chartweek
De tabel heet `date` in de database maar de entiteit heet `ChartWeek`. Hibernate-mapping: `@Table(name = "date")`.

### Tiebreaking in sortering
Alle JPQL-sorteerqueries in `ChartEntryRepository` gebruiken meerdere `ORDER BY`-kolommen:
- punten: `SUM DESC, COUNT DESC, MIN(positie) ASC`
- weken: `COUNT DESC, SUM DESC, MIN(positie) ASC`
- positie: `MIN(positie) ASC, SUM DESC, COUNT DESC`

### Authenticatie en adminrechten
Authenticatie gebruikt een server-side Spring Session in MySQL. De browser ontvangt de HttpOnly-cookie `VLASJE24_SESSION`; alle frontendrequests gebruiken `credentials: 'include'`. Rollen komen via `AppUserDetailsService` uit de database. Alleen `ROLE_ADMIN` heeft toegang tot `/api/v1/admin/**`.

CSRF staat aan. `frontend/src/api/client.ts` haalt via `GET /api/v1/auth/csrf` het token op en stuurt bij POST/PATCH/DELETE de header `X-XSRF-TOKEN`. Voeg nooit bearer/JWT-authenticatie of authdata in `localStorage` terug.

### Wachtwoord-hashing
Wachtwoorden worden opgeslagen als BCrypt-hash. `SecurityConfig` registreert een `BCryptPasswordEncoder`-bean. Admin-accounts moeten handmatig via SQL worden aangemaakt met een vooraf gegenereerde BCrypt-hash en expliciet `role = 'ADMIN'` krijgen.

## Lokaal draaien

```bash
# Volledige stack via Docker (aanbevolen)
docker compose up --build
# → Website op http://localhost, API op http://localhost:8080, phpMyAdmin op http://localhost:8081

# Backend native (vereist MySQL op localhost:3306)
cd backend && mvn spring-boot:run

# Frontend native
cd frontend && npm install && npm run dev
# → http://localhost:5173 (Vite proxyt /api/* naar localhost:8080)
```

## Tests draaien

```bash
cd backend && mvn test
```

De meeste tests draaien zonder database (datasource-autoconfiguratie uitgesloten in `src/test/resources/application.yml`). Repository- en migratietests gebruiken Testcontainers met MySQL en worden zonder bereikbare Docker-daemon overgeslagen.

**Testpatronen om te kennen:**
- `@MockitoSettings(strictness = Strictness.LENIENT)` — vereist voor services waarbij sommige stubs alleen bij bepaalde tests worden gebruikt
- Wijs mock-objecten altijd toe aan een variabele vóór gebruik in `thenReturn()` — nooit `thenReturn(mockChartEntry(...))` want Mockito raakt dan in de war
- Stub alleen wat de methode die je test daadwerkelijk aanroept — overbodige stubs veroorzaken `UnnecessaryStubbing`-fouten

## Docker-compose patroon

| Bestand | Wanneer gebruikt | Doel |
|---|---|---|
| `docker-compose.yml` | Altijd | Basis: services met pre-built images van Docker Hub |
| `docker-compose.override.yml` | Automatisch in dev | Voegt `build:` en poorten toe voor lokale ontwikkeling |
| `docker-compose.prod.yml` | `-f ... -f ...` in productie | Voegt Caddy-service toe voor HTTPS |
| `docker-compose.build.yml` | `-f ... -f ...` bij bouwen | Voegt `build:`-context toe voor lokaal builden + pushen |

**Lokale dev:** `docker compose up` (override wordt automatisch meegenomen)

**Productie:** `docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d`

**Bouwen en pushen:** `docker compose -f docker-compose.yml -f docker-compose.build.yml build && ... push`

## API-proxy

De frontend gebruikt altijd `/api/v1/...` als relatief pad (zie `src/api/client.ts`):
- **Productie:** nginx (`frontend/nginx.conf`) proxyt `/api/` → `http://backend:8080/api/`
- **Dev (Vite):** `vite.config.ts` proxyt `/api` → `http://localhost:8080`

Voeg nooit een absolute URL toe aan de API-client.

## CORS

De expliciet toegestane origins komen uit `CORS_ALLOWED_ORIGINS`. Credentialed CORS staat alleen voor die origins aan. De lokale Docker override gebruikt `http://localhost` en `http://localhost:5173`.

## Deployment-workflow

1. Lokaal: images bouwen en pushen naar Docker Hub
2. Op server: `docker compose ... pull && up -d`
3. Server bouwt NOOIT zelf — OOM-risico bij Maven-build op kleine VPS

Reden: Spring Boot Maven-build vereist veel geheugen. De server heeft een kleine heap (`-Xmx256m`) en draait meerdere containers. Bouwen op de server crashte eerder met OOM.

## Bekende issues / TODO's

- **Admin-account aanmaken:** Er is geen UI of CLI voor het aanmaken van admin-accounts. BCrypt-hash en rol moeten handmatig via SQL worden ingevoerd. Genereer een hash met: `python3 -c "import bcrypt; print(bcrypt.hashpw(b'ww', bcrypt.gensalt(10)).decode())"`.
- **Auth-rate-limit is lokaal:** Login- en registratielimieten leven in het backendproces. Bij meerdere backendreplica's is gedeelde opslag nodig.
- **phpMyAdmin in productie:** Bereikbaar via `https://pma.{DOMAIN}` (vereist DNS A-record `pma.domein.nl`). Gebruikt cookie-auth (`PMA_AUTH_TYPE=cookie`).
- **WebConfig.java leeg:** Kan worden verwijderd of gebruikt voor toekomstige web-configuratie.

## Veel voorkomende taken

### Nieuw publiek API-endpoint toevoegen
1. Voeg JPQL-query toe aan het relevante repository-interface
2. Maak een service-methode die de query aanroept en het resultaat omzet naar een DTO
3. Voeg een `@GetMapping`-methode toe in de bijbehorende controller
4. Voeg een TypeScript-interface toe aan `frontend/src/types/api.ts`
5. Voeg een functie toe aan `frontend/src/api/client.ts`

### Nieuw admin-endpoint toevoegen
Zelfde als hierboven, maar beveilig het endpoint met `ROLE_ADMIN`. De frontend stuurt
de sessiecookie automatisch mee; muterende requests krijgen via de API-client ook de
vereiste CSRF-header.

### Nieuwe frontend-pagina toevoegen
1. Maak een component in `frontend/src/pages/`
2. Voeg een `<Route>` toe in `frontend/src/App.tsx`
3. Voeg indien nodig een `<NavLink>` toe in `frontend/src/components/layout/AppShell.tsx`
4. Paden zijn Engels (bijv. `/songs`, `/artists`, `/chart/:weekId`)

### Database-schema wijzigen
1. Voeg een nieuwe versioned migratie toe in `backend/src/main/resources/db/migration` (bijv. `V4__add_comments.sql`)
2. Wijzig een al toegepaste migratie nooit
3. Pas de betrokken JPA-entiteit(en) aan
4. Flyway migreert bij backend-start; Hibernate valideert het resultaat (`ddl-auto: validate`)

### Eerste Flyway-deploy op een bestaande database
1. Maak eerst een database-back-up
2. Deploy de Flyway-introductie zonder andere schemawijzigingen
3. Zet `FLYWAY_BASELINE_ON_MIGRATE=true` voor één backend-start
4. Controleer `flyway_schema_history` en zet de variabele daarna terug op `false`
