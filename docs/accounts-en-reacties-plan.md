# Implementatieplan: gebruikersaccounts en reacties

Status: fase 1 en 2 geïmplementeerd; fase 3 en verder gepland

Laatste inhoudelijke update: 7 september 2026

Dit document is bedoeld als overdracht naar een volgende ontwikkelsessie. Het beschrijft zowel de gekozen richting als de relevante huidige situatie, migratievolgorde, API-contracten, beveiliging, e-mail, tests en uitrol.

## 1. Doel en scope

Het eerste gebruikersgerichte onderdeel wordt:

- bezoekers kunnen een account registreren en inloggen;
- een e-mailadres moet geverifieerd zijn voordat iemand kan reageren;
- reacties worden eerst toegevoegd aan chartweken;
- reacties lezen blijft mogelijk zonder account;
- gebruikers kunnen hun eigen reacties beheren;
- admins kunnen reacties modereren en accounts blokkeren;
- dezelfde accountbasis moet later bruikbaar zijn voor reacties op nummers en andere gebruikersfunctionaliteit.

Niet in de eerste versie:

- sociale login via Google, Apple of Facebook;
- geneste reacties, likes, notificaties of privéberichten;
- avatars en uitgebreide profielen;
- reacties op nummers;
- een eigen mailserver op de VPS.

## 2. Reeds gemaakte keuzes

| Onderwerp | Keuze | Reden |
|---|---|---|
| Reacties plaatsen | Alleen met account | Betere moderatie en basis voor latere functies |
| Reacties lezen | Publiek | Geen onnodige drempel |
| Accountgegevens | Gebruikersnaam, e-mailadres en wachtwoord | Herkenbare publieke naam plus herstel/verificatie |
| E-mailverificatie | Verplicht vóór eerste reactie | Remt misbruik en bevestigt accountbezit |
| Wachtwoorden | BCrypt-hashes; nooit leesbaar opslaan | Sluit aan op de huidige Spring-configuratie |
| Browserauthenticatie | Server-side sessie met Secure/HttpOnly-cookie | Geen langdurig token in `localStorage`; intrekken is eenvoudig |
| Sessiestorage | MySQL via Spring Session JDBC | Sessies overleven een containerrestart en er is maar één bestaande database nodig |
| Productiemail | Brevo Free, aanvankelijk via SMTP | Eenvoudig, provider-onafhankelijk en 300 mails per dag is voorlopig ruim genoeg |
| Ontvangen van antwoorden | Bestaand privé-e-mailadres als Reply-To | Volledig gratis; voorlopig geen aparte e-mailhosting nodig |
| Publieke afzender | `Vlasje24 <accounts@vlasje24.nl>` | Professionele afzender voor accountberichten; hoeft na domeinauthenticatie geen mailbox te zijn |
| Lokale mail | Mailpit | Geen echte berichten tijdens ontwikkeling; mails zijn lokaal inspecteerbaar |
| Reactie-inhoud | Alleen platte tekst | Vermijdt HTML/XSS en houdt moderatie simpel |
| Verwijderen | Soft delete | Moderatie en onderzoek naar misbruik blijven mogelijk |
| Databaseschema | Alleen via nieuwe Flyway-migraties | Flyway is al ingericht; toegepaste migraties worden nooit gewijzigd |

De gratis limiet van Brevo is op de datum van dit document 300 e-mails per dag en omvat transactiemail. Controleer dit opnieuw vóór het afsluiten van een account: <https://help.brevo.com/hc/en-us/articles/208589409-About-Brevo-s-pricing-plans>.

## 3. Huidige projecttoestand

### Techniek en hosting

- Backend: Java 21, Spring Boot 3.4.4, Spring Web, JPA en Spring Security.
- Frontend: React 18, TypeScript, Vite, React Router en TanStack Query.
- Database: MySQL 8.
- Productie: Docker Compose op een STRATO-VPS, met Caddy voor TLS en nginx voor de frontend/API-proxy.
- Domeinregistratie en DNS-beheer: TransIP.
- Er is geen e-mailhosting nodig voor de gekozen eerste versie: Brevo verstuurt en een bestaand privé-e-mailadres ontvangt antwoorden via Reply-To.
- Backend en frontend gebruiken in productie hetzelfde publieke domein; de frontend roept relatief `/api/v1/...` aan.
- Flyway is aanwezig met `V1__initial_schema.sql` en Hibernate gebruikt `ddl-auto: validate`.

### Relevante bestanden

- `backend/src/main/resources/db/migration/V1__initial_schema.sql`
- `backend/src/main/resources/application.yml`
- `backend/src/main/java/nl/vlasje24/domain/User.java`
- `backend/src/main/java/nl/vlasje24/controller/AuthController.java`
- `backend/src/main/java/nl/vlasje24/config/SecurityConfig.java`
- `backend/src/main/java/nl/vlasje24/security/AppUserDetailsService.java`
- `backend/src/main/java/nl/vlasje24/security/AuthRateLimiter.java`
- `backend/src/main/resources/db/migration/V3__create_spring_session_tables.sql`
- `frontend/src/api/client.ts`
- `frontend/src/App.tsx`
- `frontend/src/pages/HomePage.tsx`
- `docker-compose.yml`
- `.env.example`

### Huidige authenticatie na fase 2

- `user` bevat accountvelden, een database-backed rol en een status;
- `POST /api/v1/auth/login` start een server-side sessie en retourneert alleen de veilige account-DTO;
- login accepteert een gebruikersnaam of e-mailadres en weigert niet-actieve accounts met dezelfde generieke fout;
- Spring Session bewaart sessies 14 dagen in MySQL via de door Flyway aangemaakte tabellen;
- de browser gebruikt de Secure/HttpOnly/SameSite=Lax-cookie `VLASJE24_SESSION`;
- CSRF staat aan; `GET /api/v1/auth/csrf` initialiseert `XSRF-TOKEN` en mutaties sturen `X-XSRF-TOKEN`;
- rollen komen via `AppUserDetailsService` uit de database; `/api/v1/admin/**` vereist `ROLE_ADMIN`;
- login is begrensd op 10 pogingen per minuut en registratie op 5 pogingen per uur per client-IP;
- JWT-code, bearer-tokenrespons, `JWT_SECRET` en de `localStorage`-adminflow zijn direct verwijderd.

De overgang is bewust als één brekende backend/frontend-release uitgevoerd. Er is geen tijdelijke compatibiliteitsperiode voor oude JWT-clients; bestaande JWT's worden na de release niet meer geaccepteerd en iedereen moet opnieuw inloggen. Dit is aanvaardbaar omdat downtime en opnieuw inloggen of accounts opnieuw aanmaken voor deze release expliciet zijn toegestaan.

## 4. Doelarchitectuur

```text
Browser
  │
  ├─ registratie/login/reacties ─► Spring Security
  │                                  │
  │                                  ├─ user + account_token + comment (MySQL)
  │                                  ├─ Spring Session-tabellen (MySQL)
  │                                  └─ SMTP ─► Brevo ─► ontvanger
  │
  └─ sessie-id in Secure + HttpOnly + SameSite-cookie
```

De browser krijgt geen auth-token dat JavaScript kan uitlezen. Na login zet Spring een sessiecookie. De frontend gebruikt bij API-verzoeken `credentials: 'include'`. Omdat cookies automatisch worden meegestuurd, blijft CSRF-bescherming aan en stuurt de frontend bij mutaties ook een CSRF-token mee.

## 5. Beoogd datamodel

Exacte SQL komt per fase in een nieuwe Flyway-migratie. Gebruik `DATETIME(6)` en UTC voor tijdstippen.

### Uitbreiding van `user`

| Kolom | Type/richting | Opmerking |
|---|---|---|
| `user_id` | bestaand `INT` PK | Behouden |
| `username` | bestaand `VARCHAR(255)` | Publiek zichtbaar en uniek; applicatie begrenst nieuwe namen |
| `password` | bestaand `VARCHAR(255)` | Bevat alleen BCrypt-hash; hernoemen is niet noodzakelijk |
| `email` | `VARCHAR(254)`, uniek, aanvankelijk nullable | Nullable voor veilige migratie van bestaande admins |
| `email_verified_at` | nullable `DATETIME(6)` | `NULL` betekent nog niet geverifieerd |
| `role` | `VARCHAR(20)` | Voorlopig `USER` of `ADMIN` |
| `status` | `VARCHAR(20)` | `ACTIVE`, `SUSPENDED` of `DELETED` |
| `created_at` | `DATETIME(6)` | Aanmaaktijd |
| `updated_at` | `DATETIME(6)` | Laatste wijziging |

Migratieregel: alle gebruikers die al vóór deze feature in de tabel staan, zijn bestaande admins en moeten in de migratie expliciet `role = 'ADMIN'` krijgen. Nieuwe registraties krijgen altijd `USER`. Een bestaande admin zonder e-mailadres moet tijdelijk kunnen blijven inloggen; voeg daarna handmatig een adminmailadres toe en markeer dat als geverifieerd.

Normalisatie en validatie:

- trim gebruikersnaam en e-mailadres;
- behandel e-mailadressen case-insensitief en sla ze genormaliseerd op;
- gebruikersnaam: aanbevolen 3–30 tekens, letters/cijfers/underscore/streepje;
- e-mailadres: server-side valideren, maximaal 254 tekens;
- wachtwoord: minimaal 12 tekens en maximaal 72 bytes wegens BCrypt;
- retourneer wachtwoord(hash), e-mailadres en interne status nooit in publieke reactie-DTO's.

Verklein de bestaande databasekolom voor `username` niet zonder eerst productiegegevens te controleren. Voor de eerste versie kan de kolom 255 tekens blijven terwijl nieuwe invoer op 30 tekens wordt begrensd.

### `account_token`

Eén tabel voor e-mailverificatie en wachtwoordherstel:

| Kolom | Doel |
|---|---|
| `account_token_id` | PK |
| `user_id` | FK naar `user` |
| `purpose` | `EMAIL_VERIFICATION` of `PASSWORD_RESET` |
| `token_hash` | Unieke SHA-256-hash van het verstuurde token |
| `expires_at` | Verificatie circa 24 uur; reset circa 60 minuten |
| `used_at` | Eenmalig gebruik afdwingen |
| `created_at` | Audit en opschoning |

Genereer een cryptografisch willekeurig token van minstens 32 bytes, URL-safe gecodeerd. Stuur het ruwe token per e-mail, maar bewaar alleen de hash. Een nieuw token maakt eerdere nog geldige tokens met hetzelfde doel ongeldig.

### Spring Session-tabellen

Gebruik `spring-session-jdbc` en maak de benodigde `SPRING_SESSION`- en `SPRING_SESSION_ATTRIBUTES`-tabellen met Flyway, niet met automatische schema-initialisatie. Stel een gewenste sessieduur expliciet in; aanbevolen startwaarde is 14 dagen. Wachtwoordwijziging, blokkering en accountverwijdering moeten bestaande sessies beëindigen.

### `comment`

Voor de eerste versie:

| Kolom | Doel |
|---|---|
| `comment_id` | `BIGINT` PK |
| `chart_week_id` | FK naar `date.week_id` |
| `user_id` | FK naar auteur |
| `body` | Platte tekst, aanbevolen maximaal 1.000 tekens |
| `status` | `VISIBLE`, `DELETED_BY_AUTHOR` of `DELETED_BY_MODERATOR` |
| `created_at` | Sorteer- en weergavetijd |
| `updated_at` | Voor een later/bestaand bewerkt-label |
| `deleted_at` | Nullable soft-delete-tijd |

Voeg indexen toe op `(chart_week_id, status, created_at, comment_id)` en op `user_id`. In de MVP zijn reacties plat: geen `parent_comment_id`.

Voor reacties op nummers komt later een nieuwe migratie. Voorkeursrichting: maak `chart_week_id` nullable, voeg `song_id` als nullable FK toe en voeg een constraint toe die afdwingt dat exact één doel gevuld is. Doe dit pas wanneer nummerreacties daadwerkelijk worden gebouwd; gebruik nu geen polymorfe `target_type/target_id` zonder foreign keys.

## 6. API-ontwerp

Alle routes blijven onder `/api/v1`.

### Authenticatie

| Methode | Endpoint | Authenticatie | Gedrag |
|---|---|---|---|
| `GET` | `/auth/csrf` | Publiek | Initialiseert en retourneert het CSRF-token voor de SPA |
| `POST` | `/auth/register` | Publiek | Maakt een nog ongeverifieerd USER-account; token en mail volgen in fase 3 |
| `POST` | `/auth/login` | Publiek | Login met gebruikersnaam of e-mail; start sessie |
| `POST` | `/auth/logout` | Ingelogd | Beëindigt sessie en wist cookie |
| `GET` | `/auth/me` | Optioneel | Geeft huidige gebruiker of 401 terug |
| `POST` | `/auth/verify-email` | Publiek | Body bevat token; markeert e-mail geverifieerd |
| `POST` | `/auth/resend-verification` | Publiek | Nieuw verificatiebericht met cooldown |
| `POST` | `/auth/forgot-password` | Publiek | Altijd dezelfde 202-reactie, ook bij onbekend adres |
| `POST` | `/auth/reset-password` | Publiek | Verbruikt token, wijzigt hash en beëindigt sessies |

Aanbevolen registratiebody:

```json
{
  "username": "stefan",
  "email": "stefan@example.nl",
  "password": "een-lang-wachtwoord"
}
```

Aanbevolen veilige accountrespons:

```json
{
  "userId": 42,
  "username": "stefan",
  "role": "USER",
  "emailVerified": true
}
```

Gebruik een consistente JSON-foutstructuur, bijvoorbeeld `code`, `message` en optionele `fieldErrors`. Geef bij login altijd een generieke melding voor een onbekende gebruiker en een verkeerd wachtwoord. Het forgot-password-endpoint mag niet verraden of een e-mailadres bestaat.

### Chartreacties

| Methode | Endpoint | Toegang | Gedrag |
|---|---|---|---|
| `GET` | `/charts/{weekId}/comments?page=0&size=20` | Publiek | Alleen zichtbare reacties, gepagineerd |
| `POST` | `/charts/{weekId}/comments` | Geverifieerd USER/ADMIN | Plaatst `{ "body": "..." }` |
| `PATCH` | `/comments/{commentId}` | Eigenaar of ADMIN | Bewerkt body en `updated_at` |
| `DELETE` | `/comments/{commentId}` | Eigenaar of ADMIN | Soft delete; retourneert 204 |

Beperk `size` server-side, bijvoorbeeld tot 50. Sorteer in de eerste versie nieuwste eerst op `created_at DESC, comment_id DESC`. De reactie-DTO bevat alleen `commentId`, auteur-id, gebruikersnaam, body, created/updated-tijd en eventueel een `edited`-indicator.

Semantiek:

- 400: ongeldige invoer;
- 401: niet ingelogd;
- 403: e-mail niet geverifieerd, account geblokkeerd of geen eigenaar/admin;
- 404: chart of reactie bestaat niet;
- 409: gebruikersnaam/e-mail bestaat al of token is al verbruikt;
- 429: rate limit overschreden.

## 7. Gefaseerde uitvoering

Elke fase hoort zelfstandig getest en deploybaar te zijn. Voeg per schemawijziging een nieuwe Flyway-migratie toe; wijzig `V1__initial_schema.sql` niet.

Voorgestelde migratievolgorde:

```text
V2__expand_user_accounts.sql
V3__create_spring_session_tables.sql
V4__create_account_tokens.sql
V5__create_chart_comments.sql
V6__create_moderation_audit.sql       # pas als fase 6 dit nodig heeft
```

Controleer vóór het schrijven van iedere migratie welke versies inmiddels werkelijk bestaan; bestandsnummers mogen nooit worden hergebruikt.

### Fase 0 — Productkeuzes en voorbereiding

1. Bevestig publieke accountvoorwaarden en huisregels.
2. Gebruik als definitieve afzender voor accountmails `Vlasje24 <accounts@vlasje24.nl>`.
3. Vul een bestaand privé-e-mailadres in als Reply-To. Dit adres staat in de mailheader en kan dus zichtbaar zijn voor ontvangers.
4. Maak een Brevo-account aan en sluit waar nodig de verwerkersovereenkomst.
5. Voeg de door Brevo gegeven SPF-, DKIM- en DMARC-records toe in het DNS-beheer van TransIP.
6. Begin DMARC veilig met rapportagebeleid (`p=none`) en verscherp dit later na controle.
7. Zet open- en kliktracking uit voor verificatie- en resetmails.
8. Maak vóór de eerste productiemigratie een databaseback-up en controleer de Flyway-status.

Voor deze variant is geen mailbox, mailserver, Postfix, MX-record of TransIP-e-mailpakket nodig. Na authenticatie van `vlasje24.nl` worden afzenders binnen dat domein door Brevo automatisch als geverifieerd behandeld: <https://help.brevo.com/hc/en-us/articles/208836149-Create-a-new-sender-From-name-and-From-email>. `accounts@vlasje24.nl` ontvangt zelf geen berichten; daarom moet iedere accountmail het privé-adres als Reply-To meekrijgen.

Voeg bij TransIP alleen de DNS-records toe die Brevo daadwerkelijk toont. Behoud de bestaande A/AAAA-records naar de STRATO-VPS. Brevo is alleen de uitgaande transactiemailrelay; de app en database blijven op STRATO. Als later een publiek inkomend adres gewenst is, kan alsnog een doorstuur- of maildienst worden toegevoegd zonder wijziging van het accountschema.

Klaar wanneer: domeinverificatie slaagt en een handmatige testmail bij ten minste Gmail en Outlook niet in spam belandt.

### Fase 1 — Accountdatamodel en rollen

Status: geïmplementeerd.

1. Voeg een migratie toe, bijvoorbeeld `V2__expand_user_accounts.sql`.
2. Breid `User` uit met e-mail, verificatietijd, rol, status en timestamps.
3. Zet bestaande rijen expliciet op `ADMIN`; verlies het bestaande adminaccount niet.
4. Voeg repositories, enums en aparte publieke DTO's toe.
5. Laat Hibernate alleen valideren.
6. Voeg repository- en migratietests toe voor unieke gebruikersnaam/e-mail en de legacy-adminconversie.

Klaar wanneer: de bestaande admin kan nog inloggen en een gewone gebruiker kan in tests worden opgeslagen zonder adminrechten te krijgen.

### Fase 2 — Veilige sessieauthenticatie

Status: geïmplementeerd.

1. Voeg `spring-session-jdbc` toe.
2. Maak de sessietabellen via een Flyway-migratie.
3. Implementeer registratie, sessielogin, logout en `/auth/me`.
4. Laat Spring Security rollen uit de database laden via `UserDetailsService` of een eigen `AuthenticationProvider`.
5. Gebruik een Secure, HttpOnly, SameSite=Lax-cookie in productie.
6. Zet CSRF aan met een SPA-geschikte tokenaanpak, bijvoorbeeld `CookieCsrfTokenRepository`; stuur bij mutaties `X-XSRF-TOKEN`.
7. Zet CORS `allowCredentials` alleen aan voor de expliciet toegestane development/productie-origins.
8. Voeg basis-rate-limits toe aan login en registratie. Geïmplementeerde startwaarden: 10 loginpogingen per minuut en 5 registraties per uur per client-IP. De limiter is lokaal in het backendproces; bij meerdere backendreplica's moet deze later naar gedeelde opslag.

Bewust gekozen directe overgang van de bestaande adminfrontend:

1. Backend en frontend worden als één release uitgerold; een korte onderhoudsperiode is toegestaan.
2. De adminpagina gebruikt direct cookies en `/auth/me`; er is geen tijdelijke JWT-compatibiliteit.
3. `JwtAuthFilter`, `JwtUtil`, JJWT-dependencies, `JWT_SECRET`, bearer-tokenrespons en `vlasje24_admin_token` zijn in dezelfde release verwijderd.
4. Bestaande sessies/JWT's blijven niet geldig. Gebruikers en admins loggen na uitrol opnieuw in; indien gewenst mogen accounts opnieuw worden aangemaakt.

Registratie maakt in deze fase alleen het account aan. Omdat `account_token` en mail pas fase 3 zijn, wordt het account nog niet automatisch geverifieerd en wordt nog geen verificatiemail verstuurd.

Klaar wanneer: USER kan niet bij `/admin/**`, ADMIN wel, logout maakt de sessie werkelijk ongeldig en de frontend bevat geen JWT meer in `localStorage`.

### Fase 3 — E-mailverificatie en wachtwoordherstel

1. Voeg `account_token` via Flyway toe.
2. Voeg `spring-boot-starter-mail` en een eigen `MailService`-interface toe.
3. Maak een Brevo SMTP-implementatie voor productie en een Mailpit-configuratie voor lokaal gebruik.
4. Verstuur verificatielinks naar bijvoorbeeld `https://vlasje24.nl/verify-email?token=...`.
5. Voeg verificatie, opnieuw verzenden, vergeten wachtwoord en resetten toe.
6. Verstuur pas nadat de account/tokentransactie is gecommit. Als verzenden mislukt, blijft het account bestaan en kan de gebruiker opnieuw verzenden.
7. Voeg een periodieke opschoontaak toe voor verlopen/verbruikte tokens, of ruim ze bij creatie/gebruik op.
8. Log geen ruwe tokens, wachtwoorden of volledige mailinhoud.

Benodigde productievariabelen, exacte namen tijdens implementatie definitief maken:

```dotenv
MAIL_HOST=smtp-relay.brevo.com
MAIL_PORT=587
MAIL_USERNAME=
MAIL_PASSWORD=
MAIL_FROM=accounts@vlasje24.nl
MAIL_REPLY_TO=jouw-priveadres@example.com
APP_BASE_URL=https://vlasje24.nl
```

Sla secrets alleen op in de `.env` van de VPS en documenteer de niet-geheime variabelen in `.env.example`. Controleer de actuele host/poort in Brevo bij inrichting.

Klaar wanneer: verificatie- en resetlinks zijn eenmalig, verlopen correct, lekken geen accountbestaan en wachtwoordreset beëindigt bestaande sessies.

### Fase 4 — Accountfrontend

1. Maak registratie-, login-, verificatie-, vergeten-wachtwoord- en resetpagina's.
2. Voeg een centrale `AuthProvider`/auth-query toe op basis van `/auth/me`.
3. Pas de fetch-client aan met `credentials: 'include'`, CSRF-header en consistente foutafhandeling.
4. Toon in de header login/account/logout.
5. Maak duidelijk dat verificatie nodig is en bied opnieuw verzenden aan.
6. De adminpagina gebruikt sinds fase 2 al rollen uit `/auth/me`.
7. De oude `useAuth`-localStorageflow is sinds fase 2 al verwijderd.

Klaar wanneer: registratie tot verificatie werkt op mobiel en desktop, een refresh de sessie behoudt en adminnavigatie alleen voor ADMIN zichtbaar/toegankelijk is.

### Fase 5 — Chartreacties

1. Voeg `comment` en indexen via Flyway toe.
2. Implementeer entity, repository, service, DTO's en endpoints.
3. Sta schrijven alleen toe aan ingelogde, geverifieerde, actieve accounts.
4. Valideer en trim platte tekst server-side; accepteer geen lege tekst en interpreteer opgeslagen tekst nooit als HTML.
5. Voeg rate limiting toe, aanbevolen startpunt 5 reacties per minuut en 30 per uur per gebruiker.
6. Voeg op `HomePage` onder de chart een aparte `ChartComments`-sectie toe.
7. Gebruik TanStack Query voor ophalen, plaatsen, bewerken/verwijderen en cache-invalidation.
8. Toon passende states voor uitgelogd, niet geverifieerd, laden, leeg en fout.

Klaar wanneer: publieke bezoekers kunnen lezen, alleen toegestane accounts kunnen schrijven, eigenaarschap server-side wordt gecontroleerd en paginering werkt.

### Fase 6 — Moderatie en misbruikbeperking

1. Voeg adminoverzicht toe voor recente/verwijderde reacties.
2. Laat admins reacties soft-deleten met optionele interne reden.
3. Laat admins accounts suspenderen en alle sessies daarvan beëindigen.
4. Voeg auditlogging toe voor moderatieacties.
5. Voeg indien nodig een rapporteerfunctie toe; niet nodig voor de eerste kleinschalige release.
6. Monitor 4xx/5xx, mislukte mailaflevering en rate-limit-events zonder gevoelige inhoud te loggen.

Klaar wanneer: een admin misbruik kan verwijderen en een account kan blokkeren zonder directe database-ingreep.

### Fase 7 — Privacy, beheer en productie-afwerking

1. Publiceer privacyverklaring en korte huisregels vóór publieke registratie.
2. Documenteer welke accountgegevens en mailmetadata Brevo verwerkt.
3. Voeg accountverwijdering toe met recente wachtwoordbevestiging.
4. Aanbevolen beleid: anonimiseer/verwijder persoonlijke accountvelden en soft-delete de reacties van het verwijderde account.
5. Leg bewaartermijnen vast voor soft-deleted reacties, auditlogs en verlopen tokens.
6. Test databaseback-up én restore inclusief gebruikers, reacties en sessies.
7. Voeg monitoring toe voor Brevo-limiet, bounces en afzenderreputatie.
8. Voer een dependency- en securitycheck uit vóór publieke lancering.

## 8. Security-checklist

- Gebruik HTTPS overal in productie; Caddy verzorgt dit al.
- Geen wachtwoorden, SMTP-credentials, sessie-id's of tokens in Git of logs.
- BCrypt blijft de `PasswordEncoder`; overweeg een hogere cost alleen na een performancetest op de VPS.
- Authenticeer en autoriseer in services/controllers; vertrouw nooit op verborgen frontendknoppen.
- Beperk requestgroottes en valideer alle DTO's met Jakarta Validation.
- Gebruik generieke login- en resetmeldingen om accountenumeratie te beperken.
- Roteer verificatie/reset-tokens na nieuw verzoek en accepteer ze één keer.
- Houd CSRF aan voor cookieauthenticatie.
- Render reacties als tekst; gebruik geen `dangerouslySetInnerHTML`.
- Accepteer alleen verwachte sorteer-/paginatieparameters en begrens paginagrootte.
- Vernietig sessies na wachtwoordreset, accountblokkering en accountverwijdering.
- Maak databaseback-up vóór iedere productiemigratie en test migraties eerst op een kopie.

## 9. Teststrategie

### Backend-integratietests

- registratie geldig/ongeldig/dubbel;
- BCrypt-hash wordt opgeslagen, nooit het wachtwoord;
- login via gebruikersnaam/e-mail, foute login geeft generieke 401;
- USER krijgt 403 op adminroutes; ADMIN krijgt toegang;
- sessiecookie, logout en CSRF werken;
- verificatie/reset: geldig, verlopen, gebruikt en vervangen token;
- onbekend resetadres geeft dezelfde respons;
- ongeverifieerd of suspended account kan niet reageren;
- reactie aanmaken/lezen/bewerken/verwijderen en eigenaarschap;
- paginering, sorteervolgorde en rate limits;
- soft-deleted reacties ontbreken in publieke resultaten;
- Flyway-migratie vanaf een V1-schema met bestaand adminaccount.

Gebruik bij voorkeur Testcontainers met MySQL voor migraties en constraints; H2 wijkt hiervoor te veel af van MySQL. Mock de `MailService` in reguliere tests en voeg één integratietest tegen Mailpit/een lokale SMTP-testserver toe.

### Frontendtests

- authstatus na laden, login en logout;
- registratie- en validatiefouten;
- verificatie/reset states;
- reactieformulier per auth/verificatiestatus;
- optimistische updates alleen als rollback bij fout correct werkt; anders eerst eenvoudig refetchen;
- eigenaar/admin ziet beheeracties, andere gebruiker niet;
- mobiele layout en toegankelijk toetsenbordgebruik.

### Handmatige productiesmoke-test

1. Registreer met een nieuw extern e-mailadres.
2. Controleer afzender, SPF/DKIM en spamplaatsing.
3. Verifieer en log in na browserrefresh.
4. Plaats en verwijder een reactie op een bestaande chart.
5. Controleer dat een USER `/admin` niet kan gebruiken.
6. Reset het wachtwoord en bevestig dat de oude sessie niet meer werkt.
7. Herstart de backendcontainer en bevestig dat een geldige sessie blijft bestaan.

## 10. Uitrol en rollback

Aanbevolen volgorde vanaf fase 3:

1. databaseback-up;
2. images bouwen en tests draaien;
3. backend/schemawijziging deployen;
4. logs en Flyway `flyway_schema_history` controleren;
5. frontend deployen;
6. smoke-test uitvoeren;
7. controleer na de directe fase-2-overgang dat geen client nog bearer-authenticatie verwacht.

Voor fase 2 is bewust geen expand-and-contract-overgang gebruikt: backend en frontend moeten samen worden uitgerold. Gebruik voor toekomstige destructieve schemawijzigingen wel expand-and-contract. Een Flyway-migratie wordt niet teruggedraaid door een oud image te starten. Bij een mislukte destructieve migratie is de databaseback-up de herstelroute.

## 11. Nog expliciet te bevestigen vóór implementatie

Deze keuzes blokkeren het voorbereidende backendwerk niet, maar moeten vóór publieke lancering definitief zijn:

- het concrete privé-e-mailadres dat in productie als Reply-To wordt ingesteld;
- of de nu ingestelde sessieduur van 14 dagen later moet wijzigen;
- mogen gebruikers hun reacties onbeperkt bewerken, of alleen binnen bijvoorbeeld 15 minuten;
- exacte maximale reactielengte (advies: 1.000 tekens);
- bewaartermijn voor verwijderde reacties en moderatie-auditlogs;
- definitieve accountverwijderings- en anonimisatieregels;
- of een aparte `MODERATOR`-rol nodig wordt; advies voor de start: alleen `USER` en `ADMIN`.

## 12. Definitie van de eerste complete release

De eerste accounts-en-reactiesrelease is gereed wanneer:

- een bezoeker zich kan registreren, verifiëren, inloggen, uitloggen en het wachtwoord herstellen;
- authenticatie via een veilige HttpOnly-sessiecookie loopt;
- bestaande admins hun beheerfunctie behouden zonder automatisch adminrechten aan gewone gebruikers te geven;
- alleen geverifieerde actieve accounts chartreacties kunnen plaatsen;
- publieke bezoekers chartreacties kunnen lezen;
- eigenaar en admin een reactie veilig kunnen beheren;
- een admin een gebruiker kan blokkeren;
- mail, rate limits, privacytekst, back-up/restore en productie-smoke-tests zijn afgehandeld;
- alle schemawijzigingen reproduceerbaar via Flyway zijn.

## 13. Verwachte codeonderdelen

Dit is een richtlijn voor de implementatiestructuur, geen verplichting om exact deze klassennamen te gebruiken.

### Backend

- dependencies: `spring-boot-starter-validation`, `spring-session-jdbc`, `spring-boot-starter-mail`; later eventueel Bucket4j voor rate limiting en Testcontainers MySQL voor integratietests;
- domain: uitgebreide `User`, plus `AccountToken`, `Comment` en bijbehorende enums;
- repositories: query's op genormaliseerde username/e-mail, tokens, reacties en sessies per principal;
- services: `AccountService`, `AuthenticationService`, `AccountTokenService`, `MailService`, `CommentService` en later `ModerationService`;
- controllers: bestaande `AuthController` gecontroleerd uitbreiden; nieuwe `CommentController` en later adminmoderatie-endpoints;
- security: database-backed authorities, sessie- en CSRF-configuratie, aparte checks voor verified/active en eigenaar/admin;
- DTO's: request/response records met Jakarta Validation; entities nooit rechtstreeks serialiseren;
- config: mail-, app-URL-, cookie-/sessie- en rate-limit-instellingen via properties/env;
- tests: controller/securitytests, servicetests en MySQL-migratietests.

### Frontend

- `api/client.ts`: generieke requestfunctie met cookies, CSRF en gestructureerde API-fouten;
- centrale authcontext/query die `/auth/me` laadt;
- routes/pagina's voor register, login, verify, forgot/reset password en account;
- headeraccountmenu in `AppShell`;
- `ChartComments` met lijst, formulier, paginering en eigenaaracties;
- adminpagina's voor commentmoderatie en gebruikersblokkering;
- de JWT/localStorage-helper is in fase 2 al verwijderd.

### Docker en configuratie

- Mailpit alleen in lokale Compose-configuratie, met SMTP-poort 1025 en webinterface 8025;
- Brevo-instellingen als environmentvariabelen op de backendservice;
- geen SMTP-poorten publiek publiceren op de VPS;
- `.env.example` bevat alleen lege placeholders en uitleg, nooit werkende credentials;
- README aanvullen met lokale mailtest, nieuwe variabelen en productie-inrichting zodra de implementatie bestaat.
