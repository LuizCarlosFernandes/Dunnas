package br.com.dunnastecnologia.chamados.infrastructure.repository;

import br.com.dunnastecnologia.chamados.domain.model.Chamado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChamadoRepository extends JpaRepository<Chamado, UUID> {

    /**
     * Sincroniza o status dos chamados vencidos com base no SLA do tipo de chamado.
     */
    @Modifying
    @Query(value = """
            update chamados c
               set status_id = s.id
              from status_chamado s,
                   tipos_chamado tc
             where lower(s.nome) = 'atrasado'
               and tc.id = c.tipo_chamado_id
               and c.data_finalizacao is null
               and c.data_abertura is not null
               and tc.prazo_horas is not null
               and now() > c.data_abertura + (tc.prazo_horas * interval '1 hour')
               and c.status_id <> s.id
            """,
            nativeQuery = true)
    int marcarChamadosAtrasados();

    /**
     * Lista os chamados visiveis para o administrador, inclusive os ja finalizados.
     */
    @Query(value = """
            select *
            from fn_listar_chamados_para_admin(:adminId, :statusId, :moradorNome, :dataAbertura)
            """,
            countQuery = """
            select count(*)
            from fn_listar_chamados_para_admin(:adminId, :statusId, :moradorNome, :dataAbertura)
            """,
            nativeQuery = true)
    Page<Chamado> buscarParaAdmin(
            @Param("adminId") UUID adminId,
            @Param("statusId") UUID statusId,
            @Param("moradorNome") String moradorNome,
            @Param("dataAbertura") LocalDate dataAbertura,
            Pageable pageable
    );

    /**
     * Lista apenas os chamados do proprio morador vinculados a unidades que ele possui acesso.
     */
    @Query(value = """
            select *
            from fn_listar_chamados_do_morador(
                :moradorId,
                :statusId,
                :unidadeId,
                :tipoChamadoId,
                :dataAbertura
            )
            """,
            countQuery = """
            select count(*)
            from fn_listar_chamados_do_morador(
                :moradorId,
                :statusId,
                :unidadeId,
                :tipoChamadoId,
                :dataAbertura
            )
            """,
            nativeQuery = true)
    Page<Chamado> buscarParaMorador(
            @Param("moradorId") UUID moradorId,
            @Param("statusId") UUID statusId,
            @Param("unidadeId") UUID unidadeId,
            @Param("tipoChamadoId") UUID tipoChamadoId,
            @Param("dataAbertura") LocalDate dataAbertura,
            Pageable pageable
    );

    /**
     * Lista os chamados acessiveis ao colaborador, ocultando registros ja finalizados.
     */
    @Query(value = """
            select *
            from fn_listar_chamados_do_colaborador(
                :colaboradorId,
                :statusId,
                :tipoChamadoId,
                :unidadeIdentificacao,
                :dataAbertura
            )
            """,
            countQuery = """
            select count(*)
            from fn_listar_chamados_do_colaborador(
                :colaboradorId,
                :statusId,
                :tipoChamadoId,
                :unidadeIdentificacao,
                :dataAbertura
            )
            """,
            nativeQuery = true)
    Page<Chamado> buscarParaColaborador(
            @Param("colaboradorId") UUID colaboradorId,
            @Param("statusId") UUID statusId,
            @Param("tipoChamadoId") UUID tipoChamadoId,
            @Param("unidadeIdentificacao") String unidadeIdentificacao,
            @Param("dataAbertura") LocalDate dataAbertura,
            Pageable pageable
    );

    /**
     * Busca um chamado especifico do morador validando no banco se ele pode acessar esse registro.
     */
    @Query(value = """
            select *
            from fn_buscar_chamado_do_morador(:moradorId, :chamadoId)
            """,
            nativeQuery = true)
    Optional<Chamado> findByIdAndMoradorId(
            @Param("chamadoId") UUID chamadoId,
            @Param("moradorId") UUID moradorId
    );

    /**
     * Busca um chamado especifico para o colaborador somente se ele ainda nao estiver finalizado.
     */
    @Query(value = """
            select *
            from fn_buscar_chamado_para_colaborador(:colaboradorId, :chamadoId)
            """,
            nativeQuery = true)
    Optional<Chamado> findByIdAndColaboradorId(
            @Param("colaboradorId") UUID colaboradorId,
            @Param("chamadoId") UUID chamadoId
    );

    /**
     * Busca um chamado especifico para o administrador usando a funcao de autorizacao do banco.
     */
    @Query(value = """
            select *
            from fn_buscar_chamado_para_admin(:adminId, :chamadoId)
            """,
            nativeQuery = true)
    Optional<Chamado> findByIdAndAdminId(
            @Param("adminId") UUID adminId,
            @Param("chamadoId") UUID chamadoId
    );
}
