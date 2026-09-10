package it.uniroma3.siw.figuredb.controller;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import it.uniroma3.siw.figuredb.model.Recensione;
import it.uniroma3.siw.figuredb.service.FigureService;
import it.uniroma3.siw.figuredb.service.RecensioneService;

/**
 * CASI D'USO PUBBLICI su Figure: elenco del catalogo e dettaglio con recensioni.
 */
@Controller
public class FigureController {

    /** Quante figure mostrare per pagina in /figure. */
    private static final int FIGURE_PER_PAGINA = 12;

    private final FigureService figureService;
    private final RecensioneService recensioneService;

    public FigureController(FigureService figureService,
                            RecensioneService recensioneService) {
        this.figureService = figureService;
        this.recensioneService = recensioneService;
    }

    /**
     * Elenco del catalogo, paginato.
     * Il numero di pagina arriva come parametro ?pagina=N (0 se assente).
     */
    @GetMapping("/figure")
    public String elenco(@RequestParam(defaultValue = "0") int pagina, Model model) {
        model.addAttribute("pagina",
                this.figureService.findPagina(pagina, FIGURE_PER_PAGINA));
        return "figure/elenco";
    }

    @GetMapping("/figure/{id}")
    public String dettaglio(@PathVariable Long id, Model model,
                            @ModelAttribute("userDetails") UserDetails userDetails) {
        model.addAttribute("figure", this.figureService.findById(id));
        model.addAttribute("recensioni", this.recensioneService.findByFigure(id));
        model.addAttribute("nuovaRecensione", new Recensione());

        if (userDetails != null) {
            model.addAttribute("haGiaRecensito",
                    this.recensioneService.haGiaRecensito(userDetails.getUsername(), id));
        }
        return "figure/dettaglio";
    }
}
