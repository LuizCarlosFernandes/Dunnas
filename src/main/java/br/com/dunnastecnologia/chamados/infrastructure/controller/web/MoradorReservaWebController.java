package br.com.dunnastecnologia.chamados.infrastructure.controller.web;

import br.com.dunnastecnologia.chamados.application.UserCase.MoradorReservaUseCases;
import br.com.dunnastecnologia.chamados.infrastructure.controller.web.form.SolicitarReservaForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@Controller
@RequestMapping("/morador/reservas")
@PreAuthorize("hasRole('MORADOR')")
public class MoradorReservaWebController {

    private final MoradorReservaUseCases moradorReservaUseCases;
    private final WebControllerSupport support;

    public MoradorReservaWebController(MoradorReservaUseCases moradorReservaUseCases, WebControllerSupport support) {
        this.moradorReservaUseCases = moradorReservaUseCases;
        this.support = support;
    }

    @ModelAttribute("solicitarReservaForm")
    public SolicitarReservaForm solicitarReservaForm() {
        return new SolicitarReservaForm();
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "Lista as reservas do morador autenticado", tags = "20 - Morador Web - Reservas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pagina de reservas do morador renderizada com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acessso negado para o perfil autenticado,")
    })
    public String listarMinhasRersevas(
            Authentication authentication,
            @RequestParam(required = false) UUID areaComumId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            Model model
    ){
        var currentUser = support.authenticatedUser(authentication);
        var reservas = moradorReservaUseCases.listarMinhasReservas(
                currentUser, areaComumId, data, support.pageRequest(page, size));
        var areas = moradorReservaUseCases.listarAreasDisponiveis(currentUser, support.pageRequest(0, 100));

        model.addAttribute("pageTitle", "Minhas Reservas");
        model.addAttribute("reservas", support.mapContent(reservas.content(),support::toReservaMap));
        model.addAttribute("areasDisponiveis", support.mapContent(areas.content(),support::toAreaComumMap));
        model.addAttribute("reservasPage", support.pageMetadata(reservas));
        model.addAttribute("filtroAreaComumId", areaComumId);
        model.addAttribute("filtroData", data);
        return "morador/reservas/lista";
    }

    // RF-02/ RF-03 / CA-01-01 / CA-01-02 - form + disponibilidade na mesma pagina
    // areaComumId e Data chegam vazios no primeito GET
    // Paramentos para a pagina recarregar mostrando a disponibilidade antes do POST de confirmacao.
    @GetMapping("/nova")
    @Transactional(readOnly = true)
    @Operation(summary = "Exibe o formulario de solicitacao e disponibilidade da area escolhida", tags = "20 - Morador Web - Reservas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Formulario de solicitacao renderizado com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o perfil autenticado.")
    })
    public String novaReserva(
            Authentication authentication,
            @RequestParam(required = false) UUID areaComumId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            Model model){
        var currentUser = support.authenticatedUser(authentication);
        var areas = moradorReservaUseCases.listarAreasDisponiveis(
                currentUser, support.pageRequest(0, 100));

        model.addAttribute("pageTitle", "Solicitar Reserva");
        model.addAttribute("areasDisponiveis", support.mapContent(areas.content(),support::toAreaComumMap));
        model.addAttribute("areaComumIdSelecionada", areaComumId);
        model.addAttribute("dataSelecionada", data);

        if(areaComumId != null && data != null){
            var ocupadas = moradorReservaUseCases.consultarDisponibilidade(currentUser, areaComumId, data);
            model.addAttribute("intervalosOcupados", support.mapContent(ocupadas, support::toReservaMap));
        }
        return "morador/reservas/nova";
    }
}
