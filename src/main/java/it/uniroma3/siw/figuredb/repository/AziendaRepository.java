package it.uniroma3.siw.figuredb.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import it.uniroma3.siw.figuredb.model.Azienda;

public interface AziendaRepository extends CrudRepository<Azienda, Long> {

    List<Azienda> findAllByOrderByNomeAsc();

    boolean existsByNomeIgnoreCase(String nome);

    Optional<Azienda> findByNomeIgnoreCase(String nome);
}
