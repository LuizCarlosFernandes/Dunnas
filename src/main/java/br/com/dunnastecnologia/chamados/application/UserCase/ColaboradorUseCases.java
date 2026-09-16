package br.com.dunnastecnologia.chamados.application.UserCase;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.application.pagination.PageResult;
import br.com.dunnastecnologia.chamados.domain.model.Chamado;
import br.com.dunnastecnologia.chamados.domain.model.Comentario;
import br.com.dunnastecnologia.chamados.domain.model.StatusChamado;
import br.com.dunnastecnologia.chamados.domain.model.TipoChamado;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.UUID;

public interface ColaboradorUseCases {
    /**
     * Existe para disponibilizar ao colaborador os status validos do fluxo,
     * permitindo atualizacao consistente dos chamados sob atendimento.
     */
    PageResult<StatusChamado> listarStatusDisponiveis(
            AuthenticatedUser colaborador,
            PageRequest pageRequest
    );

    /**
     * Existe para limitar os filtros e o escopo do colaborador aos tipos de chamado sob sua responsabilidade.
     */
    PageResult<TipoChamado> listarTiposChamadoDisponiveis(
            AuthenticatedUser colaborador,
            PageRequest pageRequest
    );

    /**
     *  para permitir que o colaborador visualize os chamados dentro do seu escopo,
     * aplicando filtros operacionais para atender e acompanhar a fila de trabalho.
     */
    PageResult<Chamado> buscarChamados(
            AuthenticatedUser colaborador,
            UUID statusId,
            UUID tipoChamadoId,
            String unidadeIdentificacao,
            LocalDate dataAbertura,
            PageRequest pageRequest
    );

    /**
     *  para detalhar um chamado especifico que esteja dentro do escopo do colaborador,
     * evitando acesso indiscriminado a registros fora da area atendida por ele.
     */
    Chamado buscarChamadoPorId(
            AuthenticatedUser colaborador,
            UUID chamadoId
    );

    /**
     *  porque colaboradores podem evoluir o atendimento alterando o status do chamado
     * ate sua conclusao, conforme a regra de negocio do sistema.
     */
    Chamado atualizarStatusChamado(
            AuthenticatedUser colaborador,
            UUID chamadoId,
            UUID statusId
    );

    /**
     *  para que o colaborador registre interacoes no historico do chamado
     * sempre respeitando o proprio escopo de acesso.
     */
    Comentario comentarChamado(
            AuthenticatedUser colaborador,
            UUID chamadoId,
            String mensagem
    );
}
