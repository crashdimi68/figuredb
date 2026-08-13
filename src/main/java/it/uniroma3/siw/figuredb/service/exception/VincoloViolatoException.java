package it.uniroma3.siw.figuredb.service.exception;

/**
 * Lanciata quando una operazione violerebbe una regola di business
 * (es. duplicato a catalogo, seconda recensione dello stesso utente).
 */
public class VincoloViolatoException extends RuntimeException {

    public VincoloViolatoException(String messaggio) {
        super(messaggio);
    }
}
