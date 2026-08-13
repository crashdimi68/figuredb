package it.uniroma3.siw.figuredb.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.figuredb.model.Gacha;

public interface GachaRepository extends CrudRepository<Gacha, Long> {

    /**
     * Elenco completo. Azienda e serie sono caricate con un join fetch perche'
     * le viste le mostrano su ogni riga: con LAZY si avrebbe una query per riga.
     */
    @Query("select g from Gacha g "
         + "join fetch g.azienda "
         + "join fetch g.serie "
         + "order by g.dataUscita desc")
    List<Gacha> findAllConAziendaESerie();

    /** Dettaglio di un gacha, con azienda e serie gia' caricate. */
    @Query("select g from Gacha g "
         + "join fetch g.azienda "
         + "join fetch g.serie "
         + "where g.id = :id")
    Optional<Gacha> findByIdCompleto(@Param("id") Long id);

    /** Gacha di una serie: usato dalla pagina di dettaglio della serie. */
    @Query("select g from Gacha g "
         + "join fetch g.azienda "
         + "join fetch g.serie "
         + "where g.serie.id = :serieId "
         + "order by g.dataUscita desc")
    List<Gacha> findBySerieIdConAzienda(@Param("serieId") Long serieId);

    /** Gacha di un'azienda: usato dalla pagina di dettaglio dell'azienda. */
    @Query("select g from Gacha g "
         + "join fetch g.azienda "
         + "join fetch g.serie "
         + "where g.azienda.id = :aziendaId "
         + "order by g.dataUscita desc")
    List<Gacha> findByAziendaIdConSerie(@Param("aziendaId") Long aziendaId);

    long countBySerieId(Long serieId);
}
