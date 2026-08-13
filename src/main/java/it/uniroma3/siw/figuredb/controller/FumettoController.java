package it.uniroma3.siw.figuredb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import it.uniroma3.siw.figuredb.service.AutoreService;
import it.uniroma3.siw.figuredb.service.FumettoService;

/**
 * CASI D'USO PUBBLICI su Fumetto e sui suoi autori.
 */
@Controller
public class FumettoController {

    private final FumettoService fumettoService;
    private final AutoreService autoreService;

    public FumettoController(FumettoService fumettoService, AutoreService autoreService) {
        this.fumettoService = fumettoService;
        this.autoreService = autoreService;
    }

    @GetMapping("/fumetti")
    public String elenco(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("fumetti", this.fumettoService.cercaPerTitolo(q));
        model.addAttribute("q", q);
        return "fumetti/elenco";
    }

    @GetMapping("/fumetti/{id}")
    public String dettaglio(@PathVariable Long id, Model model) {
        model.addAttribute("fumetto", this.fumettoService.findById(id));
        return "fumetti/dettaglio";
    }

    @GetMapping("/fumetti/scrittori/{id}")
    public String dettaglioScrittore(@PathVariable Long id, Model model) {
        model.addAttribute("scrittore", this.autoreService.findScrittoreConFumetti(id));
        return "fumetti/scrittore";
    }

    @GetMapping("/fumetti/disegnatori/{id}")
    public String dettaglioDisegnatore(@PathVariable Long id, Model model) {
        model.addAttribute("disegnatore", this.autoreService.findDisegnatoreConFumetti(id));
        return "fumetti/disegnatore";
    }
}
