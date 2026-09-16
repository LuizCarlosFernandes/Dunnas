package br.com.dunnastecnologia.chamados.infrastructure.controller.api;

import br.com.dunnastecnologia.chamados.application.UserCase.AdminUseCases;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.WebControllerSupport;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.form.BlocoForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/blocos")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class BlocoApiController {

    private final AdminUseCases adminUseCases;
    private final WebControllerSupport support;

    public BlocoApiController(AdminUseCases adminUseCases, WebControllerSupport support) {
        this.adminUseCases = adminUseCases;
        this.support = support;
    }

    @Operation(summary = "Cadastra um novo bloco", tags = "03 - Admin Web - Blocos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Bloco cadastrado com sucesso e redirecionamento para a listagem."),
            @ApiResponse(responseCode = "400", description = "Dados informados sao invalidos ou violam regra de negocio."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado.")
    })
    @PostMapping
    public String cadastrarBloco(
            Authentication authentication,
            @ModelAttribute BlocoForm blocoForm,
            RedirectAttributes redirectAttributes
    ) {
        adminUseCases.cadastrarBloco(
                support.authenticatedUser(authentication),
                blocoForm.getIdentificacao(),
                defaultInteger(blocoForm.getQuantidadeAndares()),
                defaultInteger(blocoForm.getApartamentosPorAndar())
        );
        redirectAttributes.addFlashAttribute("successMessage", "Bloco cadastrado com sucesso.");
        return "redirect:/admin/blocos";
    }

    private int defaultInteger(Integer value) {
        return value == null ? 0 : value;
    }
}
