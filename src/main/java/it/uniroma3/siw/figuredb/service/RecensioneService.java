package it.uniroma3.siw.figuredb.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.figuredb.model.Credentials;
import it.uniroma3.siw.figuredb.model.Figure;
import it.uniroma3.siw.figuredb.model.Recensione;
import it.uniroma3.siw.figuredb.model.User;
import it.uniroma3.siw.figuredb.repository.CredentialsRepository;
import it.uniroma3.siw.figuredb.repository.FigureRepository;
import it.uniroma3.siw.figuredb.repository.RecensioneRepository;
import it.uniroma3.siw.figuredb.service.exception.EntitaNonTrovataException;
import it.uniroma3.siw.figuredb.service.exception.OperazioneNonAutorizzataException;
import it.uniroma3.siw.figuredb.service.exception.VincoloViolatoException;

/**
 * Casi d'uso riservati agli utenti registrati: gestione delle recensioni.
 */
@Service
public class RecensioneService {

    private final RecensioneRepository recensioneRepository;
    private final FigureRepository figureRepository;
    private final CredentialsRepository credentialsRepository;

    public RecensioneService(RecensioneRepository recensioneRepository,
                             FigureRepository figureRepository,
                             CredentialsRepository credentialsRepository) {
        this.recensioneRepository = recensioneRepository;
        this.figureRepository = figureRepository;
        this.credentialsRepository = credentialsRepository;
    }

    @Transactional(readOnly = true)
    public List<Recensione> findByFigure(Long figureId) {
        return this.recensioneRepository.findByFigureIdConAutore(figureId);
    }

    @Transactional(readOnly = true)
    public List<Recensione> findByAutore(Long autoreId) {
        return this.recensioneRepository.findByAutoreIdConFigure(autoreId);
    }

    @Transactional(readOnly = true)
    public Recensione findById(Long id) {
        return this.recensioneRepository.findById(id)
                .orElseThrow(() -> new EntitaNonTrovataException("Recensione", id));
    }

    @Transactional(readOnly = true)
    public boolean haGiaRecensito(String username, Long figureId) {
        User autore = this.recuperaUtente(username);
        return this.recensioneRepository.existsByAutoreIdAndFigureId(autore.getId(), figureId);
    }

    /**
     * CASO D'USO (USER) — INSERIMENTO DI UNA RECENSIONE.
     *
     * Operazione che coinvolge tre repository (Recensione, Figure, Credentials):
     *   1. recupero della figure recensita
     *   2. recupero dell'utente autenticato
     *   3. verifica che l'utente non abbia gia' recensito quella figure
     *   4. creazione della recensione
     *
     * Isolamento SERIALIZABLE: il passo 4 (scrittura) dipende dall'esito del
     * passo 3 (lettura). Con Read Committed due invii ravvicinati dello stesso
     * form potrebbero superare entrambi il controllo. Il vincolo di unicita'
     * dichiarato sulla tabella (uk_recensione_autore_figure) resta come
     * ultima difesa a livello di database.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Recensione inserisci(Recensione recensione, Long figureId, String username) {
        // 1. recupero della figure
        Figure figure = this.figureRepository.findById(figureId)
                .orElseThrow(() -> new EntitaNonTrovataException("Figure", figureId));

        // 2. recupero dell'utente autenticato
        User autore = this.recuperaUtente(username);

        // 3. verifica della regola "una sola recensione per utente per figure"
        if (this.recensioneRepository.existsByAutoreIdAndFigureId(autore.getId(), figureId)) {
            throw new VincoloViolatoException(
                    "Hai gia' recensito la figure '" + figure.getNome() + "'");
        }

        // 4. creazione
        recensione.setFigure(figure);
        recensione.setAutore(autore);
        recensione.setData(LocalDate.now());
        return this.recensioneRepository.save(recensione);
    }

    /**
     * Recensione da mostrare nella form di modifica.
     *
     * Il controllo di proprieta' si fa gia' qui, all'apertura della form, e non
     * solo al salvataggio: altrimenti chiunque conosca l'id (un amministratore
     * compreso) vedrebbe una form che poi rifiuta il salvataggio.
     */
    @Transactional(readOnly = true)
    public Recensione findPerModifica(Long id, String username) {
        Recensione recensione = this.findById(id);
        this.verificaProprietario(recensione, username);
        return recensione;
    }

    /**
     * CASO D'USO (USER) — MODIFICA DI UNA PROPRIA RECENSIONE.
     *
     * Solo l'autore puo' modificare il testo di una recensione: nemmeno
     * l'amministratore puo' farlo, perche' cambiare le parole di qualcun altro
     * lasciandogli la firma sarebbe una falsificazione. La moderazione
     * dell'ADMIN si esercita cancellando (vedi elimina), non riscrivendo.
     */
    @Transactional
    public Recensione aggiorna(Long recensioneId, Recensione datiAggiornati, String username) {
        Recensione recensione = this.findById(recensioneId);
        this.verificaProprietario(recensione, username);

        recensione.setTitolo(datiAggiornati.getTitolo());
        recensione.setTesto(datiAggiornati.getTesto());
        recensione.setVoto(datiAggiornati.getVoto());
        recensione.setData(LocalDate.now());
        return this.recensioneRepository.save(recensione);
    }

    /**
     * CASO D'USO (USER) — CANCELLAZIONE DI UNA PROPRIA RECENSIONE.
     * Un ADMIN puo' cancellare qualsiasi recensione: e' l'unico potere che ha
     * sulle recensioni altrui.
     */
    @Transactional
    public Long elimina(Long recensioneId, String username, boolean isAdmin) {
        Recensione recensione = this.findById(recensioneId);
        if (!isAdmin) {
            this.verificaProprietario(recensione, username);
        }
        Long figureId = recensione.getFigure().getId();
        this.recensioneRepository.delete(recensione);
        return figureId;
    }

    // ------------------------------------------------------------------

    private void verificaProprietario(Recensione recensione, String username) {
        User autore = this.recuperaUtente(username);
        if (!recensione.getAutore().getId().equals(autore.getId())) {
            throw new OperazioneNonAutorizzataException(
                    "Puoi modificare o cancellare solo le recensioni di cui sei autore");
        }
    }

    private User recuperaUtente(String username) {
        Credentials credenziali = this.credentialsRepository.findByUsername(username)
                .orElseThrow(() -> new EntitaNonTrovataException(
                        "Nessun utente registrato con username " + username));
        return credenziali.getUser();
    }
}
