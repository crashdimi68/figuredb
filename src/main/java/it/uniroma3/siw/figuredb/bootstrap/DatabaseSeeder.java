package it.uniroma3.siw.figuredb.bootstrap;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.figuredb.model.Azienda;
import it.uniroma3.siw.figuredb.model.Credentials;
import it.uniroma3.siw.figuredb.model.Disegnatore;
import it.uniroma3.siw.figuredb.model.Editore;
import it.uniroma3.siw.figuredb.model.Figure;
import it.uniroma3.siw.figuredb.model.Fumetto;
import it.uniroma3.siw.figuredb.model.Gacha;
import it.uniroma3.siw.figuredb.model.Materiale;
import it.uniroma3.siw.figuredb.model.Personaggio;
import it.uniroma3.siw.figuredb.model.Recensione;
import it.uniroma3.siw.figuredb.model.Scrittore;
import it.uniroma3.siw.figuredb.model.Serie;
import it.uniroma3.siw.figuredb.model.TipoSerie;
import it.uniroma3.siw.figuredb.model.User;
import it.uniroma3.siw.figuredb.repository.AziendaRepository;
import it.uniroma3.siw.figuredb.repository.CredentialsRepository;
import it.uniroma3.siw.figuredb.repository.DisegnatoreRepository;
import it.uniroma3.siw.figuredb.repository.EditoreRepository;
import it.uniroma3.siw.figuredb.repository.FigureRepository;
import it.uniroma3.siw.figuredb.repository.FumettoRepository;
import it.uniroma3.siw.figuredb.repository.GachaRepository;
import it.uniroma3.siw.figuredb.repository.RecensioneRepository;
import it.uniroma3.siw.figuredb.repository.ScrittoreRepository;
import it.uniroma3.siw.figuredb.repository.SerieRepository;
import it.uniroma3.siw.figuredb.repository.UserRepository;
import it.uniroma3.siw.figuredb.util.RisolutoreImmagini;

/**
 * Popola il database leggendo i file CSV di src/main/resources/dati.
 *
 * I dati del catalogo non sono scritti nel codice: stanno in file di testo
 * versionati con il progetto. Chiunque cloni il repository e avvii
 * l'applicazione ottiene esattamente lo stesso catalogo, immagini comprese.
 *
 * Il seeder interviene solo se il database e' vuoto.
 */
@Component
@Order(1)
public class DatabaseSeeder implements CommandLineRunner {

    private final SerieRepository serieRepository;
    private final AziendaRepository aziendaRepository;
    private final EditoreRepository editoreRepository;
    private final ScrittoreRepository scrittoreRepository;
    private final DisegnatoreRepository disegnatoreRepository;
    private final FumettoRepository fumettoRepository;
    private final FigureRepository figureRepository;
    private final GachaRepository gachaRepository;
    private final RecensioneRepository recensioneRepository;
    private final UserRepository userRepository;
    private final CredentialsRepository credentialsRepository;
    private final PasswordEncoder passwordEncoder;
    private final RisolutoreImmagini risolutoreImmagini;

    /** Nei test viene messo a false: le recensioni le inseriscono i test stessi. */
    @Value("${figuredb.seed.recensioni-esempio:true}")
    private boolean creaRecensioniEsempio;

    // indici nome -> entita', per risolvere i riferimenti tra i CSV
    private final Map<String, Serie> serieByNome = new HashMap<>();
    private final Map<String, Azienda> aziendeByNome = new HashMap<>();
    private final Map<String, Editore> editoriByNome = new HashMap<>();
    private final Map<String, Scrittore> scrittoriByNome = new HashMap<>();
    private final Map<String, Disegnatore> disegnatoriByNome = new HashMap<>();

    public DatabaseSeeder(SerieRepository serieRepository,
                          AziendaRepository aziendaRepository,
                          EditoreRepository editoreRepository,
                          ScrittoreRepository scrittoreRepository,
                          DisegnatoreRepository disegnatoreRepository,
                          FumettoRepository fumettoRepository,
                          FigureRepository figureRepository,
                          GachaRepository gachaRepository,
                          RecensioneRepository recensioneRepository,
                          UserRepository userRepository,
                          CredentialsRepository credentialsRepository,
                          PasswordEncoder passwordEncoder,
                          RisolutoreImmagini risolutoreImmagini) {
        this.serieRepository = serieRepository;
        this.aziendaRepository = aziendaRepository;
        this.editoreRepository = editoreRepository;
        this.scrittoreRepository = scrittoreRepository;
        this.disegnatoreRepository = disegnatoreRepository;
        this.fumettoRepository = fumettoRepository;
        this.figureRepository = figureRepository;
        this.gachaRepository = gachaRepository;
        this.recensioneRepository = recensioneRepository;
        this.userRepository = userRepository;
        this.credentialsRepository = credentialsRepository;
        this.passwordEncoder = passwordEncoder;
        this.risolutoreImmagini = risolutoreImmagini;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (this.serieRepository.count() > 0) {
            return;   // database gia' popolato: non tocco niente
        }
        System.out.println(">> [FigureDB] Carico il catalogo dai file CSV...");

        this.caricaSerie();
        this.caricaAziende();
        this.caricaEditori();
        this.caricaAutori();
        this.caricaFigure();
        this.caricaFumetti();
        this.caricaGacha();
        this.creaUtenze();
        if (this.creaRecensioniEsempio) {
            this.creaRecensioniDiEsempio();
        }

        System.out.println(">> [FigureDB] Catalogo caricato: "
                + this.figureRepository.count() + " figure, "
                + this.fumettoRepository.count() + " fumetti, "
                + this.gachaRepository.count() + " gacha, "
                + this.serieRepository.count() + " serie.");
    }

    // ------------------------------------------------------------------
    // serie.csv
    // ------------------------------------------------------------------
    private void caricaSerie() {
        for (Map<String, String> riga : LettoreCsv.leggi("dati/serie.csv")) {
            Serie serie = new Serie();
            serie.setNome(LettoreCsv.testoObbligatorio(riga, "nome", "serie.csv"));
            serie.setTipo(this.tipoSerie(
                    LettoreCsv.testoObbligatorio(riga, "tipo", "serie.csv"), serie.getNome()));
            serie.setDescrizione(LettoreCsv.testo(riga, "descrizione"));
            serie.setLogo(LettoreCsv.testo(riga, "logo"));

            List<String> nomiPersonaggi = LettoreCsv.elenco(riga, "personaggi");
            if (nomiPersonaggi.isEmpty()) {
                throw new IllegalStateException("In serie.csv la serie '" + serie.getNome()
                        + "' non ha personaggi: ne serve almeno uno.");
            }
            for (String nomePersonaggio : nomiPersonaggi) {
                Personaggio p = new Personaggio();
                p.setNome(nomePersonaggio);
                p.setDescrizione("Personaggio della serie " + serie.getNome());
                serie.aggiungiPersonaggio(p);
            }

            Serie salvata = this.serieRepository.save(serie);
            this.serieByNome.put(chiave(salvata.getNome()), salvata);
        }
    }

    // ------------------------------------------------------------------
    // aziende.csv
    // ------------------------------------------------------------------
    private void caricaAziende() {
        for (Map<String, String> riga : LettoreCsv.leggi("dati/aziende.csv")) {
            Azienda azienda = new Azienda();
            azienda.setNome(LettoreCsv.testoObbligatorio(riga, "nome", "aziende.csv"));
            azienda.setRegione(LettoreCsv.testoObbligatorio(riga, "regione", "aziende.csv"));
            azienda.setDescrizione(LettoreCsv.testo(riga, "descrizione"));
            azienda.setLogo(LettoreCsv.testo(riga, "logo"));

            Azienda salvata = this.aziendaRepository.save(azienda);
            this.aziendeByNome.put(chiave(salvata.getNome()), salvata);
        }
    }

    // ------------------------------------------------------------------
    // editori.csv
    // ------------------------------------------------------------------
    private void caricaEditori() {
        for (Map<String, String> riga : LettoreCsv.leggi("dati/editori.csv")) {
            Editore editore = new Editore();
            editore.setNome(LettoreCsv.testoObbligatorio(riga, "nome", "editori.csv"));
            editore.setRegione(LettoreCsv.testoObbligatorio(riga, "regione", "editori.csv"));
            editore.setDescrizione(LettoreCsv.testo(riga, "descrizione"));
            editore.setLogo(LettoreCsv.testo(riga, "logo"));

            Editore salvato = this.editoreRepository.save(editore);
            this.editoriByNome.put(chiave(salvato.getNome()), salvato);
        }
    }

    // ------------------------------------------------------------------
    // autori.csv  (ruolo: SCRITTORE, DISEGNATORE oppure ENTRAMBI)
    // ------------------------------------------------------------------
    private void caricaAutori() {
        for (Map<String, String> riga : LettoreCsv.leggi("dati/autori.csv")) {
            String nome = LettoreCsv.testoObbligatorio(riga, "nome", "autori.csv");
            String cognome = LettoreCsv.testoObbligatorio(riga, "cognome", "autori.csv");
            LocalDate nascita = this.data(
                    LettoreCsv.testoObbligatorio(riga, "dataNascita", "autori.csv"),
                    "autori.csv", nome + " " + cognome);
            String paese = LettoreCsv.testoObbligatorio(riga, "paese", "autori.csv");
            String ruolo = LettoreCsv.testo(riga, "ruolo");
            ruolo = (ruolo == null ? "ENTRAMBI" : ruolo.toUpperCase());

            if (ruolo.equals("SCRITTORE") || ruolo.equals("ENTRAMBI")) {
                Scrittore s = new Scrittore();
                s.setNome(nome);
                s.setCognome(cognome);
                s.setDataNascita(nascita);
                s.setPaese(paese);
                Scrittore salvato = this.scrittoreRepository.save(s);
                this.scrittoriByNome.put(chiave(salvato.getNomeCompleto()), salvato);
            }
            if (ruolo.equals("DISEGNATORE") || ruolo.equals("ENTRAMBI")) {
                Disegnatore d = new Disegnatore();
                d.setNome(nome);
                d.setCognome(cognome);
                d.setDataNascita(nascita);
                d.setPaese(paese);
                Disegnatore salvato = this.disegnatoreRepository.save(d);
                this.disegnatoriByNome.put(chiave(salvato.getNomeCompleto()), salvato);
            }
        }
    }

    // ------------------------------------------------------------------
    // figure.csv
    // ------------------------------------------------------------------
    private void caricaFigure() {
        for (Map<String, String> riga : LettoreCsv.leggi("dati/figure.csv")) {
            String nome = LettoreCsv.testoObbligatorio(riga, "nome", "figure.csv");

            Figure figure = new Figure();
            figure.setNome(nome);
            figure.setSerie(this.serie(LettoreCsv.testoObbligatorio(riga, "serie", "figure.csv"), nome));
            figure.setAzienda(this.azienda(
                    LettoreCsv.testoObbligatorio(riga, "azienda", "figure.csv"), nome));
            figure.setDataUscita(this.data(
                    LettoreCsv.testoObbligatorio(riga, "dataUscita", "figure.csv"), "figure.csv", nome));
            figure.setAltezza(LettoreCsv.numero(riga, "altezza", "figure.csv"));
            figure.setMateriale(this.materiale(
                    LettoreCsv.testoObbligatorio(riga, "materiale", "figure.csv"), nome));
            figure.setPrezzo(LettoreCsv.numero(riga, "prezzo", "figure.csv"));
            figure.setEdizioneLimitata(LettoreCsv.flag(riga, "edizioneLimitata"));
            figure.setDescrizione(LettoreCsv.testo(riga, "descrizione"));
            figure.setImmagine(this.risolutoreImmagini.risolviFigure(
                    LettoreCsv.testo(riga, "cartellaImmagini")));

            this.figureRepository.save(figure);
        }
    }

    // ------------------------------------------------------------------
    // fumetti.csv
    // ------------------------------------------------------------------
    private void caricaFumetti() {
        for (Map<String, String> riga : LettoreCsv.leggi("dati/fumetti.csv")) {
            String titolo = LettoreCsv.testoObbligatorio(riga, "titolo", "fumetti.csv");

            Fumetto fumetto = new Fumetto();
            fumetto.setTitolo(titolo);
            fumetto.setSerie(this.serie(
                    LettoreCsv.testoObbligatorio(riga, "serie", "fumetti.csv"), titolo));
            fumetto.setScrittore(this.scrittore(
                    LettoreCsv.testoObbligatorio(riga, "scrittore", "fumetti.csv"), titolo));
            fumetto.setEditore(this.editore(
                    LettoreCsv.testoObbligatorio(riga, "editore", "fumetti.csv"), titolo));
            fumetto.setIsbn(LettoreCsv.testoObbligatorio(riga, "isbn", "fumetti.csv"));
            fumetto.setPrezzo(LettoreCsv.numero(riga, "prezzo", "fumetti.csv"));
            fumetto.setGenere(LettoreCsv.testoObbligatorio(riga, "genere", "fumetti.csv"));
            fumetto.setVariant(LettoreCsv.flag(riga, "variant"));
            fumetto.setAnno(Math.round(LettoreCsv.numero(riga, "anno", "fumetti.csv")));
            fumetto.setImmagine(this.risolutoreImmagini.risolviFumetto(
                    LettoreCsv.testo(riga, "cartellaImmagini")));

            Set<Disegnatore> disegnatori = new HashSet<>();
            for (String nomeDisegnatore : LettoreCsv.elenco(riga, "disegnatori")) {
                disegnatori.add(this.disegnatore(nomeDisegnatore, titolo));
            }
            if (disegnatori.isEmpty()) {
                throw new IllegalStateException("In fumetti.csv il fumetto '" + titolo
                        + "' non ha disegnatori: ne serve almeno uno.");
            }
            fumetto.setDisegnatori(disegnatori);

            this.fumettoRepository.save(fumetto);
        }
    }

    // ------------------------------------------------------------------
    // gacha.csv
    // ------------------------------------------------------------------
    private void caricaGacha() {
        for (Map<String, String> riga : LettoreCsv.leggi("dati/gacha.csv")) {
            String nome = LettoreCsv.testoObbligatorio(riga, "nome", "gacha.csv");

            Gacha gacha = new Gacha();
            gacha.setNome(nome);
            gacha.setAzienda(this.azienda(
                    LettoreCsv.testoObbligatorio(riga, "azienda", "gacha.csv"), nome));
            gacha.setSerie(this.serie(
                    LettoreCsv.testoObbligatorio(riga, "serie", "gacha.csv"), nome));
            gacha.setDataUscita(this.data(
                    LettoreCsv.testoObbligatorio(riga, "dataUscita", "gacha.csv"), "gacha.csv", nome));
            gacha.setPrezzo(LettoreCsv.numero(riga, "prezzo", "gacha.csv"));
            gacha.setDescrizione(LettoreCsv.testo(riga, "descrizione"));
            gacha.setImmagine(this.risolutoreImmagini.risolviGacha(
                    LettoreCsv.testo(riga, "cartellaImmagini")));

            this.gachaRepository.save(gacha);
        }
    }

    // ------------------------------------------------------------------
    // Utenze e recensioni di esempio (non vengono da CSV)
    // ------------------------------------------------------------------
    private void creaUtenze() {
        this.creaUtente("Damiano", "Nanni", "admin@figuredb.it", "admin", "admin123",
                Credentials.ADMIN_ROLE);
        this.creaUtente("Mario", "Rossi", "mario.rossi@example.com", "damiano", "user123",
                Credentials.DEFAULT_ROLE);
        this.creaUtente("Giulia", "Bianchi", "giulia.bianchi@example.com", "giulia", "user123",
                Credentials.DEFAULT_ROLE);
    }

    private void creaUtente(String nome, String cognome, String email,
                            String username, String password, String ruolo) {
        User user = new User();
        user.setNome(nome);
        user.setCognome(cognome);
        user.setEmail(email);
        this.userRepository.save(user);

        Credentials credenziali = new Credentials();
        credenziali.setUsername(username);
        credenziali.setPassword(this.passwordEncoder.encode(password));
        credenziali.setRole(ruolo);
        credenziali.setUser(user);
        this.credentialsRepository.save(credenziali);
    }

    /** Qualche recensione, cosi' la pagina di dettaglio non e' vuota alla demo. */
    private void creaRecensioniDiEsempio() {
        List<Figure> figure = this.figureRepository.findAllConAziendaESerie();
        if (figure.isEmpty()) {
            return;
        }
        User mario = this.credentialsRepository.findByUsername("damiano")
                .map(Credentials::getUser).orElse(null);
        User giulia = this.credentialsRepository.findByUsername("giulia")
                .map(Credentials::getUser).orElse(null);

        String[][] testi = {
            {"Scultura impeccabile", "5",
             "I dettagli del volto e la resa del tessuto sono davvero notevoli per questa fascia di prezzo."},
            {"Bella ma fragile", "4",
             "Ottima resa cromatica, pero' gli accessori piu' sottili vanno maneggiati con molta attenzione."},
            {"Vale il prezzo", "5",
             "Confezione curata, base solida e nessuna sbavatura di vernice. Consigliata a chi colleziona la serie."},
            {"Sotto le aspettative", "3",
             "La posa e' dinamica ma la verniciatura del mio esemplare presenta qualche imprecisione."},
        };

        int indice = 0;
        for (String[] testo : testi) {
            if (indice >= figure.size()) {
                break;
            }
            User autore = (indice % 2 == 0) ? mario : giulia;
            if (autore != null) {
                Recensione r = new Recensione();
                r.setTitolo(testo[0]);
                r.setVoto(Integer.valueOf(testo[1]));
                r.setTesto(testo[2]);
                r.setData(LocalDate.now().minusDays(indice * 3L));
                r.setFigure(figure.get(indice));
                r.setAutore(autore);
                this.recensioneRepository.save(r);
            }
            indice++;
        }
    }

    // ------------------------------------------------------------------
    // Risoluzione dei riferimenti e conversioni, con errori parlanti
    // ------------------------------------------------------------------

    private static String chiave(String testo) {
        return testo == null ? "" : testo.trim().toLowerCase();
    }

    private Serie serie(String nome, String contesto) {
        Serie serie = this.serieByNome.get(chiave(nome));
        if (serie == null) {
            throw new IllegalStateException("La serie '" + nome + "' richiesta da '" + contesto
                    + "' non esiste in serie.csv. Controlla che il nome coincida esattamente.");
        }
        return serie;
    }

    private Azienda azienda(String nome, String contesto) {
        Azienda azienda = this.aziendeByNome.get(chiave(nome));
        if (azienda == null) {
            throw new IllegalStateException("L'azienda '" + nome + "' richiesta da '" + contesto
                    + "' non esiste in aziende.csv. Controlla che il nome coincida esattamente.");
        }
        return azienda;
    }

    private Editore editore(String nome, String contesto) {
        Editore editore = this.editoriByNome.get(chiave(nome));
        if (editore == null) {
            throw new IllegalStateException("L'editore '" + nome + "' richiesto da '" + contesto
                    + "' non esiste in editori.csv.");
        }
        return editore;
    }

    private Scrittore scrittore(String nomeCompleto, String contesto) {
        Scrittore scrittore = this.scrittoriByNome.get(chiave(nomeCompleto));
        if (scrittore == null) {
            throw new IllegalStateException("Lo scrittore '" + nomeCompleto + "' richiesto da '"
                    + contesto + "' non esiste in autori.csv, oppure ha ruolo DISEGNATORE.");
        }
        return scrittore;
    }

    private Disegnatore disegnatore(String nomeCompleto, String contesto) {
        Disegnatore disegnatore = this.disegnatoriByNome.get(chiave(nomeCompleto));
        if (disegnatore == null) {
            throw new IllegalStateException("Il disegnatore '" + nomeCompleto + "' richiesto da '"
                    + contesto + "' non esiste in autori.csv, oppure ha ruolo SCRITTORE.");
        }
        return disegnatore;
    }

    private LocalDate data(String valore, String file, String contesto) {
        try {
            return LocalDate.parse(valore.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalStateException("In " + file + ", alla voce '" + contesto
                    + "', la data '" + valore + "' non e' valida. Usa il formato 2024-03-15.");
        }
    }

    private TipoSerie tipoSerie(String valore, String contesto) {
        try {
            return TipoSerie.valueOf(valore.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("In serie.csv, alla serie '" + contesto
                    + "', il tipo '" + valore + "' non e' valido. Valori ammessi: "
                    + Arrays.toString(TipoSerie.values()));
        }
    }

    private Materiale materiale(String valore, String contesto) {
        try {
            return Materiale.valueOf(valore.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("In figure.csv, alla figure '" + contesto
                    + "', il materiale '" + valore + "' non e' valido. Valori ammessi: "
                    + Arrays.toString(Materiale.values()));
        }
    }
}
