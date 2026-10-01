package br.com.dunnastecnologia.chamados.application.UserCase;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.application.pagination.PageResult;
import br.com.dunnastecnologia.chamados.domain.model.AreaComum;
import org.springframework.data.domain.PageRequest;

import java.util.UUID;

public interface AdminAreaComumUseCases {

    // RF-01 somente ADMINISTRADOR cadastra área comum
    AreaComum cadastrarAreaComum(AuthenticatedUser admin, String nome);

    //Traz ativas e inativas: admin precisa continuar gerenciando areas desativadas.
    PageResult<AreaComum> listaAreasComum(AuthenticatedUser admin, PageRequest pageRequest);

    AreaComum buscarAreaComumPorId(AuthenticatedUser admin, UUID areaComumId);

    AreaComum atualizarAreaComum(AuthenticatedUser admin, UUID areaComumId, String nome);

    // RN-01-01 - soft - não aceita novas solicitações, não apaga reserevas existentes
    AreaComum desativarAreaComum(AuthenticatedUser admin, UUID areaComumId);

    //Não exigido, mas simétrico ao desativar
    AreaComum reativarAreaComum( AuthenticatedUser admin, UUID areaComumId);
}
