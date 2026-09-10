package it.uniroma3.siw.figuredb.service;

/**
 * Esito della misura di UNA strategia di accesso ai dati.
 *
 * E' un semplice contenitore di sola lettura: viene creato dal
 * {@link AnalisiPrestazioniService} e letto dal template Thymeleaf della
 * pagina /admin/prestazioni. Non e' una entita' JPA e non viene salvato:
 * i numeri si riferiscono all'esecuzione appena fatta.
 */
public class RisultatoStrategia {

    private final String nome;
    private final String descrizione;
    private final long querySql;
    private final int oggettiCaricati;
    private final double millisecondi;

    /** Larghezza della barra nel grafico, in percentuale della piu' lenta. */
    private int percentuale;

    public RisultatoStrategia(String nome, String descrizione, long querySql,
                              int oggettiCaricati, double millisecondi) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.querySql = querySql;
        this.oggettiCaricati = oggettiCaricati;
        this.millisecondi = millisecondi;
    }

    public String getNome() {
        return nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public long getQuerySql() {
        return querySql;
    }

    public int getOggettiCaricati() {
        return oggettiCaricati;
    }

    public double getMillisecondi() {
        return millisecondi;
    }

    public int getPercentuale() {
        return percentuale;
    }

    public void setPercentuale(int percentuale) {
        this.percentuale = percentuale;
    }
}
