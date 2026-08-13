package it.uniroma3.siw.figuredb.service.exception;

/** Lanciata quando una entita' richiesta non esiste nel database. */
public class EntitaNonTrovataException extends RuntimeException {

    public EntitaNonTrovataException(String tipo, Long id) {
        super("Nessun/a " + tipo + " con id " + id);
    }

    public EntitaNonTrovataException(String messaggio) {
        super(messaggio);
    }
}
