package br.com.dunnastecnologia.chamados.infrastructure.controller.api;

import br.com.dunnastecnologia.chamados.application.UserCase.AdminAreaComumUseCases;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.WebControllerSupport;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.form.AreaComumForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/admin/areas-comuns")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AreaComumApiController {

    private final AdminAreaComumUseCases adminAreaComumUseCases;
    private final WebControllerSupport support;

    public AreaComumApiController(AdminAreaComumUseCases adminAreaComumUseCases, WebControllerSupport support) {
        this.adminAreaComumUseCases = adminAreaComumUseCases;
        this.support = support;
    }

    @Operation(summary = "Cadastra uma nova area comum", tags = "25 - Admin Web - Areas Comuns")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Area comum cadastrada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Nome invalido ou ausente."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado.")
    })
    @PostMapping
    public String cadastrarAreaComum(
            Authentication authentication,
            @ModelAttribute AreaComumForm form,
            RedirectAttributes redirectAttributes
    ){
        adminAreaComumUseCases.cadastrarAreaComum(support.authenticatedUser(authentication), form.getNome());
        redirectAttributes.addFlashAttribute("successMessage", "Area comum cadastrada com sucesso.");

        return "redirect:/admin/areas-comuns";
    }

    @Operation(summary = "Atualiza uma area comum", tags = "25 - Admin Web - Areas Comnus")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Area comum atualizada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Nome invalido ou ausente."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado."),
            @ApiResponse(responseCode = "404", description = "Area comum nao encontrada.")
    })
    @PatchMapping("/{areaComumId}")
    public String atualizarAreaComum(
            Authentication authentication,
            @PathVariable UUID areaComumId,
            @ModelAttribute AreaComumForm form,
            RedirectAttributes redirectAttributes
    ){
        adminAreaComumUseCases.atualizarAreaComum(support.authenticatedUser(authentication), areaComumId, form.getNome());
        redirectAttributes.addFlashAttribute("successMessage", "Área comum atualizada com sucesso.");

        return "redirect:/admin/areas-comuns/" + areaComumId;
    }

    @Operation(summary = "Desativa uma area comum, mas não apaga as reservas existentes", tags = "25 - Admin Web - Areas Comuns")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Area comum desativada com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado." ),
            @ApiResponse(responseCode = "404", description = "Area comum não encontrada.")
    })
    @DeleteMapping("/{areaComumId}")
    public String desativarAreaComum(
            Authentication authentication,
            @PathVariable UUID areaComumId,
            RedirectAttributes redirectAttributes
    ){
        adminAreaComumUseCases.desativarAreaComum(support.authenticatedUser(authentication), areaComumId);
        redirectAttributes.addFlashAttribute("successMessage", "Area comum desativada com sucesso.");
        return "redirect:/admin/areas-comuns";
    }

    @Operation(summary = "Reativa uma area comum", tags = "25 - Admin Web - Areas Comuns")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Area comum reativada com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado."),
            @ApiResponse(responseCode = "404", description = "Area comum não encontrada.")
    })
    @PatchMapping("/{areaComumId}/reativacao")
    public String reativarAreaComum(
            Authentication authentication,
            @PathVariable UUID areaComumId,
            RedirectAttributes redirectAttributes
    ){
        adminAreaComumUseCases.reativarAreaComum(support.authenticatedUser(authentication), areaComumId);
        redirectAttributes.addFlashAttribute("successMessage", "Area comum reativada com sucesso.");

        return "redirect:/admin/areas-comuns";
    }
}
