package it.uniroma3.siw.figuredb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import it.uniroma3.siw.figuredb.model.Azienda;
import it.uniroma3.siw.figuredb.model.Figure;
import it.uniroma3.siw.figuredb.model.Materiale;
import it.uniroma3.siw.figuredb.model.Serie;
import it.uniroma3.siw.figuredb.repository.FiltroFigure;
import it.uniroma3.siw.figuredb.service.exception.VincoloViolatoException;

/**
 * Verifica il caso d'uso multi-entita' "inserimento a catalogo" e la
 * ricerca con filtri combinati usata dal frontend React.
 */
@SpringBootTest
class FigureServiceTest {

    @Autowired
    private FigureService figureService;

    @Autowired
    private AziendaService aziendaService;

    @Autowired
    private SerieService serieService;

    @Test
    void inserisceUnaFigureCollegandoAziendaESerie() {
        Azienda azienda = this.aziendaService.findAll().get(0);
        Serie serie = this.serieService.findAll().get(0);

        Figure salvata = this.figureService.inserisciACatalogo(
                this.creaFigure("Figure di test A"), azienda.getId(), serie.getId());

        assertNotNull(salvata.getId());
        assertEquals(azienda.getId(), salvata.getAzienda().getId());
        assertEquals(serie.getId(), salvata.getSerie().getId());
    }

    @Test
    void rifiutaUnDuplicatoNelCatalogo() {
        Azienda azienda = this.aziendaService.findAll().get(1);
        Serie serie = this.serieService.findAll().get(1);

        this.figureService.inserisciACatalogo(
                this.creaFigure("Figure di test B"), azienda.getId(), serie.getId());

        // stesso nome, stessa azienda, stessa data di uscita -> violazione
        assertThrows(VincoloViolatoException.class, () -> this.figureService.inserisciACatalogo(
                this.creaFigure("Figure di test B"), azienda.getId(), serie.getId()));
    }

    @Test
    void filtraPerSerieEPrezzo() {
        Serie serie = this.serieService.findAll().get(0);

        List<Figure> risultato = this.figureService.cerca(
                new FiltroFigure(null, serie.getId(), null, null, false, 0f, 10000f));

        assertFalse(risultato.isEmpty());
        assertTrue(risultato.stream().allMatch(f -> f.getSerie().getId().equals(serie.getId())));
    }

    @Test
    void filtraSoloEdizioniLimitate() {
        List<Figure> risultato = this.figureService.cerca(
                new FiltroFigure(null, null, null, null, true, null, null));
        assertTrue(risultato.stream().allMatch(Figure::getEdizioneLimitata));
    }

    private Figure creaFigure(String nome) {
        Figure figure = new Figure();
        figure.setNome(nome);
        figure.setDataUscita(LocalDate.of(2026, 3, 15));
        figure.setAltezza(18.5f);
        figure.setMateriale(Materiale.PVC);
        figure.setPrezzo(89.9f);
        figure.setEdizioneLimitata(false);
        figure.setDescrizione("Figure creata da un test automatico.");
        return figure;
    }
}
