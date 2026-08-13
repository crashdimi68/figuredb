package it.uniroma3.siw.figuredb.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

/**
 * Autore dei testi di un fumetto.
 */
@Entity
public class Scrittore {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "{scrittore.nome.notblank}")
    @Column(nullable = false)
    private String nome;

    @NotBlank(message = "{scrittore.cognome.notblank}")
    @Column(nullable = false)
    private String cognome;

    @NotNull(message = "{scrittore.dataNascita.notnull}")
    @Past(message = "{scrittore.dataNascita.past}")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(nullable = false)
    private LocalDate dataNascita;

    @NotBlank(message = "{scrittore.paese.notblank}")
    @Column(nullable = false)
    private String paese;

    /**
     * Uno scrittore ha scritto molti fumetti (0..*).
     * LAZY: la bibliografia serve solo nella pagina di dettaglio.
     */
    @OneToMany(mappedBy = "scrittore", fetch = FetchType.LAZY)
    private List<Fumetto> fumetti = new ArrayList<>();

    public Scrittore() {
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

    public List<Fumetto> getFumetti() {
        return fumetti;
    }

    public void setFumetti(List<Fumetto> fumetti) {
        this.fumetti = fumetti;
    }

    public String getNomeCompleto() {
        return this.nome + " " + this.cognome;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Scrittore other = (Scrittore) obj;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Scrittore[" + id + ", " + getNomeCompleto() + "]";
    }
}
