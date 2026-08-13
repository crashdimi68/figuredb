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
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Utente registrato del sistema (dati anagrafici).
 * Le credenziali di accesso sono nella entita' Credentials.
 */
@Entity
@Table(name = "users") // "user" e' una parola riservata in PostgreSQL
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "{user.nome.notblank}")
    @Column(nullable = false)
    private String nome;

    @NotBlank(message = "{user.cognome.notblank}")
    @Column(nullable = false)
    private String cognome;

    @NotBlank(message = "{user.email.notblank}")
    @Email(message = "{user.email.email}")
    @Column(nullable = false, unique = true)
    private String email;

    @OneToMany(mappedBy = "autore", fetch = FetchType.LAZY)
    private List<Recensione> recensioni = new ArrayList<>();

    @OneToMany(mappedBy = "utente", fetch = FetchType.LAZY)
    private List<VoceCollezione> collezione = new ArrayList<>();

    public User() {
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Recensione> getRecensioni() {
        return recensioni;
    }

    public void setRecensioni(List<Recensione> recensioni) {
        this.recensioni = recensioni;
    }

    public List<VoceCollezione> getCollezione() {
        return collezione;
    }

    public void setCollezione(List<VoceCollezione> collezione) {
        this.collezione = collezione;
    }

    public String getNomeCompleto() {
        return this.nome + " " + this.cognome;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User other = (User) obj;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "User[" + id + ", " + getNomeCompleto() + "]";
    }
}
