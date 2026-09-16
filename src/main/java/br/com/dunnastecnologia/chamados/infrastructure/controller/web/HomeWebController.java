package br.com.dunnastecnologia.chamados.infrastructure.controller.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeWebController {

    private final WebControllerSupport support;

    public HomeWebController(WebControllerSupport support) {
        this.support = support;
    }

    @Operation(summary = "Redireciona o usuario para login ou para o painel do seu perfil", tags = "01 - Publico Web - Paginas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redirecionamento para a tela de login ou para o painel correspondente ao perfil autenticado.")
    })
    @GetMapping
    public String home(Authentication authentication) {
        if (!support.isAuthenticated(authentication)) {
            return "redirect:/login";
        }

        return "redirect:" + support.homePathForRole(support.authenticatedUser(authentication).role());
    }
}
