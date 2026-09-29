package br.com.dunnastecnologia.chamados.infrastructure.service;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.domain.model.*;

import br.com.dunnastecnologia.chamados.infrastructure.exception.BusinessRuleException;
import br.com.dunnastecnologia.chamados.infrastructure.exception.ResourceNotFoundException;
import br.com.dunnastecnologia.chamados.infrastructure.exception.UnauthorizedOperationException;
import br.com.dunnastecnologia.chamados.infrastructure.repository.AreaComumRepository;
import br.com.dunnastecnologia.chamados.infrastructure.repository.ReservaRepository;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class ReservaService {
    private final ReservaRepository reservaRepository;
    private final AreaComumRepository areaComumRepository;

    public ReservaService(ReservaRepository reservaRepository, AreaComumRepository areaComumRepository) {
        this.reservaRepository = reservaRepository;
        this.areaComumRepository = areaComumRepository;
    }

    // RF-02 / RN-01-02 A RN-01-5 - não precisa de lock: SOLICITADA nunca conflita com nada
    @Transactional
    public Reserva solicitar(Morador morador, UUID areaId, LocalDate data, LocalTime inicio, LocalTime fim){
        AreaComum area = areaComumRepository.findById(areaId)
                .orElseThrow(() -> new ResourceNotFoundException("Área comum não encontrada"));

        if (!Boolean.TRUE.equals(area.getAtiva())){
            throw new BusinessRuleException("Área não está disponível para novas solicitações");
        }
        validarIntervalo(data, inicio, fim); // TODO: horário futuro, fim > inicio

        Reserva reserva = new Reserva();
        reserva.setArea(area);
        reserva.setMorador(morador);
        reserva.setData(data);
        reserva.setHoraInicio(inicio);
        reserva.setHoraFim(fim);
        reserva.setStatus(StatusReserva.SOLICITADA);
        reserva.setDataCriacao(LocalDateTime.now());

        return reservaRepository.save(reserva);
    }

    // RF-03 / CA-01-02 - leitura simples, sem lock (não decide, apenas avisa)
    @Transactional(readOnly = true)
    public List<Reserva> consultarConflitantes(UUID areaID, LocalDate data, LocalTime inicio, LocalTime fim){
        return reservaRepository.buscarAprovadasConflitantes(areaID, data, inicio, fim, null);
    }

    // RF=04 / RN-01-07 / CA-01-05, CA-01-07, CA-01-08 - Seção crítica que envolve decisão
    @Transactional
    public Reserva aprovar(UUID reservaId){
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva não encontrada"));

        if (reserva.getStatus() != StatusReserva.SOLICITADA){
            throw new BusinessRuleException("Somentes reservas SOLICITADAS podem ser aprovadas.");
        }

        //Trava a área. se outra transação já estiver aprovando algo dessa área, bloqueando até a outra terminar
        areaComumRepository.buscarComLockParaDecisao(reserva.getArea().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Área comum não encontrada."));

        //Com a área travada a leitura é garantidamente atual.
        List<Reserva> conflitos = reservaRepository.buscarAprovadasConflitantes(
                reserva.getArea().getId(), reserva.getData(),
                reserva.getHoraInicio(), reserva.getHoraFim(), reserva.getId());

        if(!conflitos.isEmpty()){
            throw new BusinessRuleException("Já existe uma reserva aprovada para esta área/horário");
        }

        reserva.setStatus(StatusReserva.APROVADA);
        reserva.setDataDecisao(LocalDateTime.now());
        return reservaRepository.save(reserva);
        // libera o lock -> próxima transação em espera acorda e reavalia
    }

    //RF-04 / RN-01-08 -  Não precisa de lock, não altera disponibilidade
    public Reserva negar(UUID reservaId, String motivo){
        if(motivo == null || motivo.isBlank()){
            throw new BusinessRuleException("Motivo de negação é obrigatório");
        }

        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva não encontrada."));

        if(reserva.getStatus() != StatusReserva.SOLICITADA){
            throw new BusinessRuleException("Apenas reservas SOLICITADAS podem ser negadas.");
        }
        reserva.setStatus(StatusReserva.NEGADA);
        reserva.setMotivoNegacao(motivo);
        reserva.setDataDecisao(LocalDateTime.now());

        return reservaRepository.save(reserva);
    }

    // RF-06 / RN-01-11, RN-01-12, dono ou admin, e só anter do inicio
    @Transactional
    public Reserva cancelar(UUID reservaId, AuthenticatedUser solicitante){
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva não encontrada."));

        boolean isDono = reserva.getMorador().getId().equals(solicitante.id());
        boolean idAdmin = "ROLE_ADMINISTRADOR".equals(solicitante.role());

        if(!isDono && !idAdmin){
            throw new UnauthorizedOperationException("Sem permissão para cancelar essa reserva.");
        }

        if(reserva.getStatus() != StatusReserva.SOLICITADA && reserva.getStatus() != StatusReserva.APROVADA){
            throw new BusinessRuleException("Reserva não pode mais ser cancelada");
        }

        if(LocalDateTime.of(reserva.getData(), reserva.getHoraInicio()).isBefore(LocalDateTime.now())){
            throw new BusinessRuleException("Não é possível cancelar após o início do horário reservado");
        }

        reserva.setStatus(StatusReserva.CANCELADA);
        return reservaRepository.save(reserva);
    }

    private void validarIntervalo(LocalDate data, LocalTime inicio, LocalTime fim) {
        // TODO: RN-01-02 — fim > início, e data+inicio no futuro em relação a LocalDateTime.now()
    }
}
