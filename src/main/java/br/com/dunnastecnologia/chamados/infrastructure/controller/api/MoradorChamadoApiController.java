package br.com.dunnastecnologia.chamados.infrastructure.controller.api;

import br.com.dunnastecnologia.chamados.application.UserCase.AnexoChamadoUseCases;
import br.com.dunnastecnologia.chamados.application.UserCase.AnexoComentarioUseCases;
import br.com.dunnastecnologia.chamados.application.UserCase.MoradorUseCases;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.WebControllerSupport;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.form.AbrirChamadoForm;
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
@RequestMapping("/morador/chamados")
@PreAuthorize("hasRole('MORADOR')")
public class MoradorChamadoApiController {

    private final MoradorUseCases moradorUseCases;
    private final AnexoChamadoUseCases anexoChamadoUseCases;
    private final AnexoComentarioUseCases anexoComentarioUseCases;
    private final WebControllerSupport support;

    public MoradorChamadoApiController(
            MoradorUseCases moradorUseCases,
            AnexoChamadoUseCases anexoChamadoUseCases,
            AnexoComentarioUseCases anexoComentarioUseCases,
            WebControllerSupport support
    ) {
        this.moradorUseCases = moradorUseCases;
        this.anexoChamadoUseCases = anexoChamadoUseCases;
        this.anexoComentarioUseCases = anexoComentarioUseCases;
        this.support = support;
    }

    @GetMapping("/{chamadoId}/comentarios/{comentarioId}/anexos/{anexoId}")
    @Transactional(readOnly = true)
    @Operation(summary = "Baixa um anexo vinculado a comentario do chamado", tags = "13 - Morador Web - Chamados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Anexo do comentario retornado com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado."),
            @ApiResponse(responseCode = "404", description = "Chamado, comentario ou anexo nao encontrado.")
    })
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

    @GetMapping("/{chamadoId}/anexos/{anexoId}")
    @Transactional(readOnly = true)
    @Operation(summary = "Baixa um anexo do chamado do morador", tags = "13 - Morador Web - Chamados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Anexo do chamado retornado com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado."),
            @ApiResponse(responseCode = "404", description = "Chamado ou anexo nao encontrado.")
    })
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

    @PostMapping
    @Operation(summary = "Abre um novo chamado para o morador", tags = "13 - Morador Web - Chamados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Chamado aberto com sucesso e redirecionamento para o detalhe do registro."),
            @ApiResponse(responseCode = "400", description = "Dados informados sao invalidos ou violam regra de negocio."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado."),
            @ApiResponse(responseCode = "404", description = "Unidade ou tipo de chamado nao encontrado.")
    })
    public String abrirChamado(
            Authentication authentication,
            @ModelAttribute AbrirChamadoForm abrirChamadoForm,
            @RequestParam(name = "arquivo", required = false) MultipartFile arquivo,
            RedirectAttributes redirectAttributes
    ) {
        var currentUser = support.authenticatedUser(authentication);
        var chamado = moradorUseCases.abrirChamado(
                currentUser,
                abrirChamadoForm.getUnidadeId(),
                abrirChamadoForm.getTipoChamadoId(),
                abrirChamadoForm.getDescricao()
        );

        var uploadedFile = support.optionalUploadedFile(
                arquivo,
                "Nao foi possivel anexar o arquivo ao abrir o chamado."
        );
        if (uploadedFile != null) {
            anexoChamadoUseCases.adicionarAnexo(
                    currentUser,
                    chamado.getId(),
                    uploadedFile.nomeArquivo(),
                    uploadedFile.contentType(),
                    uploadedFile.tamanhoBytes(),
                    uploadedFile.conteudo()
            );
        }

        redirectAttributes.addFlashAttribute("successMessage", "Chamado aberto com sucesso.");
        return "redirect:/morador/chamados/" + chamado.getId();
    }

    @PostMapping("/{chamadoId}/comentarios")
    @Operation(summary = "Adiciona comentario ao chamado do morador", tags = "13 - Morador Web - Chamados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Comentario registrado com sucesso e redirecionamento para o detalhe do chamado."),
            @ApiResponse(responseCode = "400", description = "Dados informados sao invalidos ou violam regra de negocio."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado."),
            @ApiResponse(responseCode = "404", description = "Chamado nao encontrado para o morador.")
    })
    public String comentarChamado(
            Authentication authentication,
            @PathVariable UUID chamadoId,
            @ModelAttribute ComentarioForm comentarioForm,
            @RequestParam(name = "arquivo", required = false) MultipartFile arquivo,
            RedirectAttributes redirectAttributes
    ) {
        var currentUser = support.authenticatedUser(authentication);
        var comentario = moradorUseCases.comentarChamado(
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

        redirectAttributes.addFlashAttribute("successMessage", "Comentario registrado.");
        return "redirect:/morador/chamados/" + chamadoId;
    }

    @PostMapping("/{chamadoId}/anexos")
    @Operation(summary = "Envia um anexo adicional para o chamado do morador", tags = "13 - Morador Web - Chamados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Anexo enviado com sucesso e redirecionamento para o detalhe do chamado."),
            @ApiResponse(responseCode = "400", description = "Dados informados sao invalidos ou violam regra de negocio."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado."),
            @ApiResponse(responseCode = "404", description = "Chamado nao encontrado para o morador.")
    })
    public String adicionarAnexo(
            Authentication authentication,
            @PathVariable UUID chamadoId,
            @RequestParam("arquivo") MultipartFile arquivo,
            RedirectAttributes redirectAttributes
    ) {
        var uploadedFile = support.requiredUploadedFile(
                arquivo,
                "Selecione um arquivo para anexar.",
                "Nao foi possivel anexar o arquivo ao chamado."
        );
        anexoChamadoUseCases.adicionarAnexo(
                support.authenticatedUser(authentication),
                chamadoId,
                uploadedFile.nomeArquivo(),
                uploadedFile.contentType(),
                uploadedFile.tamanhoBytes(),
                uploadedFile.conteudo()
        );
        redirectAttributes.addFlashAttribute("successMessage", "Anexo enviado com sucesso.");
        return "redirect:/morador/chamados/" + chamadoId;
    }

    @PatchMapping("/{chamadoId}/reabrir")
    @Operation(summary = "Reabre um chamado finalizado do morador", tags = "13 - Morador Web - Chamados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Chamado reaberto com sucesso e redirecionamento para o detalhe do registro."),
            @ApiResponse(responseCode = "400", description = "Dados informados sao invalidos ou violam regra de negocio."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado."),
            @ApiResponse(responseCode = "404", description = "Chamado nao encontrado para o morador.")
    })
    public String reabrirChamado(
            Authentication authentication,
            @PathVariable UUID chamadoId,
            RedirectAttributes redirectAttributes
    ) {
        moradorUseCases.reabrirChamado(support.authenticatedUser(authentication), chamadoId);
        redirectAttributes.addFlashAttribute("successMessage", "Chamado reaberto com sucesso.");
        return "redirect:/morador/chamados/" + chamadoId;
    }
}
