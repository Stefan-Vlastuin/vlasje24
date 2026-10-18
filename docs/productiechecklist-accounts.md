# Productiechecklist accounts

Voer deze korte checklist uit vóór publieke registratie en daarna maandelijks.

## Voor de lancering

- Maak een databaseback-up en herstel die in een tijdelijke, lege MySQL-database. Controleer minimaal aantallen in `user`, `comment`, `SPRING_SESSION` en `flyway_schema_history`; verwijder daarna alleen de tijdelijke database.
- Doorloop registratie, verificatie, wachtwoordreset, accountverwijdering en adminmoderatie op productie.
- Controleer in Brevo de daglimiet, mislukte afleveringen en bounces. Stel waar Brevo dat ondersteunt een melding in op het beheeradres.
- Controleer SPF, DKIM en DMARC en zet open- en kliktracking voor accountmails uit.
- Controleer vóór een publicatie of het aantal lijstabonnees binnen de dagelijkse verzendlimiet van de mailprovider past.
- Draai `npm audit --omit=dev` en de backendtests. Beoordeel meldingen vóór deploy; update niet blind een major-versie.

## Bewaarbeleid

| Gegevens | Termijn | Uitvoering |
|---|---:|---|
| Actief account | Tot verwijdering door gebruiker | Direct geanonimiseerd bij verwijdering |
| Accounttokens | 7 dagen na verloop of gebruik | Dagelijkse automatische opschoning |
| Verwijderde reacties | 90 dagen | Dagelijkse automatische opschoning |
| Moderatie-auditlog | 1 jaar | Dagelijkse automatische opschoning |
| Inactieve sessie | Maximaal 14 dagen | Spring Session |
| Databaseback-ups | 30 dagen | In te stellen op de VPS/back-upvoorziening |

Leg na iedere restoretest datum, back-upbestand, resultaat en uitvoerder vast. Controleer maandelijks het Brevo-verbruik en bounces en na iedere release de applicatielogs op terugkerende 4xx/5xx-fouten.

Laatste technische controle: op 17 oktober 2026 is een dump van de lokale MySQL-database succesvol hersteld in een tijdelijke database; aantallen voor gebruikers, reacties, sessies en Flyway-migraties kwamen overeen. Herhaal dit nog met een echte productieback-up vóór publieke lancering.
