package it.uniroma3.siw.figuredb.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Corpo della richiesta POST/PUT di una recensione inviata da React. */
public record NuovaRecensioneRequest(

        @NotBlank(message = "Il titolo e' obbligatorio")
        String titolo,

        @NotBlank(message = "Il testo e' obbligatorio")
        @Size(min = 10, max = 2000, message = "Il testo deve essere tra 10 e 2000 caratteri")
        String testo,

        @NotNull(message = "Il voto e' obbligatorio")
        @Min(value = 1, message = "Il voto deve essere tra 1 e 5")
        @Max(value = 5, message = "Il voto deve essere tra 1 e 5")
        Integer voto) {
}
