package br.com.dunnastecnologia.chamados.infrastructure.controller.api;

import br.com.dunnastecnologia.chamados.application.UserCase.AdminUseCases;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.WebControllerSupport;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.form.VincularMoradorUnidadeForm;
import br.com.dunnastecnologia.chamados.infrastructure.exception.BusinessRuleException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/admin/moradores")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class MoradorUnidadeApiController {

    private final AdminUseCases adminUseCases;
    private final WebControllerSupport support;

    public MoradorUnidadeApiController(AdminUseCases adminUseCases, WebControllerSupport support) {
        this.adminUseCases = adminUseCases;
        this.support = support;
    }

    @Operation(summary = "Vincula uma unidade a um morador usando formulario", tags = "05 - Admin Web - Moradores")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Unidade vinculada ao morador com sucesso e redirecionamento para a tela de origem."),
            @ApiResponse(responseCode = "400", description = "Dados informados sao invalidos ou violam regra de negocio."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado."),
            @ApiResponse(responseCode = "404", description = "Morador ou unidade nao encontrado.")
    })
    @PutMapping("/{moradorId}/unidades")
    public String vincularMoradorUnidadePorFormulario(
            Authentication authentication,
            @PathVariable UUID moradorId,
            @ModelAttribute VincularMoradorUnidadeForm vincularMoradorUnidadeForm,
            @RequestParam(required = false, defaultValue = "false") boolean dashboard,
            RedirectAttributes redirectAttributes
    ) {
        if (vincularMoradorUnidadeForm.getUnidadeId() == null) {
            throw new BusinessRuleException("Selecione uma unidade para vincular ao morador.");
        }

        adminUseCases.vincularMoradorUnidade(
                support.authenticatedUser(authentication),
                moradorId,
                vincularMoradorUnidadeForm.getUnidadeId()
        );
        redirectAttributes.addFlashAttribute("successMessage", "Unidade vinculada ao morador.");
        return redirectMorador(dashboard, moradorId, vincularMoradorUnidadeForm.getBlocoId());
    }

    @Operation(summary = "Desvincula uma unidade de um morador", tags = "05 - Admin Web - Moradores")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Unidade desvinculada do morador com sucesso e redirecionamento para a tela de origem."),
            @ApiResponse(responseCode = "400", description = "Dados informados sao invalidos ou violam regra de negocio."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado."),
            @ApiResponse(responseCode = "404", description = "Morador ou unidade nao encontrado.")
    })
    @DeleteMapping("/{moradorId}/unidades/{unidadeId}")
    public String desvincularMoradorUnidade(
            Authentication authentication,
            @PathVariable UUID moradorId,
            @PathVariable UUID unidadeId,
            @RequestParam(required = false) UUID blocoId,
            @RequestParam(required = false, defaultValue = "false") boolean dashboard,
            RedirectAttributes redirectAttributes
    ) {
        adminUseCases.desvincularMoradorUnidade(support.authenticatedUser(authentication), moradorId, unidadeId);
        redirectAttributes.addFlashAttribute("successMessage", "Unidade desvinculada do morador.");
        return redirectMorador(dashboard, moradorId, blocoId);
    }

    private String redirectUsuarioMorador(UUID moradorId, UUID blocoId) {
        if (blocoId == null) {
            return "redirect:/admin/usuarios/" + moradorId;
        }
        return "redirect:/admin/usuarios/" + moradorId + "?blocoId=" + blocoId;
    }

    private String redirectVinculosMorador(UUID moradorId, UUID blocoId) {
        if (blocoId == null) {
            return "redirect:/admin/vinculos-morador?moradorId=" + moradorId;
        }
        return "redirect:/admin/vinculos-morador?moradorId=" + moradorId + "&blocoId=" + blocoId;
    }

    private String redirectMorador(boolean dashboard, UUID moradorId, UUID blocoId) {
        if (dashboard) {
            return redirectVinculosMorador(moradorId, blocoId);
        }
        return redirectUsuarioMorador(moradorId, blocoId);
    }
}
