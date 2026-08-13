package it.uniroma3.siw.figuredb.repository;

import java.util.List;

import it.uniroma3.siw.figuredb.model.Figure;

/**
 * Fragment di repository per la ricerca dinamica del catalogo.
 * Implementato con Criteria API in FigureRepositoryImpl.
 */
public interface FigureRepositoryCustom {

    List<Figure> cerca(FiltroFigure filtro);
}
