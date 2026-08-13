package it.uniroma3.siw.figuredb.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.figuredb.model.Disegnatore;
import it.uniroma3.siw.figuredb.model.Editore;
import it.uniroma3.siw.figuredb.model.Scrittore;
import it.uniroma3.siw.figuredb.repository.DisegnatoreRepository;
import it.uniroma3.siw.figuredb.repository.EditoreRepository;
import it.uniroma3.siw.figuredb.repository.ScrittoreRepository;
import it.uniroma3.siw.figuredb.service.exception.EntitaNonTrovataException;
import it.uniroma3.siw.figuredb.service.exception.VincoloViolatoException;

/**
 * Service che coordina gli autori dei fumetti (scrittori e disegnatori)
 * e le case editrici: sono entita' semplici, con casi d'uso analoghi,
 * quindi le raggruppiamo in un unico aggregato di servizio.
 */
@Service
public class AutoreService {

    private final ScrittoreRepository scrittoreRepository;
    private final DisegnatoreRepository disegnatoreRepository;
    private final EditoreRepository editoreRepository;

    public AutoreService(ScrittoreRepository scrittoreRepository,
                         DisegnatoreRepository disegnatoreRepository,
                         EditoreRepository editoreRepository) {
        this.scrittoreRepository = scrittoreRepository;
        this.disegnatoreRepository = disegnatoreRepository;
        this.editoreRepository = editoreRepository;
    }

    // ---------------- Scrittori ----------------

    @Transactional(readOnly = true)
    public List<Scrittore> findAllScrittori() {
        return this.scrittoreRepository.findAllByOrderByCognomeAscNomeAsc();
    }

    @Transactional(readOnly = true)
    public Scrittore findScrittore(Long id) {
        return this.scrittoreRepository.findById(id)
                .orElseThrow(() -> new EntitaNonTrovataException("Scrittore", id));
    }

    /** Dettaglio pubblico: scrittore con la sua bibliografia (una sola query). */
    @Transactional(readOnly = true)
    public Scrittore findScrittoreConFumetti(Long id) {
        return this.scrittoreRepository.findByIdConFumetti(id)
                .orElseThrow(() -> new EntitaNonTrovataException("Scrittore", id));
    }

    @Transactional
    public Scrittore salvaScrittore(Scrittore scrittore) {
        if (scrittore.getId() == null && this.scrittoreRepository
                .existsByNomeIgnoreCaseAndCognomeIgnoreCase(scrittore.getNome(), scrittore.getCognome())) {
            throw new VincoloViolatoException(
                    "Lo scrittore " + scrittore.getNomeCompleto() + " e' gia' presente");
        }
        return this.scrittoreRepository.save(scrittore);
    }

    @Transactional
    public void eliminaScrittore(Long id) {
        Scrittore scrittore = this.findScrittore(id);
        if (!scrittore.getFumetti().isEmpty()) {
            throw new VincoloViolatoException(
                    "Impossibile eliminare " + scrittore.getNomeCompleto()
                    + ": ha fumetti associati");
        }
        this.scrittoreRepository.delete(scrittore);
    }

    // ---------------- Disegnatori ----------------

    @Transactional(readOnly = true)
    public List<Disegnatore> findAllDisegnatori() {
        return this.disegnatoreRepository.findAllByOrderByCognomeAscNomeAsc();
    }

    @Transactional(readOnly = true)
    public Disegnatore findDisegnatore(Long id) {
        return this.disegnatoreRepository.findById(id)
                .orElseThrow(() -> new EntitaNonTrovataException("Disegnatore", id));
    }

    /** Dettaglio pubblico: disegnatore con i fumetti a cui ha lavorato. */
    @Transactional(readOnly = true)
    public Disegnatore findDisegnatoreConFumetti(Long id) {
        return this.disegnatoreRepository.findByIdConFumetti(id)
                .orElseThrow(() -> new EntitaNonTrovataException("Disegnatore", id));
    }

    @Transactional
    public Disegnatore salvaDisegnatore(Disegnatore disegnatore) {
        if (disegnatore.getId() == null && this.disegnatoreRepository
                .existsByNomeIgnoreCaseAndCognomeIgnoreCase(disegnatore.getNome(), disegnatore.getCognome())) {
            throw new VincoloViolatoException(
                    "Il disegnatore " + disegnatore.getNomeCompleto() + " e' gia' presente");
        }
        return this.disegnatoreRepository.save(disegnatore);
    }

    @Transactional
    public void eliminaDisegnatore(Long id) {
        Disegnatore disegnatore = this.findDisegnatore(id);
        if (!disegnatore.getFumetti().isEmpty()) {
            throw new VincoloViolatoException(
                    "Impossibile eliminare " + disegnatore.getNomeCompleto()
                    + ": ha fumetti associati");
        }
        this.disegnatoreRepository.delete(disegnatore);
    }

    // ---------------- Editori ----------------

    @Transactional(readOnly = true)
    public List<Editore> findAllEditori() {
        return this.editoreRepository.findAllByOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public Editore findEditore(Long id) {
        return this.editoreRepository.findById(id)
                .orElseThrow(() -> new EntitaNonTrovataException("Editore", id));
    }

    @Transactional
    public Editore salvaEditore(Editore editore) {
        if (editore.getId() == null && this.editoreRepository.existsByNomeIgnoreCase(editore.getNome())) {
            throw new VincoloViolatoException(
                    "L'editore " + editore.getNome() + " e' gia' presente");
        }
        return this.editoreRepository.save(editore);
    }

    @Transactional
    public void eliminaEditore(Long id) {
        Editore editore = this.findEditore(id);
        if (!editore.getFumetti().isEmpty()) {
            throw new VincoloViolatoException(
                    "Impossibile eliminare " + editore.getNome() + ": ha fumetti associati");
        }
        this.editoreRepository.delete(editore);
    }
}
