package br.com.dunnastecnologia.chamados.unit.service;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.application.pagination.PageResult;
import br.com.dunnastecnologia.chamados.domain.model.AreaComum;
import br.com.dunnastecnologia.chamados.domain.model.Morador;
import br.com.dunnastecnologia.chamados.domain.model.Reserva;
import br.com.dunnastecnologia.chamados.domain.model.StatusReserva;
import br.com.dunnastecnologia.chamados.domain.validation.ValidationLimits;
import br.com.dunnastecnologia.chamados.infrastructure.exception.BusinessRuleException;
import br.com.dunnastecnologia.chamados.infrastructure.exception.ResourceNotFoundException;
import br.com.dunnastecnologia.chamados.infrastructure.exception.UnauthorizedOperationException;
import br.com.dunnastecnologia.chamados.infrastructure.repository.AdministradorRepository;
import br.com.dunnastecnologia.chamados.infrastructure.repository.AreaComumRepository;
import br.com.dunnastecnologia.chamados.infrastructure.repository.ColaboradorRepository;
import br.com.dunnastecnologia.chamados.infrastructure.repository.MoradorRepository;
import br.com.dunnastecnologia.chamados.infrastructure.repository.ReservaRepository;
import br.com.dunnastecnologia.chamados.infrastructure.service.ReservaService;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.AuthenticatedUserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Testes unitários do {@link ReservaService} (funcionalidade de reserva de áreas comuns).
 *
 * O {@link AuthenticatedUserValidator} usado aqui é o REAL (só os repositórios de perfil são mockados),
 * de modo que as regras de autorização por MORADOR / ADMINISTRADOR / COLABORADOR são realmente exercitadas,
 * e não apenas "assumidas" por um mock do validador.
 */
@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock private ReservaRepository reservaRepository;
    @Mock private AreaComumRepository areaComumRepository;
    @Mock private MoradorRepository moradorRepository;
    @Mock private AdministradorRepository administradorRepository;
    @Mock private ColaboradorRepository colaboradorRepository;

    private ReservaService reservaService;

    private final AuthenticatedUser moradorA =
            new AuthenticatedUser(UUID.randomUUID(), "a@cond.local", "ROLE_MORADOR");
    private final AuthenticatedUser moradorB =
            new AuthenticatedUser(UUID.randomUUID(), "b@cond.local", "ROLE_MORADOR");
    private final AuthenticatedUser admin =
            new AuthenticatedUser(UUID.randomUUID(), "admin@cond.local", "ROLE_ADMINISTRADOR");
    private final AuthenticatedUser colaborador =
            new AuthenticatedUser(UUID.randomUUID(), "colab@cond.local", "ROLE_COLABORADOR");

    @BeforeEach
    void setUp() {
        AuthenticatedUserValidator validator =
                new AuthenticatedUserValidator(administradorRepository, colaboradorRepository, moradorRepository);
        reservaService = new ReservaService(reservaRepository, areaComumRepository, moradorRepository, validator);

        lenient().when(moradorRepository.existsByIdAndAtivoTrue(moradorA.id())).thenReturn(true);
        lenient().when(moradorRepository.existsByIdAndAtivoTrue(moradorB.id())).thenReturn(true);
        lenient().when(administradorRepository.existsByIdAndAtivoTrue(admin.id())).thenReturn(true);
        lenient().when(colaboradorRepository.existsByIdAndAtivoTrue(colaborador.id())).thenReturn(true);
    }

    // ------------------------------------------------------------------ helpers

    private AreaComum area(boolean ativa) {
        AreaComum area = new AreaComum();
        area.setId(UUID.randomUUID());
        area.setNome("Salão de festas");
        area.setAtiva(ativa);
        return area;
    }

    private Reserva reserva(StatusReserva status, AuthenticatedUser dono, AreaComum area,
                            LocalDate data, LocalTime inicio, LocalTime fim) {
        Morador morador = new Morador();
        morador.setId(dono.id());
        Reserva reserva = new Reserva();
        reserva.setId(UUID.randomUUID());
        reserva.setArea(area);
        reserva.setMorador(morador);
        reserva.setData(data);
        reserva.setHoraInicio(inicio);
        reserva.setHoraFim(fim);
        reserva.setStatus(status);
        reserva.setDataCriacao(LocalDateTime.now().minusDays(1));
        return reserva;
    }

    /** Reserva futura (daqui a 3 dias, 10h–12h) do morador A. */
    private Reserva reservaFutura(StatusReserva status) {
        return reserva(status, moradorA, area(true), LocalDate.now().plusDays(3),
                LocalTime.of(10, 0), LocalTime.of(12, 0));
    }

    /** Reserva cujo horário inicial começou há 1 minuto (já alcançado). */
    private Reserva reservaJaIniciada(StatusReserva status) {
        LocalDateTime inicio = LocalDateTime.now().minusMinutes(1);
        return reserva(status, moradorA, area(true), inicio.toLocalDate(), inicio.toLocalTime(), LocalTime.MAX);
    }

    private void stubFind(Reserva reserva) {
        when(reservaRepository.findById(reserva.getId())).thenReturn(Optional.of(reserva));
    }

    private void stubSaveEcho() {
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private void stubAprovacaoSemConflito(Reserva reserva) {
        when(areaComumRepository.buscarComLockParaDecisao(reserva.getArea().getId()))
                .thenReturn(Optional.of(reserva.getArea()));
        when(reservaRepository.buscarAprovadasConflitantesExcluindo(
                reserva.getArea().getId(), reserva.getData(),
                reserva.getHoraInicio(), reserva.getHoraFim(), reserva.getId()))
                .thenReturn(List.of());
    }

    private void stubSolicitacaoValida(AreaComum area, AuthenticatedUser morador) {
        when(areaComumRepository.findById(area.getId())).thenReturn(Optional.of(area));
        when(moradorRepository.findById(morador.id())).thenReturn(Optional.of(new Morador()));
    }

    private static LocalDate amanha() {
        return LocalDate.now().plusDays(1);
    }

    // ================================================================= CRIAÇÃO — positivos

    @Test
    void solicitarReservaDevePreencherDataCriacaoSemDataDecisao() {
        AreaComum area = area(true);
        stubSolicitacaoValida(area, moradorA);
        stubSaveEcho();

        Reserva criada = reservaService.solicitarReserva(
                moradorA, area.getId(), LocalDate.now().plusDays(2), LocalTime.of(14, 0), LocalTime.of(16, 0));

        assertEquals(StatusReserva.SOLICITADA, criada.getStatus());
        assertNotNull(criada.getDataCriacao());
        assertNull(criada.getDataDecisao());
        assertNull(criada.getMotivoNegacao());
        assertSame(area, criada.getArea());
    }

    @Test
    void solicitarReservaDeveGravarDadosInformadosNaReserva() {
        AreaComum area = area(true);
        stubSolicitacaoValida(area, moradorA);
        stubSaveEcho();
        LocalDate data = LocalDate.now().plusDays(5);

        Reserva criada = reservaService.solicitarReserva(
                moradorA, area.getId(), data, LocalTime.of(8, 30), LocalTime.of(9, 45));

        assertEquals(data, criada.getData());
        assertEquals(LocalTime.of(8, 30), criada.getHoraInicio());
        assertEquals(LocalTime.of(9, 45), criada.getHoraFim());
    }

    @Test
    void solicitarReservaComHorarioSobrepostoAOutraSolicitadaDeveSerPermitida() {
        // RN-01-05: várias SOLICITADA conflitantes podem coexistir; conflito só é checado na aprovação.
        AreaComum area = area(true);
        stubSolicitacaoValida(area, moradorA);
        stubSaveEcho();

        Reserva primeira = reservaService.solicitarReserva(
                moradorA, area.getId(), amanha(), LocalTime.of(10, 0), LocalTime.of(12, 0));
        Reserva segunda = reservaService.solicitarReserva(
                moradorA, area.getId(), amanha(), LocalTime.of(11, 0), LocalTime.of(13, 0));

        assertEquals(StatusReserva.SOLICITADA, primeira.getStatus());
        assertEquals(StatusReserva.SOLICITADA, segunda.getStatus());
        verify(reservaRepository, never()).buscarAprovadasConflitantes(any(), any(), any(), any());
        verify(areaComumRepository, never()).buscarComLockParaDecisao(any());
    }

    @Test
    void solicitarReservaNaMesmaDataEmAreasDiferentesComHorariosSobrepostosDeveSerPermitida() {
        AreaComum churrasqueira = area(true);
        AreaComum salao = area(true);
        stubSolicitacaoValida(churrasqueira, moradorA);
        when(areaComumRepository.findById(salao.getId())).thenReturn(Optional.of(salao));
        stubSaveEcho();

        Reserva r1 = reservaService.solicitarReserva(
                moradorA, churrasqueira.getId(), amanha(), LocalTime.of(10, 0), LocalTime.of(12, 0));
        Reserva r2 = reservaService.solicitarReserva(
                moradorA, salao.getId(), amanha(), LocalTime.of(10, 0), LocalTime.of(12, 0));

        assertEquals(StatusReserva.SOLICITADA, r1.getStatus());
        assertEquals(StatusReserva.SOLICITADA, r2.getStatus());
        assertSame(churrasqueira, r1.getArea());
        assertSame(salao, r2.getArea());
    }

    // ================================================================= CRIAÇÃO — negativos

    @Test
    void solicitarReservaSemAreaDeveLancarRegraDeNegocio() {
        assertThrows(BusinessRuleException.class, () -> reservaService.solicitarReserva(
                moradorA, null, LocalDate.now().plusDays(2), LocalTime.of(14, 0), LocalTime.of(16, 0)));

        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void solicitarReservaComAreaInexistenteDeveLancarNaoEncontrado() {
        UUID areaId = UUID.randomUUID();
        when(areaComumRepository.findById(areaId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reservaService.solicitarReserva(
                moradorA, areaId, amanha(), LocalTime.of(10, 0), LocalTime.of(11, 0)));

        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void solicitarReservaEmAreaDesativadaDeveSerRecusada() {
        // RN-01-01: área retirada/desativada não aceita novas solicitações.
        AreaComum desativada = area(false);
        when(areaComumRepository.findById(desativada.getId())).thenReturn(Optional.of(desativada));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> reservaService.solicitarReserva(
                moradorA, desativada.getId(), amanha(), LocalTime.of(10, 0), LocalTime.of(11, 0)));

        assertTrue(ex.getMessage().contains("não disponível"));
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void solicitarReservaEmAreaComAtivaNulaDeveSerRecusada() {
        AreaComum semFlag = area(true);
        semFlag.setAtiva(null);
        when(areaComumRepository.findById(semFlag.getId())).thenReturn(Optional.of(semFlag));

        assertThrows(BusinessRuleException.class, () -> reservaService.solicitarReserva(
                moradorA, semFlag.getId(), amanha(), LocalTime.of(10, 0), LocalTime.of(11, 0)));
    }

    @Test
    void solicitarReservaSemDataOuHorariosDeveSerRecusada() {
        AreaComum area = area(true);
        when(areaComumRepository.findById(area.getId())).thenReturn(Optional.of(area));

        assertAll(
                () -> assertThrows(BusinessRuleException.class, () -> reservaService.solicitarReserva(
                        moradorA, area.getId(), null, LocalTime.of(10, 0), LocalTime.of(11, 0))),
                () -> assertThrows(BusinessRuleException.class, () -> reservaService.solicitarReserva(
                        moradorA, area.getId(), amanha(), null, LocalTime.of(11, 0))),
                () -> assertThrows(BusinessRuleException.class, () -> reservaService.solicitarReserva(
                        moradorA, area.getId(), amanha(), LocalTime.of(10, 0), null))
        );
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void solicitarReservaComFimIgualAoInicioDeveSerRecusada() {
        AreaComum area = area(true);
        when(areaComumRepository.findById(area.getId())).thenReturn(Optional.of(area));

        assertThrows(BusinessRuleException.class, () -> reservaService.solicitarReserva(
                moradorA, area.getId(), amanha(), LocalTime.of(10, 0), LocalTime.of(10, 0)));

        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void solicitarReservaComFimAnteriorAoInicioDeveSerRecusada() {
        AreaComum area = area(true);
        when(areaComumRepository.findById(area.getId())).thenReturn(Optional.of(area));

        assertThrows(BusinessRuleException.class, () -> reservaService.solicitarReserva(
                moradorA, area.getId(), amanha(), LocalTime.of(12, 0), LocalTime.of(10, 0)));

        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void solicitarReservaComDataNoPassadoDeveSerRecusada() {
        AreaComum area = area(true);
        when(areaComumRepository.findById(area.getId())).thenReturn(Optional.of(area));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> reservaService.solicitarReserva(
                moradorA, area.getId(), LocalDate.now().minusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 0)));

        assertTrue(ex.getMessage().contains("futuro"));
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void solicitarReservaComHorarioInicialJaAlcancadoHojeDeveSerRecusada() {
        AreaComum area = area(true);
        when(areaComumRepository.findById(area.getId())).thenReturn(Optional.of(area));
        LocalDateTime inicio = LocalDateTime.now().minusSeconds(30);

        assertThrows(BusinessRuleException.class, () -> reservaService.solicitarReserva(
                moradorA, area.getId(), inicio.toLocalDate(), inicio.toLocalTime(), LocalTime.MAX));

        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void solicitarReservaComHorarioInicialLogoAdianteDeveSerAceita() {
        AreaComum area = area(true);
        stubSolicitacaoValida(area, moradorA);
        stubSaveEcho();
        LocalDateTime inicio = LocalDateTime.now().plusMinutes(5);

        Reserva criada = reservaService.solicitarReserva(
                moradorA, area.getId(), inicio.toLocalDate(), inicio.toLocalTime(), LocalTime.MAX);

        assertEquals(StatusReserva.SOLICITADA, criada.getStatus());
    }

    @Test
    void solicitarReservaComMoradorInexistenteNoBancoDeveLancarNaoEncontrado() {
        AreaComum area = area(true);
        when(areaComumRepository.findById(area.getId())).thenReturn(Optional.of(area));
        when(moradorRepository.findById(moradorA.id())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reservaService.solicitarReserva(
                moradorA, area.getId(), amanha(), LocalTime.of(10, 0), LocalTime.of(11, 0)));

        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    // ================================================================= CRIAÇÃO — autorização

    @Test
    void solicitarReservaPorAdministradorOuColaboradorDeveSerNegada() {
        UUID areaId = UUID.randomUUID();

        assertAll(
                () -> assertThrows(UnauthorizedOperationException.class, () -> reservaService.solicitarReserva(
                        admin, areaId, amanha(), LocalTime.of(10, 0), LocalTime.of(11, 0))),
                () -> assertThrows(UnauthorizedOperationException.class, () -> reservaService.solicitarReserva(
                        colaborador, areaId, amanha(), LocalTime.of(10, 0), LocalTime.of(11, 0)))
        );
        verifyNoInteractions(areaComumRepository);
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void solicitarReservaPorMoradorInativoOuSemPerfilDeveSerNegada() {
        AuthenticatedUser inativo = new AuthenticatedUser(UUID.randomUUID(), "x@cond.local", "ROLE_MORADOR");
        when(moradorRepository.existsByIdAndAtivoTrue(inativo.id())).thenReturn(false);
        AuthenticatedUser semRole = new AuthenticatedUser(moradorA.id(), "a@cond.local", null);

        assertAll(
                () -> assertThrows(UnauthorizedOperationException.class, () -> reservaService.solicitarReserva(
                        inativo, UUID.randomUUID(), amanha(), LocalTime.of(10, 0), LocalTime.of(11, 0))),
                () -> assertThrows(UnauthorizedOperationException.class, () -> reservaService.solicitarReserva(
                        semRole, UUID.randomUUID(), amanha(), LocalTime.of(10, 0), LocalTime.of(11, 0))),
                () -> assertThrows(UnauthorizedOperationException.class, () -> reservaService.solicitarReserva(
                        null, UUID.randomUUID(), amanha(), LocalTime.of(10, 0), LocalTime.of(11, 0)))
        );
        verifyNoInteractions(areaComumRepository);
    }

    // ================================================================= CONSULTA

    @Test
    void listarAreasDisponiveisDeveRetornarSomenteAreasAtivasParaMorador() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<AreaComum> page = new PageImpl<>(List.of(area(true), area(true)), pageRequest, 2);
        when(areaComumRepository.findByAtivaTrue(pageRequest)).thenReturn(page);

        PageResult<AreaComum> resultado = reservaService.listarAreasDisponiveis(moradorA, pageRequest);

        assertEquals(2, resultado.content().size());
        assertEquals(2, resultado.totalElements());
        verify(areaComumRepository).findByAtivaTrue(pageRequest);
        verify(areaComumRepository, never()).findAll(any(PageRequest.class));
    }

    @Test
    void listarAreasDisponiveisPorAdministradorOuColaboradorDeveSerNegado() {
        PageRequest pageRequest = PageRequest.of(0, 10);

        assertAll(
                () -> assertThrows(UnauthorizedOperationException.class,
                        () -> reservaService.listarAreasDisponiveis(admin, pageRequest)),
                () -> assertThrows(UnauthorizedOperationException.class,
                        () -> reservaService.listarAreasDisponiveis(colaborador, pageRequest))
        );
        verifyNoInteractions(areaComumRepository);
    }

    @Test
    void consultarDisponibilidadeDeveBuscarDiaInteiroComAprovadasDaArea() {
        UUID areaId = UUID.randomUUID();
        LocalDate data = amanha();
        List<Reserva> aprovadas = List.of(new Reserva());
        when(reservaRepository.buscarAprovadasConflitantes(areaId, data, LocalTime.MIN, LocalTime.MAX))
                .thenReturn(aprovadas);

        List<Reserva> resultado = reservaService.consultarDisponibilidade(moradorA, areaId, data);

        assertSame(aprovadas, resultado);
    }

    @Test
    void consultarDisponibilidadePorAdministradorOuColaboradorDeveSerNegada() {
        assertAll(
                () -> assertThrows(UnauthorizedOperationException.class,
                        () -> reservaService.consultarDisponibilidade(admin, UUID.randomUUID(), amanha())),
                () -> assertThrows(UnauthorizedOperationException.class,
                        () -> reservaService.consultarDisponibilidade(colaborador, UUID.randomUUID(), amanha()))
        );
        verifyNoInteractions(reservaRepository);
    }

    @Test
    void listarMinhasReservasDeveFiltrarSemprePeloIdDoMoradorAutenticado() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Reserva> paginaDeA = new PageImpl<>(List.of(reservaFutura(StatusReserva.SOLICITADA)), pageRequest, 1);
        Page<Reserva> paginaDeB = new PageImpl<>(List.of(), pageRequest, 0);
        when(reservaRepository.buscarParaMorador(moradorA.id(), null, null, pageRequest)).thenReturn(paginaDeA);
        when(reservaRepository.buscarParaMorador(moradorB.id(), null, null, pageRequest)).thenReturn(paginaDeB);

        PageResult<Reserva> doA = reservaService.listarMinhasReservas(moradorA, null, null, pageRequest);
        PageResult<Reserva> doB = reservaService.listarMinhasReservas(moradorB, null, null, pageRequest);

        // RN-01-10 / RNF-03: cada morador só enxerga o que o repositório devolve para o PRÓPRIO id.
        assertEquals(1, doA.content().size());
        assertTrue(doB.content().isEmpty());
        verify(reservaRepository).buscarParaMorador(moradorA.id(), null, null, pageRequest);
        verify(reservaRepository).buscarParaMorador(moradorB.id(), null, null, pageRequest);
        verify(reservaRepository, never()).buscaraParaAdmin(any(), any(), any());
    }

    @Test
    void listarMinhasReservasDeveRepassarFiltrosDeAreaEData() {
        PageRequest pageRequest = PageRequest.of(0, 5);
        UUID areaId = UUID.randomUUID();
        LocalDate data = amanha();
        when(reservaRepository.buscarParaMorador(moradorA.id(), areaId, data, pageRequest))
                .thenReturn(new PageImpl<>(List.of(), pageRequest, 0));

        reservaService.listarMinhasReservas(moradorA, areaId, data, pageRequest);

        verify(reservaRepository).buscarParaMorador(moradorA.id(), areaId, data, pageRequest);
    }

    @Test
    void listarMinhasReservasPorAdministradorOuColaboradorDeveSerNegada() {
        PageRequest pageRequest = PageRequest.of(0, 10);

        assertAll(
                () -> assertThrows(UnauthorizedOperationException.class,
                        () -> reservaService.listarMinhasReservas(admin, null, null, pageRequest)),
                () -> assertThrows(UnauthorizedOperationException.class,
                        () -> reservaService.listarMinhasReservas(colaborador, null, null, pageRequest))
        );
        verifyNoInteractions(reservaRepository);
    }

    @Test
    void listarTodasReservasDeveSerPermitidoAoAdministrador() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Reserva> page = new PageImpl<>(
                List.of(reservaFutura(StatusReserva.SOLICITADA), reservaFutura(StatusReserva.APROVADA)),
                pageRequest, 2);
        when(reservaRepository.buscaraParaAdmin(null, null, pageRequest)).thenReturn(page);

        PageResult<Reserva> resultado = reservaService.listarTodasReservas(admin, null, null, pageRequest);

        assertEquals(2, resultado.content().size());
    }

    @Test
    void listarTodasReservasPorMoradorOuColaboradorDeveSerNegado() {
        PageRequest pageRequest = PageRequest.of(0, 10);

        assertAll(
                () -> assertThrows(UnauthorizedOperationException.class,
                        () -> reservaService.listarTodasReservas(moradorA, null, null, pageRequest)),
                () -> assertThrows(UnauthorizedOperationException.class,
                        () -> reservaService.listarTodasReservas(colaborador, null, null, pageRequest))
        );
        verifyNoInteractions(reservaRepository);
    }

    // ================================================================= APROVAÇÃO — positivos

    @Test
    void aprovarReservaDeveMarcarAprovadaERegistrarDataDecisaoPreservandoDataCriacao() {
        Reserva reserva = reservaFutura(StatusReserva.SOLICITADA);
        LocalDateTime criacao = reserva.getDataCriacao();
        stubFind(reserva);
        stubAprovacaoSemConflito(reserva);
        stubSaveEcho();

        Reserva aprovada = reservaService.aprovarReserva(admin, reserva.getId());

        assertEquals(StatusReserva.APROVADA, aprovada.getStatus());
        assertNotNull(aprovada.getDataDecisao());
        assertEquals(criacao, aprovada.getDataCriacao());
        assertNull(aprovada.getMotivoNegacao());
    }

    @Test
    void aprovarReservaDeveTravarAAreaAntesDeVerificarConflitoEAntesDeGravar() {
        Reserva reserva = reservaFutura(StatusReserva.SOLICITADA);
        stubFind(reserva);
        stubAprovacaoSemConflito(reserva);
        stubSaveEcho();

        reservaService.aprovarReserva(admin, reserva.getId());

        InOrder ordem = inOrder(areaComumRepository, reservaRepository);
        ordem.verify(areaComumRepository).buscarComLockParaDecisao(reserva.getArea().getId());
        ordem.verify(reservaRepository).buscarAprovadasConflitantesExcluindo(
                reserva.getArea().getId(), reserva.getData(),
                reserva.getHoraInicio(), reserva.getHoraFim(), reserva.getId());
        ordem.verify(reservaRepository).save(reserva);
    }

    // ================================================================= APROVAÇÃO — negativos

    @Test
    void aprovarReservaComConflitoDeveSerRecusadaSemAlterarNada() {
        Reserva reserva = reservaFutura(StatusReserva.SOLICITADA);
        stubFind(reserva);
        when(areaComumRepository.buscarComLockParaDecisao(reserva.getArea().getId()))
                .thenReturn(Optional.of(reserva.getArea()));
        when(reservaRepository.buscarAprovadasConflitantesExcluindo(
                reserva.getArea().getId(), reserva.getData(),
                reserva.getHoraInicio(), reserva.getHoraFim(), reserva.getId()))
                .thenReturn(List.of(new Reserva()));

        assertThrows(BusinessRuleException.class, () -> reservaService.aprovarReserva(admin, reserva.getId()));

        assertEquals(StatusReserva.SOLICITADA, reserva.getStatus());
        assertNull(reserva.getDataDecisao());
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void aprovarReservaInexistenteDeveLancarNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(reservaRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reservaService.aprovarReserva(admin, id));

        verify(areaComumRepository, never()).buscarComLockParaDecisao(any());
    }

    @Test
    void aprovarReservaComAreaNaoEncontradaNoLockDeveLancarNaoEncontrado() {
        Reserva reserva = reservaFutura(StatusReserva.SOLICITADA);
        stubFind(reserva);
        when(areaComumRepository.buscarComLockParaDecisao(reserva.getArea().getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reservaService.aprovarReserva(admin, reserva.getId()));

        assertEquals(StatusReserva.SOLICITADA, reserva.getStatus());
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @ParameterizedTest
    @EnumSource(value = StatusReserva.class, names = {"APROVADA", "NEGADA", "CANCELADA"})
    void aprovarReservaForaDeSolicitadaDeveSerRecusadaSemTravarArea(StatusReserva status) {
        Reserva reserva = reservaFutura(status);
        stubFind(reserva);

        assertThrows(BusinessRuleException.class, () -> reservaService.aprovarReserva(admin, reserva.getId()));

        assertEquals(status, reserva.getStatus());
        verify(areaComumRepository, never()).buscarComLockParaDecisao(any());
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void aprovarReservaPorMoradorOuColaboradorDeveSerNegada() {
        UUID id = UUID.randomUUID();

        assertAll(
                () -> assertThrows(UnauthorizedOperationException.class, () -> reservaService.aprovarReserva(moradorA, id)),
                () -> assertThrows(UnauthorizedOperationException.class, () -> reservaService.aprovarReserva(colaborador, id))
        );
        verifyNoInteractions(reservaRepository);
        verifyNoInteractions(areaComumRepository);
    }

    @Test
    void aprovarReservaPorAdministradorInativoDeveSerNegada() {
        AuthenticatedUser adminInativo = new AuthenticatedUser(UUID.randomUUID(), "i@cond.local", "ROLE_ADMINISTRADOR");
        when(administradorRepository.existsByIdAndAtivoTrue(adminInativo.id())).thenReturn(false);

        assertThrows(UnauthorizedOperationException.class,
                () -> reservaService.aprovarReserva(adminInativo, UUID.randomUUID()));
        verifyNoInteractions(reservaRepository);
    }

    // ================================================================= CONFLITO DE INTERVALOS (semântica via repositório em memória)

    @Test
    void aprovarReservaAdjacenteNaoDeveConflitar() {
        // 10:00–12:00 aprovada; 12:00–14:00 começa quando a outra termina => não há sobreposição.
        RepositorioEmMemoria repo = new RepositorioEmMemoria(true);
        AreaComum area = area(true);
        Reserva aprovada = repo.adicionar(reserva(StatusReserva.APROVADA, moradorA, area,
                amanha(), LocalTime.of(10, 0), LocalTime.of(12, 0)));
        Reserva adjacente = repo.adicionar(reserva(StatusReserva.SOLICITADA, moradorB, area,
                amanha(), LocalTime.of(12, 0), LocalTime.of(14, 0)));
        Reserva adjacenteAntes = repo.adicionar(reserva(StatusReserva.SOLICITADA, moradorB, area,
                amanha(), LocalTime.of(8, 0), LocalTime.of(10, 0)));
        ReservaService service = repo.novoServico();

        service.aprovarReserva(admin, adjacente.getId());
        service.aprovarReserva(admin, adjacenteAntes.getId());

        assertEquals(StatusReserva.APROVADA, repo.status(adjacente.getId()));
        assertEquals(StatusReserva.APROVADA, repo.status(adjacenteAntes.getId()));
        assertEquals(StatusReserva.APROVADA, repo.status(aprovada.getId()));
        assertEquals(0, repo.paresConflitantesAprovados());
    }

    @Test
    void aprovarReservaComSobreposicaoParcialOuTotalDeveSerRecusada() {
        RepositorioEmMemoria repo = new RepositorioEmMemoria(true);
        AreaComum area = area(true);
        repo.adicionar(reserva(StatusReserva.APROVADA, moradorA, area,
                amanha(), LocalTime.of(10, 0), LocalTime.of(12, 0)));
        Reserva parcialInicio = repo.adicionar(reserva(StatusReserva.SOLICITADA, moradorB, area,
                amanha(), LocalTime.of(9, 0), LocalTime.of(10, 30)));
        Reserva parcialFim = repo.adicionar(reserva(StatusReserva.SOLICITADA, moradorB, area,
                amanha(), LocalTime.of(11, 30), LocalTime.of(13, 0)));
        Reserva contida = repo.adicionar(reserva(StatusReserva.SOLICITADA, moradorB, area,
                amanha(), LocalTime.of(10, 30), LocalTime.of(11, 0)));
        Reserva envolvente = repo.adicionar(reserva(StatusReserva.SOLICITADA, moradorB, area,
                amanha(), LocalTime.of(8, 0), LocalTime.of(14, 0)));
        Reserva identica = repo.adicionar(reserva(StatusReserva.SOLICITADA, moradorB, area,
                amanha(), LocalTime.of(10, 0), LocalTime.of(12, 0)));
        ReservaService service = repo.novoServico();

        for (Reserva candidata : List.of(parcialInicio, parcialFim, contida, envolvente, identica)) {
            assertThrows(BusinessRuleException.class, () -> service.aprovarReserva(admin, candidata.getId()));
            assertEquals(StatusReserva.SOLICITADA, repo.status(candidata.getId()));
        }
        assertEquals(0, repo.paresConflitantesAprovados());
    }

    @Test
    void aprovarReservaEmOutraDataNaoDeveConflitar() {
        RepositorioEmMemoria repo = new RepositorioEmMemoria(true);
        AreaComum area = area(true);
        repo.adicionar(reserva(StatusReserva.APROVADA, moradorA, area,
                amanha(), LocalTime.of(10, 0), LocalTime.of(12, 0)));
        Reserva outroDia = repo.adicionar(reserva(StatusReserva.SOLICITADA, moradorB, area,
                amanha().plusDays(1), LocalTime.of(10, 0), LocalTime.of(12, 0)));

        repo.novoServico().aprovarReserva(admin, outroDia.getId());

        assertEquals(StatusReserva.APROVADA, repo.status(outroDia.getId()));
    }

    @Test
    void aprovarReservaEmAreasDiferentesComHorariosSobrepostosNaoDeveConflitar() {
        // CA-01-14: áreas diferentes nunca conflitam entre si.
        RepositorioEmMemoria repo = new RepositorioEmMemoria(true);
        AreaComum salao = area(true);
        AreaComum churrasqueira = area(true);
        repo.adicionar(reserva(StatusReserva.APROVADA, moradorA, salao,
                amanha(), LocalTime.of(10, 0), LocalTime.of(12, 0)));
        Reserva outraArea = repo.adicionar(reserva(StatusReserva.SOLICITADA, moradorB, churrasqueira,
                amanha(), LocalTime.of(10, 0), LocalTime.of(12, 0)));

        repo.novoServico().aprovarReserva(admin, outraArea.getId());

        assertEquals(StatusReserva.APROVADA, repo.status(outraArea.getId()));
    }

    @Test
    void apenasReservasAprovadasOcupamDisponibilidade() {
        RepositorioEmMemoria repo = new RepositorioEmMemoria(true);
        AreaComum area = area(true);
        for (StatusReserva naoOcupa : List.of(StatusReserva.SOLICITADA, StatusReserva.NEGADA, StatusReserva.CANCELADA)) {
            repo.adicionar(reserva(naoOcupa, moradorA, area, amanha(), LocalTime.of(10, 0), LocalTime.of(12, 0)));
        }
        Reserva candidata = repo.adicionar(reserva(StatusReserva.SOLICITADA, moradorB, area,
                amanha(), LocalTime.of(10, 0), LocalTime.of(12, 0)));

        repo.novoServico().aprovarReserva(admin, candidata.getId());

        assertEquals(StatusReserva.APROVADA, repo.status(candidata.getId()));
    }

    @Test
    void reservaCanceladaLiberaOHorarioParaNovaAprovacao() {
        RepositorioEmMemoria repo = new RepositorioEmMemoria(true);
        AreaComum area = area(true);
        Reserva primeira = repo.adicionar(reserva(StatusReserva.APROVADA, moradorA, area,
                amanha(), LocalTime.of(10, 0), LocalTime.of(12, 0)));
        Reserva segunda = repo.adicionar(reserva(StatusReserva.SOLICITADA, moradorB, area,
                amanha(), LocalTime.of(11, 0), LocalTime.of(13, 0)));
        ReservaService service = repo.novoServico();

        assertThrows(BusinessRuleException.class, () -> service.aprovarReserva(admin, segunda.getId()));
        service.cancelarReservaMorador(moradorA, primeira.getId());
        service.aprovarReserva(admin, segunda.getId());

        assertEquals(StatusReserva.CANCELADA, repo.status(primeira.getId()));
        assertEquals(StatusReserva.APROVADA, repo.status(segunda.getId()));
        assertEquals(0, repo.paresConflitantesAprovados());
    }

    // ================================================================= NEGAÇÃO

    @Test
    void negarReservaDeveMarcarComoNegadaEGuardarMotivoAparado() {
        Reserva reserva = reservaFutura(StatusReserva.SOLICITADA);
        LocalDateTime criacao = reserva.getDataCriacao();
        stubFind(reserva);
        stubSaveEcho();

        Reserva negada = reservaService.negarReserva(admin, reserva.getId(), "  Área em manutenção  ");

        assertEquals(StatusReserva.NEGADA, negada.getStatus());
        assertEquals("Área em manutenção", negada.getMotivoNegacao());
        assertNotNull(negada.getDataDecisao());
        assertEquals(criacao, negada.getDataCriacao());
    }

    @Test
    void negarReservaNaoDeveTravarAreaNemVerificarConflito() {
        Reserva reserva = reservaFutura(StatusReserva.SOLICITADA);
        stubFind(reserva);
        stubSaveEcho();

        reservaService.negarReserva(admin, reserva.getId(), "Motivo");

        verify(areaComumRepository, never()).buscarComLockParaDecisao(any());
        verify(reservaRepository, never()).buscarAprovadasConflitantesExcluindo(any(), any(), any(), any(), any());
    }

    @Test
    void negarReservaComMotivoNoLimiteMaximoDeveSerAceita() {
        Reserva reserva = reservaFutura(StatusReserva.SOLICITADA);
        stubFind(reserva);
        stubSaveEcho();
        String motivo = "m".repeat(ValidationLimits.RESERVA_MOTIVO_NEGACAO_MAX_LENGTH);

        Reserva negada = reservaService.negarReserva(admin, reserva.getId(), motivo);

        assertEquals(motivo, negada.getMotivoNegacao());
    }

    @Test
    void negarReservaSemMotivoNaoDeveAlterarStatus() {
        Reserva reserva = reservaFutura(StatusReserva.SOLICITADA);
        stubFind(reserva);

        assertAll(
                () -> assertThrows(BusinessRuleException.class,
                        () -> reservaService.negarReserva(admin, reserva.getId(), null)),
                () -> assertThrows(BusinessRuleException.class,
                        () -> reservaService.negarReserva(admin, reserva.getId(), "")),
                () -> assertThrows(BusinessRuleException.class,
                        () -> reservaService.negarReserva(admin, reserva.getId(), "   "))
        );

        assertEquals(StatusReserva.SOLICITADA, reserva.getStatus());
        assertNull(reserva.getMotivoNegacao());
        assertNull(reserva.getDataDecisao());
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void negarReservaComMotivoAcimaDoLimiteDeveSerRecusada() {
        Reserva reserva = reservaFutura(StatusReserva.SOLICITADA);
        stubFind(reserva);
        String motivo = "m".repeat(ValidationLimits.RESERVA_MOTIVO_NEGACAO_MAX_LENGTH + 1);

        assertThrows(BusinessRuleException.class, () -> reservaService.negarReserva(admin, reserva.getId(), motivo));

        assertEquals(StatusReserva.SOLICITADA, reserva.getStatus());
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void negarReservaInexistenteDeveLancarNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(reservaRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reservaService.negarReserva(admin, id, "Motivo"));
    }

    @ParameterizedTest
    @EnumSource(value = StatusReserva.class, names = {"APROVADA", "NEGADA", "CANCELADA"})
    void negarReservaForaDeSolicitadaDeveSerRecusada(StatusReserva status) {
        Reserva reserva = reservaFutura(status);
        stubFind(reserva);

        assertThrows(BusinessRuleException.class, () -> reservaService.negarReserva(admin, reserva.getId(), "Motivo"));

        assertEquals(status, reserva.getStatus());
        assertNull(reserva.getMotivoNegacao());
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void negarReservaPorMoradorOuColaboradorDeveSerNegada() {
        UUID id = UUID.randomUUID();

        assertAll(
                () -> assertThrows(UnauthorizedOperationException.class, () -> reservaService.negarReserva(moradorA, id, "x")),
                () -> assertThrows(UnauthorizedOperationException.class, () -> reservaService.negarReserva(colaborador, id, "x"))
        );
        verifyNoInteractions(reservaRepository);
    }

    // ================================================================= CANCELAMENTO — morador

    @ParameterizedTest
    @EnumSource(value = StatusReserva.class, names = {"SOLICITADA", "APROVADA"})
    void moradorDeveCancelarPropriaReservaAntesDoInicio(StatusReserva status) {
        Reserva reserva = reservaFutura(status);
        stubFind(reserva);
        stubSaveEcho();

        Reserva cancelada = reservaService.cancelarReservaMorador(moradorA, reserva.getId());

        assertEquals(StatusReserva.CANCELADA, cancelada.getStatus());
    }

    @Test
    void moradorNaoDeveCancelarReservaDeOutroMorador() {
        Reserva reservaDeA = reservaFutura(StatusReserva.APROVADA);
        stubFind(reservaDeA);

        assertThrows(UnauthorizedOperationException.class,
                () -> reservaService.cancelarReservaMorador(moradorB, reservaDeA.getId()));

        assertEquals(StatusReserva.APROVADA, reservaDeA.getStatus());
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void moradorNaoDeveCancelarAposOInicioDoHorarioReservado() {
        Reserva reserva = reservaJaIniciada(StatusReserva.APROVADA);
        stubFind(reserva);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> reservaService.cancelarReservaMorador(moradorA, reserva.getId()));

        assertTrue(ex.getMessage().contains("inicio"));
        assertEquals(StatusReserva.APROVADA, reserva.getStatus());
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void moradorNaoDeveCancelarReservaDeDiaAnterior() {
        Reserva reserva = reserva(StatusReserva.SOLICITADA, moradorA, area(true),
                LocalDate.now().minusDays(1), LocalTime.of(10, 0), LocalTime.of(12, 0));
        stubFind(reserva);

        assertThrows(BusinessRuleException.class, () -> reservaService.cancelarReservaMorador(moradorA, reserva.getId()));

        assertEquals(StatusReserva.SOLICITADA, reserva.getStatus());
    }

    @Test
    void moradorPodeCancelarFaltandoPoucosMinutosParaOInicio() {
        LocalDateTime inicio = LocalDateTime.now().plusMinutes(10);
        Reserva reserva = reserva(StatusReserva.APROVADA, moradorA, area(true),
                inicio.toLocalDate(), inicio.toLocalTime(), LocalTime.MAX);
        stubFind(reserva);
        stubSaveEcho();

        Reserva cancelada = reservaService.cancelarReservaMorador(moradorA, reserva.getId());

        assertEquals(StatusReserva.CANCELADA, cancelada.getStatus());
    }

    @ParameterizedTest
    @EnumSource(value = StatusReserva.class, names = {"NEGADA", "CANCELADA"})
    void moradorNaoDeveCancelarReservaEmEstadoTerminal(StatusReserva terminal) {
        Reserva reserva = reservaFutura(terminal);
        stubFind(reserva);

        assertThrows(BusinessRuleException.class, () -> reservaService.cancelarReservaMorador(moradorA, reserva.getId()));

        assertEquals(terminal, reserva.getStatus());
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @Test
    void cancelarReservaInexistenteDeveLancarNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(reservaRepository.findById(id)).thenReturn(Optional.empty());

        assertAll(
                () -> assertThrows(ResourceNotFoundException.class, () -> reservaService.cancelarReservaMorador(moradorA, id)),
                () -> assertThrows(ResourceNotFoundException.class, () -> reservaService.cancelarReserva(admin, id))
        );
    }

    @Test
    void cancelarReservaComoMoradorPorAdministradorOuColaboradorDeveSerNegado() {
        UUID id = UUID.randomUUID();

        assertAll(
                () -> assertThrows(UnauthorizedOperationException.class, () -> reservaService.cancelarReservaMorador(admin, id)),
                () -> assertThrows(UnauthorizedOperationException.class, () -> reservaService.cancelarReservaMorador(colaborador, id))
        );
        verifyNoInteractions(reservaRepository);
    }

    @Test
    void cancelamentoNaoDeveAlterarDataCriacaoNemMotivoNemDecisao() {
        Reserva reserva = reservaFutura(StatusReserva.SOLICITADA);
        LocalDateTime criacao = reserva.getDataCriacao();
        stubFind(reserva);
        stubSaveEcho();

        Reserva cancelada = reservaService.cancelarReservaMorador(moradorA, reserva.getId());

        assertEquals(criacao, cancelada.getDataCriacao());
        assertNull(cancelada.getDataDecisao());
        assertNull(cancelada.getMotivoNegacao());
    }

    // ================================================================= CANCELAMENTO — administrador

    @ParameterizedTest
    @EnumSource(value = StatusReserva.class, names = {"SOLICITADA", "APROVADA"})
    void administradorDeveCancelarReservaDeQualquerMoradorAntesDoInicio(StatusReserva status) {
        Reserva reservaDeA = reservaFutura(status);
        stubFind(reservaDeA);
        stubSaveEcho();

        Reserva cancelada = reservaService.cancelarReserva(admin, reservaDeA.getId());

        assertEquals(StatusReserva.CANCELADA, cancelada.getStatus());
    }

    @Test
    void administradorNaoDeveCancelarAposOInicioDoHorarioReservado() {
        Reserva reserva = reservaJaIniciada(StatusReserva.APROVADA);
        stubFind(reserva);

        assertThrows(BusinessRuleException.class, () -> reservaService.cancelarReserva(admin, reserva.getId()));

        assertEquals(StatusReserva.APROVADA, reserva.getStatus());
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    @ParameterizedTest
    @EnumSource(value = StatusReserva.class, names = {"NEGADA", "CANCELADA"})
    void administradorNaoDeveCancelarReservaEmEstadoTerminal(StatusReserva terminal) {
        Reserva reserva = reservaFutura(terminal);
        stubFind(reserva);

        assertThrows(BusinessRuleException.class, () -> reservaService.cancelarReserva(admin, reserva.getId()));

        assertEquals(terminal, reserva.getStatus());
    }

    @Test
    void cancelarReservaPorMoradorOuColaboradorNaViaAdministrativaDeveSerNegado() {
        UUID id = UUID.randomUUID();

        assertAll(
                () -> assertThrows(UnauthorizedOperationException.class, () -> reservaService.cancelarReserva(moradorA, id)),
                () -> assertThrows(UnauthorizedOperationException.class, () -> reservaService.cancelarReserva(colaborador, id))
        );
        verifyNoInteractions(reservaRepository);
    }

    // ================================================================= TRANSIÇÕES (matriz completa)

    @ParameterizedTest
    @EnumSource(StatusReserva.class)
    void matrizDeTransicoesAprovar(StatusReserva origem) {
        Reserva reserva = reservaFutura(origem);
        stubFind(reserva);
        if (origem == StatusReserva.SOLICITADA) {
            stubAprovacaoSemConflito(reserva);
            stubSaveEcho();
            assertEquals(StatusReserva.APROVADA, reservaService.aprovarReserva(admin, reserva.getId()).getStatus());
        } else {
            assertThrows(BusinessRuleException.class, () -> reservaService.aprovarReserva(admin, reserva.getId()));
            assertEquals(origem, reserva.getStatus());
        }
    }

    @ParameterizedTest
    @EnumSource(StatusReserva.class)
    void matrizDeTransicoesNegar(StatusReserva origem) {
        Reserva reserva = reservaFutura(origem);
        stubFind(reserva);
        if (origem == StatusReserva.SOLICITADA) {
            stubSaveEcho();
            assertEquals(StatusReserva.NEGADA, reservaService.negarReserva(admin, reserva.getId(), "x").getStatus());
        } else {
            assertThrows(BusinessRuleException.class, () -> reservaService.negarReserva(admin, reserva.getId(), "x"));
            assertEquals(origem, reserva.getStatus());
        }
    }

    @ParameterizedTest
    @EnumSource(StatusReserva.class)
    void matrizDeTransicoesCancelar(StatusReserva origem) {
        Reserva reserva = reservaFutura(origem);
        stubFind(reserva);
        if (origem == StatusReserva.SOLICITADA || origem == StatusReserva.APROVADA) {
            stubSaveEcho();
            assertEquals(StatusReserva.CANCELADA, reservaService.cancelarReserva(admin, reserva.getId()).getStatus());
        } else {
            assertThrows(BusinessRuleException.class, () -> reservaService.cancelarReserva(admin, reserva.getId()));
            assertEquals(origem, reserva.getStatus());
        }
    }

    @Test
    void estadosTerminaisNaoPodemSerReabertosPorNenhumaAcao() {
        for (StatusReserva terminal : List.of(StatusReserva.NEGADA, StatusReserva.CANCELADA)) {
            Reserva reserva = reservaFutura(terminal);
            stubFind(reserva);

            assertAll(
                    () -> assertThrows(BusinessRuleException.class, () -> reservaService.aprovarReserva(admin, reserva.getId())),
                    () -> assertThrows(BusinessRuleException.class, () -> reservaService.negarReserva(admin, reserva.getId(), "x")),
                    () -> assertThrows(BusinessRuleException.class, () -> reservaService.cancelarReserva(admin, reserva.getId())),
                    () -> assertThrows(BusinessRuleException.class, () -> reservaService.cancelarReservaMorador(moradorA, reserva.getId()))
            );
            assertEquals(terminal, reserva.getStatus());
        }
        verify(reservaRepository, never()).save(any(Reserva.class));
    }

    // ================================================================= HISTÓRICO (datas e motivo registrados)

    @Test
    void historicoDeAprovacaoRegistraCriacaoEDecisaoEmOrdemCronologica() {
        AreaComum area = area(true);
        stubSolicitacaoValida(area, moradorA);
        stubSaveEcho();
        Reserva solicitada = reservaService.solicitarReserva(
                moradorA, area.getId(), LocalDate.now().plusDays(2), LocalTime.of(10, 0), LocalTime.of(12, 0));
        solicitada.setId(UUID.randomUUID());
        assertNull(solicitada.getDataDecisao());
        LocalDateTime criacao = solicitada.getDataCriacao();

        stubFind(solicitada);
        stubAprovacaoSemConflito(solicitada);
        Reserva aprovada = reservaService.aprovarReserva(admin, solicitada.getId());

        assertEquals(criacao, aprovada.getDataCriacao());
        assertFalse(aprovada.getDataDecisao().isBefore(aprovada.getDataCriacao()));
    }

    @Test
    void historicoDeNegacaoRegistraMotivoEDataDeDecisao() {
        Reserva reserva = reservaFutura(StatusReserva.SOLICITADA);
        stubFind(reserva);
        stubSaveEcho();
        LocalDateTime antes = LocalDateTime.now().minusSeconds(1);

        Reserva negada = reservaService.negarReserva(admin, reserva.getId(), "Evento do condomínio");

        assertEquals("Evento do condomínio", negada.getMotivoNegacao());
        assertTrue(negada.getDataDecisao().isAfter(antes));
    }

    // ================================================================= ÁREA RETIRADA/DESATIVADA COM RESERVAS EXISTENTES

    @Test
    void reservasExistentesDeAreaDesativadaContinuamCancelaveis() {
        // Desativar a área não apaga reservas (RN-01-01): o ciclo de vida das já existentes segue valendo.
        AreaComum desativada = area(false);
        Reserva existente = reserva(StatusReserva.APROVADA, moradorA, desativada,
                LocalDate.now().plusDays(4), LocalTime.of(10, 0), LocalTime.of(12, 0));
        stubFind(existente);
        stubSaveEcho();

        Reserva cancelada = reservaService.cancelarReservaMorador(moradorA, existente.getId());

        assertEquals(StatusReserva.CANCELADA, cancelada.getStatus());
        verify(reservaRepository, never()).delete(any(Reserva.class));
        verify(reservaRepository, never()).deleteById(any());
    }

    @Test
    void reservasExistentesDeAreaDesativadaContinuamVisiveisAoAdministrador() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        AreaComum desativada = area(false);
        Reserva existente = reserva(StatusReserva.SOLICITADA, moradorA, desativada,
                LocalDate.now().plusDays(4), LocalTime.of(10, 0), LocalTime.of(12, 0));
        when(reservaRepository.buscaraParaAdmin(desativada.getId(), null, pageRequest))
                .thenReturn(new PageImpl<>(List.of(existente), pageRequest, 1));

        PageResult<Reserva> resultado = reservaService.listarTodasReservas(admin, desativada.getId(), null, pageRequest);

        assertEquals(1, resultado.content().size());
        assertSame(desativada, resultado.content().get(0).getArea());
    }

    @Test
    void listagemDeAreasDisponiveisNaoDeveIncluirAreasDesativadas() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(areaComumRepository.findByAtivaTrue(pageRequest)).thenReturn(new PageImpl<>(List.of(), pageRequest, 0));

        PageResult<AreaComum> resultado = reservaService.listarAreasDisponiveis(moradorA, pageRequest);

        assertTrue(resultado.content().isEmpty());
        verify(areaComumRepository).findByAtivaTrue(eq(pageRequest));
        verify(areaComumRepository, never()).findAll(any(PageRequest.class));
        verify(areaComumRepository, never()).findAll();
    }

    // ================================================================= CONCORRÊNCIA — duas aprovações simultâneas

    /**
     * Evidência reproduzível (nível unitário) de que duas aprovações concorrentes de reservas conflitantes
     * não produzem duas reservas APROVADAS conflitantes.
     *
     * O repositório em memória reproduz a semântica relevante: cada transação lê um snapshot (cópia) da reserva,
     * o lock da área é exclusivo e só é liberado quando a "transação" termina (commit/rollback), e a consulta de
     * conflito segue a mesma regra de {@code ReservaRepository.buscarAprovadasConflitantesExcluindo}.
     *
     * LIMITAÇÃO: isto valida o PROTOCOLO do serviço (lock -> checar -> gravar) com um lock simulado. A exclusão
     * mútua real é do banco (SELECT ... FOR UPDATE) e só um teste de integração contra PostgreSQL a comprova.
     */
    @Test
    void duasAprovacoesConcorrentesDeReservasConflitantesProduzemApenasUmaAprovada() throws Exception {
        final int repeticoes = 25;
        for (int i = 0; i < repeticoes; i++) {
            RepositorioEmMemoria repo = new RepositorioEmMemoria(true);
            AreaComum area = area(true);
            Reserva r1 = repo.adicionar(reserva(StatusReserva.SOLICITADA, moradorA, area,
                    amanha(), LocalTime.of(10, 0), LocalTime.of(12, 0)));
            Reserva r2 = repo.adicionar(reserva(StatusReserva.SOLICITADA, moradorB, area,
                    amanha(), LocalTime.of(11, 0), LocalTime.of(13, 0)));

            ResultadoConcorrente resultado = repo.aprovarEmParalelo(r1.getId(), r2.getId());

            assertEquals(1, resultado.sucessos, "exatamente uma aprovação deve vencer (rodada " + i + ")");
            assertEquals(1, resultado.recusasDeNegocio, "a outra deve ser recusada por conflito (rodada " + i + ")");
            assertEquals(0, resultado.outrasFalhas, "nenhuma falha inesperada (rodada " + i + ")");
            assertEquals(1, repo.contar(StatusReserva.APROVADA));
            assertEquals(1, repo.contar(StatusReserva.SOLICITADA));
            assertEquals(0, repo.paresConflitantesAprovados());
        }
    }

    @Test
    void controleNegativoSemLockDuasAprovacoesConcorrentesGeramConflito() throws Exception {
        // Prova que o teste acima É sensível ao lock: com o lock desligado e as duas threads forçadas a
        // interlaçar (ambas conferem antes de qualquer uma gravar), o conflito acontece. Se este teste
        // passar, a evidência com lock não é falso-positivo.
        RepositorioEmMemoria repo = new RepositorioEmMemoria(false);
        AreaComum area = area(true);
        Reserva r1 = repo.adicionar(reserva(StatusReserva.SOLICITADA, moradorA, area,
                amanha(), LocalTime.of(10, 0), LocalTime.of(12, 0)));
        Reserva r2 = repo.adicionar(reserva(StatusReserva.SOLICITADA, moradorB, area,
                amanha(), LocalTime.of(11, 0), LocalTime.of(13, 0)));

        ResultadoConcorrente resultado = repo.aprovarEmParalelo(r1.getId(), r2.getId());

        assertEquals(2, resultado.sucessos);
        assertEquals(2, repo.contar(StatusReserva.APROVADA));
        assertEquals(1, repo.paresConflitantesAprovados(),
                "sem lock, as duas aprovações passam e geram o conflito que o lock existe para evitar");
    }

    @Test
    void aprovacoesConcorrentesEmAreasDiferentesNaoDevemSeBloquearNemConflitar() throws Exception {
        RepositorioEmMemoria repo = new RepositorioEmMemoria(true);
        Reserva noSalao = repo.adicionar(reserva(StatusReserva.SOLICITADA, moradorA, area(true),
                amanha(), LocalTime.of(10, 0), LocalTime.of(12, 0)));
        Reserva naChurrasqueira = repo.adicionar(reserva(StatusReserva.SOLICITADA, moradorB, area(true),
                amanha(), LocalTime.of(10, 0), LocalTime.of(12, 0)));

        ResultadoConcorrente resultado = repo.aprovarEmParalelo(noSalao.getId(), naChurrasqueira.getId());

        assertEquals(2, resultado.sucessos);
        assertEquals(2, repo.contar(StatusReserva.APROVADA));
        assertEquals(0, repo.paresConflitantesAprovados());
    }

    @Test
    void aprovacoesConcorrentesDeReservasAdjacentesNaMesmaAreaDevemAmbasPassar() throws Exception {
        RepositorioEmMemoria repo = new RepositorioEmMemoria(true);
        AreaComum area = area(true);
        Reserva manha = repo.adicionar(reserva(StatusReserva.SOLICITADA, moradorA, area,
                amanha(), LocalTime.of(8, 0), LocalTime.of(10, 0)));
        Reserva meioDia = repo.adicionar(reserva(StatusReserva.SOLICITADA, moradorB, area,
                amanha(), LocalTime.of(10, 0), LocalTime.of(12, 0)));

        ResultadoConcorrente resultado = repo.aprovarEmParalelo(manha.getId(), meioDia.getId());

        assertEquals(2, resultado.sucessos);
        assertEquals(0, repo.paresConflitantesAprovados());
    }

    // ================================================================= infraestrutura de teste

    private static final class ResultadoConcorrente {
        int sucessos;
        int recusasDeNegocio;
        int outrasFalhas;
    }

    /**
     * Repositório em memória que replica a semântica relevante dos repositórios reais para exercitar o
     * {@link ReservaService} de verdade (sem mock do serviço). Cada chamada a findById devolve uma CÓPIA,
     * como uma sessão JPA por transação; save grava a cópia de volta (last-write-wins, como o banco).
     */
    private final class RepositorioEmMemoria {
        private final Map<UUID, Reserva> tabela = new ConcurrentHashMap<>();
        private final Map<UUID, ReentrantLock> locksPorArea = new ConcurrentHashMap<>();
        private final ThreadLocal<List<ReentrantLock>> locksDaTransacao = ThreadLocal.withInitial(ArrayList::new);
        private final boolean lockAtivo;
        private final ReservaRepository reservas = org.mockito.Mockito.mock(ReservaRepository.class);
        private final AreaComumRepository areas = org.mockito.Mockito.mock(AreaComumRepository.class);
        private final MoradorRepository moradores = org.mockito.Mockito.mock(MoradorRepository.class);
        private final AdministradorRepository administradores = org.mockito.Mockito.mock(AdministradorRepository.class);
        private final ColaboradorRepository colaboradores = org.mockito.Mockito.mock(ColaboradorRepository.class);
        private CyclicBarrier barreiraSemLock;

        RepositorioEmMemoria(boolean lockAtivo) {
            this.lockAtivo = lockAtivo;
            lenient().when(administradores.existsByIdAndAtivoTrue(any(UUID.class))).thenReturn(true);
            lenient().when(moradores.existsByIdAndAtivoTrue(any(UUID.class))).thenReturn(true);
            lenient().when(reservas.findById(any(UUID.class))).thenAnswer(inv ->
                    Optional.ofNullable(tabela.get(inv.<UUID>getArgument(0))).map(RepositorioEmMemoria::copia));
            lenient().when(reservas.save(any(Reserva.class))).thenAnswer(inv -> {
                Reserva salva = inv.getArgument(0);
                tabela.put(salva.getId(), copia(salva));
                return salva;
            });
            lenient().when(areas.buscarComLockParaDecisao(any(UUID.class))).thenAnswer(inv -> {
                UUID areaId = inv.getArgument(0);
                if (lockAtivo) {
                    ReentrantLock lock = locksPorArea.computeIfAbsent(areaId, k -> new ReentrantLock());
                    if (!lock.tryLock(10, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("timeout aguardando lock da área (equivale a lock timeout)");
                    }
                    locksDaTransacao.get().add(lock);
                }
                AreaComum area = new AreaComum();
                area.setId(areaId);
                return Optional.of(area);
            });
            lenient().when(reservas.buscarAprovadasConflitantesExcluindo(
                            any(UUID.class), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class), any(UUID.class)))
                    .thenAnswer(inv -> {
                        List<Reserva> conflitantes = conflitantes(
                                inv.getArgument(0), inv.getArgument(1), inv.getArgument(2),
                                inv.getArgument(3), inv.getArgument(4));
                        aguardarJanelaDeCorrida();
                        return conflitantes;
                    });
        }

        ReservaService novoServico() {
            AuthenticatedUserValidator validator = new AuthenticatedUserValidator(administradores, colaboradores, moradores);
            return new ReservaService(reservas, areas, moradores, validator);
        }

        Reserva adicionar(Reserva reserva) {
            tabela.put(reserva.getId(), copia(reserva));
            return reserva;
        }

        StatusReserva status(UUID id) {
            return tabela.get(id).getStatus();
        }

        long contar(StatusReserva status) {
            return tabela.values().stream().filter(r -> r.getStatus() == status).count();
        }

        /** Número de pares de reservas APROVADAS, da mesma área/data, com intervalos sobrepostos. */
        int paresConflitantesAprovados() {
            List<Reserva> aprovadas = tabela.values().stream()
                    .filter(r -> r.getStatus() == StatusReserva.APROVADA).toList();
            int pares = 0;
            for (int i = 0; i < aprovadas.size(); i++) {
                for (int j = i + 1; j < aprovadas.size(); j++) {
                    Reserva a = aprovadas.get(i);
                    Reserva b = aprovadas.get(j);
                    if (a.getArea().getId().equals(b.getArea().getId()) && a.getData().equals(b.getData())
                            && a.getHoraInicio().isBefore(b.getHoraFim()) && a.getHoraFim().isAfter(b.getHoraInicio())) {
                        pares++;
                    }
                }
            }
            return pares;
        }

        ResultadoConcorrente aprovarEmParalelo(UUID idA, UUID idB) throws Exception {
            // Sem lock, força as duas threads a terminarem a conferência antes de qualquer gravação.
            // Com lock, não há barreira (ela travaria a segunda thread por causa do lock, de propósito).
            barreiraSemLock = lockAtivo ? null : new CyclicBarrier(2, () -> { }) ;
            ReservaService service = novoServico();
            ExecutorService pool = Executors.newFixedThreadPool(2);
            CountDownLatch largada = new CountDownLatch(1);
            try {
                List<Future<Reserva>> futuros = new ArrayList<>();
                for (UUID id : List.of(idA, idB)) {
                    futuros.add(pool.submit(transacao(largada, () -> service.aprovarReserva(admin, id))));
                }
                largada.countDown();
                ResultadoConcorrente resultado = new ResultadoConcorrente();
                for (Future<Reserva> futuro : futuros) {
                    try {
                        futuro.get(30, TimeUnit.SECONDS);
                        resultado.sucessos++;
                    } catch (java.util.concurrent.ExecutionException e) {
                        if (e.getCause() instanceof BusinessRuleException) {
                            resultado.recusasDeNegocio++;
                        } else {
                            resultado.outrasFalhas++;
                        }
                    }
                }
                return resultado;
            } finally {
                pool.shutdownNow();
            }
        }

        /** Simula a fronteira @Transactional: ao fim (commit ou rollback) libera os locks adquiridos. */
        private Callable<Reserva> transacao(CountDownLatch largada, Callable<Reserva> corpo) {
            return () -> {
                largada.await();
                try {
                    return corpo.call();
                } finally {
                    locksDaTransacao.get().forEach(ReentrantLock::unlock);
                    locksDaTransacao.get().clear();
                }
            };
        }

        private void aguardarJanelaDeCorrida() {
            try {
                if (barreiraSemLock != null) {
                    barreiraSemLock.await(10, TimeUnit.SECONDS);
                } else {
                    Thread.sleep(40); // alarga a janela entre "checar" e "gravar"
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        private List<Reserva> conflitantes(UUID areaId, LocalDate data, LocalTime inicio, LocalTime fim, UUID excluir) {
            return tabela.values().stream()
                    .filter(r -> r.getArea().getId().equals(areaId))
                    .filter(r -> r.getData().equals(data))
                    .filter(r -> r.getStatus() == StatusReserva.APROVADA)
                    .filter(r -> r.getHoraInicio().isBefore(fim) && r.getHoraFim().isAfter(inicio))
                    .filter(r -> !r.getId().equals(excluir))
                    .map(RepositorioEmMemoria::copia)
                    .toList();
        }

        private static Reserva copia(Reserva origem) {
            Reserva c = new Reserva();
            c.setId(origem.getId());
            c.setArea(origem.getArea());
            c.setMorador(origem.getMorador());
            c.setData(origem.getData());
            c.setHoraInicio(origem.getHoraInicio());
            c.setHoraFim(origem.getHoraFim());
            c.setStatus(origem.getStatus());
            c.setMotivoNegacao(origem.getMotivoNegacao());
            c.setDataCriacao(origem.getDataCriacao());
            c.setDataDecisao(origem.getDataDecisao());
            return c;
        }
    }
}
