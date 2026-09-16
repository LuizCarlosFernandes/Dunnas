package br.com.dunnastecnologia.chamados.application.UserCase;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.application.pagination.PageResult;
import br.com.dunnastecnologia.chamados.domain.model.StatusChamado;
import org.springframework.data.domain.PageRequest;

import java.util.UUID;

public interface StatusChamadoUseCase {

    StatusChamado cadastrarStatus(
            AuthenticatedUser admin,
            String nome
    );

    PageResult<StatusChamado> listarStatus(PageRequest pageRequest);

    StatusChamado buscarStatusPorId(UUID statusId);

    StatusChamado atualizarStatus(
            AuthenticatedUser admin,
            UUID statusId,
            String nome
    );

    void definirStatusInicialPadrao(
            AuthenticatedUser admin,
            UUID statusId
    );
}
