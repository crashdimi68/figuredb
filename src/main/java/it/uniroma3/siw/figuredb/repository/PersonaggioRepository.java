package it.uniroma3.siw.figuredb.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import it.uniroma3.siw.figuredb.model.Personaggio;

public interface PersonaggioRepository extends CrudRepository<Personaggio, Long> {

    List<Personaggio> findBySerieIdOrderByNomeAsc(Long serieId);

    boolean existsByNomeIgnoreCaseAndSerieId(String nome, Long serieId);
}
