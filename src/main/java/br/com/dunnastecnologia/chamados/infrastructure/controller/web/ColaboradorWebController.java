package br.com.dunnastecnologia.chamados.infrastructure.controller.web;

import br.com.dunnastecnologia.chamados.application.UserCase.AnexoChamadoUseCases;
import br.com.dunnastecnologia.chamados.application.UserCase.ColaboradorUseCases;
import br.com.dunnastecnologia.chamados.application.UserCase.ComentarioUseCase;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.form.AtualizarStatusForm;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.form.ComentarioForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.UUID;

@Controller
@RequestMapping("/colaborador")
@PreAuthorize("hasRole('COLABORADOR')")
public class ColaboradorWebController {

    private final ColaboradorUseCases colaboradorUseCases;
    private final AnexoChamadoUseCases anexoChamadoUseCases;
    private final ComentarioUseCase comentarioUseCase;
    private final WebControllerSupport support;

    public ColaboradorWebController(
            ColaboradorUseCases colaboradorUseCases,
            AnexoChamadoUseCases anexoChamadoUseCases,
            ComentarioUseCase comentarioUseCase,
            WebControllerSupport support
    ) {
        this.colaboradorUseCases = colaboradorUseCases;
        this.anexoChamadoUseCases = anexoChamadoUseCases;
        this.comentarioUseCase = comentarioUseCase;
        this.support = support;
    }

    @ModelAttribute("atualizarStatusForm")
    public AtualizarStatusForm atualizarStatusForm() {
        return new AtualizarStatusForm();
    }

    @ModelAttribute("comentarioForm")
    public ComentarioForm comentarioForm() {
        return new ComentarioForm();
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "Exibe o dashboard do colaborador", tags = "10 - Colaborador Web - Paginas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagina inicial do colaborador renderizada com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado.")
    })
    public String dashboard(
            Authentication authentication,
            Model model
    ) {
        var currentUser = support.authenticatedUser(authentication);
        var statusDisponiveis = colaboradorUseCases.listarStatusDisponiveis(currentUser, support.pageRequest(0, 100));
        var chamados = colaboradorUseCases.buscarChamados(currentUser, null, null, null, null, support.pageRequest(0, 5));
        UUID statusAtrasadoId = statusDisponiveis.content().stream()
                .filter(statusChamado -> "Atrasado".equalsIgnoreCase(statusChamado.getNome()))
                .map(br.com.dunnastecnologia.chamados.domain.model.StatusChamado::getId)
                .findFirst()
                .orElse(null);
        long totalChamadosAtrasados = statusAtrasadoId == null
                ? 0
                : colaboradorUseCases.buscarChamados(currentUser, statusAtrasadoId, null, null, null, support.pageRequest(0, 1)).totalElements();

        model.addAttribute("pageTitle", "Painel do Colaborador");
        model.addAttribute("chamados", support.mapContent(chamados.content(), support::toChamadoMap));
        model.addAttribute("totalChamadosAbertos", chamados.totalElements());
        model.addAttribute("totalChamadosAtrasados", totalChamadosAtrasados);
        return "colaborador/dashboard";
    }

    @GetMapping("/chamados")
    @Transactional(readOnly = true)
    @Operation(summary = "Lista a fila de chamados do colaborador com filtros e paginacao", tags = "10 - Colaborador Web - Paginas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagina de chamados do colaborador renderizada com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado.")
    })
    public String listarChamados(
            Authentication authentication,
            @RequestParam(required = false) UUID statusId,
            @RequestParam(required = false) UUID tipoChamadoId,
            @RequestParam(required = false) String unidade,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataAbertura,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            Model model
    ) {
        var currentUser = support.authenticatedUser(authentication);
        var chamados = colaboradorUseCases.buscarChamados(
                currentUser,
                statusId,
                tipoChamadoId,
                unidade,
                dataAbertura,
                support.pageRequest(page, size)
        );
        var status = colaboradorUseCases.listarStatusDisponiveis(currentUser, support.pageRequest(0, 100));
        var tipos = colaboradorUseCases.listarTiposChamadoDisponiveis(currentUser, support.pageRequest(0, 100));

        model.addAttribute("pageTitle", "Chamados em Atendimento");
        model.addAttribute("chamados", support.mapContent(chamados.content(), support::toChamadoMap));
        model.addAttribute("chamadosPage", support.pageMetadata(chamados));
        model.addAttribute("statusDisponiveis", support.mapContent(status.content(), support::toStatusChamadoMap));
        model.addAttribute("tiposChamadoDisponiveis", support.mapContent(tipos.content(), support::toTipoChamadoMap));
        model.addAttribute("filtroStatusId", statusId);
        model.addAttribute("filtroTipoChamadoId", tipoChamadoId);
        model.addAttribute("filtroUnidade", unidade);
        model.addAttribute("filtroDataAbertura", dataAbertura);
        return "colaborador/chamados/lista";
    }

    @GetMapping("/chamados/{chamadoId}")
    @Transactional(readOnly = true)
    @Operation(summary = "Exibe o detalhe de um chamado no escopo do colaborador", tags = "10 - Colaborador Web - Paginas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagina de detalhe do chamado renderizada com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado."),
            @ApiResponse(responseCode = "404", description = "Chamado nao encontrado no escopo do colaborador.")
    })
    public String detalharChamado(
            Authentication authentication,
            @PathVariable UUID chamadoId,
            Model model
    ) {
        var currentUser = support.authenticatedUser(authentication);
        var chamado = colaboradorUseCases.buscarChamadoPorId(currentUser, chamadoId);
        var status = colaboradorUseCases.listarStatusDisponiveis(currentUser, support.pageRequest(0, 100));
        var comentarios = comentarioUseCase.listarComentariosDoChamado(currentUser, chamadoId, support.pageRequest(0, 100));
        var anexos = anexoChamadoUseCases.listarAnexosDoChamado(currentUser, chamadoId, support.pageRequest(0, 100));

        model.addAttribute("pageTitle", "Detalhes do Chamado");
        model.addAttribute("chamado", support.toChamadoMap(chamado));
        model.addAttribute("statusDisponiveis", support.mapContent(status.content(), support::toStatusChamadoMap));
        model.addAttribute("comentarios", support.mapContent(comentarios.content(), support::toComentarioMap));
        model.addAttribute(
                "anexos",
                anexos.content().stream()
                        .map(anexo -> support.toAnexoMap(
                                anexo.id(),
                                anexo.nomeArquivo(),
                                anexo.contentType(),
                                anexo.tamanhoBytes()
                        ))
                        .toList()
        );
        return "colaborador/chamados/detalhe";
    }
}
