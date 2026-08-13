package it.uniroma3.siw.figuredb.controller;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.uniroma3.siw.figuredb.model.Credentials;
import it.uniroma3.siw.figuredb.model.Recensione;
import it.uniroma3.siw.figuredb.service.CollezioneService;
import it.uniroma3.siw.figuredb.service.FigureService;
import it.uniroma3.siw.figuredb.service.RecensioneService;
import it.uniroma3.siw.figuredb.service.exception.VincoloViolatoException;
import jakarta.validation.Valid;

/**
 * CASI D'USO RISERVATI AGLI UTENTI REGISTRATI: recensioni.
 * Il controller si limita a validare l'input e a delegare al Service Layer.
 */
@Controller
public class RecensioneController {

    private final RecensioneService recensioneService;
    private final FigureService figureService;
    private final CollezioneService collezioneService;

    public RecensioneController(RecensioneService recensioneService,
                                FigureService figureService,
                                CollezioneService collezioneService) {
        this.recensioneService = recensioneService;
        this.figureService = figureService;
        this.collezioneService = collezioneService;
    }

    /** Inserimento di una recensione su una figure. */
    @PostMapping("/figure/{figureId}/recensioni")
    public String inserisci(@PathVariable Long figureId,
                            @Valid @ModelAttribute("nuovaRecensione") Recensione recensione,
                            BindingResult errori,
                            @ModelAttribute("userDetails") UserDetails userDetails,
                            Model model,
                            RedirectAttributes redirect) {

        if (errori.hasErrors()) {
            model.addAttribute("figure", this.figureService.findById(figureId));
            model.addAttribute("recensioni", this.recensioneService.findByFigure(figureId));
            model.addAttribute("mediaVoti", this.recensioneService.mediaVoti(figureId));
            model.addAttribute("haGiaRecensito", false);
            model.addAttribute("inCollezione",
                    this.collezioneService.isInCollezione(userDetails.getUsername(), figureId));
            return "figure/dettaglio";
        }
        try {
            this.recensioneService.inserisci(recensione, figureId, userDetails.getUsername());
            redirect.addFlashAttribute("successo", "Recensione pubblicata.");
        } catch (VincoloViolatoException e) {
            redirect.addFlashAttribute("errore", e.getMessage());
        }
        return "redirect:/figure/" + figureId;
    }

    /** Form di modifica di una propria recensione. */
    @GetMapping("/recensioni/{id}/modifica")
    public String mostraModifica(@PathVariable Long id, Model model) {
        model.addAttribute("recensione", this.recensioneService.findById(id));
        return "recensioni/modifica";
    }

    @PostMapping("/recensioni/{id}/modifica")
    public String modifica(@PathVariable Long id,
                           @Valid @ModelAttribute("recensione") Recensione recensione,
                           BindingResult errori,
                           @ModelAttribute("userDetails") UserDetails userDetails,
                           RedirectAttributes redirect) {

        if (errori.hasErrors()) {
            return "recensioni/modifica";
        }
        Recensione aggiornata = this.recensioneService.aggiorna(id, recensione, userDetails.getUsername());
        redirect.addFlashAttribute("successo", "Recensione aggiornata.");
        return "redirect:/figure/" + aggiornata.getFigure().getId();
    }

    /** Cancellazione di una propria recensione (o di qualsiasi recensione se ADMIN). */
    @PostMapping("/recensioni/{id}/elimina")
    public String elimina(@PathVariable Long id,
                          @ModelAttribute("userDetails") UserDetails userDetails,
                          @ModelAttribute("credenziali") Credentials credenziali,
                          RedirectAttributes redirect) {

        boolean isAdmin = credenziali != null && credenziali.isAdmin();
        Long figureId = this.recensioneService.elimina(id, userDetails.getUsername(), isAdmin);
        redirect.addFlashAttribute("successo", "Recensione eliminata.");
        return "redirect:/figure/" + figureId;
    }

    /** Elenco delle proprie recensioni. */
    @GetMapping("/le-mie-recensioni")
    public String mieRecensioni(@ModelAttribute("credenziali") Credentials credenziali, Model model) {
        model.addAttribute("recensioni",
                this.recensioneService.findByAutore(credenziali.getUser().getId()));
        return "recensioni/mie";
    }
}
