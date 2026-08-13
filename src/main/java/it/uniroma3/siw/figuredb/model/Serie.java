package it.uniroma3.siw.figuredb.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Serie: l'opera (manga, anime, videogioco, ...) a cui appartengono
 * i fumetti, i personaggi e le figure da collezione.
 */
@Entity
public class Serie {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "{serie.nome.notblank}")
    @Column(nullable = false, unique = true)
    private String nome;

    @NotNull(message = "{serie.tipo.notnull}")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoSerie tipo;

    @Size(max = 2000)
    @Column(length = 2000)
    private String descrizione;

    /** URL o path del logo della serie. */
    private String logo;

    /**
     * Una serie contiene 1..* personaggi.
     * L'associazione e' una composizione: il personaggio non ha senso
     * al di fuori della serie, quindi propaghiamo PERSIST/MERGE/REMOVE.
     */
    @OneToMany(mappedBy = "serie", fetch = FetchType.LAZY,
               cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
               orphanRemoval = true)
    private List<Personaggio> personaggi = new ArrayList<>();

    /** LAZY: la lista dei fumetti serve solo nel dettaglio della serie. */
    @OneToMany(mappedBy = "serie", fetch = FetchType.LAZY)
    private List<Fumetto> fumetti = new ArrayList<>();

    /** LAZY: la lista delle figure serve solo nel dettaglio della serie. */
    @OneToMany(mappedBy = "serie", fetch = FetchType.LAZY)
    private List<Figure> figure = new ArrayList<>();

    /** LAZY: le serie gacha ispirate a questa serie. */
    @OneToMany(mappedBy = "serie", fetch = FetchType.LAZY)
    private List<Gacha> gacha = new ArrayList<>();

    public Serie() {
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

    public TipoSerie getTipo() {
        return tipo;
    }

    public void setTipo(TipoSerie tipo) {
        this.tipo = tipo;
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

    public List<Personaggio> getPersonaggi() {
        return personaggi;
    }

    public void setPersonaggi(List<Personaggio> personaggi) {
        this.personaggi = personaggi;
    }

    public List<Fumetto> getFumetti() {
        return fumetti;
    }

    public void setFumetti(List<Fumetto> fumetti) {
        this.fumetti = fumetti;
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

    /** Metodo di comodo che mantiene coerente l'associazione bidirezionale. */
    public void aggiungiPersonaggio(Personaggio personaggio) {
        this.personaggi.add(personaggio);
        personaggio.setSerie(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Serie other = (Serie) obj;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Serie[" + id + ", " + nome + "]";
    }
}
