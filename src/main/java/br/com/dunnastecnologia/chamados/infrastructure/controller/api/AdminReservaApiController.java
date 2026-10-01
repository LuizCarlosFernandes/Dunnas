package br.com.dunnastecnologia.chamados.infrastructure.controller.api;

import br.com.dunnastecnologia.chamados.application.UserCase.AdminReservaUseCases;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.WebControllerSupport;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.form.NegarReservaForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("admin/reservas")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminReservaApiController {

    private final AdminReservaUseCases adminReservaUseCases;
    private final WebControllerSupport support;

    public AdminReservaApiController(AdminReservaUseCases adminReservaUseCases, WebControllerSupport support) {
        this.adminReservaUseCases = adminReservaUseCases;
        this.support = support;
    }

    @Operation(summary = "Aprova uma solicitacao reserva", tags = "23 - Admin Web - Reserva")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Reserva aprovada com sucesso."),
            @ApiResponse(responseCode = "409", description = "Conflito com outra reserva aprovada ou decisao concorrente em andamento"),
            @ApiResponse(responseCode = "404", description = "Reserva nao encontrada.")
    })
    @PatchMapping("/{reservaId}/aprovacao")
    public String aprovarReserva(
            Authentication authentication,
            @PathVariable UUID reservaId,
            RedirectAttributes redirectAttributes
    ){
        adminReservaUseCases.aprovarReserva(support.authenticatedUser(authentication), reservaId);
        redirectAttributes.addFlashAttribute("successMessage", "Reserva aprovada com sucesso.");
        return "redirect:/admin/reservas";
    }

    @Operation(summary = "Negar uma solicitacao de reserva mediante motivo", tags = "23 - Admin Web - Reservas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Reserva negada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Motivo de negacao ausente"),
            @ApiResponse(responseCode = "404", description = "Reserva nao encontrada.")
    })
    @PatchMapping("/{reservaId}/negacao")
    public String negarReserva(
            Authentication authentication,
            @PathVariable UUID reservaId,
            @ModelAttribute NegarReservaForm form,
            RedirectAttributes redirectAttributes
    ){
        adminReservaUseCases.negarReserva(support.authenticatedUser(authentication), reservaId, form.getMotivo());
        redirectAttributes.addFlashAttribute("successMessage", "Reserva negada.");
        return "redirect:/admin/reservas";
    }

    @Operation(summary = "Cancela qualquer reserva", tags = "23 - Admin Web - Reservas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Reserva cancelada com sucesso,"),
            @ApiResponse(responseCode = "404", description = "Reserva nao encontrada,")
    })
    @PatchMapping("/{reservaId}/cancelamento")
    public String cancelarReserva(
            Authentication authentication,
            @PathVariable UUID reservaId,
            RedirectAttributes redirectAttributes
    ){
        adminReservaUseCases.cancelarReserva(support.authenticatedUser(authentication), reservaId);
        redirectAttributes.addFlashAttribute("successMessage", "Reserva cancelada pelo administrador.");
        return "redirect:/admin/reservas";
    }
}
