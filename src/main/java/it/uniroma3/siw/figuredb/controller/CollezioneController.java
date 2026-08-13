package it.uniroma3.siw.figuredb.controller;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.uniroma3.siw.figuredb.model.StatoVoce;
import it.uniroma3.siw.figuredb.service.CollezioneService;
import it.uniroma3.siw.figuredb.service.exception.VincoloViolatoException;

/**
 * CASI D'USO RISERVATI AGLI UTENTI REGISTRATI: collezione personale e wishlist.
 */
@Controller
public class CollezioneController {

    private final CollezioneService collezioneService;

    public CollezioneController(CollezioneService collezioneService) {
        this.collezioneService = collezioneService;
    }

    @GetMapping("/collezione")
    public String miaCollezione(@ModelAttribute("userDetails") UserDetails userDetails,
                                @RequestParam(required = false) StatoVoce stato,
                                Model model) {
        String username = userDetails.getUsername();
        model.addAttribute("voci", stato == null
                ? this.collezioneService.collezioneDi(username)
                : this.collezioneService.collezioneDi(username, stato));
        model.addAttribute("valore", this.collezioneService.valoreCollezione(username));
        model.addAttribute("statoSelezionato", stato);
        model.addAttribute("stati", StatoVoce.values());
        return "collezione/mia";
    }

    @PostMapping("/collezione/aggiungi/{figureId}")
    public String aggiungi(@PathVariable Long figureId,
                           @RequestParam StatoVoce stato,
                           @RequestParam(required = false) Integer quantita,
                           @RequestParam(required = false) String note,
                           @ModelAttribute("userDetails") UserDetails userDetails,
                           RedirectAttributes redirect) {
        try {
            this.collezioneService.aggiungi(figureId, stato, quantita, note, userDetails.getUsername());
            redirect.addFlashAttribute("successo", "Aggiunta alla tua collezione.");
        } catch (VincoloViolatoException e) {
            redirect.addFlashAttribute("errore", e.getMessage());
        }
        return "redirect:/figure/" + figureId;
    }

    @PostMapping("/collezione/{voceId}/aggiorna")
    public String aggiorna(@PathVariable Long voceId,
                           @RequestParam StatoVoce stato,
                           @RequestParam(required = false) Integer quantita,
                           @RequestParam(required = false) String note,
                           @ModelAttribute("userDetails") UserDetails userDetails,
                           RedirectAttributes redirect) {
        this.collezioneService.aggiorna(voceId, stato, quantita, note, userDetails.getUsername());
        redirect.addFlashAttribute("successo", "Voce aggiornata.");
        return "redirect:/collezione";
    }

    @PostMapping("/collezione/{voceId}/rimuovi")
    public String rimuovi(@PathVariable Long voceId,
                          @ModelAttribute("userDetails") UserDetails userDetails,
                          RedirectAttributes redirect) {
        this.collezioneService.rimuovi(voceId, userDetails.getUsername());
        redirect.addFlashAttribute("successo", "Voce rimossa dalla collezione.");
        return "redirect:/collezione";
    }
}
