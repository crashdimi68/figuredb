package it.uniroma3.siw.figuredb.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Casa editrice che pubblica i fumetti.
 */
@Entity
public class Editore {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "{editore.nome.notblank}")
    @Column(nullable = false, unique = true)
    private String nome;

    @Size(max = 2000)
    @Column(length = 2000)
    private String descrizione;

    /** Area geografica di riferimento (es. Giappone, Italia, USA). */
    @NotBlank(message = "{editore.regione.notblank}")
    @Column(nullable = false)
    private String regione;

    private String logo;

    @OneToMany(mappedBy = "editore", fetch = FetchType.LAZY)
    private List<Fumetto> fumetti = new ArrayList<>();

    public Editore() {
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

    public String getRegione() {
        return regione;
    }

    public void setRegione(String regione) {
        this.regione = regione;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public List<Fumetto> getFumetti() {
        return fumetti;
    }

    public void setFumetti(List<Fumetto> fumetti) {
        this.fumetti = fumetti;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Editore other = (Editore) obj;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Editore[" + id + ", " + nome + "]";
    }
}
