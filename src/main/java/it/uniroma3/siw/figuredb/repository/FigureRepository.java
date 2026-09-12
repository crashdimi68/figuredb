package it.uniroma3.siw.figuredb.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.figuredb.model.Figure;

public interface FigureRepository extends CrudRepository<Figure, Long>, FigureRepositoryCustom {

    /** Elenco completo con azienda e serie gia' caricate (una sola query). */
    @Query("select distinct f from Figure f "
         + "join fetch f.azienda "
         + "join fetch f.serie "
         + "order by f.nome")
    List<Figure> findAllConAziendaESerie();

    /**
     * Elenco paginato, usato da /figure.
     *
     * Con Pageable il database restituisce solo la pagina richiesta (LIMIT e
     * OFFSET) invece di tutte le figure. La countQuery separata serve a Spring
     * Data per sapere quante pagine ci sono in totale: senza di essa non
     * saprebbe contare le righe di una query con join fetch.
     */
    @Query(value = "select f from Figure f "
                 + "join fetch f.azienda "
                 + "join fetch f.serie",
           countQuery = "select count(f) from Figure f")
    Page<Figure> findPaginaConAziendaESerie(Pageable pageable);

    /** Dettaglio: carica in una sola query figure + azienda + serie. */
    @Query("select f from Figure f "
         + "join fetch f.azienda "
         + "join fetch f.serie "
         + "where f.id = :id")
    Optional<Figure> findByIdConAziendaESerie(@Param("id") Long id);

    /** Dettaglio con le recensioni e i relativi autori (per la pagina di dettaglio). */
    @Query("select distinct f from Figure f "
         + "left join fetch f.recensioni r "
         + "left join fetch r.autore "
         + "where f.id = :id")
    Optional<Figure> findByIdConRecensioni(@Param("id") Long id);

    /**
     * Edizioni limitate con azienda e serie gia' caricate.
     * Anche qui il fetch join e' necessario: la vista mostra il produttore
     * di ogni figure, e con LAZY avremmo una query per riga.
     */
    @Query("select f from Figure f "
         + "join fetch f.azienda "
         + "join fetch f.serie "
         + "where f.edizioneLimitata = true "
         + "order by f.dataUscita desc")
    List<Figure> findEdizioniLimitate();



    /** Figure di una azienda, con serie gia' caricata. */
    @Query("select f from Figure f "
         + "join fetch f.azienda "
         + "join fetch f.serie "
         + "where f.azienda.id = :aziendaId "
         + "order by f.dataUscita desc")
    List<Figure> findByAziendaIdConSerie(@Param("aziendaId") Long aziendaId);

    long countBySerieId(Long serieId);

    /**
     * Verifica del vincolo di consistenza del catalogo: la stessa azienda
     * non puo' pubblicare due figure con lo stesso nome nella stessa data.
     */
    @Query("select count(f) > 0 from Figure f "
         + "where lower(f.nome) = lower(:nome) "
         + "and f.azienda.id = :aziendaId "
         + "and f.dataUscita = :dataUscita")
    boolean esisteDuplicato(@Param("nome") String nome,
                            @Param("aziendaId") Long aziendaId,
                            @Param("dataUscita") LocalDate dataUscita);

    /** Variante usata in aggiornamento: esclude la figure che si sta modificando. */
    @Query("select count(f) > 0 from Figure f "
         + "where lower(f.nome) = lower(:nome) "
         + "and f.azienda.id = :aziendaId "
         + "and f.dataUscita = :dataUscita "
         + "and f.id <> :idDaEscludere")
    boolean esisteDuplicatoEscludendo(@Param("nome") String nome,
                                      @Param("aziendaId") Long aziendaId,
                                      @Param("dataUscita") LocalDate dataUscita,
                                      @Param("idDaEscludere") Long idDaEscludere);

    // ---------------------------------------------------------------------
    // Metodi usati dallo script di analisi delle strategie di fetch
    // (vedi bootstrap/FetchStrategyRunner). Realizzano lo STESSO caso d'uso
    // con tre strategie diverse.
    // ---------------------------------------------------------------------

    /** STRATEGIA 1 - LAZY: azienda e serie NON vengono caricate (rischio N+1). */
    List<Figure> findBySerieId(Long serieId);

    /** STRATEGIA 2 - JOIN FETCH esplicita nella query JPQL. */
    @Query("select distinct f from Figure f "
         + "join fetch f.azienda "
         + "join fetch f.serie "
         + "where f.serie.id = :serieId")
    List<Figure> findBySerieIdConJoinFetch(@Param("serieId") Long serieId);

    /** STRATEGIA 3 - EntityGraph dichiarativo definito sulla entita' Figure. */
    @EntityGraph(value = "Figure.conAziendaESerie")
    @Query("select f from Figure f where f.serie.id = :serieId")
    List<Figure> findBySerieIdConEntityGraph(@Param("serieId") Long serieId);
}
