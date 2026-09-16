package br.com.dunnastecnologia.chamados.infrastructure.controller.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthWebController {

    @Operation(summary = "Exibe a tela de login", tags = "01 - Publico Web - Paginas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagina de login renderizada com sucesso.")
    })
    @GetMapping({"/login", "/login/"})
    public String login(Model model) {
        model.addAttribute("pageTitle", "Login");
        return "auth/login";
    }
}
