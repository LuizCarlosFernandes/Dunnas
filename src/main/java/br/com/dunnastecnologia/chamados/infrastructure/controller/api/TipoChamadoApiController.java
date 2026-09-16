package br.com.dunnastecnologia.chamados.infrastructure.controller.api;

import br.com.dunnastecnologia.chamados.application.UserCase.AdminUseCases;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.WebControllerSupport;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.form.TipoChamadoForm;
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
@RequestMapping("/admin/tipos-chamado")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class TipoChamadoApiController {

    private final AdminUseCases adminUseCases;
    private final WebControllerSupport support;

    public TipoChamadoApiController(AdminUseCases adminUseCases, WebControllerSupport support) {
        this.adminUseCases = adminUseCases;
        this.support = support;
    }

    @Operation(summary = "Cadastra um novo tipo de chamado", tags = "07 - Admin Web - Tipos de Chamado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Tipo de chamado cadastrado com sucesso e redirecionamento para a listagem."),
            @ApiResponse(responseCode = "400", description = "Dados informados sao invalidos ou violam regra de negocio."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado.")
    })
    @PostMapping
    public String cadastrarTipoChamado(
            Authentication authentication,
            @ModelAttribute TipoChamadoForm tipoChamadoForm,
            RedirectAttributes redirectAttributes
    ) {
        adminUseCases.cadastrarTipoChamado(
                support.authenticatedUser(authentication),
                tipoChamadoForm.getTitulo(),
                defaultInteger(tipoChamadoForm.getPrazoHoras())
        );
        redirectAttributes.addFlashAttribute("successMessage", "Tipo de chamado cadastrado com sucesso.");
        return "redirect:/admin/tipos-chamado";
    }

    @Operation(summary = "Atualiza um tipo de chamado", tags = "07 - Admin Web - Tipos de Chamado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Tipo de chamado atualizado com sucesso e redirecionamento para o detalhe."),
            @ApiResponse(responseCode = "400", description = "Dados informados sao invalidos ou violam regra de negocio."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado."),
            @ApiResponse(responseCode = "404", description = "Tipo de chamado nao encontrado.")
    })
    @PatchMapping("/{tipoId}")
    public String atualizarTipoChamado(
            Authentication authentication,
            @PathVariable UUID tipoId,
            @ModelAttribute TipoChamadoForm tipoChamadoForm,
            RedirectAttributes redirectAttributes
    ) {
        adminUseCases.atualizarTipoChamado(
                support.authenticatedUser(authentication),
                tipoId,
                tipoChamadoForm.getTitulo(),
                defaultInteger(tipoChamadoForm.getPrazoHoras())
        );
        redirectAttributes.addFlashAttribute("successMessage", "Tipo de chamado atualizado com sucesso.");
        return "redirect:/admin/tipos-chamado?tipoId=" + tipoId;
    }

    private int defaultInteger(Integer value) {
        return value == null ? 0 : value;
    }
}
