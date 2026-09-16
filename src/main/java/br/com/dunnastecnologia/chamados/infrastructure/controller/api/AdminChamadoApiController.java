package br.com.dunnastecnologia.chamados.infrastructure.controller.api;

import br.com.dunnastecnologia.chamados.application.UserCase.AdminUseCases;
import br.com.dunnastecnologia.chamados.application.UserCase.AnexoChamadoUseCases;
import br.com.dunnastecnologia.chamados.application.UserCase.AnexoComentarioUseCases;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.WebControllerSupport;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.form.AtualizarStatusForm;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.form.ComentarioForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/admin/chamados")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminChamadoApiController {

    private final AdminUseCases adminUseCases;
    private final AnexoChamadoUseCases anexoChamadoUseCases;
    private final AnexoComentarioUseCases anexoComentarioUseCases;
    private final WebControllerSupport support;

    public AdminChamadoApiController(
            AdminUseCases adminUseCases,
            AnexoChamadoUseCases anexoChamadoUseCases,
            AnexoComentarioUseCases anexoComentarioUseCases,
            WebControllerSupport support
    ) {
        this.adminUseCases = adminUseCases;
        this.anexoChamadoUseCases = anexoChamadoUseCases;
        this.anexoComentarioUseCases = anexoComentarioUseCases;
        this.support = support;
    }

    @Operation(summary = "Baixa um anexo vinculado a comentario do chamado", tags = "09 - Admin Web - Chamados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Anexo do comentario retornado com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado."),
            @ApiResponse(responseCode = "404", description = "Chamado, comentario ou anexo nao encontrado.")
    })
    @GetMapping("/{chamadoId}/comentarios/{comentarioId}/anexos/{anexoId}")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> baixarAnexoDoComentario(
            Authentication authentication,
            @PathVariable UUID chamadoId,
            @PathVariable UUID comentarioId,
            @PathVariable UUID anexoId
    ) {
        var anexo = anexoComentarioUseCases.buscarAnexoPorId(
                support.authenticatedUser(authentication),
                chamadoId,
                comentarioId,
                anexoId
        );
        return support.downloadResponse(
                anexo.nomeArquivo(),
                anexo.contentType(),
                anexo.tamanhoBytes(),
                anexo.conteudo()
        );
    }

    @Operation(summary = "Baixa um anexo do chamado", tags = "09 - Admin Web - Chamados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Anexo do chamado retornado com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado."),
            @ApiResponse(responseCode = "404", description = "Chamado ou anexo nao encontrado.")
    })
    @GetMapping("/{chamadoId}/anexos/{anexoId}")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> baixarAnexo(
            Authentication authentication,
            @PathVariable UUID chamadoId,
            @PathVariable UUID anexoId
    ) {
        var anexo = anexoChamadoUseCases.buscarAnexoPorId(
                support.authenticatedUser(authentication),
                chamadoId,
                anexoId
        );
        return support.downloadResponse(
                anexo.nomeArquivo(),
                anexo.contentType(),
                anexo.tamanhoBytes(),
                anexo.conteudo()
        );
    }

    @Operation(summary = "Adiciona comentario ao chamado", tags = "09 - Admin Web - Chamados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Comentario registrado com sucesso e redirecionamento para o detalhe do chamado."),
            @ApiResponse(responseCode = "400", description = "Dados informados sao invalidos ou violam regra de negocio."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado."),
            @ApiResponse(responseCode = "404", description = "Chamado nao encontrado.")
    })
    @PostMapping("/{chamadoId}/comentarios")
    public String comentarChamado(
            Authentication authentication,
            @PathVariable UUID chamadoId,
            @ModelAttribute ComentarioForm comentarioForm,
            @RequestParam(name = "arquivo", required = false) MultipartFile arquivo,
            RedirectAttributes redirectAttributes
    ) {
        var currentUser = support.authenticatedUser(authentication);
        var comentario = adminUseCases.comentarChamado(
                currentUser,
                chamadoId,
                comentarioForm.getMensagem()
        );

        var uploadedFile = support.optionalUploadedFile(
                arquivo,
                "Nao foi possivel anexar o arquivo ao comentario."
        );
        if (uploadedFile != null) {
            anexoComentarioUseCases.adicionarAnexoAoComentario(
                    currentUser,
                    chamadoId,
                    comentario.getId(),
                    uploadedFile.nomeArquivo(),
                    uploadedFile.contentType(),
                    uploadedFile.tamanhoBytes(),
                    uploadedFile.conteudo()
            );
        }

        redirectAttributes.addFlashAttribute("successMessage", "Comentario adicionado ao chamado.");
        return "redirect:/admin/chamados/" + chamadoId;
    }

    @Operation(summary = "Atualiza o status de um chamado", tags = "09 - Admin Web - Chamados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Status do chamado atualizado com sucesso e redirecionamento para o detalhe."),
            @ApiResponse(responseCode = "400", description = "Dados informados sao invalidos ou violam regra de negocio."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado."),
            @ApiResponse(responseCode = "404", description = "Chamado ou status nao encontrado.")
    })
    @PatchMapping("/{chamadoId}/status")
    public String atualizarStatusChamadoDoAdmin(
            Authentication authentication,
            @PathVariable UUID chamadoId,
            @ModelAttribute AtualizarStatusForm atualizarStatusForm,
            RedirectAttributes redirectAttributes
    ) {
        adminUseCases.atualizarStatusChamado(
                support.authenticatedUser(authentication),
                chamadoId,
                atualizarStatusForm.getStatusId()
        );
        redirectAttributes.addFlashAttribute("successMessage", "Status do chamado atualizado.");
        return "redirect:/admin/chamados/" + chamadoId;
    }
}
