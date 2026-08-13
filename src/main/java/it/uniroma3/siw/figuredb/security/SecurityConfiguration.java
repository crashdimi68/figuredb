package it.uniroma3.siw.figuredb.security;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import it.uniroma3.siw.figuredb.model.Credentials;

/**
 * Configurazione di autenticazione e autorizzazione.
 *
 * Ruoli:
 *   DEFAULT  utente registrato: recensioni e collezione personale
 *   ADMIN    amministratore: gestione del catalogo
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final DataSource dataSource;

    public SecurityConfiguration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** Le credenziali sono lette dalla tabella credentials. */
    @Bean
    public UserDetailsService userDetailsService() {
        JdbcUserDetailsManager manager = new JdbcUserDetailsManager(this.dataSource);
        manager.setUsersByUsernameQuery(
                "SELECT username, password, 1 as enabled FROM credentials WHERE username=?");
        manager.setAuthoritiesByUsernameQuery(
                "SELECT username, role FROM credentials WHERE username=?");
        return manager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity httpSecurity) throws Exception {

        // Il token CSRF viene messo in un cookie leggibile da JavaScript, cosi'
        // il frontend React puo' rispedirlo nell'header X-XSRF-TOKEN.
        CsrfTokenRequestAttributeHandler csrfRequestHandler = new CsrfTokenRequestAttributeHandler();
        csrfRequestHandler.setCsrfRequestAttributeName(null);

        httpSecurity.csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(csrfRequestHandler));

        httpSecurity.authorizeHttpRequests(authorize -> {
            // risorse statiche e pagine pubbliche
            authorize.requestMatchers(HttpMethod.GET,
                    "/", "/index", "/login", "/register", "/errore", "/error",
                    "/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico").permitAll();
            authorize.requestMatchers(HttpMethod.POST, "/register", "/login").permitAll();

            // consultazione pubblica del catalogo
            authorize.requestMatchers(HttpMethod.GET,
                    "/serie/**", "/fumetti/**", "/figure/**",
                    "/aziende/**", "/gacha/**", "/catalogo").permitAll();

            // API REST in sola lettura: pubbliche
            authorize.requestMatchers(HttpMethod.GET, "/api/**").permitAll();

            // API REST di scrittura: solo utenti autenticati
            authorize.requestMatchers(HttpMethod.POST, "/api/**").authenticated();
            authorize.requestMatchers(HttpMethod.PUT, "/api/**").authenticated();
            authorize.requestMatchers(HttpMethod.DELETE, "/api/**").authenticated();

            // area di amministrazione
            authorize.requestMatchers("/admin/**").hasAuthority(Credentials.ADMIN_ROLE);

            // tutto il resto (recensioni, collezione, ...) richiede autenticazione
            authorize.anyRequest().authenticated();
        });

        httpSecurity.formLogin(form -> {
            form.loginPage("/login").permitAll();
            form.defaultSuccessUrl("/", true);
            form.failureUrl("/login?error=true");
        });

        httpSecurity.logout(logout -> {
            logout.logoutUrl("/logout");
            logout.logoutSuccessUrl("/");
            logout.invalidateHttpSession(true);
            logout.deleteCookies("JSESSIONID");
            logout.clearAuthentication(true);
            logout.permitAll();
        });

        httpSecurity.exceptionHandling(handling -> handling.accessDeniedPage("/accesso-negato"));

        return httpSecurity.build();
    }
}
