package it.uniroma3.siw.figuredb.controller;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import it.uniroma3.siw.figuredb.service.exception.EntitaNonTrovataException;
import it.uniroma3.siw.figuredb.service.exception.OperazioneNonAutorizzataException;
import it.uniroma3.siw.figuredb.service.exception.VincoloViolatoException;

/**
 * Gestione centralizzata degli errori per la parte MVC/Thymeleaf.
 * Le eccezioni del Service Layer vengono tradotte in pagine di errore
 * con il codice HTTP appropriato.
 */
@ControllerAdvice(basePackages = "it.uniroma3.siw.figuredb.controller")
public class GestoreErroriController {

    @ExceptionHandler(EntitaNonTrovataException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String entitaNonTrovata(EntitaNonTrovataException e, Model model) {
        model.addAttribute("codice", 404);
        model.addAttribute("messaggio", e.getMessage());
        return "errore";
    }

    @ExceptionHandler(VincoloViolatoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String vincoloViolato(VincoloViolatoException e, Model model) {
        model.addAttribute("codice", 409);
        model.addAttribute("messaggio", e.getMessage());
        return "errore";
    }

    @ExceptionHandler(OperazioneNonAutorizzataException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String nonAutorizzato(OperazioneNonAutorizzataException e, Model model) {
        model.addAttribute("codice", 403);
        model.addAttribute("messaggio", e.getMessage());
        return "errore";
    }
}
