package it.uniroma3.siw.figuredb.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.figuredb.model.Disegnatore;

public interface DisegnatoreRepository extends CrudRepository<Disegnatore, Long> {

    List<Disegnatore> findAllByOrderByCognomeAscNomeAsc();

    boolean existsByNomeIgnoreCaseAndCognomeIgnoreCase(String nome, String cognome);

    /** Dettaglio: carica il disegnatore con i fumetti a cui ha lavorato. */
    @Query("select distinct d from Disegnatore d left join fetch d.fumetti where d.id = :id")
    Optional<Disegnatore> findByIdConFumetti(@Param("id") Long id);
}
