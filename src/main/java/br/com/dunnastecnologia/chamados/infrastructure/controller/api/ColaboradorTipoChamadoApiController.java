package br.com.dunnastecnologia.chamados.infrastructure.controller.api;

import br.com.dunnastecnologia.chamados.application.UserCase.AdminUseCases;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.WebControllerSupport;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.form.VincularColaboradorTipoChamadoForm;
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
@RequestMapping("/admin/colaboradores")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class ColaboradorTipoChamadoApiController {

    private final AdminUseCases adminUseCases;
    private final WebControllerSupport support;

    public ColaboradorTipoChamadoApiController(AdminUseCases adminUseCases, WebControllerSupport support) {
        this.adminUseCases = adminUseCases;
        this.support = support;
    }

    @Operation(summary = "Vincula um tipo de chamado ao escopo do colaborador", tags = "06 - Admin Web - Colaboradores")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Tipo de chamado vinculado ao colaborador com sucesso e redirecionamento para a tela de origem."),
            @ApiResponse(responseCode = "400", description = "Dados informados sao invalidos ou violam regra de negocio."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado."),
            @ApiResponse(responseCode = "404", description = "Colaborador ou tipo de chamado nao encontrado.")
    })
    @PutMapping("/{colaboradorId}/tipos-chamado")
    public String vincularColaboradorTipoChamado(
            Authentication authentication,
            @PathVariable UUID colaboradorId,
            @ModelAttribute VincularColaboradorTipoChamadoForm vincularColaboradorTipoChamadoForm,
            @RequestParam(required = false, defaultValue = "false") boolean dashboard,
            RedirectAttributes redirectAttributes
    ) {
        if (vincularColaboradorTipoChamadoForm.getTipoChamadoId() == null) {
            throw new BusinessRuleException("Selecione um tipo de chamado para vincular ao colaborador.");
        }

        adminUseCases.vincularColaboradorTipoChamado(
                support.authenticatedUser(authentication),
                colaboradorId,
                vincularColaboradorTipoChamadoForm.getTipoChamadoId()
        );
        redirectAttributes.addFlashAttribute("successMessage", "Tipo de chamado vinculado ao colaborador.");
        return redirectColaborador(dashboard, colaboradorId);
    }

    @Operation(summary = "Remove um tipo de chamado do escopo do colaborador", tags = "06 - Admin Web - Colaboradores")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Tipo de chamado removido do escopo do colaborador com sucesso e redirecionamento para a tela de origem."),
            @ApiResponse(responseCode = "400", description = "Dados informados sao invalidos ou violam regra de negocio."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado."),
            @ApiResponse(responseCode = "404", description = "Colaborador ou tipo de chamado nao encontrado.")
    })
    @DeleteMapping("/{colaboradorId}/tipos-chamado/{tipoChamadoId}")
    public String desvincularColaboradorTipoChamado(
            Authentication authentication,
            @PathVariable UUID colaboradorId,
            @PathVariable UUID tipoChamadoId,
            @RequestParam(required = false, defaultValue = "false") boolean dashboard,
            RedirectAttributes redirectAttributes
    ) {
        adminUseCases.desvincularColaboradorTipoChamado(
                support.authenticatedUser(authentication),
                colaboradorId,
                tipoChamadoId
        );
        redirectAttributes.addFlashAttribute("successMessage", "Tipo de chamado desvinculado do colaborador.");
        return redirectColaborador(dashboard, colaboradorId);
    }

    private String redirectEscopoColaborador(UUID colaboradorId) {
        return "redirect:/admin/escopo-colaborador?colaboradorId=" + colaboradorId;
    }

    private String redirectColaborador(boolean dashboard, UUID colaboradorId) {
        if (dashboard) {
            return redirectEscopoColaborador(colaboradorId);
        }
        return "redirect:/admin/usuarios/" + colaboradorId;
    }
}
