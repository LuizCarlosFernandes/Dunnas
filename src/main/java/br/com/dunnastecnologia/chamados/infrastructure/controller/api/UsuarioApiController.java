package br.com.dunnastecnologia.chamados.infrastructure.controller.api;

import br.com.dunnastecnologia.chamados.application.UserCase.AdminUseCases;
import br.com.dunnastecnologia.chamados.domain.model.Administrador;
import br.com.dunnastecnologia.chamados.domain.model.Colaborador;
import br.com.dunnastecnologia.chamados.domain.model.Morador;
import br.com.dunnastecnologia.chamados.domain.model.Usuario;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.WebControllerSupport;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.form.UsuarioForm;
import br.com.dunnastecnologia.chamados.infrastructure.exception.BusinessRuleException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/admin/usuarios")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class UsuarioApiController {

    private final AdminUseCases adminUseCases;
    private final WebControllerSupport support;

    public UsuarioApiController(AdminUseCases adminUseCases, WebControllerSupport support) {
        this.adminUseCases = adminUseCases;
        this.support = support;
    }

    @Operation(summary = "Cadastra um novo usuario", tags = "04 - Admin Web - Usuarios")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Usuario cadastrado com sucesso e redirecionamento para a listagem."),
            @ApiResponse(responseCode = "400", description = "Dados informados sao invalidos ou violam regra de negocio."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado.")
    })
    @PostMapping
    public String cadastrarUsuario(
            Authentication authentication,
            @ModelAttribute UsuarioForm usuarioForm,
            RedirectAttributes redirectAttributes
    ) {
        adminUseCases.cadastrarUsuario(
                support.authenticatedUser(authentication),
                toUsuario(usuarioForm.getTipo(), usuarioForm)
        );
        redirectAttributes.addFlashAttribute("successMessage", "Usuario cadastrado com sucesso.");
        return "redirect:/admin/usuarios";
    }

    @Operation(summary = "Atualiza os dados de um usuario", tags = "04 - Admin Web - Usuarios")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Usuario atualizado com sucesso e redirecionamento para o detalhe."),
            @ApiResponse(responseCode = "400", description = "Dados informados sao invalidos ou violam regra de negocio."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado."),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado.")
    })
    @PatchMapping("/{usuarioId}")
    public String atualizarUsuario(
            Authentication authentication,
            @PathVariable UUID usuarioId,
            @ModelAttribute UsuarioForm usuarioForm,
            RedirectAttributes redirectAttributes
    ) {
        Usuario existente = adminUseCases.buscarUsuarioPorId(usuarioId);
        adminUseCases.atualizarUsuario(
                support.authenticatedUser(authentication),
                usuarioId,
                toUsuario(resolveTipoUsuario(existente), usuarioForm)
        );
        redirectAttributes.addFlashAttribute("successMessage", "Usuario atualizado com sucesso.");
        return "redirect:/admin/usuarios/" + usuarioId;
    }

    @Operation(summary = "Realiza a remocao logica de um usuario", tags = "04 - Admin Web - Usuarios")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Usuario removido com sucesso e redirecionamento para a listagem."),
            @ApiResponse(responseCode = "400", description = "Dados informados sao invalidos ou violam regra de negocio."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado."),
            @ApiResponse(responseCode = "404", description = "Usuario nao encontrado.")
    })
    @DeleteMapping("/{usuarioId}")
    public String removerUsuario(
            Authentication authentication,
            @PathVariable UUID usuarioId,
            RedirectAttributes redirectAttributes
    ) {
        adminUseCases.removerUsuario(support.authenticatedUser(authentication), usuarioId);
        redirectAttributes.addFlashAttribute("successMessage", "Usuario removido com sucesso.");
        return "redirect:/admin/usuarios";
    }

    private Usuario toUsuario(String tipo, UsuarioForm usuarioForm) {
        Usuario usuario = switch (tipo) {
            case "ADMINISTRADOR" -> new Administrador();
            case "COLABORADOR" -> new Colaborador();
            case "MORADOR" -> new Morador();
            default -> throw new BusinessRuleException("Tipo de usuario invalido");
        };
        usuario.setNome(usuarioForm.getNome());
        usuario.setEmail(usuarioForm.getEmail());
        usuario.setSenha(usuarioForm.getSenha());
        return usuario;
    }

    private String resolveTipoUsuario(Usuario usuario) {
        return switch (usuario.getRole()) {
            case "ROLE_ADMINISTRADOR" -> "ADMINISTRADOR";
            case "ROLE_COLABORADOR" -> "COLABORADOR";
            case "ROLE_MORADOR" -> "MORADOR";
            default -> throw new BusinessRuleException("Tipo de usuario nao suportado");
        };
    }
}
