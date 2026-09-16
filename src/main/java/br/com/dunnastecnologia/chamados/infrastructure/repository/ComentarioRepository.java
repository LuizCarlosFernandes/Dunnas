package br.com.dunnastecnologia.chamados.infrastructure.repository;

import br.com.dunnastecnologia.chamados.domain.model.Comentario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, UUID> {

    Optional<Comentario> findByIdAndChamadoId(UUID id, UUID chamadoId);

    /**
     * Lista os comentarios de um chamado em ordem cronologica usando funcao SQL dedicada.
     */
    @Query(value = """
            select *
            from fn_listar_comentarios_do_chamado(:chamadoId)
            """,
            countQuery = """
            select count(*)
            from fn_listar_comentarios_do_chamado(:chamadoId)
            """,
            nativeQuery = true)
    Page<Comentario> findByChamadoId(@Param("chamadoId") UUID chamadoId, Pageable pageable);
}
