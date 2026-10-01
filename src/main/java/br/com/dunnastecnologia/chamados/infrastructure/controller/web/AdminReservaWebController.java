package br.com.dunnastecnologia.chamados.infrastructure.controller.web;

import br.com.dunnastecnologia.chamados.application.UserCase.AdminAreaComumUseCases;
import br.com.dunnastecnologia.chamados.application.UserCase.AdminReservaUseCases;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.form.NegarReservaForm;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.UUID;

@Controller
@RequestMapping("/admin/reservas")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminReservaWebController {

    private final AdminReservaUseCases adminReservaUseCases;
    private final AdminAreaComumUseCases adminAreaComumUseCases;
    private final WebControllerSupport support;

    public AdminReservaWebController(
            AdminReservaUseCases adminReservaUseCases,
            AdminAreaComumUseCases adminAreaComumUseCases,
            WebControllerSupport support
    ){
        this.adminReservaUseCases = adminReservaUseCases;
        this.adminAreaComumUseCases = adminAreaComumUseCases;
        this.support = support;
    }

    @ModelAttribute("negarReservaForm")
    public NegarReservaForm NegarReservaForm() {
        return new NegarReservaForm();
    }

    @Operation(summary = "Lista as reservas de todos os moradores", tags = "22 - Admin Web - Reservas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagina de reservas do admin rederizada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado.")
            })
    @GetMapping({"", "/"})
    @Transactional(readOnly = true)
    public String listarTodasReservas(
            Authentication authentication,
            @RequestParam(required = false) UUID areaComumId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            Model model
            ){
        var admin = support.authenticatedUser(authentication);
        var reservas =adminReservaUseCases.listarTodasReservas(
                admin, areaComumId, data, support.pageRequest(page, size)
        );

        var areas = adminAreaComumUseCases.listaAreasComum(admin, support.pageRequest(0, 100));

        model.addAttribute("pageTitle", "Reservas de Áreas Comuns");
        model.addAttribute("reservas", support.mapContent(reservas.content(), support::toReservaMap));
        model.addAttribute("reservasPage", support.pageMetadata(reservas));
        model.addAttribute("areasFiltor", support.mapContent(areas.content(), support::toAreaComumMap));
        model.addAttribute("filtroAreaComumId", areaComumId);
        model.addAttribute("filtroData", data);
        return "admin/reservas/lista";
    }
}
