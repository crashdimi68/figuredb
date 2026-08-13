package it.uniroma3.siw.figuredb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import it.uniroma3.siw.figuredb.service.FigureService;
import it.uniroma3.siw.figuredb.service.FumettoService;
import it.uniroma3.siw.figuredb.service.GachaService;
import it.uniroma3.siw.figuredb.service.SerieService;

/**
 * CASI D'USO PUBBLICI su Serie: elenco, ricerca e dettaglio.
 */
@Controller
public class SerieController {

    private final SerieService serieService;
    private final FumettoService fumettoService;
    private final FigureService figureService;
    private final GachaService gachaService;

    public SerieController(SerieService serieService,
                           FumettoService fumettoService,
                           FigureService figureService,
                           GachaService gachaService) {
        this.serieService = serieService;
        this.fumettoService = fumettoService;
        this.figureService = figureService;
        this.gachaService = gachaService;
    }

    @GetMapping("/serie")
    public String elenco(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("serie", this.serieService.cercaPerNome(q));
        model.addAttribute("q", q);
        return "serie/elenco";
    }

    @GetMapping("/serie/{id}")
    public String dettaglio(@PathVariable Long id, Model model) {
        model.addAttribute("serie", this.serieService.findByIdConPersonaggi(id));
        model.addAttribute("fumetti", this.fumettoService.findBySerie(id));
        model.addAttribute("figure", this.figureService.findBySerie(id));
        model.addAttribute("gacha", this.gachaService.findBySerie(id));
        return "serie/dettaglio";
    }
}
