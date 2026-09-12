package it.uniroma3.siw.figuredb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /**
     * Home page. E' una pagina statica di benvenuto: non interroga il
     * database, quindi al controller non serve nessun service.
     */
    @GetMapping({"/", "/index"})
    public String home() {
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
