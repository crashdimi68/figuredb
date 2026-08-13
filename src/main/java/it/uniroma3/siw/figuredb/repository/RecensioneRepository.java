package it.uniroma3.siw.figuredb.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.figuredb.model.Recensione;

public interface RecensioneRepository extends CrudRepository<Recensione, Long> {

    /** Regola di business: una sola recensione per coppia (autore, figure). */
    boolean existsByAutoreIdAndFigureId(Long autoreId, Long figureId);

    Optional<Recensione> findByAutoreIdAndFigureId(Long autoreId, Long figureId);

    @Query("select r from Recensione r join fetch r.autore "
         + "where r.figure.id = :figureId order by r.data desc")
    List<Recensione> findByFigureIdConAutore(@Param("figureId") Long figureId);

    @Query("select r from Recensione r join fetch r.figure "
         + "where r.autore.id = :autoreId order by r.data desc")
    List<Recensione> findByAutoreIdConFigure(@Param("autoreId") Long autoreId);

    @Query("select avg(r.voto) from Recensione r where r.figure.id = :figureId")
    Double mediaVotiPerFigure(@Param("figureId") Long figureId);

    long countByFigureId(Long figureId);
}
