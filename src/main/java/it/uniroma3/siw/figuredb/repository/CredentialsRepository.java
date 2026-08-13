package it.uniroma3.siw.figuredb.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import it.uniroma3.siw.figuredb.model.Credentials;

public interface CredentialsRepository extends CrudRepository<Credentials, Long> {

    Optional<Credentials> findByUsername(String username);

    boolean existsByUsername(String username);
}
