package it.uniroma3.siw.figuredb.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Volume a fumetti appartenente ad una Serie.
 */
@Entity
public class Fumetto {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "{fumetto.titolo.notblank}")
    @Column(nullable = false)
    private String titolo;

    /** true se si tratta di una edizione variant / cover alternativa. */
    @Column(nullable = false)
    private Boolean variant = Boolean.FALSE;

    @NotNull(message = "{fumetto.anno.notnull}")
    @Column(nullable = false)
    private Integer anno;

    @NotBlank(message = "{fumetto.isbn.notblank}")
    @Pattern(regexp = "^(97(8|9))?\\d{9}(\\d|X)$", message = "{fumetto.isbn.pattern}")
    @Column(nullable = false, unique = true)
    private String isbn;

    @NotNull(message = "{fumetto.prezzo.notnull}")
    @PositiveOrZero(message = "{fumetto.prezzo.positive}")
    @Column(nullable = false)
    private Float prezzo;

    @NotBlank(message = "{fumetto.genere.notblank}")
    @Column(nullable = false)
    private String genere;

    /** Percorso della copertina, risolto dalla cartella immagini. Facoltativo. */
    private String immagine;

    /**
     * Molti-a-uno verso Scrittore. LAZY: nell'elenco dei fumetti mostriamo
     * solo il titolo; quando serve lo scrittore usiamo una JOIN FETCH esplicita
     * (vedi FumettoRepository) per evitare il problema N+1.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Scrittore scrittore;

    /**
     * Molti-a-molti con Disegnatore: un fumetto ha 1..* disegnatori,
     * un disegnatore lavora su 0..* fumetti.
     * Questo e' il lato PROPRIETARIO dell'associazione.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "fumetto_disegnatore",
               joinColumns = @JoinColumn(name = "fumetto_id"),
               inverseJoinColumns = @JoinColumn(name = "disegnatore_id"))
    private Set<Disegnatore> disegnatori = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Editore editore;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Serie serie;

    public Fumetto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public Boolean getVariant() {
        return variant;
    }

    public void setVariant(Boolean variant) {
        this.variant = variant;
    }

    public Integer getAnno() {
        return anno;
    }

    public void setAnno(Integer anno) {
        this.anno = anno;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Float getPrezzo() {
        return prezzo;
    }

    public void setPrezzo(Float prezzo) {
        this.prezzo = prezzo;
    }

    public String getGenere() {
        return genere;
    }

    public void setGenere(String genere) {
        this.genere = genere;
    }

    public String getImmagine() {
        return immagine;
    }

    public void setImmagine(String immagine) {
        this.immagine = immagine;
    }

    public Scrittore getScrittore() {
        return scrittore;
    }

    public void setScrittore(Scrittore scrittore) {
        this.scrittore = scrittore;
    }

    public Set<Disegnatore> getDisegnatori() {
        return disegnatori;
    }

    public void setDisegnatori(Set<Disegnatore> disegnatori) {
        this.disegnatori = disegnatori;
    }

    public Editore getEditore() {
        return editore;
    }

    public void setEditore(Editore editore) {
        this.editore = editore;
    }

    public Serie getSerie() {
        return serie;
    }

    public void setSerie(Serie serie) {
        this.serie = serie;
    }

    /** Mantiene coerente l'associazione bidirezionale molti-a-molti. */
    public void aggiungiDisegnatore(Disegnatore disegnatore) {
        this.disegnatori.add(disegnatore);
        disegnatore.getFumetti().add(this);
    }

    public void rimuoviDisegnatore(Disegnatore disegnatore) {
        this.disegnatori.remove(disegnatore);
        disegnatore.getFumetti().remove(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Fumetto other = (Fumetto) obj;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Fumetto[" + id + ", " + titolo + "]";
    }
}
