package it.uniroma3.siw.figuredb.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.figuredb.model.Credentials;
import it.uniroma3.siw.figuredb.model.User;
import it.uniroma3.siw.figuredb.repository.CredentialsRepository;
import it.uniroma3.siw.figuredb.repository.UserRepository;
import it.uniroma3.siw.figuredb.service.exception.EntitaNonTrovataException;
import it.uniroma3.siw.figuredb.service.exception.VincoloViolatoException;

/**
 * Registrazione e recupero delle credenziali di accesso.
 */
@Service
public class CredentialsService {

    private final CredentialsRepository credentialsRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CredentialsService(CredentialsRepository credentialsRepository,
                              UserRepository userRepository,
                              PasswordEncoder passwordEncoder) {
        this.credentialsRepository = credentialsRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Credentials findByUsername(String username) {
        return this.credentialsRepository.findByUsername(username)
                .orElseThrow(() -> new EntitaNonTrovataException(
                        "Nessun utente registrato con username " + username));
    }

    @Transactional(readOnly = true)
    public User findUserByUsername(String username) {
        return this.findByUsername(username).getUser();
    }

    /**
     * Registrazione di un nuovo utente.
     * Il ruolo assegnato e' sempre DEFAULT: le utenze ADMIN vengono create
     * solo tramite il seeding iniziale del database.
     * SERIALIZABLE perche' la scrittura dipende dai controlli di unicita'.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Credentials registra(Credentials credenziali, User user) {
        if (this.credentialsRepository.existsByUsername(credenziali.getUsername())) {
            throw new VincoloViolatoException(
                    "Lo username '" + credenziali.getUsername() + "' e' gia' in uso");
        }
        if (this.userRepository.existsByEmail(user.getEmail())) {
            throw new VincoloViolatoException(
                    "L'email '" + user.getEmail() + "' e' gia' registrata");
        }
        credenziali.setUser(user);
        credenziali.setRole(Credentials.DEFAULT_ROLE);
        credenziali.setPassword(this.passwordEncoder.encode(credenziali.getPassword()));
        return this.credentialsRepository.save(credenziali);
    }
}
