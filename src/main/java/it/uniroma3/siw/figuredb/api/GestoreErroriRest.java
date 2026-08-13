package it.uniroma3.siw.figuredb.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import it.uniroma3.siw.figuredb.api.dto.ErroreDto;
import it.uniroma3.siw.figuredb.service.exception.EntitaNonTrovataException;
import it.uniroma3.siw.figuredb.service.exception.OperazioneNonAutorizzataException;
import it.uniroma3.siw.figuredb.service.exception.VincoloViolatoException;

/**
 * Traduce le eccezioni del Service Layer in risposte HTTP con il codice
 * di stato corretto, per la sola parte REST (package api).
 */
@RestControllerAdvice(basePackages = "it.uniroma3.siw.figuredb.api")
public class GestoreErroriRest {

    /** 404: risorsa inesistente. */
    @ExceptionHandler(EntitaNonTrovataException.class)
    public ResponseEntity<ErroreDto> nonTrovata(EntitaNonTrovataException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErroreDto.di(404, "Not Found", e.getMessage()));
    }

    /** 409: violazione di una regola di business (es. recensione duplicata). */
    @ExceptionHandler(VincoloViolatoException.class)
    public ResponseEntity<ErroreDto> conflitto(VincoloViolatoException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErroreDto.di(409, "Conflict", e.getMessage()));
    }

    /** 403: l'utente non e' proprietario della risorsa. */
    @ExceptionHandler(OperazioneNonAutorizzataException.class)
    public ResponseEntity<ErroreDto> vietato(OperazioneNonAutorizzataException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErroreDto.di(403, "Forbidden", e.getMessage()));
    }

    /** 400: input non valido (bean validation sul @RequestBody). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroreDto> inputNonValido(MethodArgumentNotValidException e) {
        List<String> dettagli = e.getBindingResult().getFieldErrors().stream()
                .map(errore -> errore.getField() + ": " + errore.getDefaultMessage())
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErroreDto.di(400, "Bad Request", "Dati non validi", dettagli));
    }
}
