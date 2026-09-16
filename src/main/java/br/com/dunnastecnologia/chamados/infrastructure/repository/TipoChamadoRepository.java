package br.com.dunnastecnologia.chamados.infrastructure.repository;

import br.com.dunnastecnologia.chamados.domain.model.TipoChamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TipoChamadoRepository extends JpaRepository<TipoChamado, UUID> {

    /**
     * Busca um tipo de chamado pelo titulo para evitar duplicidade de catalogo.
     */
    @Query("""
            select t
            from TipoChamado t
            where lower(t.titulo) = lower(:titulo)
            """)
    Optional<TipoChamado> findByTitulo(@Param("titulo") String titulo);

    @Query("""
            select t
            from TipoChamado t
            left join t.colaboradoresResponsaveis c
            where c.id = :colaboradorId
            order by t.titulo, t.id
            """)
    Page<TipoChamado> findByColaboradorId(@Param("colaboradorId") UUID colaboradorId, Pageable pageable);
}
