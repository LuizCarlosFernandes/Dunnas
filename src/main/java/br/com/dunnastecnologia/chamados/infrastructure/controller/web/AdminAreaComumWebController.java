package br.com.dunnastecnologia.chamados.infrastructure.controller.web;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.application.UserCase.AdminAreaComumUseCases;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.form.AreaComumForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/admin/areas-comuns")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminAreaComumWebController {

    private final AdminAreaComumUseCases adminAreaComumUseCases;
    private final WebControllerSupport support;

    public AdminAreaComumWebController(AdminAreaComumUseCases adminAreaComumUseCases, WebControllerSupport support) {
        this.adminAreaComumUseCases = adminAreaComumUseCases;
        this.support = support;
    }

    @ModelAttribute("areaComumForm")
    public AreaComumForm areaComumForm(){
        return new AreaComumForm();
    }

    @Operation(summary = "Lista as areas comuns cadastradas", tags = "24 - Admin Web - Areas Comuns")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagina de listagem de areas comuns renderizada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado")
    })
    @GetMapping({"", "/"})
    @Transactional(readOnly = true)
    public String listarAreasComuns(
            Authentication authentication,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            Model model
            ){
        var areas = adminAreaComumUseCases.listaAreasComum(
                support.authenticatedUser(authentication), support.pageRequest(page, size)
        );

        model.addAttribute("pageTitle", "Áreas Comuns");
        model.addAttribute("areas", support.mapContent(areas.content(), support::toAreaComumMap));
        model.addAttribute("areasPage", support.pageMetadata(areas));

        return "admin/areas-comuns/lista";
    }

    @Operation(summary = "Exibe o detalhe de uma area comum", tags = "24 - Admin Web - Areas Comuns")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagina de detalhe da area comum renderizada com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado."),
            @ApiResponse(responseCode = "404", description = "Area comum não encontrada")
    })
    @GetMapping("/{areaComumId}")
    @Transactional(readOnly = true)
    public String detalharAreaComum(
            Authentication authentication,
            @PathVariable UUID areaComumId,
            Model model
            ){
        var area = adminAreaComumUseCases.buscarAreaComumPorId(support.authenticatedUser(authentication), areaComumId);
        model.addAttribute("pageTittle", "Detalhes da Área Comum");
        model.addAttribute("area", support.toAreaComumMap(area));

        return "admin/areas-comuns/detalhe";
    }
}
