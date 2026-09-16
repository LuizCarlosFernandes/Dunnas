package br.com.dunnastecnologia.chamados.application.UserCase;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.application.pagination.PageResult;
import br.com.dunnastecnologia.chamados.domain.model.Comentario;
import org.springframework.data.domain.PageRequest;

import java.util.UUID;

public interface ComentarioUseCase {

    Comentario comentar(
            AuthenticatedUser usuario,
            UUID chamadoId,
            String mensagem
    );

    PageResult<Comentario> listarComentariosDoChamado(
            AuthenticatedUser usuario,
            UUID chamadoId,
            PageRequest pageRequest
    );
}
