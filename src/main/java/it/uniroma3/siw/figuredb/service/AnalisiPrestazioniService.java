package it.uniroma3.siw.figuredb.service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.figuredb.model.Figure;
import it.uniroma3.siw.figuredb.model.Serie;
import it.uniroma3.siw.figuredb.repository.FigureRepository;
import it.uniroma3.siw.figuredb.repository.SerieRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * ANALISI SPERIMENTALE DELL'ACCESSO AI DATI (sezione 8.2 della specifica).
 *
 * E' la versione "da interfaccia web" di quello che il
 * {@link it.uniroma3.siw.figuredb.bootstrap.FetchStrategyRunner} stampa a
 * console: l'amministratore apre /admin/prestazioni e il confronto viene
 * eseguito dal vivo sui dati che ci sono in quel momento nel database.
 *
 * Il caso d'uso misurato e' sempre lo stesso -- "caricare tutte le figure di
 * una serie mostrando per ognuna azienda produttrice e serie di appartenenza"
 * -- realizzato con tre strategie diverse:
 *
 *   1. LAZY          associazioni caricate su richiesta -> problema N+1
 *   2. JOIN FETCH    fetch join esplicito in JPQL       -> 1 query
 *   3. ENTITY GRAPH  @NamedEntityGraph dichiarativo     -> 1 query
 *
 * Perche' il confronto sia onesto:
 *   - si usa la serie con piu' figure, cosi' l'effetto N+1 si vede davvero;
 *   - prima di misurare si esegue un giro "a vuoto" di riscaldamento (la JVM
 *     compila il codice a caldo: la prima esecuzione e' sempre la piu' lenta,
 *     e senza riscaldamento la prima strategia risulterebbe penalizzata);
 *   - prima di ogni misura il persistence context viene svuotato, altrimenti
 *     la cache di primo livello servirebbe gli oggetti gia' caricati e la
 *     strategia successiva sembrerebbe istantanea.
 *
 * Tutto avviene dentro UNA sola transazione di sola lettura: la sessione deve
 * restare aperta, altrimenti la strategia LAZY solleverebbe
 * LazyInitializationException invece di mostrare le query aggiuntive.
 */
@Service
public class AnalisiPrestazioniService {

    private final FigureRepository figureRepository;
    private final SerieRepository serieRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public AnalisiPrestazioniService(FigureRepository figureRepository,
                                     SerieRepository serieRepository) {
        this.figureRepository = figureRepository;
        this.serieRepository = serieRepository;
    }

    /**
     * Esegue il confronto e restituisce l'esito gia' pronto per la vista.
     * Se il database e' vuoto restituisce un esito "senza dati".
     */
    @Transactional(readOnly = true)
    public EsitoAnalisi eseguiConfronto() {
        Serie serie = this.serieConPiuFigure();
        if (serie == null) {
            return new EsitoAnalisi(null, 0, List.of());
        }
        // Nome e id vanno letti adesso: piu' avanti il persistence context
        // viene svuotato a ogni misura e l'entita' resta staccata.
        Long serieId = serie.getId();
        String nomeSerie = serie.getNome();
        int quanteFigure = this.figureRepository.findBySerieIdConJoinFetch(serieId).size();

        Statistics statistiche = this.entityManager.getEntityManagerFactory()
                .unwrap(SessionFactory.class).getStatistics();
        statistiche.setStatisticsEnabled(true);

        // Giro di riscaldamento: i risultati vengono buttati via.
        this.misura("", "", statistiche, () -> this.figureRepository.findBySerieId(serieId));
        this.misura("", "", statistiche, () -> this.figureRepository.findBySerieIdConJoinFetch(serieId));
        this.misura("", "", statistiche, () -> this.figureRepository.findBySerieIdConEntityGraph(serieId));

        List<RisultatoStrategia> risultati = new ArrayList<>();

        risultati.add(this.misura("Default (LAZY)",
                "findBySerieId() e poi, per ogni figure, getAzienda().getNome(): le "
                + "associazioni sono LAZY, quindi ogni accesso scatena una SELECT "
                + "separata (1 query per l'elenco + N per le associazioni: e' il "
                + "problema delle N+1 query).",
                statistiche, () -> this.figureRepository.findBySerieId(serieId)));

        risultati.add(this.misura("JOIN FETCH esplicito",
                "findBySerieIdConJoinFetch(): JPQL scritta a mano 'select distinct f "
                + "from Figure f join fetch f.azienda join fetch f.serie', un'unica "
                + "query. Azienda e serie arrivano insieme alla figure, gia' "
                + "inizializzate.",
                statistiche, () -> this.figureRepository.findBySerieIdConJoinFetch(serieId)));

        risultati.add(this.misura("@EntityGraph",
                "findBySerieIdConEntityGraph() con @EntityGraph(\"Figure.conAziendaESerie\"): "
                + "stesso risultato del JOIN FETCH, ma dichiarativo, senza scrivere "
                + "la JPQL a mano.",
                statistiche, () -> this.figureRepository.findBySerieIdConEntityGraph(serieId)));

        this.calcolaPercentuali(risultati);
        return new EsitoAnalisi(nomeSerie, quanteFigure, risultati);
    }

    // ------------------------------------------------------------------

    /**
     * Esegue una strategia contando query SQL, oggetti caricati e tempo.
     */
    private RisultatoStrategia misura(String nome, String descrizione,
                                      Statistics statistiche,
                                      Supplier<List<Figure>> caricamento) {
        this.entityManager.clear();
        statistiche.clear();

        long inizio = System.nanoTime();
        List<Figure> figure = caricamento.get();

        // Simula cio' che fa la vista: legge azienda e serie di ogni figure.
        // Con la strategia LAZY e' qui che scattano le query aggiuntive.
        for (Figure f : figure) {
            f.getAzienda().getNome();
            f.getSerie().getNome();
        }
        double millisecondi = (System.nanoTime() - inizio) / 1_000_000.0;

        return new RisultatoStrategia(nome, descrizione,
                statistiche.getPrepareStatementCount(),
                figure.size(),
                millisecondi);
    }

    /** La strategia piu' lenta vale 100%, le altre in proporzione. */
    private void calcolaPercentuali(List<RisultatoStrategia> risultati) {
        double massimo = risultati.stream()
                .mapToDouble(RisultatoStrategia::getMillisecondi)
                .max().orElse(0);
        for (RisultatoStrategia r : risultati) {
            int percentuale = massimo > 0
                    ? (int) Math.round(r.getMillisecondi() * 100 / massimo)
                    : 100;
            r.setPercentuale(Math.max(percentuale, 4));   // barra sempre visibile
        }
    }

    /**
     * Sceglie la serie su cui misurare: quella con piu' figure a catalogo,
     * perche' e' quella in cui il divario fra le strategie e' piu' evidente.
     */
    private Serie serieConPiuFigure() {
        Serie migliore = null;
        long massimo = -1;
        for (Serie s : this.serieRepository.findAllByOrderByNomeAsc()) {
            long quante = this.figureRepository.countBySerieId(s.getId());
            if (quante > massimo) {
                massimo = quante;
                migliore = s;
            }
        }
        return massimo > 0 ? migliore : null;
    }

    /** Esito completo dell'analisi, pronto per il template. */
    public static class EsitoAnalisi {

        private final String serie;
        private final int quanteFigure;
        private final List<RisultatoStrategia> risultati;

        public EsitoAnalisi(String serie, int quanteFigure, List<RisultatoStrategia> risultati) {
            this.serie = serie;
            this.quanteFigure = quanteFigure;
            this.risultati = risultati;
        }

        public String getSerie() {
            return serie;
        }

        public int getQuanteFigure() {
            return quanteFigure;
        }

        public List<RisultatoStrategia> getRisultati() {
            return risultati;
        }

        public boolean isVuoto() {
            return risultati.isEmpty();
        }
    }
}
