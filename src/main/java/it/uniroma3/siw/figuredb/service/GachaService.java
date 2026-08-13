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

@Service
public class GachaService {

    private final GachaRepository gachaRepository;
    private final AziendaRepository aziendaRepository;
    private final SerieRepository serieRepository;

    public GachaService(GachaRepository gachaRepository,
                        AziendaRepository aziendaRepository,
                        SerieRepository serieRepository) {
        this.gachaRepository = gachaRepository;
        this.aziendaRepository = aziendaRepository;
        this.serieRepository = serieRepository;
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
        Azienda azienda = this.aziendaRepository.findById(aziendaId)
                .orElseThrow(() -> new EntitaNonTrovataException("Azienda", aziendaId));
        Serie serie = this.serieRepository.findById(serieId)
                .orElseThrow(() -> new EntitaNonTrovataException("Serie", serieId));
        gacha.setAzienda(azienda);
        gacha.setSerie(serie);
        return this.gachaRepository.save(gacha);
    }

    @Transactional
    public void elimina(Long id) {
        this.gachaRepository.delete(this.findById(id));
    }
}
