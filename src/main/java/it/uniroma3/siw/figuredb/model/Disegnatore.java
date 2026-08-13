package it.uniroma3.siw.figuredb.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

/**
 * Autore dei disegni di un fumetto.
 * Lato NON proprietario dell'associazione molti-a-molti con Fumetto.
 */
@Entity
public class Disegnatore {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "{disegnatore.nome.notblank}")
    @Column(nullable = false)
    private String nome;

    @NotBlank(message = "{disegnatore.cognome.notblank}")
    @Column(nullable = false)
    private String cognome;

    @NotNull(message = "{disegnatore.dataNascita.notnull}")
    @Past(message = "{disegnatore.dataNascita.past}")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(nullable = false)
    private LocalDate dataNascita;

    @NotBlank(message = "{disegnatore.paese.notblank}")
    @Column(nullable = false)
    private String paese;

    /** mappedBy: il lato proprietario e' Fumetto.disegnatori. */
    @ManyToMany(mappedBy = "disegnatori", fetch = FetchType.LAZY)
    private Set<Fumetto> fumetti = new HashSet<>();

    public Disegnatore() {
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

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public LocalDate getDataNascita() {
        return dataNascita;
    }

    public void setDataNascita(LocalDate dataNascita) {
        this.dataNascita = dataNascita;
    }

    public String getPaese() {
        return paese;
    }

    public void setPaese(String paese) {
        this.paese = paese;
    }

    public Set<Fumetto> getFumetti() {
        return fumetti;
    }

    public void setFumetti(Set<Fumetto> fumetti) {
        this.fumetti = fumetti;
    }

    public String getNomeCompleto() {
        return this.nome + " " + this.cognome;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Disegnatore other = (Disegnatore) obj;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Disegnatore[" + id + ", " + getNomeCompleto() + "]";
    }
}
