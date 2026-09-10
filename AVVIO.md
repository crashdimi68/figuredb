# FigureDB — come compilare e avviare

Guida per Windows, da zero. Ogni comando va scritto nel terminale aperto **dentro la cartella del progetto** (quella che contiene `pom.xml`).

---

## 0. Cosa NON devi installare

- **HTML e CSS** non si compilano: sono file di testo che il browser legge così come sono.
- **React**: in questa bozza è caricato dal browser tramite CDN, quindi **non serve npm né Node.js**. Il file `catalogo.jsx` viene tradotto al volo da Babel dentro il browser.
- **Maven**: non si installa. Nel progetto c'è `mvnw.cmd` (Maven Wrapper): è uno script che scarica Maven da solo alla prima esecuzione.

L'unica cosa che devi davvero installare è **Java**. E, più avanti, PostgreSQL.

---

## 1. Installa Java (JDK 17 o superiore)

Apri PowerShell (tasto Windows → scrivi `powershell` → Invio) e digita:

```powershell
java -version
```

**Se vedi `17`, `21`, `24` o superiore** → sei a posto, salta al passo 2.

**Se dà errore o vedi un numero minore di 17**, installa il JDK:

1. Vai su <https://adoptium.net/>
2. Scarica **Temurin 21 (LTS)** per Windows x64, file `.msi`
3. Durante l'installazione, nella schermata dei componenti, metti su **"Set JAVA_HOME variable"** e **"Add to PATH"** l'opzione *"Will be installed on local hard drive"*
4. **Chiudi e riapri PowerShell** (le variabili d'ambiente si aggiornano solo nelle finestre nuove)
5. Ricontrolla con `java -version`

> `JDK` = kit per sviluppare (compilatore incluso). `JRE` = solo per eseguire. A te serve il **JDK**.

---

## 2. Apri il terminale dentro la cartella del progetto

Il modo più semplice, senza scrivere percorsi:

1. Apri Esplora File e vai nella cartella `figuredb` (quella con dentro `pom.xml`, `src`, `mvnw.cmd`)
2. Clicca nella **barra dell'indirizzo** in alto, cancella tutto e scrivi `powershell`, poi Invio

Si apre PowerShell già posizionato lì. Verifica:

```powershell
dir
```

Devi vedere `pom.xml`, `mvnw.cmd`, `src`, `README.md`.

---

## 3. Prima compilazione

```powershell
.\mvnw.cmd clean compile
```

Il `.\` davanti è obbligatorio in PowerShell: dice "esegui il file che sta in questa cartella".

**Cosa succede:** la prima volta scarica Maven e tutte le librerie di Spring Boot (circa 150 MB). Vedrai centinaia di righe `Downloading from central: ...`. Può volerci **3–10 minuti**. Dalla seconda volta sono pochi secondi.

**Risultato atteso**, in fondo:

```
[INFO] BUILD SUCCESS
```

Se invece leggi `BUILD FAILURE`, cerca la prima riga che inizia con `[ERROR]` — è lì il problema. Vedi la sezione *Errori comuni* in fondo.

**Cosa significa "compilare":** Maven trasforma i file `.java` in file `.class` (bytecode che la JVM sa eseguire) e li mette nella cartella `target/`. Quella cartella è generata: non la tocchi mai e non va su Git.

---

## 4. Avvia l'applicazione (modo veloce, senza database da installare)

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=h2"
```

> Le virgolette servono: senza, PowerShell interpreta male il parametro.

`h2` è un database che vive **nella memoria RAM**: non installi niente, ma i dati spariscono quando fermi l'applicazione. Perfetto per provare subito.

**Aspetta** finché non vedi righe simili a queste:

```
>> [FigureDB] Popolamento del database di esempio...
>> [FigureDB] Database popolato: 120 figure, 32 fumetti, 8 serie.
Tomcat started on port 8080 (http)
Started FiguredbApplication in 4.312 seconds
```

Il terminale **resta occupato**: è normale, l'applicazione è in esecuzione. Per fermarla: **Ctrl + C**.

---

## 5. Guarda il sito

Apri il browser su **<http://localhost:8080>**

Cose da provare:

| Dove | Cosa vedi |
|---|---|
| `/catalogo` | La parte React: filtri per serie, azienda, materiale, prezzo |
| `/serie` | Elenco serie (Thymeleaf) |
| `/figure/1` | Dettaglio di una figure |
| `/api/figure` | Il JSON grezzo che React consuma |

**Accedi** da `/login`:

- `admin` / `admin123` → vedi il menu **Amministrazione**
- `damiano` / `user123` → puoi scrivere e modificare le tue recensioni

---

## 6. Passare a PostgreSQL (serve per la consegna)

Il prof richiede PostgreSQL. H2 va bene per sviluppare, ma il progetto finale deve girare su Postgres.

**Installazione:**

1. Scarica da <https://www.postgresql.org/download/windows/>
2. Durante l'installazione ti chiede una **password per l'utente `postgres`**: scrivila da qualche parte, ti serve subito dopo
3. Lascia la porta di default **5432**
4. Installa anche **pgAdmin 4** (viene proposto nella stessa installazione): è l'interfaccia grafica

**Crea il database:**

Apri pgAdmin 4 → nel pannello a sinistra espandi `Servers` → `PostgreSQL` (ti chiede la password) → tasto destro su `Databases` → **Create → Database…** → Nome: `figuredb` → Save.

**Configura il progetto:**

Apri `src\main\resources\application.properties` con un editor di testo e metti la tua password:

```properties
spring.datasource.username=postgres
spring.datasource.password=LA_TUA_PASSWORD
```

**Avvia** (senza il profilo `h2`):

```powershell
.\mvnw.cmd spring-boot:run
```

---

## 7. Esegui i test

```powershell
.\mvnw.cmd test
```

Verificano le regole di business: una sola recensione per utente per figure, modifica riservata all'autore, niente duplicati a catalogo, filtri di ricerca. Girano su H2, quindi non serve Postgres.

Alla fine devi leggere:

```
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 8. Lo script sulle strategie di fetch (quello che il prof ti farà eseguire all'orale)

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=fetch-demo"
```

Nel terminale, subito dopo l'avvio, comparirà il confronto tra le tre strategie con il numero di query SQL eseguite. È la dimostrazione del problema **N+1**: la strategia LAZY fa 16 query dove le altre ne fanno 1.

Ferma con Ctrl + C quando hai finito di leggere.

---

## Errori comuni

**`.\mvnw.cmd : Impossibile trovare il percorso`**
Non sei nella cartella giusta. Fai `dir` e controlla che ci sia `mvnw.cmd`.

**`JAVA_HOME not found in your environment`**
Java non è installato o PowerShell non lo vede. Torna al passo 1 e ricorda di **riaprire il terminale** dopo l'installazione.

**`invalid target release: 17`**
Hai una versione di Java troppo vecchia. Serve il JDK 17 o superiore.

**`Web server failed to start. Port 8080 was already in use`**
Un'altra applicazione occupa la porta. Aggiungi in `application.properties`:
```properties
server.port=8081
```
e usa `http://localhost:8081`.

**`Connection to localhost:5432 refused`**
PostgreSQL non è avviato. Tasto Windows → `services.msc` → cerca `postgresql-x64-…` → tasto destro → Avvia.

**`FATAL: password authentication failed for user "postgres"`**
La password in `application.properties` non coincide con quella scelta durante l'installazione di Postgres.

**Whitelabel Error Page** nel browser
L'URL non esiste o c'è un errore applicativo. Guarda il **terminale**: lo stack trace ti dice la riga esatta.

---

## Se preferisci un ambiente grafico

Invece del terminale puoi usare **IntelliJ IDEA Community** (gratuito, <https://www.jetbrains.com/idea/download/>):

1. `File → Open…` → seleziona la cartella `figuredb` (non un file, la cartella)
2. Attendi che in basso finisca l'indicizzazione e il download delle dipendenze
3. Apri `src/main/java/it/uniroma3/siw/figuredb/FiguredbApplication.java`
4. Clicca il triangolino verde accanto a `public class FiguredbApplication`

Per i profili: `Run → Edit Configurations… → Active profiles: h2`.

---

## Riepilogo comandi

```powershell
.\mvnw.cmd clean compile                                      # compila
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=h2"    # avvia con H2
.\mvnw.cmd spring-boot:run                                    # avvia con PostgreSQL
.\mvnw.cmd test                                               # esegue i test
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=fetch-demo"   # demo N+1
```
