package it.uniroma3.siw.figuredb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import it.uniroma3.siw.figuredb.model.Figure;
import it.uniroma3.siw.figuredb.model.Recensione;
import it.uniroma3.siw.figuredb.service.exception.OperazioneNonAutorizzataException;
import it.uniroma3.siw.figuredb.service.exception.VincoloViolatoException;

/**
 * Verifica le due regole di business piu' importanti sulle recensioni:
 *   - un utente puo' inserire al massimo una recensione per la stessa figure
 *   - un utente puo' modificare solo le recensioni di cui e' autore
 */
@SpringBootTest
class RecensioneServiceTest {

    @Autowired
    private RecensioneService recensioneService;

    @Autowired
    private FigureService figureService;

    @Test
    void nonPuoInserireDueRecensioniPerLaStessaFigure() {
        Figure figure = this.figureService.findAll().get(0);

        Recensione prima = this.recensioneService.inserisci(
                this.creaRecensione("Ottima", 5), figure.getId(), "damiano");
        assertNotNull(prima.getId());

        assertThrows(VincoloViolatoException.class, () -> this.recensioneService.inserisci(
                this.creaRecensione("Ci ripenso", 3), figure.getId(), "damiano"));
    }

    @Test
    void nonPuoModificareLaRecensioneDiUnAltroUtente() {
        Figure figure = this.figureService.findAll().get(1);

        Recensione recensione = this.recensioneService.inserisci(
                this.creaRecensione("Bella scultura", 4), figure.getId(), "damiano");

        assertThrows(OperazioneNonAutorizzataException.class,
                () -> this.recensioneService.aggiorna(recensione.getId(),
                        this.creaRecensione("Modificata da altri", 1), "giulia"));
    }

    @Test
    void leRecensioniDiUnaFigureSonoRecuperabili() {
        Figure figure = this.figureService.findAll().get(2);
        this.recensioneService.inserisci(this.creaRecensione("Consigliata", 5),
                figure.getId(), "giulia");

        List<Recensione> recensioni = this.recensioneService.findByFigure(figure.getId());
        assertEquals(1, recensioni.size());
        assertEquals(5, recensioni.get(0).getVoto());
    }

    private Recensione creaRecensione(String titolo, int voto) {
        Recensione r = new Recensione();
        r.setTitolo(titolo);
        r.setTesto("Testo di prova sufficientemente lungo per la validazione.");
        r.setVoto(voto);
        return r;
    }
}
