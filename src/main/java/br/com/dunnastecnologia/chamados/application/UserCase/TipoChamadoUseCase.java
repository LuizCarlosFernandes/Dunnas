package br.com.dunnastecnologia.chamados.application.UserCase;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.application.pagination.PageResult;
import br.com.dunnastecnologia.chamados.domain.model.TipoChamado;
import org.springframework.data.domain.PageRequest;

import java.util.UUID;

public interface TipoChamadoUseCase {

    TipoChamado cadastrarTipoChamado(
            AuthenticatedUser admin,
            String titulo,
            int prazoHoras
    );

    PageResult<TipoChamado> listarTiposChamado(PageRequest pageRequest);

    TipoChamado buscarTipoChamadoPorId(UUID tipoId);

    TipoChamado atualizarTipoChamado(
            AuthenticatedUser admin,
            UUID tipoId,
            String titulo,
            int prazoHoras
    );
}
