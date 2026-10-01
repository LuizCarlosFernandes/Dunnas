package br.com.dunnastecnologia.chamados.infrastructure.service;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.application.UserCase.AdminReservaUseCases;
import br.com.dunnastecnologia.chamados.application.UserCase.MoradorReservaUseCases;
import br.com.dunnastecnologia.chamados.application.pagination.PageResult;
import br.com.dunnastecnologia.chamados.domain.model.*;

import br.com.dunnastecnologia.chamados.domain.validation.ValidationLimits;
import br.com.dunnastecnologia.chamados.infrastructure.exception.BusinessRuleException;
import br.com.dunnastecnologia.chamados.infrastructure.exception.ResourceNotFoundException;
import br.com.dunnastecnologia.chamados.infrastructure.exception.UnauthorizedOperationException;
import br.com.dunnastecnologia.chamados.infrastructure.repository.AreaComumRepository;
import br.com.dunnastecnologia.chamados.infrastructure.repository.MoradorRepository;
import br.com.dunnastecnologia.chamados.infrastructure.repository.ReservaRepository;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.AuthenticatedUserValidator;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.InputValidationSupport;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.PageResultMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
public class ReservaService implements MoradorReservaUseCases, AdminReservaUseCases {
    private final ReservaRepository reservaRepository;
    private final AreaComumRepository areaComumRepository;
    private final MoradorRepository moradorRepository;
    private final AuthenticatedUserValidator authenticatedUserValidator;

    public ReservaService(
            ReservaRepository reservaRepository, AreaComumRepository areaComumRepository,
            MoradorRepository moradorRepository, AuthenticatedUserValidator authenticatedUserValidator) {
        this.reservaRepository = reservaRepository;
        this.areaComumRepository = areaComumRepository;
        this.moradorRepository = moradorRepository;
        this.authenticatedUserValidator = authenticatedUserValidator;
    }

    // ============= MoradorReservaUseCases =============

    @Override
    @Transactional(readOnly = true)
    public PageResult<AreaComum> listarAreasDisponiveis(AuthenticatedUser morador, PageRequest pageRequest) {
        authenticatedUserValidator.assertMorador(morador);
        return PageResultMapper.fromPage(areaComumRepository.findByAtivaTrue(pageRequest));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reserva> consultarDisponibilidade(AuthenticatedUser morador, UUID areaComumId, LocalDate data) {
        authenticatedUserValidator.assertMorador(morador);
        //Dia inteiro: mostra tudo já aprovado nesse dia, para o morador escolher um horario livre.
        return reservaRepository.buscarAprovadasConflitantes(areaComumId, data, LocalTime.MIN, LocalTime.MAX);
    }

    @Override
    @Transactional
    public Reserva solicitarReserva(
            AuthenticatedUser morador,
            UUID areaComumId,
            LocalDate data,
            LocalTime horaInicio,
            LocalTime horaFim
    ) {
        authenticatedUserValidator.assertMorador(morador);
        if (areaComumId == null) {
            throw new BusinessRuleException("Área, data e intervalo de início e fim são obrigatórios");
        }
        AreaComum area = areaComumRepository.findById(areaComumId)
                .orElseThrow(() -> new ResourceNotFoundException("Área comum não encontrada."));
        if (!Boolean.TRUE.equals(area.getAtiva())) {
            // RF-01 / RN-01-01: área retirada ou desativada não aceita novas solicitações
            throw new BusinessRuleException("Área comum não disponível para novas solicitações");
        }

        validarDadosDaSolicitacao(data, horaInicio, horaFim);

        Morador moradorEntity = moradorRepository.findById(morador.id())
                .orElseThrow(() -> new ResourceNotFoundException("Morador não encontrado"));

        Reserva reserva = new Reserva();
        reserva.setArea(area);
        reserva.setMorador(moradorEntity);
        reserva.setData(data);
        reserva.setHoraInicio(horaInicio);
        reserva.setHoraFim(horaFim);
        reserva.setStatus(StatusReserva.SOLICITADA);
        // A data de decisão só é preenchida quando o administrador aprovar/negar.
        reserva.setDataCriacao(LocalDateTime.now());
        return reservaRepository.save(reserva);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Reserva> listarMinhasReservas(
            AuthenticatedUser morador,
            UUID areaComumId,
            LocalDate data,
            PageRequest pageRequest
    ) {
        authenticatedUserValidator.assertMorador(morador);
        // RN-01-10 - morador só vê as próprias reservas,
        return PageResultMapper.fromPage(
                reservaRepository.buscarParaMorador(morador.id(), areaComumId, data, pageRequest)
        );
    }

    @Override
    @Transactional
    public Reserva cancelarReservaMorador(AuthenticatedUser morador, UUID reservaId) {
        authenticatedUserValidator.assertMorador(morador);
        Reserva reserva = buscarReservaOuFalhar(reservaId);

        if (!reserva.getMorador().getId().equals(morador.id())) {
            // RNF-03: recusa serm expor dados de reserva do morador.
            throw new UnauthorizedOperationException("Reserva não pertence ao morador autenticado");
        }

        cancelar(reserva);
        return reservaRepository.save(reserva);
    }

    // ============= AdminReservaUseCases =============

    @Override
    @Transactional(readOnly = true)
    public PageResult<Reserva> listarTodasReservas(
            AuthenticatedUser admin,
            UUID areaComumId,
            LocalDate data,
            PageRequest pageRequest
    ) {
        authenticatedUserValidator.assertAdministrador(admin);
        return PageResultMapper.fromPage(reservaRepository.buscaraParaAdmin(areaComumId, data, pageRequest));
    }

    @Override
    @Transactional
    public Reserva aprovarReserva(AuthenticatedUser admin, UUID reservaId) {
        authenticatedUserValidator.assertAdministrador(admin);
        Reserva reserva = buscarReservaOuFalhar(reservaId);

        if (reserva.getStatus() != StatusReserva.SOLICITADA) {
            throw new BusinessRuleException("Somente reservas SOLICITADA podem ser aprovadas");
        }

        //Trava a área até o fim da transação, usando LOCK PESSIMISTA.
        areaComumRepository.buscarComLockParaDecisao(reserva.getArea().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Área comum não encontrada"));

        List<Reserva> conflitos = reservaRepository.buscarAprovadasConflitantesExcluindo(
                reserva.getArea().getId(), reserva.getData(),
                reserva.getHoraInicio(), reserva.getHoraFim(), reserva.getId()
        );
        if (!conflitos.isEmpty()) {
            // RN-01-07 / CA-01-07 - não aprova se no momento da decisão existe conflito com outra solicitação já aprovada.
            throw new BusinessRuleException("Já existe reserva aprovada para esta área/horário");
        }
        reserva.setStatus(StatusReserva.APROVADA);
        reserva.setDataDecisao(LocalDateTime.now());
        return reservaRepository.save(reserva);
    }

    @Override
    @Transactional
    public Reserva negarReserva(AuthenticatedUser admin, UUID reservaId, String motivo) {
        authenticatedUserValidator.assertAdministrador(admin);
        Reserva reserva = buscarReservaOuFalhar(reservaId);

        if (reserva.getStatus() != StatusReserva.SOLICITADA) {
            throw new BusinessRuleException("Somente reservas SOLICITADA podem ser negadas");
        }

        //RN-01-08 - obrigatório incluir motivo para decisão, sem ele não pode ser concluido
        String motivoNormalizado = InputValidationSupport.normalizeRequiredText(
                motivo,
                "Motivo da negação é obrigatório",
                "Motivo da negação excende o tamanho máximo permitido",
                ValidationLimits.RESERVA_MOTIVO_NEGACAO_MAX_LENGTH
        );

        // Negar é diferente de cancelar: NEGADA é a decisão do admin sobre a solicitação.
        reserva.setStatus(StatusReserva.NEGADA);
        reserva.setMotivoNegacao(motivoNormalizado);
        reserva.setDataDecisao(LocalDateTime.now());
        return reservaRepository.save(reserva);
    }

    @Override
    @Transactional
    public Reserva cancelarReserva(AuthenticatedUser admin, UUID reservaId) {
        authenticatedUserValidator.assertAdministrador(admin);
        Reserva reserva = buscarReservaOuFalhar(reservaId);

        // RN-01-12 - administrador cancela qualquer reserva antes do inicio, sem checar dono
        cancelar(reserva);
        return reservaRepository.save(reserva);
    }

    // ============= privados compartilhados =============

    private Reserva buscarReservaOuFalhar(UUID reservaId) {
        return reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva não encontrada"));
    }

    private void cancelar(Reserva reserva) {
        if (reserva.getStatus() != StatusReserva.SOLICITADA && reserva.getStatus() != StatusReserva.APROVADA) {
            //RN-01-14 - NEGADA/CANCELADA são estados terminais.
            throw new BusinessRuleException("Reserva não está em um estado que permita cancelamento");
        }

        if (!LocalDateTime.of(reserva.getData(), reserva.getHoraInicio()).isAfter(LocalDateTime.now())) {
            //RN-01-11 / RN-01-12 / RN-01-14 - não é possível cancelar se o horário inicial já foi alcançado
            throw new BusinessRuleException("Não é possível cancelar após o inicio do horário reservado.");
        }
        reserva.setStatus(StatusReserva.CANCELADA);
    }

    private void validarDadosDaSolicitacao(LocalDate data, LocalTime horaInicio, LocalTime horaFim) {
        if (data == null || horaInicio == null || horaFim == null) {
            throw new BusinessRuleException("Área, data e intervalo de início e fim são obrigatórios");
        }
        if (!horaFim.isAfter(horaInicio)) {
            //RN-01-02 - Horário de fim deve ser anterior ao de inicio
            throw new BusinessRuleException("O horário de fim deve ser posterior ao horário de início");
        }

        if (!LocalDateTime.of(data, horaInicio).isAfter(LocalDateTime.now())) {
            // RN-01-02 / CA-01-03 - data/horário de inicio já alcançado ou no passado recusa a solicitação
            throw new BusinessRuleException("O horário inicial da reserva deve ser no futuro");
        }
    }
}