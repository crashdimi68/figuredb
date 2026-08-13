package it.uniroma3.siw.figuredb.repository;

import java.util.ArrayList;
import java.util.List;

import it.uniroma3.siw.figuredb.model.Azienda;
import it.uniroma3.siw.figuredb.model.Figure;
import it.uniroma3.siw.figuredb.model.Serie;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Implementazione della ricerca dinamica sul catalogo delle figure.
 *
 * Scelta progettuale: i filtri sono opzionali e combinabili, quindi una
 * singola query JPQL statica richiederebbe molte condizioni "is null or ...".
 * Con la Criteria API costruiamo solo i predicati effettivamente richiesti,
 * generando SQL piu' semplice e senza parametri di tipo indeterminato.
 *
 * NB: le associazioni azienda e serie sono caricate con FETCH JOIN nella
 * stessa query, per evitare il problema delle N+1 query sull'elenco.
 */
public class FigureRepositoryCustomImpl implements FigureRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Figure> cerca(FiltroFigure filtro) {
        CriteriaBuilder cb = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<Figure> query = cb.createQuery(Figure.class);
        Root<Figure> figure = query.from(Figure.class);

        // Fetch join: azienda e serie vengono caricate nella stessa query.
        // Il risultato di fetch() viene poi usato anche come Join, per poterci
        // scrivere sopra i predicati senza generare un secondo join in SQL.
        // In Hibernate l'oggetto restituito implementa entrambe le interfacce
        // (Join<Z,X> estende Fetch<Z,X>), ma vanno indicati esplicitamente i
        // parametri di tipo, altrimenti il compilatore inferisce Fetch<Object,Object>.
        Join<Figure, Azienda> azienda =
                (Join<Figure, Azienda>) figure.<Figure, Azienda>fetch("azienda", JoinType.INNER);
        Join<Figure, Serie> serie =
                (Join<Figure, Serie>) figure.<Figure, Serie>fetch("serie", JoinType.INNER);

        List<Predicate> predicati = new ArrayList<>();

        if (filtro.testo() != null && !filtro.testo().isBlank()) {
            String pattern = "%" + filtro.testo().trim().toLowerCase() + "%";
            predicati.add(cb.or(
                    cb.like(cb.lower(figure.get("nome")), pattern),
                    cb.like(cb.lower(serie.get("nome")), pattern),
                    cb.like(cb.lower(azienda.get("nome")), pattern)));
        }
        if (filtro.serieId() != null) {
            predicati.add(cb.equal(serie.get("id"), filtro.serieId()));
        }
        if (filtro.aziendaId() != null) {
            predicati.add(cb.equal(azienda.get("id"), filtro.aziendaId()));
        }
        if (filtro.materiale() != null) {
            predicati.add(cb.equal(figure.get("materiale"), filtro.materiale()));
        }
        if (Boolean.TRUE.equals(filtro.soloEdizioniLimitate())) {
            predicati.add(cb.isTrue(figure.get("edizioneLimitata")));
        }
        if (filtro.prezzoMin() != null) {
            predicati.add(cb.greaterThanOrEqualTo(figure.get("prezzo"), filtro.prezzoMin()));
        }
        if (filtro.prezzoMax() != null) {
            predicati.add(cb.lessThanOrEqualTo(figure.get("prezzo"), filtro.prezzoMax()));
        }

        if (!predicati.isEmpty()) {
            query.where(cb.and(predicati.toArray(new Predicate[0])));
        }
        query.orderBy(cb.asc(figure.get("nome")));

        return this.entityManager.createQuery(query).getResultList();
    }
}
