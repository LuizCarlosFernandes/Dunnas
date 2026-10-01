package br.com.dunnastecnologia.chamados.unit.service;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.domain.model.AreaComum;
import br.com.dunnastecnologia.chamados.domain.model.Morador;
import br.com.dunnastecnologia.chamados.domain.model.Reserva;
import br.com.dunnastecnologia.chamados.domain.model.StatusReserva;
import br.com.dunnastecnologia.chamados.infrastructure.exception.BusinessRuleException;
import br.com.dunnastecnologia.chamados.infrastructure.repository.AreaComumRepository;
import br.com.dunnastecnologia.chamados.infrastructure.repository.MoradorRepository;
import br.com.dunnastecnologia.chamados.infrastructure.repository.ReservaRepository;
import br.com.dunnastecnologia.chamados.infrastructure.service.ReservaService;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.AuthenticatedUserValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private AreaComumRepository areaComumRepository;

    @Mock
    private MoradorRepository moradorRepository;

    @Mock
    private AuthenticatedUserValidator authenticatedUserValidator;

    @InjectMocks
    private ReservaService reservaService;

    private final AuthenticatedUser admin =
            new AuthenticatedUser(UUID.randomUUID(), "admin@cond.local", "ROLE_ADMINISTRADOR");

    private Reserva reservaSolicitada() {
        AreaComum area = new AreaComum();
        area.setId(UUID.randomUUID());
        area.setNome("Salão de festas");

        Reserva reserva = new Reserva();
        reserva.setId(UUID.randomUUID());
        reserva.setArea(area);
        reserva.setData(LocalDate.now().plusDays(3));
        reserva.setHoraInicio(LocalTime.of(10, 0));
        reserva.setHoraFim(LocalTime.of(12, 0));
        reserva.setStatus(StatusReserva.SOLICITADA);
        return reserva;
    }

    @Test
    void solicitarReservaDevePreencherDataCriacaoSemDataDecisao() {
        AuthenticatedUser morador =
                new AuthenticatedUser(UUID.randomUUID(), "morador@cond.local", "ROLE_MORADOR");
        AreaComum area = new AreaComum();
        area.setId(UUID.randomUUID());
        area.setAtiva(Boolean.TRUE);

        when(areaComumRepository.findById(area.getId())).thenReturn(Optional.of(area));
        when(moradorRepository.findById(morador.id())).thenReturn(Optional.of(new Morador()));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reserva criada = reservaService.solicitarReserva(
                morador, area.getId(), LocalDate.now().plusDays(2), LocalTime.of(14, 0), LocalTime.of(16, 0));

        assertEquals(StatusReserva.SOLICITADA, criada.getStatus());
        assertNotNull(criada.getDataCriacao());
        assertNull(criada.getDataDecisao());
    }

    @Test
    void solicitarReservaSemAreaDeveLancarRegraDeNegocio() {
        AuthenticatedUser morador =
                new AuthenticatedUser(UUID.randomUUID(), "morador@cond.local", "ROLE_MORADOR");

        assertThrows(
                BusinessRuleException.class,
                () -> reservaService.solicitarReserva(
                        morador, null, LocalDate.now().plusDays(2), LocalTime.of(14, 0), LocalTime.of(16, 0))
        );

        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void negarReservaDeveMarcarComoNegadaEGuardarMotivo() {
        Reserva reserva = reservaSolicitada();
        when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));
        when(reservaRepository.save(reserva)).thenReturn(reserva);

        Reserva negada = reservaService.negarReserva(admin, reserva.getId(), "  Área em manutenção  ");

        assertEquals(StatusReserva.NEGADA, negada.getStatus());
        assertEquals("Área em manutenção", negada.getMotivoNegacao());
        assertNotNull(negada.getDataDecisao());
    }

    @Test
    void negarReservaSemMotivoNaoDeveAlterarStatus() {
        Reserva reserva = reservaSolicitada();
        when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));

        assertThrows(
                BusinessRuleException.class,
                () -> reservaService.negarReserva(admin, reserva.getId(), "   ")
        );

        assertEquals(StatusReserva.SOLICITADA, reserva.getStatus());
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void aprovarReservaComConflitoDeveSerRecusada() {
        Reserva reserva = reservaSolicitada();
        when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));
        when(areaComumRepository.buscarComLockParaDecisao(reserva.getArea().getId()))
                .thenReturn(Optional.of(reserva.getArea()));
        when(reservaRepository.buscarAprovadasConflitantesExcluindo(
                reserva.getArea().getId(), reserva.getData(),
                reserva.getHoraInicio(), reserva.getHoraFim(), reserva.getId()))
                .thenReturn(List.of(new Reserva()));

        assertThrows(
                BusinessRuleException.class,
                () -> reservaService.aprovarReserva(admin, reserva.getId())
        );

        assertEquals(StatusReserva.SOLICITADA, reserva.getStatus());
        verify(reservaRepository, never()).save(any(Reserva.class));
    }
}
