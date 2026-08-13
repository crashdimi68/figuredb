package it.uniroma3.siw.figuredb.bootstrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;

/**
 * Lettore di file CSV per i dati iniziali del catalogo.
 *
 * Gestisce il formato prodotto da Google Sheets ed Excel:
 *   - la prima riga contiene i nomi delle colonne;
 *   - i campi che contengono virgole, virgolette o a capo sono racchiusi
 *     tra virgolette doppie;
 *   - una virgoletta doppia dentro un campo quotato si scrive raddoppiata ("").
 *
 * Ogni riga viene restituita come mappa nome-colonna -> valore, cosi' il
 * chiamante non dipende dall'ordine delle colonne nel file.
 */
public final class LettoreCsv {

    private LettoreCsv() {
    }

    /**
     * Legge un file CSV dal classpath.
     *
     * @param percorso percorso relativo a src/main/resources, es. "dati/figure.csv"
     * @return una mappa per ogni riga di dati; lista vuota se il file non esiste
     */
    public static List<Map<String, String>> leggi(String percorso) {
        ClassPathResource risorsa = new ClassPathResource(percorso);
        if (!risorsa.exists()) {
            System.out.println(">> [FigureDB] File non trovato, lo salto: " + percorso);
            return List.of();
        }

        List<Map<String, String>> righe = new ArrayList<>();
        try (BufferedReader lettore = new BufferedReader(
                new InputStreamReader(risorsa.getInputStream(), StandardCharsets.UTF_8))) {

            String contenuto = leggiTutto(lettore);
            List<List<String>> celle = analizza(contenuto);
            if (celle.isEmpty()) {
                return List.of();
            }

            List<String> intestazioni = celle.get(0);
            for (int i = 1; i < celle.size(); i++) {
                List<String> valori = celle.get(i);
                if (rigaVuota(valori)) {
                    continue;
                }
                // Piu' campi che colonne significa quasi sempre una virgola
                // scritta a mano dentro un testo, senza le virgolette che la
                // proteggono. Invece di buttare via il resto in silenzio, lo
                // ricompongo nell'ultima colonna e lo segnalo.
                if (valori.size() > intestazioni.size()) {
                    int ultima = intestazioni.size() - 1;
                    String ricomposto = String.join(",", valori.subList(ultima, valori.size()));
                    valori = new ArrayList<>(valori.subList(0, ultima));
                    valori.add(ricomposto);
                    System.out.println("   [avviso] " + percorso + ", riga " + (i + 1)
                            + ": virgola non protetta da virgolette nel campo '"
                            + intestazioni.get(ultima).trim() + "'. Testo ricomposto,"
                            + " ma conviene correggere il file.");
                }

                Map<String, String> riga = new LinkedHashMap<>();
                for (int c = 0; c < intestazioni.size(); c++) {
                    String valore = c < valori.size() ? valori.get(c).trim() : "";
                    riga.put(intestazioni.get(c).trim(), valore);
                }
                righe.add(riga);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Impossibile leggere " + percorso, e);
        }
        return righe;
    }

    private static String leggiTutto(BufferedReader lettore) throws IOException {
        StringBuilder sb = new StringBuilder();
        String riga;
        while ((riga = lettore.readLine()) != null) {
            sb.append(riga).append('\n');
        }
        return sb.toString();
    }

    /**
     * Analizza il contenuto carattere per carattere tenendo conto delle
     * virgolette: una virgola dentro un campo quotato non separa i campi.
     */
    private static List<List<String>> analizza(String contenuto) {
        List<List<String>> risultato = new ArrayList<>();
        List<String> rigaCorrente = new ArrayList<>();
        StringBuilder campo = new StringBuilder();
        boolean dentroVirgolette = false;

        for (int i = 0; i < contenuto.length(); i++) {
            char c = contenuto.charAt(i);

            if (dentroVirgolette) {
                if (c == '"') {
                    boolean virgolettaRaddoppiata = i + 1 < contenuto.length()
                            && contenuto.charAt(i + 1) == '"';
                    if (virgolettaRaddoppiata) {
                        campo.append('"');
                        i++;
                    } else {
                        dentroVirgolette = false;
                    }
                } else {
                    campo.append(c);
                }
            } else if (c == '"') {
                dentroVirgolette = true;
            } else if (c == ',') {
                rigaCorrente.add(campo.toString());
                campo.setLength(0);
            } else if (c == '\n') {
                rigaCorrente.add(campo.toString());
                campo.setLength(0);
                risultato.add(rigaCorrente);
                rigaCorrente = new ArrayList<>();
            } else if (c != '\r') {
                campo.append(c);
            }
        }

        if (campo.length() > 0 || !rigaCorrente.isEmpty()) {
            rigaCorrente.add(campo.toString());
            risultato.add(rigaCorrente);
        }
        return risultato;
    }

    private static boolean rigaVuota(List<String> valori) {
        return valori.stream().allMatch(v -> v == null || v.isBlank());
    }

    // ------------------------------------------------------------------
    // Conversioni con messaggi di errore comprensibili
    // ------------------------------------------------------------------

    public static String testo(Map<String, String> riga, String colonna) {
        String valore = riga.get(colonna);
        if (valore == null || valore.isBlank()) {
            return null;
        }
        return valore.trim();
    }

    public static String testoObbligatorio(Map<String, String> riga, String colonna, String file) {
        String valore = testo(riga, colonna);
        if (valore == null) {
            throw new IllegalStateException(
                    "In " + file + " manca il valore obbligatorio della colonna '" + colonna
                    + "'. Riga: " + riga);
        }
        return valore;
    }

    public static Float numero(Map<String, String> riga, String colonna, String file) {
        String valore = testoObbligatorio(riga, colonna, file);
        try {
            return Float.parseFloat(valore.replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    "In " + file + " la colonna '" + colonna + "' contiene '" + valore
                    + "', che non e' un numero. Usa il punto come separatore decimale.");
        }
    }

    public static boolean flag(Map<String, String> riga, String colonna) {
        String valore = testo(riga, colonna);
        if (valore == null) {
            return false;
        }
        String v = valore.toLowerCase();
        return v.equals("si") || v.equals("sì") || v.equals("true") || v.equals("1")
                || v.equals("x") || v.equals("yes");
    }

    /** Divide un campo con piu' valori separati dal carattere barra verticale. */
    public static List<String> elenco(Map<String, String> riga, String colonna) {
        String valore = testo(riga, colonna);
        if (valore == null) {
            return List.of();
        }
        List<String> voci = new ArrayList<>();
        for (String pezzo : valore.split("\\|")) {
            if (!pezzo.isBlank()) {
                voci.add(pezzo.trim());
            }
        }
        return voci;
    }
}
