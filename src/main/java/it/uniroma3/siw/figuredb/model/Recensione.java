package it.uniroma3.siw.figuredb.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Recensione scritta da un utente registrato su una Figure.
 *
 * Regola di business: un utente puo' inserire al massimo una recensione
 * per la stessa figure. Il vincolo e' dichiarato qui a livello di schema
 * e verificato nel Service Layer (RecensioneService).
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_recensione_autore_figure",
        columnNames = {"autore_id", "figure_id"}))
public class Recensione {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "{recensione.titolo.notblank}")
    @Column(nullable = false)
    private String titolo;

    @NotBlank(message = "{recensione.testo.notblank}")
    @Size(min = 10, max = 2000, message = "{recensione.testo.size}")
    @Column(nullable = false, length = 2000)
    private String testo;

    @NotNull(message = "{recensione.voto.notnull}")
    @Min(value = 1, message = "{recensione.voto.range}")
    @Max(value = 5, message = "{recensione.voto.range}")
    @Column(nullable = false)
    private Integer voto;

    @Column(nullable = false)
    private LocalDate data;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Figure figure;

    /** EAGER: il nome dell'autore e' sempre mostrato accanto alla recensione. */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private User autore;

    public Recensione() {
        this.data = LocalDate.now();
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

    public String getTesto() {
        return testo;
    }

    public void setTesto(String testo) {
        this.testo = testo;
    }

    public Integer getVoto() {
        return voto;
    }

    public void setVoto(Integer voto) {
        this.voto = voto;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public Figure getFigure() {
        return figure;
    }

    public void setFigure(Figure figure) {
        this.figure = figure;
    }

    public User getAutore() {
        return autore;
    }

    public void setAutore(User autore) {
        this.autore = autore;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Recensione other = (Recensione) obj;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Recensione[" + id + ", voto=" + voto + "]";
    }
}
