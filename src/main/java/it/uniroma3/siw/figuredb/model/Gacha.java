package it.uniroma3.siw.figuredb.model;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Serie gacha (capsule / blind box) prodotta da una Azienda.
 */
@Entity
public class Gacha {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "{gacha.nome.notblank}")
    @Column(nullable = false)
    private String nome;

    @Size(max = 2000)
    @Column(length = 2000)
    private String descrizione;

    @NotNull(message = "{gacha.dataUscita.notnull}")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(nullable = false)
    private LocalDate dataUscita;

    private String immagine;

    @NotNull(message = "{gacha.prezzo.notnull}")
    @Positive(message = "{gacha.prezzo.positive}")
    @Column(nullable = false)
    private Float prezzo;

    /**
     * LAZY come tutte le associazioni a singolo oggetto usate negli elenchi:
     * le query che servono le viste caricano azienda e serie con un join fetch
     * esplicito (vedi GachaRepository), evitando il problema delle N+1 query.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Azienda azienda;

    /** La serie a cui il gacha e' ispirato. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Serie serie;

    public Gacha() {
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

    public LocalDate getDataUscita() {
        return dataUscita;
    }

    public void setDataUscita(LocalDate dataUscita) {
        this.dataUscita = dataUscita;
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Gacha other = (Gacha) obj;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Gacha[" + id + ", " + nome + "]";
    }
}
