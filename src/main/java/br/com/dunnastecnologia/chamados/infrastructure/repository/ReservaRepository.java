package br.com.dunnastecnologia.chamados.infrastructure.repository;

import br.com.dunnastecnologia.chamados.domain.model.Reserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, UUID> {

    /**
     * Reservas APROVADAS da mesma área/data cujo intervalo sobrepõe [inicio, fim).
     * Usado tanto para checar disponibilidade, quanto dentro da seção
     * crítica da aprovação (protegida pelo lock da área).
     *
     * buscarAprovadasConflitantes: todas as aprovadas que sobrepõem o intervalo (consulta de disponibilidade).
     * buscarAprovadasConflitantesExcluindo: o mesmo, ignorando uma reserva (a que está sendo aprovada).
     *
     * São dois métodos com nomes distintos, sem sobrecarga nem método default: evita
     * parâmetro UUID nulo em "is null" (o PostgreSQL não infere o tipo) e evita recursão
     * ao chamar métodos do próprio repositório através do proxy do Spring Data.
     */
    @Query("""
        select r
        from Reserva r
        where r.area.id = :areaId
        and r.data = :data
        and r.status = br.com.dunnastecnologia.chamados.domain.model.StatusReserva.APROVADA
        and r.horaInicio < :fim
        and r.horaFim > :inicio
        order by r.horaInicio
    """)
    List<Reserva> buscarAprovadasConflitantes(
            @Param("areaId") UUID areaId,
            @Param("data") LocalDate data,
            @Param("inicio") LocalTime inicio,
            @Param("fim") LocalTime fim
    );

    @Query("""
        select r
        from Reserva r
        where r.area.id = :areaId
        and r.data = :data
        and r.status = br.com.dunnastecnologia.chamados.domain.model.StatusReserva.APROVADA
        and r.horaInicio < :fim
        and r.horaFim > :inicio
        and r.id <> :excluirReservaId
        order by r.horaInicio
    """)
    List<Reserva> buscarAprovadasConflitantesExcluindo(
            @Param("areaId") UUID areaId,
            @Param("data") LocalDate data,
            @Param("inicio") LocalTime inicio,
            @Param("fim") LocalTime fim,
            @Param("excluirReservaId") UUID excluirReservaId
    );

    // RF-05 / RN-01-10 - Morador só vê as próprias reservas.
    // Native query com cast explícito: filtros opcionais nulos (uuid/date) não são
    // tipáveis pelo PostgreSQL em "(:param is null or ...)" via JPQL.
    @Query(value = """
        select r.*
        from reservas r
        where r.morador_id = :moradorId
        and (cast(:areaComumId as uuid) is null or r.area_comum_id = cast(:areaComumId as uuid))
        and (cast(:data as date) is null or r.data = cast(:data as date))
        order by r.data desc, r.hora_inicio desc
        """,
            countQuery = """
        select count(*)
        from reservas r
        where r.morador_id = :moradorId
        and (cast(:areaComumId as uuid) is null or r.area_comum_id = cast(:areaComumId as uuid))
        and (cast(:data as date) is null or r.data = cast(:data as date))
        """,
            nativeQuery = true)
    Page<Reserva> buscarParaMorador(
            @Param("moradorId") UUID moradorId,
            @Param("areaComumId") UUID areaComumId,
            @Param("data") LocalDate data,
            Pageable pageable
    );

    // RF-05 / RN-01-10 - Administrador vê todas as reservas.
    @Query(value = """
        select r.*
        from reservas r
        where (cast(:areaComumId as uuid) is null or r.area_comum_id = cast(:areaComumId as uuid))
        and (cast(:data as date) is null or r.data = cast(:data as date))
        order by r.data desc, r.hora_inicio desc
        """,
            countQuery = """
        select count(*)
        from reservas r
        where (cast(:areaComumId as uuid) is null or r.area_comum_id = cast(:areaComumId as uuid))
        and (cast(:data as date) is null or r.data = cast(:data as date))
        """,
            nativeQuery = true)
    Page<Reserva> buscaraParaAdmin(
            @Param("areaComumId") UUID areaComumId,
            @Param("data") LocalDate data,
            Pageable pageable
    );

    List<Reserva> findByMoradorId(UUID moradorId);
}
