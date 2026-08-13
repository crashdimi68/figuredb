package it.uniroma3.siw.figuredb.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import it.uniroma3.siw.figuredb.model.Editore;

public interface EditoreRepository extends CrudRepository<Editore, Long> {

    List<Editore> findAllByOrderByNomeAsc();

    boolean existsByNomeIgnoreCase(String nome);
}
