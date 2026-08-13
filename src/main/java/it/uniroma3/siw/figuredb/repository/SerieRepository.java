package it.uniroma3.siw.figuredb.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.figuredb.model.Serie;
import it.uniroma3.siw.figuredb.model.TipoSerie;

public interface SerieRepository extends CrudRepository<Serie, Long> {

    List<Serie> findAllByOrderByNomeAsc();

    List<Serie> findByTipoOrderByNomeAsc(TipoSerie tipo);

    List<Serie> findByNomeContainingIgnoreCaseOrderByNomeAsc(String nome);

    boolean existsByNomeIgnoreCase(String nome);

    Optional<Serie> findByNomeIgnoreCase(String nome);

    /** Dettaglio serie: carica in una query la serie con i suoi personaggi. */
    @Query("select distinct s from Serie s left join fetch s.personaggi where s.id = :id")
    Optional<Serie> findByIdConPersonaggi(@Param("id") Long id);
}
