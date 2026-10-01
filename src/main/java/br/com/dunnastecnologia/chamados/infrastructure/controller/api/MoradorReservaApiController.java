package br.com.dunnastecnologia.chamados.infrastructure.controller.api;

import br.com.dunnastecnologia.chamados.application.UserCase.MoradorReservaUseCases;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.WebControllerSupport;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.form.SolicitarReservaForm;
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
@RequestMapping("/morador/reservas")
@PreAuthorize("hasRole('MORADOR')")
public class MoradorReservaApiController {
    private final MoradorReservaUseCases moradorReservaUseCases;
    private final WebControllerSupport support;

    public MoradorReservaApiController( MoradorReservaUseCases moradorReservaUseCases, WebControllerSupport support) {
        this.moradorReservaUseCases = moradorReservaUseCases;
        this.support = support;
    }

    @Operation(summary = "Solicita uma nova reserva de area comum", tags ="21 - Morador Web - Reservas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Reserva solicita com sucesso e redirecionamento para a lista."),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado.")
    })
    @PostMapping
    public String solicitarReserva(
            Authentication authentication,
            @ModelAttribute SolicitarReservaForm form,
            RedirectAttributes redirectAttributes
    ){
        moradorReservaUseCases.solicitarReserva(
                support.authenticatedUser(authentication),
                form.getAreaComumId(),
                form.getData(),
                form.getHoraInicio(),
                form.getHoraFim()
        );
        redirectAttributes.addFlashAttribute("successMessage", "Reserva solicitada com sucesso. Aguarde a decisão do administrador.");
        return "redirect:/morador/reservas";
    }

    // RF-06 - o morador cancela somente a própria reserva (SOLICITADA ou APROVADA) antes do início.
    @Operation(summary = "Cancela uma reserva do morador autenticado", tags = "21 - Morador Web - Reservas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Reserva cancelada e redirecionamento para a lista."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado."),
            @ApiResponse(responseCode = "404", description = "Reserva não encontrada.")
    })
    @PatchMapping("/{reservaId}/cancelamento")
    public String cancelarReserva(
            Authentication authentication,
            @PathVariable UUID reservaId,
            RedirectAttributes redirectAttributes
    ) {
        moradorReservaUseCases.cancelarReservaMorador(support.authenticatedUser(authentication), reservaId);
        redirectAttributes.addFlashAttribute("successMessage", "Reserva cancelada com sucesso.");
        return "redirect:/morador/reservas";
    }

}
