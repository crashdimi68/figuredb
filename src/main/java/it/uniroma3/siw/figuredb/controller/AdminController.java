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
import it.uniroma3.siw.figuredb.model.Gacha;
import it.uniroma3.siw.figuredb.model.Materiale;
import it.uniroma3.siw.figuredb.model.Serie;
import it.uniroma3.siw.figuredb.model.TipoSerie;
import it.uniroma3.siw.figuredb.service.AnalisiPrestazioniService;
import it.uniroma3.siw.figuredb.service.AutoreService;
import it.uniroma3.siw.figuredb.service.AziendaService;
import it.uniroma3.siw.figuredb.service.FigureService;
import it.uniroma3.siw.figuredb.service.FumettoService;
import it.uniroma3.siw.figuredb.service.GachaService;
import it.uniroma3.siw.figuredb.service.SerieService;
import it.uniroma3.siw.figuredb.service.exception.VincoloViolatoException;
import jakarta.validation.Valid;

/**
 * CASI D'USO RISERVATI ALL'AMMINISTRATORE.
 * Tutti gli URL sono sotto /admin/** e protetti da Spring Security.
 *
 * Per ogni entita' del catalogo (figure, fumetti, serie, aziende, gacha)
 * ci sono tre operazioni: inserimento, modifica e cancellazione. Le form di
 * inserimento e di modifica sono lo stesso template: cambia solo l'URL a cui
 * vengono inviate, passato alla vista nell'attributo "azione".
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final FigureService figureService;
    private final FumettoService fumettoService;
    private final SerieService serieService;
    private final AziendaService aziendaService;
    private final GachaService gachaService;
    private final AutoreService autoreService;
    private final AnalisiPrestazioniService analisiPrestazioniService;

    public AdminController(FigureService figureService,
                           FumettoService fumettoService,
                           SerieService serieService,
                           AziendaService aziendaService,
                           GachaService gachaService,
                           AutoreService autoreService,
                           AnalisiPrestazioniService analisiPrestazioniService) {
        this.figureService = figureService;
        this.fumettoService = fumettoService;
        this.serieService = serieService;
        this.aziendaService = aziendaService;
        this.gachaService = gachaService;
        this.autoreService = autoreService;
        this.analisiPrestazioniService = analisiPrestazioniService;
    }

    @GetMapping
    public String cruscotto() {
        return "admin/cruscotto";
    }

    // ================= ANALISI DELLE PRESTAZIONI =================

    /**
     * Confronto sperimentale fra le tre strategie di accesso ai dati.
     * L'analisi viene rieseguita a ogni caricamento della pagina, quindi i
     * numeri si riferiscono sempre allo stato attuale del database.
     */
    @GetMapping("/prestazioni")
    public String prestazioni(Model model) {
        model.addAttribute("analisi", this.analisiPrestazioniService.eseguiConfronto());
        return "admin/prestazioni";
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
        this.popolaSelectFigure(model, "Nuova figure", "/admin/figure/nuova");
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
            this.popolaSelectFigure(model, "Nuova figure", "/admin/figure/nuova");
            return "admin/figure/form";
        }
        try {
            Figure salvata = this.figureService.inserisciACatalogo(figure, aziendaId, serieId);
            redirect.addFlashAttribute("successo", "Figure inserita a catalogo.");
            return "redirect:/figure/" + salvata.getId();
        } catch (VincoloViolatoException e) {
            model.addAttribute("errore", e.getMessage());
            this.popolaSelectFigure(model, "Nuova figure", "/admin/figure/nuova");
            return "admin/figure/form";
        }
    }

    @GetMapping("/figure/{id}/modifica")
    public String formModificaFigure(@PathVariable Long id, Model model) {
        model.addAttribute("figure", this.figureService.findById(id));
        this.popolaSelectFigure(model, "Modifica figure", "/admin/figure/" + id + "/modifica");
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
        String azione = "/admin/figure/" + id + "/modifica";
        if (errori.hasErrors()) {
            this.popolaSelectFigure(model, "Modifica figure", azione);
            return "admin/figure/form";
        }
        try {
            this.figureService.aggiorna(id, figure, aziendaId, serieId);
            redirect.addFlashAttribute("successo", "Figure aggiornata.");
            return "redirect:/figure/" + id;
        } catch (VincoloViolatoException e) {
            model.addAttribute("errore", e.getMessage());
            this.popolaSelectFigure(model, "Modifica figure", azione);
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
        this.popolaSelectFumetto(model, "Nuovo fumetto", "/admin/fumetti/nuovo");
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
            this.popolaSelectFumetto(model, "Nuovo fumetto", "/admin/fumetti/nuovo");
            return "admin/fumetti/form";
        }
        try {
            Fumetto salvato = this.fumettoService.inserisci(
                    fumetto, scrittoreId, editoreId, serieId, disegnatoriIds);
            redirect.addFlashAttribute("successo", "Fumetto inserito.");
            return "redirect:/fumetti/" + salvato.getId();
        } catch (VincoloViolatoException e) {
            model.addAttribute("errore", e.getMessage());
            this.popolaSelectFumetto(model, "Nuovo fumetto", "/admin/fumetti/nuovo");
            return "admin/fumetti/form";
        }
    }

    @GetMapping("/fumetti/{id}/modifica")
    public String formModificaFumetto(@PathVariable Long id, Model model) {
        Fumetto fumetto = this.fumettoService.findById(id);
        model.addAttribute("fumetto", fumetto);
        model.addAttribute("disegnatoriSelezionati",
                fumetto.getDisegnatori().stream().map(d -> d.getId()).toList());
        this.popolaSelectFumetto(model, "Modifica fumetto", "/admin/fumetti/" + id + "/modifica");
        return "admin/fumetti/form";
    }

    @PostMapping("/fumetti/{id}/modifica")
    public String modificaFumetto(@PathVariable Long id,
                                  @Valid @ModelAttribute("fumetto") Fumetto fumetto,
                                  BindingResult errori,
                                  @RequestParam Long scrittoreId,
                                  @RequestParam Long editoreId,
                                  @RequestParam Long serieId,
                                  @RequestParam(required = false) List<Long> disegnatoriIds,
                                  Model model,
                                  RedirectAttributes redirect) {
        String azione = "/admin/fumetti/" + id + "/modifica";
        if (errori.hasErrors()) {
            this.popolaSelectFumetto(model, "Modifica fumetto", azione);
            return "admin/fumetti/form";
        }
        try {
            this.fumettoService.aggiorna(id, fumetto, scrittoreId, editoreId, serieId, disegnatoriIds);
            redirect.addFlashAttribute("successo", "Fumetto aggiornato.");
            return "redirect:/fumetti/" + id;
        } catch (VincoloViolatoException e) {
            model.addAttribute("errore", e.getMessage());
            this.popolaSelectFumetto(model, "Modifica fumetto", azione);
            return "admin/fumetti/form";
        }
    }

    @PostMapping("/fumetti/{id}/elimina")
    public String eliminaFumetto(@PathVariable Long id, RedirectAttributes redirect) {
        this.fumettoService.elimina(id);
        redirect.addFlashAttribute("successo", "Fumetto eliminato.");
        return "redirect:/fumetti";
    }

    // ================= SERIE =================

    @GetMapping("/serie/nuova")
    public String formNuovaSerie(Model model) {
        model.addAttribute("serieForm", new Serie());
        this.popolaSelectSerie(model, "Nuova serie", "/admin/serie/nuova");
        return "admin/serie/form";
    }

    @PostMapping("/serie/nuova")
    public String creaSerie(@Valid @ModelAttribute("serieForm") Serie serie,
                            BindingResult errori, Model model, RedirectAttributes redirect) {
        if (errori.hasErrors()) {
            this.popolaSelectSerie(model, "Nuova serie", "/admin/serie/nuova");
            return "admin/serie/form";
        }
        try {
            Serie salvata = this.serieService.salva(serie);
            redirect.addFlashAttribute("successo", "Serie creata.");
            return "redirect:/serie/" + salvata.getId();
        } catch (VincoloViolatoException e) {
            model.addAttribute("errore", e.getMessage());
            this.popolaSelectSerie(model, "Nuova serie", "/admin/serie/nuova");
            return "admin/serie/form";
        }
    }

    @GetMapping("/serie/{id}/modifica")
    public String formModificaSerie(@PathVariable Long id, Model model) {
        model.addAttribute("serieForm", this.serieService.findById(id));
        this.popolaSelectSerie(model, "Modifica serie", "/admin/serie/" + id + "/modifica");
        return "admin/serie/form";
    }

    @PostMapping("/serie/{id}/modifica")
    public String modificaSerie(@PathVariable Long id,
                                @Valid @ModelAttribute("serieForm") Serie serie,
                                BindingResult errori, Model model, RedirectAttributes redirect) {
        String azione = "/admin/serie/" + id + "/modifica";
        if (errori.hasErrors()) {
            this.popolaSelectSerie(model, "Modifica serie", azione);
            return "admin/serie/form";
        }
        try {
            this.serieService.aggiorna(id, serie);
            redirect.addFlashAttribute("successo", "Serie aggiornata.");
            return "redirect:/serie/" + id;
        } catch (VincoloViolatoException e) {
            model.addAttribute("errore", e.getMessage());
            this.popolaSelectSerie(model, "Modifica serie", azione);
            return "admin/serie/form";
        }
    }

    /**
     * La cancellazione puo' fallire: una serie con fumetti, figure o gacha
     * collegati non si puo' eliminare. Il messaggio torna alla pagina di
     * dettaglio come avviso, senza pagina di errore.
     */
    @PostMapping("/serie/{id}/elimina")
    public String eliminaSerie(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            this.serieService.elimina(id);
            redirect.addFlashAttribute("successo", "Serie eliminata.");
            return "redirect:/serie";
        } catch (VincoloViolatoException e) {
            redirect.addFlashAttribute("errore", e.getMessage());
            return "redirect:/serie/" + id;
        }
    }

    // ================= AZIENDE =================

    @GetMapping("/aziende/nuova")
    public String formNuovaAzienda(Model model) {
        model.addAttribute("azienda", new Azienda());
        this.intestazioneForm(model, "Nuova azienda", "/admin/aziende/nuova");
        return "admin/aziende/form";
    }

    @PostMapping("/aziende/nuova")
    public String creaAzienda(@Valid @ModelAttribute("azienda") Azienda azienda,
                              BindingResult errori, Model model, RedirectAttributes redirect) {
        if (errori.hasErrors()) {
            this.intestazioneForm(model, "Nuova azienda", "/admin/aziende/nuova");
            return "admin/aziende/form";
        }
        try {
            Azienda salvata = this.aziendaService.salva(azienda);
            redirect.addFlashAttribute("successo", "Azienda creata.");
            return "redirect:/aziende/" + salvata.getId();
        } catch (VincoloViolatoException e) {
            model.addAttribute("errore", e.getMessage());
            this.intestazioneForm(model, "Nuova azienda", "/admin/aziende/nuova");
            return "admin/aziende/form";
        }
    }

    @GetMapping("/aziende/{id}/modifica")
    public String formModificaAzienda(@PathVariable Long id, Model model) {
        model.addAttribute("azienda", this.aziendaService.findById(id));
        this.intestazioneForm(model, "Modifica azienda", "/admin/aziende/" + id + "/modifica");
        return "admin/aziende/form";
    }

    @PostMapping("/aziende/{id}/modifica")
    public String modificaAzienda(@PathVariable Long id,
                                  @Valid @ModelAttribute("azienda") Azienda azienda,
                                  BindingResult errori, Model model, RedirectAttributes redirect) {
        String azione = "/admin/aziende/" + id + "/modifica";
        if (errori.hasErrors()) {
            this.intestazioneForm(model, "Modifica azienda", azione);
            return "admin/aziende/form";
        }
        try {
            this.aziendaService.aggiorna(id, azienda);
            redirect.addFlashAttribute("successo", "Azienda aggiornata.");
            return "redirect:/aziende/" + id;
        } catch (VincoloViolatoException e) {
            model.addAttribute("errore", e.getMessage());
            this.intestazioneForm(model, "Modifica azienda", azione);
            return "admin/aziende/form";
        }
    }

    @PostMapping("/aziende/{id}/elimina")
    public String eliminaAzienda(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            this.aziendaService.elimina(id);
            redirect.addFlashAttribute("successo", "Azienda eliminata.");
            return "redirect:/aziende";
        } catch (VincoloViolatoException e) {
            redirect.addFlashAttribute("errore", e.getMessage());
            return "redirect:/aziende/" + id;
        }
    }

    // ================= GACHA =================

    @GetMapping("/gacha/nuovo")
    public String formNuovoGacha(Model model) {
        model.addAttribute("gachaForm", new Gacha());
        this.popolaSelectGacha(model, "Nuova serie gacha", "/admin/gacha/nuovo");
        return "admin/gacha/form";
    }

    @PostMapping("/gacha/nuovo")
    public String creaGacha(@Valid @ModelAttribute("gachaForm") Gacha gacha,
                            BindingResult errori,
                            @RequestParam Long aziendaId,
                            @RequestParam Long serieId,
                            Model model,
                            RedirectAttributes redirect) {
        if (errori.hasErrors()) {
            this.popolaSelectGacha(model, "Nuova serie gacha", "/admin/gacha/nuovo");
            return "admin/gacha/form";
        }
        try {
            Gacha salvato = this.gachaService.salva(gacha, aziendaId, serieId);
            redirect.addFlashAttribute("successo", "Serie gacha creata.");
            return "redirect:/gacha/" + salvato.getId();
        } catch (VincoloViolatoException e) {
            model.addAttribute("errore", e.getMessage());
            this.popolaSelectGacha(model, "Nuova serie gacha", "/admin/gacha/nuovo");
            return "admin/gacha/form";
        }
    }

    @GetMapping("/gacha/{id}/modifica")
    public String formModificaGacha(@PathVariable Long id, Model model) {
        model.addAttribute("gachaForm", this.gachaService.findById(id));
        this.popolaSelectGacha(model, "Modifica serie gacha", "/admin/gacha/" + id + "/modifica");
        return "admin/gacha/form";
    }

    @PostMapping("/gacha/{id}/modifica")
    public String modificaGacha(@PathVariable Long id,
                                @Valid @ModelAttribute("gachaForm") Gacha gacha,
                                BindingResult errori,
                                @RequestParam Long aziendaId,
                                @RequestParam Long serieId,
                                Model model,
                                RedirectAttributes redirect) {
        String azione = "/admin/gacha/" + id + "/modifica";
        if (errori.hasErrors()) {
            this.popolaSelectGacha(model, "Modifica serie gacha", azione);
            return "admin/gacha/form";
        }
        try {
            this.gachaService.aggiorna(id, gacha, aziendaId, serieId);
            redirect.addFlashAttribute("successo", "Serie gacha aggiornata.");
            return "redirect:/gacha/" + id;
        } catch (VincoloViolatoException e) {
            model.addAttribute("errore", e.getMessage());
            this.popolaSelectGacha(model, "Modifica serie gacha", azione);
            return "admin/gacha/form";
        }
    }

    @PostMapping("/gacha/{id}/elimina")
    public String eliminaGacha(@PathVariable Long id, RedirectAttributes redirect) {
        this.gachaService.elimina(id);
        redirect.addFlashAttribute("successo", "Serie gacha eliminata.");
        return "redirect:/gacha";
    }

    // ------------------------------------------------------------------
    // Dati comuni alle form (titolo, URL di invio, tendine)
    // ------------------------------------------------------------------

    private void intestazioneForm(Model model, String titolo, String azione) {
        model.addAttribute("titoloForm", titolo);
        model.addAttribute("azione", azione);
    }

    private void popolaSelectFigure(Model model, String titolo, String azione) {
        this.intestazioneForm(model, titolo, azione);
        model.addAttribute("aziende", this.aziendaService.findAll());
        model.addAttribute("serie", this.serieService.findAll());
        model.addAttribute("materiali", Materiale.values());
    }

    private void popolaSelectFumetto(Model model, String titolo, String azione) {
        this.intestazioneForm(model, titolo, azione);
        model.addAttribute("scrittori", this.autoreService.findAllScrittori());
        model.addAttribute("disegnatori", this.autoreService.findAllDisegnatori());
        model.addAttribute("editori", this.autoreService.findAllEditori());
        model.addAttribute("serie", this.serieService.findAll());
    }

    private void popolaSelectSerie(Model model, String titolo, String azione) {
        this.intestazioneForm(model, titolo, azione);
        model.addAttribute("tipi", TipoSerie.values());
    }

    private void popolaSelectGacha(Model model, String titolo, String azione) {
        this.intestazioneForm(model, titolo, azione);
        model.addAttribute("aziende", this.aziendaService.findAll());
        model.addAttribute("serie", this.serieService.findAll());
    }
}
