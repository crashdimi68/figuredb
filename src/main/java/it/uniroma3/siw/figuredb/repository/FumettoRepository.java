package it.uniroma3.siw.figuredb.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.figuredb.model.Fumetto;

public interface FumettoRepository extends CrudRepository<Fumetto, Long> {

    /** Elenco fumetti con scrittore, editore e serie in una sola query. */
    @Query("select distinct f from Fumetto f "
         + "join fetch f.scrittore "
         + "join fetch f.editore "
         + "join fetch f.serie "
         + "order by f.titolo")
    List<Fumetto> findAllConAutoriEditoreSerie();

    /** Dettaglio: aggiunge anche i disegnatori (molti-a-molti). */
    @Query("select distinct f from Fumetto f "
         + "join fetch f.scrittore "
         + "join fetch f.editore "
         + "join fetch f.serie "
         + "left join fetch f.disegnatori "
         + "where f.id = :id")
    Optional<Fumetto> findByIdCompleto(@Param("id") Long id);

    @Query("select distinct f from Fumetto f "
         + "join fetch f.scrittore "
         + "join fetch f.editore "
         + "where f.serie.id = :serieId order by f.anno")
    List<Fumetto> findBySerieIdConAutori(@Param("serieId") Long serieId);

    /**
     * Ricerca per titolo.
     *
     * NB: il fetch join non e' un dettaglio opzionale. L'elenco dei fumetti
     * mostra serie, scrittore ed editore di ogni riga, e con open-in-view=false
     * la transazione e' gia' chiusa quando il template legge quei valori.
     * Senza il fetch si otterrebbe una LazyInitializationException.
     */
    @Query("select distinct f from Fumetto f "
         + "join fetch f.scrittore "
         + "join fetch f.editore "
         + "join fetch f.serie "
         + "where lower(f.titolo) like lower(concat('%', :titolo, '%')) "
         + "order by f.titolo")
    List<Fumetto> cercaPerTitolo(@Param("titolo") String titolo);

    /** Stesso discorso: l'elenco filtrato per genere usa lo stesso template. */
    @Query("select distinct f from Fumetto f "
         + "join fetch f.scrittore "
         + "join fetch f.editore "
         + "join fetch f.serie "
         + "where lower(f.genere) = lower(:genere) "
         + "order by f.titolo")
    List<Fumetto> cercaPerGenere(@Param("genere") String genere);

    boolean existsByIsbn(String isbn);

    Optional<Fumetto> findByIsbn(String isbn);
}
