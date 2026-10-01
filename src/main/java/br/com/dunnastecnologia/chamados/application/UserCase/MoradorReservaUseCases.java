package br.com.dunnastecnologia.chamados.application.UserCase;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.application.pagination.PageResult;
import br.com.dunnastecnologia.chamados.domain.model.AreaComum;
import br.com.dunnastecnologia.chamados.domain.model.Reserva;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface MoradorReservaUseCases {

    //áreas ativas disponíveis para reserva, para popular o form de solicitação
    PageResult<AreaComum> listarAreasDisponiveis(AuthenticatedUser morador, PageRequest pageRequest);

    // Reservas APROVADA que ocupam o intervalo consultado, para o morador decidir o horário
    List<Reserva> consultarDisponibilidade(AuthenticatedUser morador, UUID areaComumId, LocalDate data);

    // RF-02 - cria a solicitação como SOLICITADA  após validar regras de negócio.
    Reserva solicitarReserva(
            AuthenticatedUser morador,
            UUID areaComumId,
            LocalDate data,
            LocalTime horaInicio,
            LocalTime horaFim
    );

    // RF-05 - somente as reservas do próprio morador.

    PageResult<Reserva> listarMinhasReservas(
            AuthenticatedUser morador,
            UUID areaComumId,
            LocalDate data,
            PageRequest pageRequest
    );

    // RF-06 - O morador só cancela a própria reserva.
    Reserva cancelarReservaMorador(AuthenticatedUser morador, UUID reservaId);
}
