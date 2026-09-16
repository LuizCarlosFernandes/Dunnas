package br.com.dunnastecnologia.chamados.application.UserCase;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.application.pagination.PageResult;
import br.com.dunnastecnologia.chamados.domain.model.Bloco;
import br.com.dunnastecnologia.chamados.domain.model.Chamado;
import br.com.dunnastecnologia.chamados.domain.model.Colaborador;
import br.com.dunnastecnologia.chamados.domain.model.Comentario;
import br.com.dunnastecnologia.chamados.domain.model.Morador;
import br.com.dunnastecnologia.chamados.domain.model.StatusChamado;
import br.com.dunnastecnologia.chamados.domain.model.TipoChamado;
import br.com.dunnastecnologia.chamados.domain.model.Unidade;
import br.com.dunnastecnologia.chamados.domain.model.Usuario;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AdminUseCases {

    /**
     *  para que o administrador cadastre a estrutura fisica do condominio,
     * origem da geracao automatica das unidades a partir de bloco, andar e apartamento.
     */
    Bloco cadastrarBloco(
            AuthenticatedUser admin,
            String identificacao,
            int quantidadeAndares,
            int apartamentosPorAndar
    );

    /**
     *  para consultar um bloco especifico e permitir manutencao da estrutura cadastrada.
     */
    Bloco buscarBlocoPorId(UUID blocoId);

    /**
     *  para listar os blocos do condominio com paginacao durante a administracao da estrutura.
     */
    PageResult<Bloco> listarBlocos(PageRequest pageRequest);

    /**
     * Existe para consultar as unidades geradas automaticamente para um bloco,
     * validando a estrutura criada a partir de andares e apartamentos por andar.
     */
    PageResult<Unidade> listarUnidadesDoBloco(
            UUID blocoId,
            PageRequest pageRequest
    );

    /**
     * Existe para permitir ao administrador visualizar rapidamente quais moradores
     * estao vinculados a cada unidade dentro de um bloco.
     */
    Map<UUID, List<Morador>> listarMoradoresPorUnidadeIds(List<UUID> unidadeIds);

    /**
     * Existe para detalhar uma unidade especifica antes de vincular moradores
     * ou auditar a estrutura fisica cadastrada no condominio.
     */
    Unidade buscarUnidadePorId(UUID unidadeId);

    /**
     *  para o administrador criar moradores, colaboradores e outros administradores,
     * centralizando o gerenciamento de usuarios do sistema.
     */
    Usuario cadastrarUsuario(
            AuthenticatedUser admin,
            Usuario usuario
    );

    /**
     *  para manter os dados cadastrais e perfis de acesso dos usuarios sempre atualizados.
     */
    Usuario atualizarUsuario(
            AuthenticatedUser admin,
            UUID usuarioId,
            Usuario usuario
    );

    /**
     *  para dar visibilidade paginada aos usuarios cadastrados durante a operacao administrativa.
     */
    PageResult<Usuario> listarUsuarios(PageRequest pageRequest);

    /**
     * Existe para fornecer ao administrador uma lista direta de moradores
     * durante o processo de vinculacao com unidades.
     */
    PageResult<Morador> listarMoradores(PageRequest pageRequest);

    /**
     * Existe para localizar moradores pelo prefixo do e-mail
     * durante o processo de vinculacao com unidades.
     */
    PageResult<Morador> listarMoradoresPorPrefixoEmail(String prefixoEmail, PageRequest pageRequest);

    /**
     * Existe para destacar moradores que ainda nao possuem unidade vinculada.
     */
    PageResult<Morador> listarMoradoresSemUnidade(PageRequest pageRequest);

    /**
     * Existe para localizar moradores sem unidade pelo prefixo do e-mail.
     */
    PageResult<Morador> listarMoradoresSemUnidadePorPrefixoEmail(String prefixoEmail, PageRequest pageRequest);

    /**
     * Existe para fornecer ao administrador uma lista direta de colaboradores
     * durante o processo de definicao de escopo por tipo de chamado.
     */
    PageResult<Colaborador> listarColaboradores(PageRequest pageRequest);

    /**
     * Existe para localizar colaboradores pelo prefixo do e-mail
     * durante o processo de definicao de escopo por tipo de chamado.
     */
    PageResult<Colaborador> listarColaboradoresPorPrefixoEmail(String prefixoEmail, PageRequest pageRequest);

    /**
     *  para detalhar um usuario especifico antes de manutencao, auditoria ou vinculacao.
     */
    Usuario buscarUsuarioPorId(UUID usuarioId);

    /**
     *  para remover usuarios quando deixarem de fazer parte da operacao do condominio.
     */
    void removerUsuario(
            AuthenticatedUser admin,
            UUID usuarioId
    );

    /**
     *  para materializar no sistema quais unidades pertencem a cada morador,
     * regra necessaria para abertura e visualizacao de chamados.
     */
    void vincularMoradorUnidade(
            AuthenticatedUser admin,
            UUID moradorId,
            UUID unidadeId
    );

    /**
     *  para retirar o acesso do morador a uma unidade quando o vinculo deixar de existir.
     */
    void desvincularMoradorUnidade(
            AuthenticatedUser admin,
            UUID moradorId,
            UUID unidadeId
    );

    /**
     * Existe para definir quais tipos de chamado estao sob responsabilidade de um colaborador.
     */
    void vincularColaboradorTipoChamado(
            AuthenticatedUser admin,
            UUID colaboradorId,
            UUID tipoChamadoId
    );

    /**
     * Existe para retirar de um colaborador a responsabilidade sobre um tipo de chamado.
     */
    void desvincularColaboradorTipoChamado(
            AuthenticatedUser admin,
            UUID colaboradorId,
            UUID tipoChamadoId
    );

    /**
     * Existe para listar os tipos de chamado atualmente atribuidos a um colaborador.
     */
    PageResult<TipoChamado> listarTiposChamadoDoColaborador(
            UUID colaboradorId,
            PageRequest pageRequest
    );

    /**
     * Existe para consultar todas as unidades vinculadas a um morador,
     * apoiando manutencao cadastral e auditoria dos vinculos de acesso.
     */
    PageResult<Unidade> listarUnidadesDoMorador(
            UUID moradorId,
            PageRequest pageRequest
    );

    /**
     *  para cadastrar os tipos de chamado disponiveis e seu prazo maximo de atendimento.
     */
    TipoChamado cadastrarTipoChamado(
            AuthenticatedUser admin,
            String titulo,
            int prazoHoras
    );

    /**
     *  para administrar o catalogo de tipos de chamado usado na abertura de ocorrencias.
     */
    PageResult<TipoChamado> listarTiposChamado(PageRequest pageRequest);

    /**
     *  para consultar um tipo de chamado especifico durante manutencao administrativa.
     */
    TipoChamado buscarPorId(UUID tipoId);

    /**
     * Existe para permitir manutencao do catalogo de tipos de chamado
     * sem precisar recriar registros ja existentes.
     */
    TipoChamado atualizarTipoChamado(
            AuthenticatedUser admin,
            UUID tipoId,
            String titulo,
            int prazoHoras
    );


    /**
     *  para o administrador definir quais status podem ser usados no fluxo dos chamados.
     */
    StatusChamado cadastrarStatus(
            AuthenticatedUser admin,
            String nome
    );

    /**
     *  para listar os status configurados e permitir gestao do fluxo operacional.
     */
    PageResult<StatusChamado> listarStatus(PageRequest pageRequest);

    /**
     *  para consultar um status especifico antes de uso ou manutencao.
     */
    StatusChamado buscarStatusPorId(UUID statusId);

    /**
     * Existe para permitir manutencao dos status do fluxo de atendimento
     * ao longo da evolucao operacional do sistema.
     */
    StatusChamado atualizarStatus(
            AuthenticatedUser admin,
            UUID statusId,
            String nome
    );

    /**
     * Existe para definir qual status deve ser atribuido automaticamente
     * a todo novo chamado no momento da abertura.
     */
    void definirStatusInicialPadrao(
            AuthenticatedUser admin,
            UUID statusId
    );

    /**
     *  porque o administrador tambem pode acompanhar os chamados do condominio
     * dentro do escopo de gestao e aplicar filtros operacionais.
     */
    PageResult<Chamado> buscarChamados(
            AuthenticatedUser admin,
            UUID statusId,
            String moradorNome,
            LocalDate dataAbertura,
            PageRequest pageRequest
    );

    /**
     *  para detalhar um chamado especifico durante supervisao, triagem ou auditoria.
     */
    Chamado buscarChamadoPorId(
            AuthenticatedUser admin,
            UUID chamadoId
    );

    /**
     *  porque administradores podem alterar o status dos chamados durante a gestao do atendimento.
     */
    Chamado atualizarStatusChamado(
            AuthenticatedUser admin,
            UUID chamadoId,
            UUID statusId
    );

    /**
     *  para que o administrador participe do historico de interacoes dos chamados
     * quando houver necessidade de orientacao, validacao ou acompanhamento.
     */
    Comentario comentarChamado(
            AuthenticatedUser admin,
            UUID chamadoId,
            String mensagem
    );

}
