package it.uniroma3.siw.figuredb.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Credenziali di accesso associate ad un User.
 * Il ruolo determina le autorizzazioni (vedi SecurityConfiguration).
 */
@Entity
public class Credentials {

    public static final String DEFAULT_ROLE = "DEFAULT";
    public static final String ADMIN_ROLE = "ADMIN";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "{credentials.username.notblank}")
    @Size(min = 3, max = 30, message = "{credentials.username.size}")
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message = "{credentials.password.notblank}")
    @Size(min = 6, message = "{credentials.password.size}")
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    /** Composizione: le credenziali appartengono ad un solo utente. */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = false)
    private User user;

    public Credentials() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isAdmin() {
        return ADMIN_ROLE.equals(this.role);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Credentials other = (Credentials) obj;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Credentials[" + id + ", " + username + ", " + role + "]";
    }
}
