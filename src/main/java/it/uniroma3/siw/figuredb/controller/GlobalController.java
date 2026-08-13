package it.uniroma3.siw.figuredb.controller;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;

import it.uniroma3.siw.figuredb.model.Credentials;
import it.uniroma3.siw.figuredb.service.CredentialsService;

/**
 * Rende disponibili a tutti i template le informazioni sull'utente loggato
 * e normalizza gli spazi nei campi testuali inviati dalle form.
 */
@ControllerAdvice(basePackages = "it.uniroma3.siw.figuredb.controller")
public class GlobalController {

    private final CredentialsService credentialsService;

    public GlobalController(CredentialsService credentialsService) {
        this.credentialsService = credentialsService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @ModelAttribute("userDetails")
    public UserDetails getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        return (principal instanceof UserDetails userDetails) ? userDetails : null;
    }

    @ModelAttribute("credenziali")
    public Credentials getCredenziali() {
        UserDetails userDetails = this.getUserDetails();
        if (userDetails == null) {
            return null;
        }
        try {
            return this.credentialsService.findByUsername(userDetails.getUsername());
        } catch (RuntimeException e) {
            return null;
        }
    }
}
