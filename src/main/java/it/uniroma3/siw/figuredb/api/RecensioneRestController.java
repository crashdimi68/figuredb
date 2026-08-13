package it.uniroma3.siw.figuredb.api;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.uniroma3.siw.figuredb.api.dto.NuovaRecensioneRequest;
import it.uniroma3.siw.figuredb.api.dto.RecensioneDto;
import it.uniroma3.siw.figuredb.model.Credentials;
import it.uniroma3.siw.figuredb.model.Recensione;
import it.uniroma3.siw.figuredb.service.CredentialsService;
import it.uniroma3.siw.figuredb.service.RecensioneService;
import jakarta.validation.Valid;

/**
 * API REST per le recensioni, usate dal componente React del catalogo.
 *
 *   GET    /api/figure/{id}/recensioni    pubblica
 *   POST   /api/figure/{id}/recensioni    utente autenticato       201 Created
 *   PUT    /api/recensioni/{id}           solo l'autore            200 OK
 *   DELETE /api/recensioni/{id}           autore o ADMIN           204 No Content
 */
@RestController
@RequestMapping("/api")
public class RecensioneRestController {

    private final RecensioneService recensioneService;
    private final CredentialsService credentialsService;

    public RecensioneRestController(RecensioneService recensioneService,
                                    CredentialsService credentialsService) {
        this.recensioneService = recensioneService;
        this.credentialsService = credentialsService;
    }

    @GetMapping("/figure/{figureId}/recensioni")
    public ResponseEntity<List<RecensioneDto>> elenco(@PathVariable Long figureId) {
        return ResponseEntity.ok(this.recensioneService.findByFigure(figureId).stream()
                .map(RecensioneDto::da)
                .toList());
    }

    @GetMapping("/figure/{figureId}/recensioni/statistiche")
    public ResponseEntity<Map<String, Object>> statistiche(@PathVariable Long figureId) {
        Double media = this.recensioneService.mediaVoti(figureId);
        List<Recensione> recensioni = this.recensioneService.findByFigure(figureId);
        return ResponseEntity.ok(Map.of(
                "media", media != null ? media : 0.0,
                "numero", recensioni.size()));
    }

    @PostMapping("/figure/{figureId}/recensioni")
    public ResponseEntity<RecensioneDto> inserisci(@PathVariable Long figureId,
                                                   @Valid @RequestBody NuovaRecensioneRequest richiesta,
                                                   @AuthenticationPrincipal UserDetails utente) {
        Recensione recensione = new Recensione();
        recensione.setTitolo(richiesta.titolo());
        recensione.setTesto(richiesta.testo());
        recensione.setVoto(richiesta.voto());

        Recensione salvata = this.recensioneService.inserisci(recensione, figureId, utente.getUsername());

        return ResponseEntity
                .created(URI.create("/api/recensioni/" + salvata.getId()))
                .body(RecensioneDto.da(salvata));
    }

    @GetMapping("/recensioni/{id}")
    public ResponseEntity<RecensioneDto> dettaglio(@PathVariable Long id) {
        return ResponseEntity.ok(RecensioneDto.da(this.recensioneService.findById(id)));
    }

    @PutMapping("/recensioni/{id}")
    public ResponseEntity<RecensioneDto> aggiorna(@PathVariable Long id,
                                                  @Valid @RequestBody NuovaRecensioneRequest richiesta,
                                                  @AuthenticationPrincipal UserDetails utente) {
        Recensione dati = new Recensione();
        dati.setTitolo(richiesta.titolo());
        dati.setTesto(richiesta.testo());
        dati.setVoto(richiesta.voto());

        Recensione aggiornata = this.recensioneService.aggiorna(id, dati, utente.getUsername());
        return ResponseEntity.ok(RecensioneDto.da(aggiornata));
    }

    @DeleteMapping("/recensioni/{id}")
    public ResponseEntity<Void> elimina(@PathVariable Long id,
                                        @AuthenticationPrincipal UserDetails utente) {
        Credentials credenziali = this.credentialsService.findByUsername(utente.getUsername());
        this.recensioneService.elimina(id, utente.getUsername(), credenziali.isAdmin());
        return ResponseEntity.noContent().build();
    }
}
