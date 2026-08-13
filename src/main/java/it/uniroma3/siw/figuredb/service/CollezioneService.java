package it.uniroma3.siw.figuredb.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.figuredb.model.Credentials;
import it.uniroma3.siw.figuredb.model.Figure;
import it.uniroma3.siw.figuredb.model.StatoVoce;
import it.uniroma3.siw.figuredb.model.User;
import it.uniroma3.siw.figuredb.model.VoceCollezione;
import it.uniroma3.siw.figuredb.repository.CredentialsRepository;
import it.uniroma3.siw.figuredb.repository.FigureRepository;
import it.uniroma3.siw.figuredb.repository.VoceCollezioneRepository;
import it.uniroma3.siw.figuredb.service.exception.EntitaNonTrovataException;
import it.uniroma3.siw.figuredb.service.exception.OperazioneNonAutorizzataException;
import it.uniroma3.siw.figuredb.service.exception.VincoloViolatoException;

/**
 * Casi d'uso riservati agli utenti registrati: collezione personale e wishlist.
 */
@Service
public class CollezioneService {

    private final VoceCollezioneRepository voceRepository;
    private final FigureRepository figureRepository;
    private final CredentialsRepository credentialsRepository;

    public CollezioneService(VoceCollezioneRepository voceRepository,
                             FigureRepository figureRepository,
                             CredentialsRepository credentialsRepository) {
        this.voceRepository = voceRepository;
        this.figureRepository = figureRepository;
        this.credentialsRepository = credentialsRepository;
    }

    @Transactional(readOnly = true)
    public List<VoceCollezione> collezioneDi(String username) {
        User utente = this.recuperaUtente(username);
        return this.voceRepository.findByUtenteIdConFigure(utente.getId());
    }

    @Transactional(readOnly = true)
    public List<VoceCollezione> collezioneDi(String username, StatoVoce stato) {
        User utente = this.recuperaUtente(username);
        return this.voceRepository.findByUtenteIdEStato(utente.getId(), stato);
    }

    @Transactional(readOnly = true)
    public Double valoreCollezione(String username) {
        User utente = this.recuperaUtente(username);
        Number valore = this.voceRepository.valoreCollezione(utente.getId(), StatoVoce.POSSEDUTA);
        return valore != null ? valore.doubleValue() : 0.0;
    }

    @Transactional(readOnly = true)
    public boolean isInCollezione(String username, Long figureId) {
        User utente = this.recuperaUtente(username);
        return this.voceRepository.existsByUtenteIdAndFigureId(utente.getId(), figureId);
    }

    /**
     * CASO D'USO (USER) — AGGIUNTA DI UNA FIGURE ALLA PROPRIA COLLEZIONE.
     * Coinvolge tre repository; la scrittura dipende da una lettura
     * (la figure non deve essere gia' presente), quindi SERIALIZABLE.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public VoceCollezione aggiungi(Long figureId, StatoVoce stato, Integer quantita,
                                   String note, String username) {
        Figure figure = this.figureRepository.findById(figureId)
                .orElseThrow(() -> new EntitaNonTrovataException("Figure", figureId));
        User utente = this.recuperaUtente(username);

        if (this.voceRepository.existsByUtenteIdAndFigureId(utente.getId(), figureId)) {
            throw new VincoloViolatoException(
                    "'" + figure.getNome() + "' e' gia' nella tua collezione");
        }

        VoceCollezione voce = new VoceCollezione();
        voce.setFigure(figure);
        voce.setUtente(utente);
        voce.setStato(stato);
        voce.setQuantita(quantita != null && quantita > 0 ? quantita : 1);
        voce.setNote(note);
        return this.voceRepository.save(voce);
    }

    /** CASO D'USO (USER) — AGGIORNAMENTO di una voce della propria collezione. */
    @Transactional
    public VoceCollezione aggiorna(Long voceId, StatoVoce stato, Integer quantita,
                                   String note, String username) {
        VoceCollezione voce = this.recuperaVoceDiProprieta(voceId, username);
        voce.setStato(stato);
        if (quantita != null && quantita > 0) {
            voce.setQuantita(quantita);
        }
        voce.setNote(note);
        return this.voceRepository.save(voce);
    }

    /** CASO D'USO (USER) — RIMOZIONE di una voce dalla propria collezione. */
    @Transactional
    public void rimuovi(Long voceId, String username) {
        this.voceRepository.delete(this.recuperaVoceDiProprieta(voceId, username));
    }

    // ------------------------------------------------------------------

    private VoceCollezione recuperaVoceDiProprieta(Long voceId, String username) {
        VoceCollezione voce = this.voceRepository.findById(voceId)
                .orElseThrow(() -> new EntitaNonTrovataException("VoceCollezione", voceId));
        User utente = this.recuperaUtente(username);
        if (!voce.getUtente().getId().equals(utente.getId())) {
            throw new OperazioneNonAutorizzataException(
                    "Puoi modificare solo le voci della tua collezione");
        }
        return voce;
    }

    private User recuperaUtente(String username) {
        Credentials credenziali = this.credentialsRepository.findByUsername(username)
                .orElseThrow(() -> new EntitaNonTrovataException(
                        "Nessun utente registrato con username " + username));
        return credenziali.getUser();
    }
}
