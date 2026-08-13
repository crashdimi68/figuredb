package it.uniroma3.siw.figuredb.controller;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import it.uniroma3.siw.figuredb.model.Recensione;
import it.uniroma3.siw.figuredb.service.CollezioneService;
import it.uniroma3.siw.figuredb.service.FigureService;
import it.uniroma3.siw.figuredb.service.RecensioneService;

/**
 * CASI D'USO PUBBLICI su Figure: elenco del catalogo e dettaglio con recensioni.
 */
@Controller
public class FigureController {

    private final FigureService figureService;
    private final RecensioneService recensioneService;
    private final CollezioneService collezioneService;

    public FigureController(FigureService figureService,
                            RecensioneService recensioneService,
                            CollezioneService collezioneService) {
        this.figureService = figureService;
        this.recensioneService = recensioneService;
        this.collezioneService = collezioneService;
    }

    @GetMapping("/figure")
    public String elenco(Model model) {
        model.addAttribute("figure", this.figureService.findAll());
        return "figure/elenco";
    }

    @GetMapping("/figure/{id}")
    public String dettaglio(@PathVariable Long id, Model model,
                            @ModelAttribute("userDetails") UserDetails userDetails) {
        model.addAttribute("figure", this.figureService.findById(id));
        model.addAttribute("recensioni", this.recensioneService.findByFigure(id));
        model.addAttribute("mediaVoti", this.recensioneService.mediaVoti(id));
        model.addAttribute("nuovaRecensione", new Recensione());

        if (userDetails != null) {
            String username = userDetails.getUsername();
            model.addAttribute("haGiaRecensito", this.recensioneService.haGiaRecensito(username, id));
            model.addAttribute("inCollezione", this.collezioneService.isInCollezione(username, id));
        }
        return "figure/dettaglio";
    }
}
