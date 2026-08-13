# FigureDB

Sistema informativo su Web per il collezionismo di **figure**, **fumetti** e **serie gacha**.

Progetto per il corso di *Sistemi Informativi su Web* (a.a. 2025/2026) — Universita' degli Studi Roma Tre.

---

## Documentazione

| File | Contenuto |
|---|---|
| `docs/figuredb-specifica-progetto.pdf` | Specifica del progetto: entita', casi d'uso, requisiti |
| `docs/figuredb-guida-studio.pdf` | Guida allo studio: ogni strato spiegato dalla teoria al codice |
| `AVVIO.md` | Come compilare e avviare, passo per passo |

## Stack

| Livello | Tecnologia |
|---|---|
| Backend | Spring Boot 4.1 (Java 17) |
| Persistenza | JPA / Hibernate |
| Database | PostgreSQL (H2 in memoria per test e sviluppo) |
| Sicurezza | Spring Security (form login, ruoli DEFAULT/ADMIN) |
| Frontend | Thymeleaf + React (catalogo con ricerca e filtri) |
| API | REST su `/api/**`, consumate dal componente React |

## Avvio rapido

### Con PostgreSQL (configurazione di consegna)

```bash
createdb figuredb                       # una volta sola
./mvnw spring-boot:run
```

Le credenziali del database **non stanno nel progetto**: si leggono da variabili
d'ambiente, cosi' ognuno usa le proprie. Su Windows, una volta sola in PowerShell:

```powershell
setx DB_USERNAME "postgres"
setx DB_PASSWORD "la-tua-password-di-postgres"
```

Poi chiudi e riapri il terminale. Se non le imposti, l'applicazione prova con
`postgres` / `postgres`.

### Senza PostgreSQL (H2 in memoria)

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```

Console H2: <http://localhost:8080/h2-console>

L'applicazione parte su <http://localhost:8080> e il `DatabaseSeeder` carica il catalogo
dai file CSV di `src/main/resources/dati/`, risolvendo le immagini dalle cartelle di
`src/main/resources/static/images/`.

Lo schema viene ricreato a ogni avvio (`ddl-auto=create-drop`), quindi per ricaricare
il catalogo dopo aver modificato un CSV basta riavviare l'applicazione.

### Utenze di prova

| Username | Password | Ruolo |
|---|---|---|
| `admin` | `admin123` | ADMIN |
| `damiano` | `user123` | DEFAULT |
| `giulia` | `user123` | DEFAULT |

## Modificare il catalogo

I dati non sono scritti nel codice: stanno in file CSV versionati con il progetto, quindi
chi clona il repository ottiene lo stesso identico catalogo.

```
src/main/resources/dati/
    serie.csv      aziende.csv    editori.csv    autori.csv
    figure.csv     fumetti.csv    gacha.csv
    LEGGIMI.txt    <- descrizione di ogni colonna
```

Le immagini stanno in `src/main/resources/static/images/`, una cartella per elemento:

```
static/images/figure/<cartellaImmagini>/01.jpg
static/images/gacha/<cartellaImmagini>/01.jpg
```

Il seeder mostra il primo file in ordine alfabetico. Se la cartella manca, l'elemento
resta senza immagine e il sito continua a funzionare.

**Per applicare le modifiche ai CSV basta riavviare.** La configurazione usa
`ddl-auto=create-drop`: a ogni avvio lo schema viene ricreato e il seeder ricarica i CSV.

Il rovescio della medaglia: cio' che inserisci dall'interfaccia del sito (recensioni,
voci di collezione, figure create da amministratore) sparisce al riavvio. Se ti serve
che resti, mettilo nei CSV oppure passa temporaneamente a `ddl-auto=update`.

## Analisi delle strategie di accesso ai dati

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=fetch-demo
```

Esegue `FetchStrategyRunner`, che carica **le figure di una serie con azienda e
serie associate** con tre strategie diverse sullo stesso insieme di dati,
stampando query SQL eseguite, tempo e oggetti caricati:

1. **LAZY** — associazioni caricate su richiesta → problema delle N+1 query
2. **JOIN FETCH** — fetch join esplicito in JPQL → 1 query
3. **ENTITY GRAPH** — `@NamedEntityGraph` dichiarativo → 1 query

## Architettura

```
it.uniroma3.siw.figuredb
├── model/          13 entita' JPA + 3 enum
├── repository/     repository Spring Data + fragment Criteria API per la ricerca
├── service/        casi d'uso, logica di business, @Transactional
│   └── exception/  eccezioni di dominio
├── controller/     MVC Thymeleaf (pubblico, utente registrato, /admin)
├── api/            REST controller + DTO per React
├── security/       SecurityConfiguration
└── bootstrap/      DatabaseSeeder, FetchStrategyRunner
```

La logica applicativa vive **solo** nel Service Layer: i controller validano
l'input e delegano.

## Modello di dominio

```
Scrittore 1 ────< Fumetto >──── * Disegnatore      (molti-a-molti)
                     │
Editore 1 ───────────┤
                     │
                  Serie 1 ────< Personaggio        (1..*)
                     │
                  Figure * ──── 1 Azienda ────< Gacha
                     │
              Recensione, VoceCollezione ──── User ──── Credentials
```

## API REST

| Metodo | Endpoint | Accesso |
|---|---|---|
| GET | `/api/figure?q=&serieId=&aziendaId=&materiale=&soloLimitate=&prezzoMin=&prezzoMax=` | pubblico |
| GET | `/api/figure/{id}` | pubblico |
| GET | `/api/serie`, `/api/aziende`, `/api/materiali` | pubblico |
| GET | `/api/figure/{id}/recensioni` | pubblico |
| POST | `/api/figure/{id}/recensioni` | autenticato → 201 |
| PUT | `/api/recensioni/{id}` | solo autore → 200 |
| DELETE | `/api/recensioni/{id}` | autore o ADMIN → 204 |

Errori: 400 validazione, 403 non proprietario, 404 non trovato, 409 vincolo violato.

## Frontend React

Il catalogo (`/catalogo`) e' un componente React montato su `#root-catalogo`
(`src/main/resources/static/js/catalogo.jsx`). Attualmente React, ReactDOM e
Babel sono caricati da CDN, cosi' il progetto gira senza passaggio di build.

Per passare a una build vera con Vite:

```bash
npm create vite@latest frontend -- --template react
# build output -> src/main/resources/static/js/
```

## Test

```bash
./mvnw test
```

I test girano su H2 in memoria e verificano le regole di business principali:
una sola recensione per utente per figure, modifica riservata all'autore,
vincolo di unicita' del catalogo, filtri di ricerca.

## Cosa manca (prossimi passi)

- paginazione dell'elenco figure e delle recensioni
- upload delle immagini dall'interfaccia (oggi i file si aggiungono a mano nel progetto)
- migrazione del frontend React a una build Vite
- documentazione OpenAPI/Swagger delle API REST
- deploy su piattaforma cloud e autenticazione OAuth
