package it.uniroma3.siw.figuredb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import it.uniroma3.siw.figuredb.service.AziendaService;
import it.uniroma3.siw.figuredb.service.FigureService;
import it.uniroma3.siw.figuredb.service.GachaService;

@Controller
public class AziendaController {

    private final AziendaService aziendaService;
    private final GachaService gachaService;
    private final FigureService figureService;

    public AziendaController(AziendaService aziendaService,
                             GachaService gachaService,
                             FigureService figureService) {
        this.aziendaService = aziendaService;
        this.gachaService = gachaService;
        this.figureService = figureService;
    }

    @GetMapping("/aziende")
    public String elenco(Model model) {
        model.addAttribute("aziende", this.aziendaService.findAll());
        return "aziende/elenco";
    }

    @GetMapping("/aziende/{id}")
    public String dettaglio(@PathVariable Long id, Model model) {
        model.addAttribute("azienda", this.aziendaService.findById(id));
        model.addAttribute("figure", this.figureService.findByAzienda(id));
        model.addAttribute("gacha", this.gachaService.findByAzienda(id));
        return "aziende/dettaglio";
    }

    @GetMapping("/gacha")
    public String elencoGacha(Model model) {
        model.addAttribute("gacha", this.gachaService.findAll());
        return "gacha/elenco";
    }

    @GetMapping("/gacha/{id}")
    public String dettaglioGacha(@PathVariable Long id, Model model) {
        model.addAttribute("gacha", this.gachaService.findById(id));
        return "gacha/dettaglio";
    }
}
