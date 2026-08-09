# Vlasje24 — Agent Instructions

Nederlandse muziekchartswebsite. Wekelijks worden 24 nummers ingevoerd; bezoekers zien de hitlijst, kunnen navigeren door de geschiedenis en statistieken bekijken.

## Tech stack

| Laag | Technologie |
|---|---|
| Backend | Spring Boot 3.4.4, Java 21, Spring Security + JWT (JJWT 0.12.6), JPA/Hibernate, MySQL |
| Frontend | React 18, TypeScript 5, Vite 6, TailwindCSS 3, TanStack Query v5, React Router v6, Recharts |
| Database | MySQL 8.0 |
| Infra | Docker Compose, Caddy v2 (HTTPS), nginx (SPA + API-proxy) |

## Projectstructuur

```
backend/src/main/java/nl/vlasje24/
├── Main.java                          # Entry point
├── config/
│   ├── SecurityConfig.java            # CORS, JWT-filter, BCrypt, endpoint-rechten
│   └── WebConfig.java                 # Leeg placeholder
├── controller/                        # REST-endpoints (dunne laag, delegeert naar services)
├── domain/                            # JPA-entiteiten
├── dto/                               # Java records (request/response)
├── exception/                         # NotFoundException + GlobalExceptionHandler (→ 404)
├── repository/                        # Spring Data JPA interfaces met JPQL
├── security/
│   ├── JwtUtil.java                   # Token genereren/valideren (HMAC-SHA256, TTL 24u)
│   └── JwtAuthFilter.java             # OncePerRequestFilter: Bearer-token → SecurityContext
└── service/                           # Businesslogica

frontend/src/
├── App.tsx                            # Routes
├── api/client.ts                      # Alle fetch-calls, BASE_URL = '/api/v1'
├── types/api.ts                       # TypeScript-interfaces die overeenkomen met backend-DTOs
├── hooks/
│   ├── useAuth.ts                     # JWT in localStorage ('vlasje24_admin_token')
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

### Admin-authenticatie
JWT-token zit in localStorage (`vlasje24_admin_token`). Token wordt als `Authorization: Bearer <token>` meegestuurd bij admin-calls. De `JwtAuthFilter` valideert het en stelt `ROLE_ADMIN` in de `SecurityContext`.

### Wachtwoord-hashing
Wachtwoorden worden opgeslagen als BCrypt-hash. `SecurityConfig` registreert een `BCryptPasswordEncoder`-bean. Admin-accounts moeten handmatig via SQL worden aangemaakt met een vooraf gegenereerde BCrypt-hash.

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

33 tests in 7 klassen. Draaien zonder database (datasource-autoconfiguratie uitgesloten in `src/test/resources/application.yml`).

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

`SecurityConfig.corsConfigurationSource()` staat momenteel hardcoded origins toe:
```java
List.of("http://localhost:5173", "http://localhost", "https://vlasje24.nl", "https://new.vlasje24.nl")
```

**Bekende verbetering:** Dit zou configureerbaar moeten zijn via een omgevingsvariabele (bijv. `CORS_ALLOWED_ORIGINS`) zodat het zonder code-rebuild aanpasbaar is per omgeving.

## Deployment-workflow

1. Lokaal: images bouwen en pushen naar Docker Hub
2. Op server: `docker compose ... pull && up -d`
3. Server bouwt NOOIT zelf — OOM-risico bij Maven-build op kleine VPS

Reden: Spring Boot Maven-build vereist veel geheugen. De server heeft een kleine heap (`-Xmx256m`) en draait meerdere containers. Bouwen op de server crashte eerder met OOM.

## Bekende issues / TODO's

- **CORS hardcoded:** Productiedomeinen zijn hardcoded in `SecurityConfig.java`. Bij een nieuw domein moet de code worden aangepast en de backend opnieuw worden gebouwd.
- **Admin-account aanmaken:** Er is geen UI of CLI voor het aanmaken van admin-accounts. BCrypt-hash moet handmatig via SQL worden ingevoerd. Genereer een hash met: `python3 -c "import bcrypt; print(bcrypt.hashpw(b'ww', bcrypt.gensalt(10)).decode())"`.
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
Zelfde als hierboven, maar gebruik `@PostMapping` in `AdminController` en stuur een `token` mee in de `api.post()`-call in de frontend.

### Nieuwe frontend-pagina toevoegen
1. Maak een component in `frontend/src/pages/`
2. Voeg een `<Route>` toe in `frontend/src/App.tsx`
3. Voeg indien nodig een `<NavLink>` toe in `frontend/src/components/layout/AppShell.tsx`
4. Paden zijn Engels (bijv. `/songs`, `/artists`, `/chart/:weekId`)

### Database-schema wijzigen
1. Pas `db/init.sql` aan
2. Pas de betrokken JPA-entiteit(en) aan
3. Bij een bestaande database: pas het schema handmatig aan via phpMyAdmin of SQL — Hibernate doet dit NIET automatisch (`ddl-auto: validate`)
