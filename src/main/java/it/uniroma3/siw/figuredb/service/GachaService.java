package it.uniroma3.siw.figuredb.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.figuredb.model.Azienda;
import it.uniroma3.siw.figuredb.model.Gacha;
import it.uniroma3.siw.figuredb.model.Serie;
import it.uniroma3.siw.figuredb.repository.AziendaRepository;
import it.uniroma3.siw.figuredb.repository.GachaRepository;
import it.uniroma3.siw.figuredb.repository.SerieRepository;
import it.uniroma3.siw.figuredb.service.exception.EntitaNonTrovataException;
import it.uniroma3.siw.figuredb.util.RisolutoreImmagini;

@Service
public class GachaService {

    private final GachaRepository gachaRepository;
    private final AziendaRepository aziendaRepository;
    private final SerieRepository serieRepository;
    private final RisolutoreImmagini risolutoreImmagini;

    public GachaService(GachaRepository gachaRepository,
                        AziendaRepository aziendaRepository,
                        SerieRepository serieRepository,
                        RisolutoreImmagini risolutoreImmagini) {
        this.gachaRepository = gachaRepository;
        this.aziendaRepository = aziendaRepository;
        this.serieRepository = serieRepository;
        this.risolutoreImmagini = risolutoreImmagini;
    }

    @Transactional(readOnly = true)
    public List<Gacha> findAll() {
        return this.gachaRepository.findAllConAziendaESerie();
    }

    @Transactional(readOnly = true)
    public Gacha findById(Long id) {
        return this.gachaRepository.findByIdCompleto(id)
                .orElseThrow(() -> new EntitaNonTrovataException("Gacha", id));
    }

    @Transactional(readOnly = true)
    public List<Gacha> findByAzienda(Long aziendaId) {
        return this.gachaRepository.findByAziendaIdConSerie(aziendaId);
    }

    /** CASO D'USO PUBBLICO: i gacha ispirati a una serie. */
    @Transactional(readOnly = true)
    public List<Gacha> findBySerie(Long serieId) {
        return this.gachaRepository.findBySerieIdConAzienda(serieId);
    }

    /**
     * CASO D'USO (ADMIN): inserimento di una serie gacha.
     * Coinvolge tre repository: recupera azienda e serie, poi crea il gacha.
     */
    @Transactional
    public Gacha salva(Gacha gacha, Long aziendaId, Long serieId) {
        gacha.setAzienda(this.recuperaAzienda(aziendaId));
        gacha.setSerie(this.recuperaSerie(serieId));
        gacha.setImmagine(this.risolutoreImmagini.normalizza("gacha", gacha.getImmagine()));
        return this.gachaRepository.save(gacha);
    }

    /**
     * CASO D'USO (ADMIN): modifica di una serie gacha esistente.
     * I dati arrivano dalla form e vengono copiati sull'entita' gestita.
     */
    @Transactional
    public Gacha aggiorna(Long id, Gacha dati, Long aziendaId, Long serieId) {
        Gacha esistente = this.gachaRepository.findById(id)
                .orElseThrow(() -> new EntitaNonTrovataException("Gacha", id));

        esistente.setNome(dati.getNome());
        esistente.setDataUscita(dati.getDataUscita());
        esistente.setPrezzo(dati.getPrezzo());
        esistente.setDescrizione(dati.getDescrizione());
        esistente.setImmagine(this.risolutoreImmagini.normalizza("gacha", dati.getImmagine()));
        esistente.setAzienda(this.recuperaAzienda(aziendaId));
        esistente.setSerie(this.recuperaSerie(serieId));
        return this.gachaRepository.save(esistente);
    }

    /** CASO D'USO (ADMIN): cancellazione di una serie gacha. */
    @Transactional
    public void elimina(Long id) {
        this.gachaRepository.delete(this.findById(id));
    }

    // ------------------------------------------------------------------

    private Azienda recuperaAzienda(Long aziendaId) {
        return this.aziendaRepository.findById(aziendaId)
                .orElseThrow(() -> new EntitaNonTrovataException("Azienda", aziendaId));
    }

    private Serie recuperaSerie(Long serieId) {
        return this.serieRepository.findById(serieId)
                .orElseThrow(() -> new EntitaNonTrovataException("Serie", serieId));
    }
}
