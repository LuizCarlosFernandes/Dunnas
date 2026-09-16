package br.com.dunnastecnologia.chamados.infrastructure.repository;

import br.com.dunnastecnologia.chamados.domain.model.Unidade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UnidadeRepository extends JpaRepository<Unidade, UUID> {

    /**
     * Lista as unidades de um bloco em ordem logica de andar e identificacao usando funcao SQL.
     */
    @Query(value = """
            select *
            from fn_listar_unidades_do_bloco(:blocoId)
            """,
            countQuery = """
            select count(*)
            from fn_listar_unidades_do_bloco(:blocoId)
            """,
            nativeQuery = true)
    Page<Unidade> findByBlocoId(@Param("blocoId") UUID blocoId, Pageable pageable);

    /**
     * Lista as unidades vinculadas ao morador usando funcao SQL baseada na tabela de associacao.
     */
    @Query(value = """
            select *
            from fn_listar_unidades_do_morador(:moradorId)
            """,
            countQuery = """
            select count(*)
            from fn_listar_unidades_do_morador(:moradorId)
            """,
            nativeQuery = true)
    Page<Unidade> findByMoradorId(@Param("moradorId") UUID moradorId, Pageable pageable);
}
