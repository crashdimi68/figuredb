package it.uniroma3.siw.figuredb.api.dto;

import java.time.LocalDate;

import it.uniroma3.siw.figuredb.model.Recensione;

public record RecensioneDto(
        Long id,
        String titolo,
        String testo,
        Integer voto,
        LocalDate data,
        String autore,
        Long figureId) {

    public static RecensioneDto da(Recensione r) {
        return new RecensioneDto(
                r.getId(),
                r.getTitolo(),
                r.getTesto(),
                r.getVoto(),
                r.getData(),
                r.getAutore().getNomeCompleto(),
                r.getFigure().getId());
    }
}
