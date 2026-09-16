package br.com.dunnastecnologia.chamados.infrastructure.repository;

import br.com.dunnastecnologia.chamados.domain.model.Morador;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MoradorRepository extends JpaRepository<Morador, UUID> {

    @Query("""
            select m
            from Morador m
            where m.unidades is empty
              and m.ativo = true
            """)
    Page<Morador> findByUnidadesIsEmpty(Pageable pageable);

    @Query("""
            select m
            from Morador m
            where m.ativo = true
            """)
    Page<Morador> findAllActive(Pageable pageable);

    @Query("""
            select m
            from Morador m
            where lower(m.email) like lower(concat(:prefixoEmail, '%'))
              and m.ativo = true
            """)
    Page<Morador> findByEmailStartingWithIgnoreCase(
            @Param("prefixoEmail") String prefixoEmail,
            Pageable pageable
    );

    @Query("""
            select m
            from Morador m
            where m.unidades is empty
              and lower(m.email) like lower(concat(:prefixoEmail, '%'))
              and m.ativo = true
            """)
    Page<Morador> findByUnidadesIsEmptyAndEmailStartingWithIgnoreCase(
            @Param("prefixoEmail") String prefixoEmail,
            Pageable pageable
    );

    /**
     * Busca um morador pelo e-mail para autenticacao e validacoes cadastrais.
     */
    @Query("""
            select m
            from Morador m
            where lower(m.email) = lower(:email)
              and m.ativo = true
            """)
    Optional<Morador> findByEmail(@Param("email") String email);

    Optional<Morador> findByIdAndAtivoTrue(UUID id);

    boolean existsByIdAndAtivoTrue(UUID id);

    /**
     * Verifica no banco se o morador esta vinculado a unidade informada.
     */
    @Query("""
            select count(m) > 0
            from Morador m
            join m.unidades u
            where m.id = :moradorId
              and m.ativo = true
              and u.id = :unidadeId
            """)
    boolean existsByIdAndUnidadeId(
            @Param("moradorId") UUID moradorId,
            @Param("unidadeId") UUID unidadeId
    );

    @Query("""
            select u.id, m
            from Morador m
            join m.unidades u
            where m.ativo = true
              and u.id in :unidadeIds
            order by lower(m.nome)
            """)
    List<Object[]> findActiveByUnidadeIds(@Param("unidadeIds") List<UUID> unidadeIds);
}
