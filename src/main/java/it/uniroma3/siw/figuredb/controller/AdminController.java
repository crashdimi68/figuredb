package it.uniroma3.siw.figuredb.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.uniroma3.siw.figuredb.model.Azienda;
import it.uniroma3.siw.figuredb.model.Figure;
import it.uniroma3.siw.figuredb.model.Fumetto;
import it.uniroma3.siw.figuredb.model.Materiale;
import it.uniroma3.siw.figuredb.model.Serie;
import it.uniroma3.siw.figuredb.model.TipoSerie;
import it.uniroma3.siw.figuredb.service.AutoreService;
import it.uniroma3.siw.figuredb.service.AziendaService;
import it.uniroma3.siw.figuredb.service.FigureService;
import it.uniroma3.siw.figuredb.service.FumettoService;
import it.uniroma3.siw.figuredb.service.SerieService;
import it.uniroma3.siw.figuredb.service.exception.VincoloViolatoException;
import jakarta.validation.Valid;

/**
 * CASI D'USO RISERVATI ALL'AMMINISTRATORE.
 * Tutti gli URL sono sotto /admin/** e protetti da Spring Security.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final FigureService figureService;
    private final FumettoService fumettoService;
    private final SerieService serieService;
    private final AziendaService aziendaService;
    private final AutoreService autoreService;

    public AdminController(FigureService figureService,
                           FumettoService fumettoService,
                           SerieService serieService,
                           AziendaService aziendaService,
                           AutoreService autoreService) {
        this.figureService = figureService;
        this.fumettoService = fumettoService;
        this.serieService = serieService;
        this.aziendaService = aziendaService;
        this.autoreService = autoreService;
    }

    @GetMapping
    public String cruscotto(Model model) {
        model.addAttribute("numeroFigure", this.figureService.findAll().size());
        model.addAttribute("numeroFumetti", this.fumettoService.findAll().size());
        model.addAttribute("numeroSerie", this.serieService.findAll().size());
        model.addAttribute("numeroAziende", this.aziendaService.findAll().size());
        return "admin/cruscotto";
    }

    // ================= FIGURE =================

    @GetMapping("/figure")
    public String elencoFigure(Model model) {
        model.addAttribute("figure", this.figureService.findAll());
        return "admin/figure/elenco";
    }

    @GetMapping("/figure/nuova")
    public String formNuovaFigure(Model model) {
        model.addAttribute("figure", new Figure());
        this.popolaSelectFigure(model);
        return "admin/figure/form";
    }

    @PostMapping("/figure/nuova")
    public String creaFigure(@Valid @ModelAttribute("figure") Figure figure,
                             BindingResult errori,
                             @RequestParam Long aziendaId,
                             @RequestParam Long serieId,
                             Model model,
                             RedirectAttributes redirect) {
        if (errori.hasErrors()) {
            this.popolaSelectFigure(model);
            return "admin/figure/form";
        }
        try {
            Figure salvata = this.figureService.inserisciACatalogo(figure, aziendaId, serieId);
            redirect.addFlashAttribute("successo", "Figure inserita a catalogo.");
            return "redirect:/figure/" + salvata.getId();
        } catch (VincoloViolatoException e) {
            model.addAttribute("errore", e.getMessage());
            this.popolaSelectFigure(model);
            return "admin/figure/form";
        }
    }

    @GetMapping("/figure/{id}/modifica")
    public String formModificaFigure(@PathVariable Long id, Model model) {
        model.addAttribute("figure", this.figureService.findById(id));
        this.popolaSelectFigure(model);
        return "admin/figure/form";
    }

    @PostMapping("/figure/{id}/modifica")
    public String modificaFigure(@PathVariable Long id,
                                 @Valid @ModelAttribute("figure") Figure figure,
                                 BindingResult errori,
                                 @RequestParam Long aziendaId,
                                 @RequestParam Long serieId,
                                 Model model,
                                 RedirectAttributes redirect) {
        if (errori.hasErrors()) {
            this.popolaSelectFigure(model);
            return "admin/figure/form";
        }
        try {
            this.figureService.aggiorna(id, figure, aziendaId, serieId);
            redirect.addFlashAttribute("successo", "Figure aggiornata.");
            return "redirect:/figure/" + id;
        } catch (VincoloViolatoException e) {
            model.addAttribute("errore", e.getMessage());
            this.popolaSelectFigure(model);
            return "admin/figure/form";
        }
    }

    @PostMapping("/figure/{id}/elimina")
    public String eliminaFigure(@PathVariable Long id, RedirectAttributes redirect) {
        this.figureService.elimina(id);
        redirect.addFlashAttribute("successo", "Figure eliminata dal catalogo.");
        return "redirect:/admin/figure";
    }

    // ================= FUMETTI =================

    @GetMapping("/fumetti/nuovo")
    public String formNuovoFumetto(Model model) {
        model.addAttribute("fumetto", new Fumetto());
        this.popolaSelectFumetto(model);
        return "admin/fumetti/form";
    }

    @PostMapping("/fumetti/nuovo")
    public String creaFumetto(@Valid @ModelAttribute("fumetto") Fumetto fumetto,
                              BindingResult errori,
                              @RequestParam Long scrittoreId,
                              @RequestParam Long editoreId,
                              @RequestParam Long serieId,
                              @RequestParam(required = false) List<Long> disegnatoriIds,
                              Model model,
                              RedirectAttributes redirect) {
        if (errori.hasErrors()) {
            this.popolaSelectFumetto(model);
            return "admin/fumetti/form";
        }
        try {
            Fumetto salvato = this.fumettoService.inserisci(
                    fumetto, scrittoreId, editoreId, serieId, disegnatoriIds);
            redirect.addFlashAttribute("successo", "Fumetto inserito.");
            return "redirect:/fumetti/" + salvato.getId();
        } catch (VincoloViolatoException e) {
            model.addAttribute("errore", e.getMessage());
            this.popolaSelectFumetto(model);
            return "admin/fumetti/form";
        }
    }

    @PostMapping("/fumetti/{id}/elimina")
    public String eliminaFumetto(@PathVariable Long id, RedirectAttributes redirect) {
        this.fumettoService.elimina(id);
        redirect.addFlashAttribute("successo", "Fumetto eliminato.");
        return "redirect:/fumetti";
    }

    // ================= SERIE E AZIENDE =================

    @GetMapping("/serie/nuova")
    public String formNuovaSerie(Model model) {
        model.addAttribute("serieForm", new Serie());
        model.addAttribute("tipi", TipoSerie.values());
        return "admin/serie/form";
    }

    @PostMapping("/serie/nuova")
    public String creaSerie(@Valid @ModelAttribute("serieForm") Serie serie,
                            BindingResult errori, Model model, RedirectAttributes redirect) {
        if (errori.hasErrors()) {
            model.addAttribute("tipi", TipoSerie.values());
            return "admin/serie/form";
        }
        try {
            Serie salvata = this.serieService.salva(serie);
            redirect.addFlashAttribute("successo", "Serie creata.");
            return "redirect:/serie/" + salvata.getId();
        } catch (VincoloViolatoException e) {
            model.addAttribute("errore", e.getMessage());
            model.addAttribute("tipi", TipoSerie.values());
            return "admin/serie/form";
        }
    }

    @GetMapping("/aziende/nuova")
    public String formNuovaAzienda(Model model) {
        model.addAttribute("azienda", new Azienda());
        return "admin/aziende/form";
    }

    @PostMapping("/aziende/nuova")
    public String creaAzienda(@Valid @ModelAttribute("azienda") Azienda azienda,
                              BindingResult errori, Model model, RedirectAttributes redirect) {
        if (errori.hasErrors()) {
            return "admin/aziende/form";
        }
        try {
            Azienda salvata = this.aziendaService.salva(azienda);
            redirect.addFlashAttribute("successo", "Azienda creata.");
            return "redirect:/aziende/" + salvata.getId();
        } catch (VincoloViolatoException e) {
            model.addAttribute("errore", e.getMessage());
            return "admin/aziende/form";
        }
    }

    // ------------------------------------------------------------------

    private void popolaSelectFigure(Model model) {
        model.addAttribute("aziende", this.aziendaService.findAll());
        model.addAttribute("serie", this.serieService.findAll());
        model.addAttribute("materiali", Materiale.values());
    }

    private void popolaSelectFumetto(Model model) {
        model.addAttribute("scrittori", this.autoreService.findAllScrittori());
        model.addAttribute("disegnatori", this.autoreService.findAllDisegnatori());
        model.addAttribute("editori", this.autoreService.findAllEditori());
        model.addAttribute("serie", this.serieService.findAll());
    }
}
