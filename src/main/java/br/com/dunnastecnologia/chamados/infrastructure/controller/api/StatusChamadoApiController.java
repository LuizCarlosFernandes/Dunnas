package br.com.dunnastecnologia.chamados.infrastructure.controller.api;

import br.com.dunnastecnologia.chamados.application.UserCase.AdminUseCases;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.WebControllerSupport;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.form.StatusChamadoForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/admin/status-chamado")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class StatusChamadoApiController {

    private final AdminUseCases adminUseCases;
    private final WebControllerSupport support;

    public StatusChamadoApiController(AdminUseCases adminUseCases, WebControllerSupport support) {
        this.adminUseCases = adminUseCases;
        this.support = support;
    }

    @Operation(summary = "Cadastra um novo status de chamado", tags = "08 - Admin Web - Status de Chamado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Status de chamado cadastrado com sucesso e redirecionamento para a listagem."),
            @ApiResponse(responseCode = "400", description = "Dados informados sao invalidos ou violam regra de negocio."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado.")
    })
    @PostMapping
    public String cadastrarStatusChamado(
            Authentication authentication,
            @ModelAttribute StatusChamadoForm statusChamadoForm,
            RedirectAttributes redirectAttributes
    ) {
        adminUseCases.cadastrarStatus(
                support.authenticatedUser(authentication),
                statusChamadoForm.getNome()
        );
        redirectAttributes.addFlashAttribute("successMessage", "Status cadastrado com sucesso.");
        return "redirect:/admin/status-chamado";
    }

    @Operation(summary = "Atualiza um status de chamado", tags = "08 - Admin Web - Status de Chamado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Status de chamado atualizado com sucesso e redirecionamento para o detalhe."),
            @ApiResponse(responseCode = "400", description = "Dados informados sao invalidos ou violam regra de negocio."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado."),
            @ApiResponse(responseCode = "404", description = "Status de chamado nao encontrado.")
    })
    @PatchMapping("/{statusId}")
    public String atualizarStatusChamado(
            Authentication authentication,
            @PathVariable UUID statusId,
            @ModelAttribute StatusChamadoForm statusChamadoForm,
            RedirectAttributes redirectAttributes
    ) {
        adminUseCases.atualizarStatus(
                support.authenticatedUser(authentication),
                statusId,
                statusChamadoForm.getNome()
        );
        redirectAttributes.addFlashAttribute("successMessage", "Status atualizado com sucesso.");
        return "redirect:/admin/status-chamado?statusId=" + statusId;
    }

    @Operation(summary = "Define o status inicial padrao dos chamados", tags = "08 - Admin Web - Status de Chamado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Status inicial padrao definido com sucesso e redirecionamento para a listagem."),
            @ApiResponse(responseCode = "400", description = "Dados informados sao invalidos ou violam regra de negocio."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado."),
            @ApiResponse(responseCode = "404", description = "Status de chamado nao encontrado.")
    })
    @PatchMapping("/{statusId}/inicial-padrao")
    public String definirStatusInicialPadrao(
            Authentication authentication,
            @PathVariable UUID statusId,
            RedirectAttributes redirectAttributes
    ) {
        adminUseCases.definirStatusInicialPadrao(support.authenticatedUser(authentication), statusId);
        redirectAttributes.addFlashAttribute("successMessage", "Status inicial padrao atualizado.");
        return "redirect:/admin/status-chamado";
    }
}
