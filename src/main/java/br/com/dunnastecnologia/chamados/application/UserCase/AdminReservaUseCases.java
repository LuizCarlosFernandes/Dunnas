package br.com.dunnastecnologia.chamados.application.UserCase;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.application.pagination.PageResult;
import br.com.dunnastecnologia.chamados.domain.model.Reserva;
import org.springframework.data.domain.PageRequest;


import java.time.LocalDate;
import java.util.UUID;

public interface AdminReservaUseCases {

    //RF-05 - Administrador vê as reservas de todos.
    PageResult<Reserva> listarTodasReservas(
      AuthenticatedUser admin,
      UUID areaComumId,
      LocalDate data,
      PageRequest pageRequest
    );

    // RF-04 / RN-01-07 - seção protegida por lock pessimista.
    Reserva aprovarReserva( AuthenticatedUser admin, UUID reservaId);

    // RF-04 / RN-01-09 - Exige motivo não vazio para negação
    Reserva negarReserva( AuthenticatedUser admin, UUID reservaId, String motivo);

    // RF-06 - adminitrador também pode cancelar qualquer reserva.
    Reserva cancelarReserva( AuthenticatedUser admin, UUID reservaId);

}
