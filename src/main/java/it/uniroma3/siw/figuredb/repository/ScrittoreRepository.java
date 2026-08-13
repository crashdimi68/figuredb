package it.uniroma3.siw.figuredb.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.figuredb.model.Scrittore;

public interface ScrittoreRepository extends CrudRepository<Scrittore, Long> {

    List<Scrittore> findAllByOrderByCognomeAscNomeAsc(); 

    boolean existsByNomeIgnoreCaseAndCognomeIgnoreCase(String nome, String cognome);

    /** Dettaglio: carica lo scrittore con la sua bibliografia in una query. */
    @Query("select distinct s from Scrittore s left join fetch s.fumetti where s.id = :id")
    Optional<Scrittore> findByIdConFumetti(@Param("id") Long id);
}
