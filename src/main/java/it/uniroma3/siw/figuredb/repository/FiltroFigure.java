package it.uniroma3.siw.figuredb.repository;

import it.uniroma3.siw.figuredb.model.Materiale;

/**
 * Oggetto che raccoglie i criteri di ricerca del catalogo figure.
 * Ogni campo null (o false, per i flag) significa "nessun filtro".
 */
public record FiltroFigure(
        String testo,
        Long serieId,
        Long aziendaId,
        Materiale materiale,
        Boolean soloEdizioniLimitate,
        Float prezzoMin,
        Float prezzoMax) {

    public static FiltroFigure vuoto() {
        return new FiltroFigure(null, null, null, null, null, null, null);
    }
}
