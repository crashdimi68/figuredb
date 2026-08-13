package it.uniroma3.siw.figuredb.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Personaggio appartenente ad una Serie.
 */
@Entity
public class Personaggio {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "{personaggio.nome.notblank}")
    @Column(nullable = false)
    private String nome;

    @Size(max = 2000)
    @Column(length = 2000)
    private String descrizione;

    /**
     * Lato "molti" dell'associazione con Serie.
     * EAGER: quando carichiamo un personaggio vogliamo quasi sempre
     * mostrare anche la serie di appartenenza, ed e' un singolo oggetto.
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private Serie serie;

    public Personaggio() {
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

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public Serie getSerie() {
        return serie;
    }

    public void setSerie(Serie serie) {
        this.serie = serie;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Personaggio other = (Personaggio) obj;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Personaggio[" + id + ", " + nome + "]";
    }
}
