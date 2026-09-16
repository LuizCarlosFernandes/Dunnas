package br.com.dunnastecnologia.chamados.infrastructure.controller.web;

import br.com.dunnastecnologia.chamados.application.UserCase.AnexoChamadoUseCases;
import br.com.dunnastecnologia.chamados.application.UserCase.ComentarioUseCase;
import br.com.dunnastecnologia.chamados.application.UserCase.MoradorUseCases;
import br.com.dunnastecnologia.chamados.application.UserCase.StatusChamadoUseCase;
import br.com.dunnastecnologia.chamados.application.UserCase.TipoChamadoUseCase;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.form.AbrirChamadoForm;
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
@RequestMapping("/morador")
@PreAuthorize("hasRole('MORADOR')")
public class MoradorWebController {

    private final MoradorUseCases moradorUseCases;
    private final TipoChamadoUseCase tipoChamadoUseCase;
    private final StatusChamadoUseCase statusChamadoUseCase;
    private final AnexoChamadoUseCases anexoChamadoUseCases;
    private final ComentarioUseCase comentarioUseCase;
    private final WebControllerSupport support;

    public MoradorWebController(
            MoradorUseCases moradorUseCases,
            TipoChamadoUseCase tipoChamadoUseCase,
            StatusChamadoUseCase statusChamadoUseCase,
            AnexoChamadoUseCases anexoChamadoUseCases,
            ComentarioUseCase comentarioUseCase,
            WebControllerSupport support
    ) {
        this.moradorUseCases = moradorUseCases;
        this.tipoChamadoUseCase = tipoChamadoUseCase;
        this.statusChamadoUseCase = statusChamadoUseCase;
        this.anexoChamadoUseCases = anexoChamadoUseCases;
        this.comentarioUseCase = comentarioUseCase;
        this.support = support;
    }

    @ModelAttribute("abrirChamadoForm")
    public AbrirChamadoForm abrirChamadoForm() {
        return new AbrirChamadoForm();
    }

    @ModelAttribute("comentarioForm")
    public ComentarioForm comentarioForm() {
        return new ComentarioForm();
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "Exibe o dashboard do morador", tags = "12 - Morador Web - Paginas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagina inicial do morador renderizada com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado.")
    })
    public String dashboard(
            Authentication authentication,
            Model model
    ) {
        var currentUser = support.authenticatedUser(authentication);
        var unidades = moradorUseCases.listarMinhasUnidades(currentUser, support.pageRequest(0, 20));
        var chamados = moradorUseCases.listarMeusChamados(currentUser, null, null, null, null, support.pageRequest(0, 5));

        model.addAttribute("pageTitle", "Meu Painel");
        model.addAttribute("minhasUnidades", support.mapContent(unidades.content(), support::toUnidadeMap));
        model.addAttribute("meusChamados", support.mapContent(chamados.content(), support::toChamadoMap));
        model.addAttribute("totalUnidades", unidades.totalElements());
        model.addAttribute("totalChamados", chamados.totalElements());
        return "morador/dashboard";
    }

    @GetMapping("/chamados")
    @Transactional(readOnly = true)
    @Operation(summary = "Lista os chamados do morador com filtros e paginacao", tags = "12 - Morador Web - Paginas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagina de chamados do morador renderizada com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado.")
    })
    public String listarChamados(
            Authentication authentication,
            @RequestParam(required = false) UUID statusId,
            @RequestParam(required = false) UUID unidadeId,
            @RequestParam(required = false) UUID tipoChamadoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataAbertura,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            Model model
    ) {
        var currentUser = support.authenticatedUser(authentication);
        var chamados = moradorUseCases.listarMeusChamados(
                currentUser,
                statusId,
                unidadeId,
                tipoChamadoId,
                dataAbertura,
                support.pageRequest(page, size)
        );
        var status = statusChamadoUseCase.listarStatus(support.pageRequest(0, 100));
        var unidades = moradorUseCases.listarMinhasUnidades(currentUser, support.pageRequest(0, 100));
        var tipos = tipoChamadoUseCase.listarTiposChamado(support.pageRequest(0, 100));

        model.addAttribute("pageTitle", "Meus Chamados");
        model.addAttribute("chamados", support.mapContent(chamados.content(), support::toChamadoMap));
        model.addAttribute("chamadosPage", support.pageMetadata(chamados));
        model.addAttribute("statusDisponiveis", support.mapContent(status.content(), support::toStatusChamadoMap));
        model.addAttribute("unidadesDisponiveis", support.mapContent(unidades.content(), support::toUnidadeMap));
        model.addAttribute("tiposChamadoDisponiveis", support.mapContent(tipos.content(), support::toTipoChamadoMap));
        model.addAttribute("filtroStatusId", statusId);
        model.addAttribute("filtroUnidadeId", unidadeId);
        model.addAttribute("filtroTipoChamadoId", tipoChamadoId);
        model.addAttribute("filtroDataAbertura", dataAbertura);
        return "morador/chamados/lista";
    }

    @GetMapping("/chamados/novo")
    @Transactional(readOnly = true)
    @Operation(summary = "Exibe o formulario para abertura de chamado", tags = "12 - Morador Web - Paginas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Formulario de abertura de chamado renderizado com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado.")
    })
    public String novoChamado(
            Authentication authentication,
            Model model
    ) {
        var currentUser = support.authenticatedUser(authentication);
        var unidades = moradorUseCases.listarMinhasUnidades(currentUser, support.pageRequest(0, 100));
        var tipos = tipoChamadoUseCase.listarTiposChamado(support.pageRequest(0, 100));

        model.addAttribute("pageTitle", "Abrir Chamado");
        model.addAttribute("unidades", support.mapContent(unidades.content(), support::toUnidadeMap));
        model.addAttribute("tiposChamado", support.mapContent(tipos.content(), support::toTipoChamadoMap));
        return "morador/chamados/novo";
    }

    @GetMapping("/chamados/{chamadoId}")
    @Transactional(readOnly = true)
    @Operation(summary = "Exibe o detalhe de um chamado do morador", tags = "12 - Morador Web - Paginas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagina de detalhe do chamado renderizada com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado."),
            @ApiResponse(responseCode = "404", description = "Chamado nao encontrado para o morador.")
    })
    public String detalharChamado(
            Authentication authentication,
            @PathVariable UUID chamadoId,
            Model model
    ) {
        var currentUser = support.authenticatedUser(authentication);
        var chamado = moradorUseCases.buscarMeuChamadoPorId(currentUser, chamadoId);
        var comentarios = comentarioUseCase.listarComentariosDoChamado(currentUser, chamadoId, support.pageRequest(0, 100));
        var anexos = anexoChamadoUseCases.listarAnexosDoChamado(currentUser, chamadoId, support.pageRequest(0, 100));

        model.addAttribute("pageTitle", "Detalhes do Chamado");
        model.addAttribute("chamado", support.toChamadoMap(chamado));
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
        return "morador/chamados/detalhe";
    }
}
