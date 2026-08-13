package it.uniroma3.siw.figuredb.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Figure da collezione: entita' centrale del catalogo FigureDB.
 *
 * Vincolo di consistenza a livello di schema: la stessa azienda non puo'
 * pubblicare due figure con lo stesso nome nella stessa data di uscita.
 * Il vincolo e' inoltre verificato nel Service Layer (FigureService).
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_figure_nome_azienda_data",
        columnNames = {"nome", "azienda_id", "data_uscita"}))
@NamedEntityGraph(
        name = "Figure.conAziendaESerie",
        attributeNodes = {
                @NamedAttributeNode("azienda"),
                @NamedAttributeNode("serie")
        })
public class Figure {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "{figure.nome.notblank}")
    @Column(nullable = false)
    private String nome;

    @NotNull(message = "{figure.dataUscita.notnull}")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(nullable = false)
    private LocalDate dataUscita;

    /** Altezza in centimetri. */
    @NotNull(message = "{figure.altezza.notnull}")
    @Positive(message = "{figure.altezza.positive}")
    @Column(nullable = false)
    private Float altezza;

    @NotNull(message = "{figure.materiale.notnull}")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Materiale materiale;

    private String immagine;

    @NotNull(message = "{figure.prezzo.notnull}")
    @Positive(message = "{figure.prezzo.positive}")
    @Column(nullable = false)
    private Float prezzo;

    @Size(max = 2000)
    @Column(length = 2000)
    private String descrizione;

    /** true se si tratta di una edizione limitata. */
    @Column(nullable = false)
    private Boolean edizioneLimitata = Boolean.FALSE;

    /**
     * LAZY: nell'elenco del catalogo mostriamo molte figure; caricare
     * l'azienda con una query per riga produrrebbe il problema N+1.
     * Nelle query di elenco usiamo JOIN FETCH / EntityGraph.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Azienda azienda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Serie serie;

    /**
     * Composizione: le recensioni non hanno senso senza la figure recensita.
     * Cancellando la figure si cancellano le sue recensioni.
     */
    @OneToMany(mappedBy = "figure", fetch = FetchType.LAZY,
               cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
               orphanRemoval = true)
    private List<Recensione> recensioni = new ArrayList<>();

    public Figure() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getDataUscita() {
        return dataUscita;
    }

    public void setDataUscita(LocalDate dataUscita) {
        this.dataUscita = dataUscita;
    }

    public Float getAltezza() {
        return altezza;
    }

    public void setAltezza(Float altezza) {
        this.altezza = altezza;
    }

    public Materiale getMateriale() {
        return materiale;
    }

    public void setMateriale(Materiale materiale) {
        this.materiale = materiale;
    }

    public String getImmagine() {
        return immagine;
    }

    public void setImmagine(String immagine) {
        this.immagine = immagine;
    }

    public Float getPrezzo() {
        return prezzo;
    }

    public void setPrezzo(Float prezzo) {
        this.prezzo = prezzo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public Boolean getEdizioneLimitata() {
        return edizioneLimitata;
    }

    public void setEdizioneLimitata(Boolean edizioneLimitata) {
        this.edizioneLimitata = edizioneLimitata;
    }

    public Azienda getAzienda() {
        return azienda;
    }

    public void setAzienda(Azienda azienda) {
        this.azienda = azienda;
    }

    public Serie getSerie() {
        return serie;
    }

    public void setSerie(Serie serie) {
        this.serie = serie;
    }

    public List<Recensione> getRecensioni() {
        return recensioni;
    }

    public void setRecensioni(List<Recensione> recensioni) {
        this.recensioni = recensioni;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Figure other = (Figure) obj;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Figure[" + id + ", " + nome + "]";
    }
}
