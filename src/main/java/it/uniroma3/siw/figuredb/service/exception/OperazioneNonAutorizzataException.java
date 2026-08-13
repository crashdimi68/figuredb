package it.uniroma3.siw.figuredb.service.exception;

/**
 * Lanciata quando l'utente autenticato tenta una operazione su una risorsa
 * di cui non e' proprietario (es. modificare la recensione di un altro utente).
 */
public class OperazioneNonAutorizzataException extends RuntimeException {

    public OperazioneNonAutorizzataException(String messaggio) {
        super(messaggio);
    }
}
