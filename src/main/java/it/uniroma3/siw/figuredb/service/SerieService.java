package it.uniroma3.siw.figuredb.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.figuredb.model.Personaggio;
import it.uniroma3.siw.figuredb.model.Serie;
import it.uniroma3.siw.figuredb.model.TipoSerie;
import it.uniroma3.siw.figuredb.repository.PersonaggioRepository;
import it.uniroma3.siw.figuredb.repository.SerieRepository;
import it.uniroma3.siw.figuredb.service.exception.EntitaNonTrovataException;
import it.uniroma3.siw.figuredb.service.exception.VincoloViolatoException;

@Service
public class SerieService {

    private final SerieRepository serieRepository;
    private final PersonaggioRepository personaggioRepository;

    public SerieService(SerieRepository serieRepository,
                        PersonaggioRepository personaggioRepository) {
        this.serieRepository = serieRepository;
        this.personaggioRepository = personaggioRepository;
    }

    @Transactional(readOnly = true)
    public List<Serie> findAll() {
        return this.serieRepository.findAllByOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public List<Serie> findByTipo(TipoSerie tipo) {
        return this.serieRepository.findByTipoOrderByNomeAsc(tipo);
    }

    @Transactional(readOnly = true)
    public Serie findById(Long id) {
        return this.serieRepository.findById(id)
                .orElseThrow(() -> new EntitaNonTrovataException("Serie", id));
    }

    /** Dettaglio: serie + personaggi in una sola query (evita N+1). */
    @Transactional(readOnly = true)
    public Serie findByIdConPersonaggi(Long id) {
        return this.serieRepository.findByIdConPersonaggi(id)
                .orElseThrow(() -> new EntitaNonTrovataException("Serie", id));
    }

    @Transactional(readOnly = true)
    public List<Serie> cercaPerNome(String testo) {
        if (testo == null || testo.isBlank()) {
            return this.findAll();
        }
        return this.serieRepository.findByNomeContainingIgnoreCaseOrderByNomeAsc(testo.trim());
    }

    /**
     * CASO D'USO (ADMIN): creazione di una nuova serie con i suoi personaggi.
     * Operazione di scrittura: coinvolge Serie e Personaggio (cascade PERSIST).
     */
    @Transactional
    public Serie salva(Serie serie) {
        if (serie.getId() == null && this.serieRepository.existsByNomeIgnoreCase(serie.getNome())) {
            throw new VincoloViolatoException(
                    "Esiste gia' una serie chiamata '" + serie.getNome() + "'");
        }
        for (Personaggio p : serie.getPersonaggi()) {
            p.setSerie(serie);
        }
        return this.serieRepository.save(serie);
    }

    /**
     * CASO D'USO (ADMIN): aggiunta di un personaggio ad una serie esistente.
     * Coinvolge due repository nella stessa transazione.
     */
    @Transactional
    public Personaggio aggiungiPersonaggio(Long serieId, Personaggio personaggio) {
        Serie serie = this.findById(serieId);
        if (this.personaggioRepository.existsByNomeIgnoreCaseAndSerieId(personaggio.getNome(), serieId)) {
            throw new VincoloViolatoException(
                    "Il personaggio '" + personaggio.getNome() + "' e' gia' presente nella serie "
                    + serie.getNome());
        }
        personaggio.setSerie(serie);
        return this.personaggioRepository.save(personaggio);
    }

    /** CASO D'USO (ADMIN): cancellazione di una serie e dei suoi personaggi. */
    @Transactional
    public void elimina(Long id) {
        Serie serie = this.findByIdConPersonaggi(id);
        if (!serie.getFumetti().isEmpty() || !serie.getFigure().isEmpty()
                || !serie.getGacha().isEmpty()) {
            throw new VincoloViolatoException(
                    "Impossibile eliminare la serie '" + serie.getNome()
                    + "': esistono fumetti, figure o gacha collegati");
        }
        this.serieRepository.delete(serie);
    }
}
