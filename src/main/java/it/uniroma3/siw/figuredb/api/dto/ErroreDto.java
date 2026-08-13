package it.uniroma3.siw.figuredb.api.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Corpo standard delle risposte di errore delle API REST. */
public record ErroreDto(
        int stato,
        String errore,
        String messaggio,
        List<String> dettagli,
        LocalDateTime istante) {

    public static ErroreDto di(int stato, String errore, String messaggio) {
        return new ErroreDto(stato, errore, messaggio, List.of(), LocalDateTime.now());
    }

    public static ErroreDto di(int stato, String errore, String messaggio, List<String> dettagli) {
        return new ErroreDto(stato, errore, messaggio, dettagli, LocalDateTime.now());
    }
}
