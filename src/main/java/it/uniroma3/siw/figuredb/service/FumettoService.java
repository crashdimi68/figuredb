package it.uniroma3.siw.figuredb.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.figuredb.model.Disegnatore;
import it.uniroma3.siw.figuredb.model.Editore;
import it.uniroma3.siw.figuredb.model.Fumetto;
import it.uniroma3.siw.figuredb.model.Scrittore;
import it.uniroma3.siw.figuredb.model.Serie;
import it.uniroma3.siw.figuredb.repository.DisegnatoreRepository;
import it.uniroma3.siw.figuredb.repository.EditoreRepository;
import it.uniroma3.siw.figuredb.repository.FumettoRepository;
import it.uniroma3.siw.figuredb.repository.ScrittoreRepository;
import it.uniroma3.siw.figuredb.repository.SerieRepository;
import it.uniroma3.siw.figuredb.service.exception.EntitaNonTrovataException;
import it.uniroma3.siw.figuredb.service.exception.VincoloViolatoException;
import it.uniroma3.siw.figuredb.util.RisolutoreImmagini;

/**
 * Casi d'uso relativi ai volumi a fumetti.
 */
@Service
public class FumettoService {

    private final FumettoRepository fumettoRepository;
    private final ScrittoreRepository scrittoreRepository;
    private final DisegnatoreRepository disegnatoreRepository;
    private final EditoreRepository editoreRepository;
    private final SerieRepository serieRepository;
    private final RisolutoreImmagini risolutoreImmagini;

    public FumettoService(FumettoRepository fumettoRepository,
                          ScrittoreRepository scrittoreRepository,
                          DisegnatoreRepository disegnatoreRepository,
                          EditoreRepository editoreRepository,
                          SerieRepository serieRepository,
                          RisolutoreImmagini risolutoreImmagini) {
        this.fumettoRepository = fumettoRepository;
        this.scrittoreRepository = scrittoreRepository;
        this.disegnatoreRepository = disegnatoreRepository;
        this.editoreRepository = editoreRepository;
        this.serieRepository = serieRepository;
        this.risolutoreImmagini = risolutoreImmagini;
    }

    @Transactional(readOnly = true)
    public List<Fumetto> findAll() {
        return this.fumettoRepository.findAllConAutoriEditoreSerie();
    }

    @Transactional(readOnly = true)
    public Fumetto findById(Long id) {
        return this.fumettoRepository.findByIdCompleto(id)
                .orElseThrow(() -> new EntitaNonTrovataException("Fumetto", id));
    }

    @Transactional(readOnly = true)
    public List<Fumetto> findBySerie(Long serieId) {
        return this.fumettoRepository.findBySerieIdConAutori(serieId);
    }

    @Transactional(readOnly = true)
    public List<Fumetto> cercaPerTitolo(String titolo) {
        if (titolo == null || titolo.isBlank()) {
            return this.findAll();
        }
        return this.fumettoRepository.cercaPerTitolo(titolo.trim());
    }

    /**
     * CASO D'USO (ADMIN) — INSERIMENTO DI UN NUOVO FUMETTO.
     *
     * Operazione multi-entita': recupera scrittore, editore, serie e
     * l'insieme dei disegnatori (associazione molti-a-molti), verifica
     * l'unicita' dell'ISBN e crea il fumetto. Isolamento SERIALIZABLE
     * perche' la scrittura dipende dal controllo di unicita' sull'ISBN.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Fumetto inserisci(Fumetto fumetto, Long scrittoreId, Long editoreId,
                             Long serieId, List<Long> disegnatoriIds) {

        if (this.fumettoRepository.existsByIsbn(fumetto.getIsbn())) {
            throw new VincoloViolatoException(
                    "Esiste gia' un fumetto con ISBN " + fumetto.getIsbn());
        }

        Scrittore scrittore = this.scrittoreRepository.findById(scrittoreId)
                .orElseThrow(() -> new EntitaNonTrovataException("Scrittore", scrittoreId));
        Editore editore = this.editoreRepository.findById(editoreId)
                .orElseThrow(() -> new EntitaNonTrovataException("Editore", editoreId));
        Serie serie = this.serieRepository.findById(serieId)
                .orElseThrow(() -> new EntitaNonTrovataException("Serie", serieId));

        Set<Disegnatore> disegnatori = this.recuperaDisegnatori(disegnatoriIds);

        fumetto.setScrittore(scrittore);
        fumetto.setEditore(editore);
        fumetto.setSerie(serie);
        fumetto.setDisegnatori(disegnatori);
        fumetto.setImmagine(this.risolutoreImmagini.normalizza("fumetti", fumetto.getImmagine()));

        return this.fumettoRepository.save(fumetto);
    }

    /** CASO D'USO (ADMIN) — AGGIORNAMENTO di un fumetto esistente. */
    @Transactional
    public Fumetto aggiorna(Long id, Fumetto dati, Long scrittoreId, Long editoreId,
                            Long serieId, List<Long> disegnatoriIds) {
        Fumetto esistente = this.fumettoRepository.findById(id)
                .orElseThrow(() -> new EntitaNonTrovataException("Fumetto", id));

        this.fumettoRepository.findByIsbn(dati.getIsbn()).ifPresent(altro -> {
            if (!altro.getId().equals(id)) {
                throw new VincoloViolatoException(
                        "L'ISBN " + dati.getIsbn() + " e' gia' assegnato ad un altro fumetto");
            }
        });

        esistente.setTitolo(dati.getTitolo());
        esistente.setAnno(dati.getAnno());
        esistente.setIsbn(dati.getIsbn());
        esistente.setPrezzo(dati.getPrezzo());
        esistente.setGenere(dati.getGenere());
        esistente.setVariant(dati.getVariant());
        esistente.setImmagine(
                this.risolutoreImmagini.normalizza("fumetti", dati.getImmagine()));
        esistente.setScrittore(this.scrittoreRepository.findById(scrittoreId)
                .orElseThrow(() -> new EntitaNonTrovataException("Scrittore", scrittoreId)));
        esistente.setEditore(this.editoreRepository.findById(editoreId)
                .orElseThrow(() -> new EntitaNonTrovataException("Editore", editoreId)));
        esistente.setSerie(this.serieRepository.findById(serieId)
                .orElseThrow(() -> new EntitaNonTrovataException("Serie", serieId)));
        esistente.setDisegnatori(this.recuperaDisegnatori(disegnatoriIds));

        return this.fumettoRepository.save(esistente);
    }

    /** CASO D'USO (ADMIN) — CANCELLAZIONE di un fumetto. */
    @Transactional
    public void elimina(Long id) {
        Fumetto fumetto = this.fumettoRepository.findById(id)
                .orElseThrow(() -> new EntitaNonTrovataException("Fumetto", id));
        this.fumettoRepository.delete(fumetto);
    }

    private Set<Disegnatore> recuperaDisegnatori(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new VincoloViolatoException("Un fumetto deve avere almeno un disegnatore");
        }
        Set<Disegnatore> disegnatori = new HashSet<>();
        for (Long disegnatoreId : ids) {
            disegnatori.add(this.disegnatoreRepository.findById(disegnatoreId)
                    .orElseThrow(() -> new EntitaNonTrovataException("Disegnatore", disegnatoreId)));
        }
        return disegnatori;
    }
}
