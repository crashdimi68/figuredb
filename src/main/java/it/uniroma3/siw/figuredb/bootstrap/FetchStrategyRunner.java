package it.uniroma3.siw.figuredb.bootstrap;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
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
 * Esegue lo STESSO caso d'uso -- "caricare tutte le figure di una serie
 * mostrando per ognuna azienda produttrice e serie di appartenenza" --
 * con tre strategie di accesso diverse, sullo stesso insieme di dati:
 *
 *   1. LAZY            associazioni caricate su richiesta  -> N+1 query
 *   2. JOIN FETCH      fetch join esplicito in JPQL        -> 1 query
 *   3. ENTITY GRAPH    @NamedEntityGraph dichiarativo      -> 1 query
 *
 * Per ogni strategia vengono stampati numero di query SQL, tempo di
 * esecuzione e numero di oggetti caricati.
 *
 * ESECUZIONE:
 *   ./mvnw spring-boot:run -Dspring-boot.run.profiles=fetch-demo
 *
 * Il conteggio delle query usa le statistiche di Hibernate, abilitate nel
 * profilo con hibernate.generate_statistics=true.
 */
@Component
@Profile("fetch-demo")
@Order(2)
public class FetchStrategyRunner implements CommandLineRunner {

    private final FigureRepository figureRepository;
    private final SerieRepository serieRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public FetchStrategyRunner(FigureRepository figureRepository, SerieRepository serieRepository) {
        this.figureRepository = figureRepository;
        this.serieRepository = serieRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public void run(String... args) {
        List<Serie> tutteLeSerie = this.serieRepository.findAllByOrderByNomeAsc();
        if (tutteLeSerie.isEmpty()) {
            System.out.println("Nessuna serie nel database: impossibile eseguire il test.");
            return;
        }
        Long serieId = tutteLeSerie.get(0).getId();
        String nomeSerie = tutteLeSerie.get(0).getNome();

        Statistics statistiche = this.entityManager.getEntityManagerFactory()
                .unwrap(SessionFactory.class).getStatistics();
        statistiche.setStatisticsEnabled(true);

        System.out.println();
        System.out.println("=== Test accesso alle figure della serie \"" + nomeSerie + "\" ===");
        System.out.println("Caso d'uso: per ogni figure servono nome, azienda produttrice e serie.");
        System.out.println();

        this.esegui("Strategia 1: LAZY (associazioni caricate su richiesta)", statistiche,
                () -> this.figureRepository.findBySerieId(serieId));

        this.esegui("Strategia 2: JOIN FETCH (fetch join esplicito in JPQL)", statistiche,
                () -> this.figureRepository.findBySerieIdConJoinFetch(serieId));

        this.esegui("Strategia 3: ENTITY GRAPH (@NamedEntityGraph)", statistiche,
                () -> this.figureRepository.findBySerieIdConEntityGraph(serieId));

        System.out.println("Nota: la strategia LAZY esegue 1 query per l'elenco piu' una query");
        System.out.println("per ogni associazione non ancora caricata (problema delle N+1 query).");
        System.out.println("Le altre due strategie risolvono tutto con una sola query SQL.");
        System.out.println();
    }

    /**
     * Esegue una strategia misurando query eseguite, tempo e oggetti caricati.
     * Il persistence context viene svuotato prima di ogni misura, altrimenti
     * la cache di primo livello falserebbe il confronto.
     */
    private void esegui(String etichetta, Statistics statistiche, Caricamento caricamento) {
        this.entityManager.clear();
        statistiche.clear();

        long inizio = System.nanoTime();
        List<Figure> figure = caricamento.esegui();

        // Simula cio' che fa la vista: accede ad azienda e serie di ogni figure.
        // Con la strategia LAZY e' qui che scattano le query aggiuntive.
        int caratteriLetti = 0;
        for (Figure f : figure) {
            caratteriLetti += f.getAzienda().getNome().length();
            caratteriLetti += f.getSerie().getNome().length();
        }
        long durataMs = (System.nanoTime() - inizio) / 1_000_000;

        System.out.println(etichetta);
        System.out.println("  Figure caricate : " + figure.size());
        System.out.println("  Query SQL       : " + statistiche.getPrepareStatementCount());
        System.out.println("  Tempo           : " + durataMs + " ms");
        System.out.println("  Entita' in cache: " + statistiche.getEntityLoadCount()
                + " (caratteri letti: " + caratteriLetti + ")");
        System.out.println();
    }

    @FunctionalInterface
    private interface Caricamento {
        List<Figure> esegui();
    }
}
