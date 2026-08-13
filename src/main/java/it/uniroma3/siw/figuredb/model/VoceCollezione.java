package it.uniroma3.siw.figuredb.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Voce della collezione personale di un utente: associa un utente ad una
 * Figure indicando se la possiede, la desidera o l'ha preordinata.
 *
 * Regola di business: una sola voce per coppia (utente, figure).
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_voce_utente_figure",
        columnNames = {"utente_id", "figure_id"}))
public class VoceCollezione {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull(message = "{voce.stato.notnull}")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatoVoce stato;

    @NotNull(message = "{voce.quantita.notnull}")
    @Positive(message = "{voce.quantita.positive}")
    @Column(nullable = false)
    private Integer quantita = 1;

    @Column(nullable = false)
    private LocalDate dataAggiunta;

    @Size(max = 500)
    @Column(length = 500)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User utente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Figure figure;

    public VoceCollezione() {
        this.dataAggiunta = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StatoVoce getStato() {
        return stato;
    }

    public void setStato(StatoVoce stato) {
        this.stato = stato;
    }

    public Integer getQuantita() {
        return quantita;
    }

    public void setQuantita(Integer quantita) {
        this.quantita = quantita;
    }

    public LocalDate getDataAggiunta() {
        return dataAggiunta;
    }

    public void setDataAggiunta(LocalDate dataAggiunta) {
        this.dataAggiunta = dataAggiunta;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public User getUtente() {
        return utente;
    }

    public void setUtente(User utente) {
        this.utente = utente;
    }

    public Figure getFigure() {
        return figure;
    }

    public void setFigure(Figure figure) {
        this.figure = figure;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        VoceCollezione other = (VoceCollezione) obj;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "VoceCollezione[" + id + ", " + stato + "]";
    }
}
