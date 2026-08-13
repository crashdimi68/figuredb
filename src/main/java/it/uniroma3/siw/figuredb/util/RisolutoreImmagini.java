package it.uniroma3.siw.figuredb.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * Trova l'immagine di una figure o di un gacha dal nome della sua cartella.
 *
 * Convenzione: ogni elemento ha una cartella dentro
 * src/main/resources/static/images/TIPO/ e il file mostrato sul sito e' il
 * PRIMO in ordine alfabetico. Chiamando i file 01.jpg, 02.jpg, ... la 01 e'
 * quella che compare, le altre restano disponibili per un uso futuro.
 *
 * Il vantaggio di dedurre il percorso invece di scriverlo nel CSV e' che il
 * CSV e i file non possono andare fuori sincrono: se la cartella non esiste
 * o e' vuota, l'elemento semplicemente non ha immagine e il sito non si rompe.
 */
@Component
public class RisolutoreImmagini {

    private static final List<String> ESTENSIONI =
            List.of(".jpg", ".jpeg", ".png", ".webp", ".gif");

    private final PathMatchingResourcePatternResolver risolutore =
            new PathMatchingResourcePatternResolver();

    /** Immagine di una figure: cerca in static/images/figure/<cartella>/ */
    public String risolviFigure(String cartella) {
        return risolvi("figure", cartella);
    }

    /**
     * Normalizza il valore inserito da un amministratore nella form.
     *
     * Accetta due forme, cosi' chi compila non deve conoscere la convenzione
     * interna dei percorsi:
     *   - un percorso gia' pronto  ("/images/figure/rufy/01.jpg" o un URL)
     *     viene lasciato com'e';
     *   - un semplice nome di cartella ("rufy-nendoroid") viene risolto come
     *     fa il seeder, prendendo il primo file della cartella.
     *
     * @param tipo    figure, fumetti oppure gacha
     * @param valore  quello che l'utente ha scritto nel campo
     * @return il percorso web dell'immagine, oppure null
     */
    public String normalizza(String tipo, String valore) {
        if (valore == null || valore.isBlank()) {
            return null;
        }
        String v = valore.trim();
        if (v.startsWith("/") || v.startsWith("http://") || v.startsWith("https://")) {
            return v;                      // e' gia' un percorso
        }
        return this.risolvi(tipo, v);      // e' il nome di una cartella
    }

    /** Copertina di un fumetto: cerca in static/images/fumetti/<cartella>/ */
    public String risolviFumetto(String cartella) {
        return risolvi("fumetti", cartella);
    }

    /** Immagine di un gacha: cerca in static/images/gacha/<cartella>/ */
    public String risolviGacha(String cartella) {
        return risolvi("gacha", cartella);
    }

    /**
     * @param tipo     sottocartella di static/images (figure, fumetti o gacha)
     * @param cartella nome della cartella, dalla colonna cartellaImmagini del CSV
     * @return il percorso web dell'immagine, oppure null se non ce ne sono
     */
    public String risolvi(String tipo, String cartella) {
        if (cartella == null || cartella.isBlank()) {
            return null;
        }
        String nome = cartella.trim();
        try {
            Resource[] risorse = this.risolutore.getResources(
                    "classpath:/static/images/" + tipo + "/" + nome + "/*");

            Optional<String> primoFile = Arrays.stream(risorse)
                    .map(Resource::getFilename)
                    .filter(this::eImmagine)
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .findFirst();

            if (primoFile.isEmpty()) {
                System.out.println("   [avviso] nessuna immagine in static/images/"
                        + tipo + "/" + nome);
                return null;
            }
            return "/images/" + tipo + "/" + nome + "/" + primoFile.get();

        } catch (IOException e) {
            System.out.println("   [avviso] cartella immagini non leggibile: " + nome);
            return null;
        }
    }

    private boolean eImmagine(String nomeFile) {
        if (nomeFile == null) {
            return false;
        }
        String minuscolo = nomeFile.toLowerCase();
        return ESTENSIONI.stream().anyMatch(minuscolo::endsWith);
    }
}
