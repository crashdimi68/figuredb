package it.uniroma3.siw.figuredb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import it.uniroma3.siw.figuredb.service.FigureService;
import it.uniroma3.siw.figuredb.service.SerieService;

@Controller
public class HomeController {

    private final FigureService figureService;
    private final SerieService serieService;

    public HomeController(FigureService figureService, SerieService serieService) {
        this.figureService = figureService;
        this.serieService = serieService;
    }

    @GetMapping({"/", "/index"})
    public String home(Model model) {
        model.addAttribute("ultimeUscite", this.figureService.findUltimeUscite());
        model.addAttribute("serie", this.serieService.findAll());
        return "index";
    }

    /** Pagina che ospita il catalogo realizzato in React. */
    @GetMapping("/catalogo")
    public String catalogo() {
        return "catalogo";
    }

    @GetMapping("/accesso-negato")
    public String accessoNegato(Model model) {
        model.addAttribute("codice", 403);
        model.addAttribute("messaggio", "Non hai i permessi per accedere a questa pagina.");
        return "errore";
    }
}
