package it.uniroma3.siw.figuredb.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.figuredb.model.Azienda;
import it.uniroma3.siw.figuredb.repository.AziendaRepository;
import it.uniroma3.siw.figuredb.service.exception.EntitaNonTrovataException;
import it.uniroma3.siw.figuredb.service.exception.VincoloViolatoException;

@Service
public class AziendaService {

    private final AziendaRepository aziendaRepository;

    public AziendaService(AziendaRepository aziendaRepository) {
        this.aziendaRepository = aziendaRepository;
    }

    @Transactional(readOnly = true)
    public List<Azienda> findAll() {
        return this.aziendaRepository.findAllByOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public Azienda findById(Long id) {
        return this.aziendaRepository.findById(id)
                .orElseThrow(() -> new EntitaNonTrovataException("Azienda", id));
    }

    @Transactional
    public Azienda salva(Azienda azienda) {
        if (azienda.getId() == null && this.aziendaRepository.existsByNomeIgnoreCase(azienda.getNome())) {
            throw new VincoloViolatoException(
                    "Esiste gia' un'azienda chiamata '" + azienda.getNome() + "'");
        }
        return this.aziendaRepository.save(azienda);
    }

    @Transactional
    public void elimina(Long id) {
        Azienda azienda = this.findById(id);
        if (!azienda.getFigure().isEmpty() || !azienda.getGacha().isEmpty()) {
            throw new VincoloViolatoException(
                    "Impossibile eliminare l'azienda '" + azienda.getNome()
                    + "': esistono figure o gacha collegati");
        }
        this.aziendaRepository.delete(azienda);
    }
}
