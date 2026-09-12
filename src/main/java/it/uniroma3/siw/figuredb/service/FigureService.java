package it.uniroma3.siw.figuredb.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.figuredb.model.Azienda;
import it.uniroma3.siw.figuredb.model.Figure;
import it.uniroma3.siw.figuredb.model.Serie;
import it.uniroma3.siw.figuredb.repository.AziendaRepository;
import it.uniroma3.siw.figuredb.repository.FigureRepository;
import it.uniroma3.siw.figuredb.repository.FiltroFigure;
import it.uniroma3.siw.figuredb.repository.SerieRepository;
import it.uniroma3.siw.figuredb.service.exception.EntitaNonTrovataException;
import it.uniroma3.siw.figuredb.service.exception.VincoloViolatoException;
import it.uniroma3.siw.figuredb.util.RisolutoreImmagini;

/**
 * Casi d'uso relativi al catalogo delle figure da collezione.
 */
@Service
public class FigureService {

    private final FigureRepository figureRepository;
    private final AziendaRepository aziendaRepository;
    private final SerieRepository serieRepository;
    private final RisolutoreImmagini risolutoreImmagini;

    public FigureService(FigureRepository figureRepository,
                         AziendaRepository aziendaRepository,
                         SerieRepository serieRepository,
                         RisolutoreImmagini risolutoreImmagini) {
        this.figureRepository = figureRepository;
        this.aziendaRepository = aziendaRepository;
        this.serieRepository = serieRepository;
        this.risolutoreImmagini = risolutoreImmagini;
    }

    // ------------------------------------------------------------------
    // Casi d'uso pubblici (sola lettura)
    // ------------------------------------------------------------------

    /**
     * CASO D'USO PUBBLICO: elenco del catalogo.
     * Usiamo la query con JOIN FETCH: azienda e serie sono mostrate in
     * ogni riga dell'elenco, quindi caricarle LAZY genererebbe N+1 query.
     */
    @Transactional(readOnly = true)
    public List<Figure> findAll() {
        return this.figureRepository.findAllConAziendaESerie();
    }

    /**
     * CASO D'USO PUBBLICO: elenco del catalogo UNA PAGINA ALLA VOLTA.
     *
     * Serve alla schermata /figure: con un catalogo grande caricare tutto in
     * una sola pagina web e' inutile e lento. Il numero di pagina arriva dal
     * controller; l'ordinamento per nome rende stabile la suddivisione
     * (senza order by il database non garantisce lo stesso ordine fra una
     * pagina e l'altra, e qualche figure potrebbe comparire due volte).
     */
    @Transactional(readOnly = true)
    public Page<Figure> findPagina(int numeroPagina, int figurePerPagina) {
        int pagina = Math.max(numeroPagina, 0);
        return this.figureRepository.findPaginaConAziendaESerie(
                PageRequest.of(pagina, figurePerPagina, Sort.by("nome")));
    }

    /**
     * CASO D'USO PUBBLICO: ricerca nel catalogo con filtri combinabili.
     * Delegata al fragment Criteria API, che applica solo i predicati
     * effettivamente valorizzati e carica azienda e serie in fetch join.
     */
    @Transactional(readOnly = true)
    public List<Figure> cerca(FiltroFigure filtro) {
        return this.figureRepository.cerca(filtro != null ? filtro : FiltroFigure.vuoto());
    }

    /** CASO D'USO PUBBLICO: dettaglio di una figure. */
    @Transactional(readOnly = true)
    public Figure findById(Long id) {
        return this.figureRepository.findByIdConAziendaESerie(id)
                .orElseThrow(() -> new EntitaNonTrovataException("Figure", id));
    }

    @Transactional(readOnly = true)
    public Figure findByIdSemplice(Long id) {
        return this.figureRepository.findById(id)
                .orElseThrow(() -> new EntitaNonTrovataException("Figure", id));
    }

    @Transactional(readOnly = true)
    public List<Figure> findEdizioniLimitate() {
        return this.figureRepository.findEdizioniLimitate();
    }

    @Transactional(readOnly = true)
    public List<Figure> findBySerie(Long serieId) {
        return this.figureRepository.findBySerieIdConJoinFetch(serieId);
    }

    @Transactional(readOnly = true)
    public List<Figure> findByAzienda(Long aziendaId) {
        return this.figureRepository.findByAziendaIdConSerie(aziendaId);
    }

    // ------------------------------------------------------------------
    // Casi d'uso riservati all'amministratore (scrittura)
    // ------------------------------------------------------------------

    /**
     * CASO D'USO (ADMIN) — INSERIMENTO A CATALOGO DI UNA NUOVA FIGURE.
     *
     * E' l'operazione multi-entita' / multi-repository richiesta dalla
     * specifica. I passi sono:
     *   1. recupero dell'Azienda produttrice
     *   2. recupero della Serie di appartenenza
     *   3. verifica del vincolo di consistenza del catalogo
     *      (stessa azienda + stesso nome + stessa data di uscita)
     *   4. creazione della Figure
     *   5. aggiornamento delle associazioni coinvolte
     *
     * Isolamento SERIALIZABLE: la decisione di scrivere dipende da una
     * lettura (il controllo di duplicato). Con Read Committed due richieste
     * concorrenti potrebbero superare entrambe il controllo e inserire due
     * volte la stessa figure. In caso di errore in un qualsiasi passo la
     * transazione viene annullata e il catalogo resta consistente.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Figure inserisciACatalogo(Figure figure, Long aziendaId, Long serieId) {
        // 1. recupero azienda
        Azienda azienda = this.aziendaRepository.findById(aziendaId)
                .orElseThrow(() -> new EntitaNonTrovataException("Azienda", aziendaId));

        // 2. recupero serie
        Serie serie = this.serieRepository.findById(serieId)
                .orElseThrow(() -> new EntitaNonTrovataException("Serie", serieId));

        // 3. verifica del vincolo di consistenza
        if (this.figureRepository.esisteDuplicato(figure.getNome(), aziendaId, figure.getDataUscita())) {
            throw new VincoloViolatoException(
                    "L'azienda " + azienda.getNome() + " ha gia' a catalogo una figure '"
                    + figure.getNome() + "' con data di uscita " + figure.getDataUscita());
        }

        // 4. e 5. creazione della figure e aggiornamento delle associazioni
        figure.setAzienda(azienda);
        figure.setSerie(serie);
        figure.setImmagine(this.risolutoreImmagini.normalizza("figure", figure.getImmagine()));
        return this.figureRepository.save(figure);
    }

    /**
     * CASO D'USO (ADMIN) — AGGIORNAMENTO dei dati di una figure a catalogo.
     * Anche l'aggiornamento deve rispettare il vincolo di unicita', escludendo
     * pero' la figure che si sta modificando.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Figure aggiorna(Long id, Figure datiAggiornati, Long aziendaId, Long serieId) {
        Figure esistente = this.findByIdSemplice(id);

        Azienda azienda = this.aziendaRepository.findById(aziendaId)
                .orElseThrow(() -> new EntitaNonTrovataException("Azienda", aziendaId));
        Serie serie = this.serieRepository.findById(serieId)
                .orElseThrow(() -> new EntitaNonTrovataException("Serie", serieId));

        if (this.figureRepository.esisteDuplicatoEscludendo(
                datiAggiornati.getNome(), aziendaId, datiAggiornati.getDataUscita(), id)) {
            throw new VincoloViolatoException(
                    "Esiste gia' un'altra figure '" + datiAggiornati.getNome()
                    + "' della stessa azienda con la stessa data di uscita");
        }

        esistente.setNome(datiAggiornati.getNome());
        esistente.setDataUscita(datiAggiornati.getDataUscita());
        esistente.setAltezza(datiAggiornati.getAltezza());
        esistente.setMateriale(datiAggiornati.getMateriale());
        esistente.setPrezzo(datiAggiornati.getPrezzo());
        esistente.setDescrizione(datiAggiornati.getDescrizione());
        esistente.setImmagine(
                this.risolutoreImmagini.normalizza("figure", datiAggiornati.getImmagine()));
        esistente.setEdizioneLimitata(datiAggiornati.getEdizioneLimitata());
        esistente.setAzienda(azienda);
        esistente.setSerie(serie);

        return this.figureRepository.save(esistente);
    }

    /**
     * CASO D'USO (ADMIN) — CANCELLAZIONE di una figure dal catalogo.
     * Le recensioni collegate vengono rimosse in cascata (composizione).
     */
    @Transactional
    public void elimina(Long id) {
        Figure figure = this.findByIdSemplice(id);
        this.figureRepository.delete(figure);
    }
}
