package br.com.dunnastecnologia.chamados.infrastructure.repository;

import br.com.dunnastecnologia.chamados.domain.model.Reserva;
import br.com.dunnastecnologia.chamados.domain.model.StatusReserva;
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
     * Reservas aprovadas da mesma área/data cujo intervalo sobrepôe [inicio, fim).
     * Usado tanto para checar disponibilidade, quanto dentro da seção
     * Critica da aprovação (protegida pelo lock da área)
     */
    @Query("""
        select r
        from Reserva r
        where r.area.id = :areaID
        and r.data = :data
        and r.status = br.com.dunnastecnologia.chamados.domain.model.StatusReserva.APROVADA
        and r.horaInicio < :fim
        and r.horaFim > :inicio
        and (:excluidReservaId is null or r.id <> :excluirReservaId)
    """)
    List<Reserva> buscarAprovadasConflitantes(
            @Param("areaId")UUID areaId,
            @Param("data") LocalDate data,
            @Param("inicio") LocalTime inicio,
            @Param("fim") LocalTime fim,
            @Param("exluidReservaId") UUID excluidReservaId
    );

    List<Reserva> findByMoradorID(UUID moradorID);
}
