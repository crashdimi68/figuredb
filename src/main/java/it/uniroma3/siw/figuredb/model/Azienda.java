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
 * Azienda produttrice di figure da collezione e di serie gacha
 * (es. Good Smile Company, Banpresto, Bandai, Kotobukiya).
 */
@Entity
public class Azienda {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "{azienda.nome.notblank}")
    @Column(nullable = false, unique = true)
    private String nome;

    @Size(max = 2000)
    @Column(length = 2000)
    private String descrizione;

    private String logo;

    @NotBlank(message = "{azienda.regione.notblank}")
    @Column(nullable = false)
    private String regione;

    @OneToMany(mappedBy = "azienda", fetch = FetchType.LAZY)
    private List<Figure> figure = new ArrayList<>();

    /**
     * Composizione: una serie gacha e' sempre prodotta da una azienda
     * e non ha senso senza di essa.
     */
    @OneToMany(mappedBy = "azienda", fetch = FetchType.LAZY)
    private List<Gacha> gacha = new ArrayList<>();

    public Azienda() {
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

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getRegione() {
        return regione;
    }

    public void setRegione(String regione) {
        this.regione = regione;
    }

    public List<Figure> getFigure() {
        return figure;
    }

    public void setFigure(List<Figure> figure) {
        this.figure = figure;
    }

    public List<Gacha> getGacha() {
        return gacha;
    }

    public void setGacha(List<Gacha> gacha) {
        this.gacha = gacha;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Azienda other = (Azienda) obj;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Azienda[" + id + ", " + nome + "]";
    }
}
