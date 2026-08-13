package it.uniroma3.siw.figuredb.api.dto;

import java.time.LocalDate;

import it.uniroma3.siw.figuredb.model.Figure;

/**
 * Proiezione di Figure esposta dalle API REST.
 *
 * Usiamo un DTO invece dell'entita' per tre motivi:
 *  - evita i cicli di serializzazione delle associazioni bidirezionali;
 *  - evita LazyInitializationException fuori dalla transazione;
 *  - disaccoppia il contratto dell'API dal modello di persistenza.
 */
public record FigureDto(
        Long id,
        String nome,
        LocalDate dataUscita,
        Float altezza,
        String materiale,
        String immagine,
        Float prezzo,
        String descrizione,
        Boolean edizioneLimitata,
        Long aziendaId,
        String aziendaNome,
        Long serieId,
        String serieNome) {

    public static FigureDto da(Figure f) {
        return new FigureDto(
                f.getId(),
                f.getNome(),
                f.getDataUscita(),
                f.getAltezza(),
                f.getMateriale() != null ? f.getMateriale().name() : null,
                f.getImmagine(),
                f.getPrezzo(),
                f.getDescrizione(),
                f.getEdizioneLimitata(),
                f.getAzienda().getId(),
                f.getAzienda().getNome(),
                f.getSerie().getId(),
                f.getSerie().getNome());
    }
}
