package it.uniroma3.siw.figuredb.api;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.uniroma3.siw.figuredb.api.dto.FigureDto;
import it.uniroma3.siw.figuredb.api.dto.RiferimentoDto;
import it.uniroma3.siw.figuredb.model.Materiale;
import it.uniroma3.siw.figuredb.repository.FiltroFigure;
import it.uniroma3.siw.figuredb.service.AziendaService;
import it.uniroma3.siw.figuredb.service.FigureService;
import it.uniroma3.siw.figuredb.service.SerieService;

/**
 * API REST consumate dal catalogo React.
 *
 *   GET /api/figure                  elenco filtrato del catalogo
 *   GET /api/figure/{id}             dettaglio di una figure
 *   GET /api/serie                   elenco serie (per i filtri)
 *   GET /api/aziende                 elenco aziende (per i filtri)
 *   GET /api/materiali               valori ammessi per il materiale
 */
@RestController
@RequestMapping("/api")
public class FigureRestController {

    private final FigureService figureService;
    private final SerieService serieService;
    private final AziendaService aziendaService;

    public FigureRestController(FigureService figureService,
                                SerieService serieService,
                                AziendaService aziendaService) {
        this.figureService = figureService;
        this.serieService = serieService;
        this.aziendaService = aziendaService;
    }

    @GetMapping("/figure")
    public ResponseEntity<List<FigureDto>> elenco(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long serieId,
            @RequestParam(required = false) Long aziendaId,
            @RequestParam(required = false) Materiale materiale,
            @RequestParam(required = false, defaultValue = "false") Boolean soloLimitate,
            @RequestParam(required = false) Float prezzoMin,
            @RequestParam(required = false) Float prezzoMax) {

        FiltroFigure filtro = new FiltroFigure(
                q, serieId, aziendaId, materiale, soloLimitate, prezzoMin, prezzoMax);

        List<FigureDto> risultato = this.figureService.cerca(filtro).stream()
                .map(FigureDto::da)
                .toList();

        return ResponseEntity.ok(risultato);
    }

    @GetMapping("/figure/{id}")
    public ResponseEntity<FigureDto> dettaglio(@PathVariable Long id) {
        return ResponseEntity.ok(FigureDto.da(this.figureService.findById(id)));
    }

    @GetMapping("/serie")
    public ResponseEntity<List<RiferimentoDto>> serie() {
        return ResponseEntity.ok(this.serieService.findAll().stream()
                .map(s -> new RiferimentoDto(s.getId(), s.getNome()))
                .toList());
    }

    @GetMapping("/aziende")
    public ResponseEntity<List<RiferimentoDto>> aziende() {
        return ResponseEntity.ok(this.aziendaService.findAll().stream()
                .map(a -> new RiferimentoDto(a.getId(), a.getNome()))
                .toList());
    }

    @GetMapping("/materiali")
    public ResponseEntity<List<String>> materiali() {
        return ResponseEntity.ok(Arrays.stream(Materiale.values())
                .map(Enum::name)
                .toList());
    }
}
