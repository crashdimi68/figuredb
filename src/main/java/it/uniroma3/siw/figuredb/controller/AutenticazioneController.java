package it.uniroma3.siw.figuredb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import it.uniroma3.siw.figuredb.model.Credentials;
import it.uniroma3.siw.figuredb.model.User;
import it.uniroma3.siw.figuredb.service.CredentialsService;
import it.uniroma3.siw.figuredb.service.exception.VincoloViolatoException;
import jakarta.validation.Valid;

@Controller
public class AutenticazioneController {

    private final CredentialsService credentialsService;

    public AutenticazioneController(CredentialsService credentialsService) {
        this.credentialsService = credentialsService;
    }

    @GetMapping("/login")
    public String mostraLogin() {
        return "login";
    }

    @GetMapping("/register")
    public String mostraRegistrazione(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("credentials", new Credentials());
        return "registrazione";
    }

    @PostMapping("/register")
    public String registra(@Valid @ModelAttribute("user") User user,
                           BindingResult erroriUser,
                           @Valid @ModelAttribute("credentials") Credentials credentials,
                           BindingResult erroriCredentials,
                           Model model) {

        if (erroriUser.hasErrors() || erroriCredentials.hasErrors()) {
            return "registrazione";
        }
        try {
            this.credentialsService.registra(credentials, user);
        } catch (VincoloViolatoException e) {
            model.addAttribute("erroreRegistrazione", e.getMessage());
            return "registrazione";
        }
        return "registrazioneCompletata";
    }
}
