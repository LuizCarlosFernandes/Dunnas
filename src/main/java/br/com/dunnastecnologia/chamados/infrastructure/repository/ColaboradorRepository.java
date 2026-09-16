package br.com.dunnastecnologia.chamados.infrastructure.repository;

import br.com.dunnastecnologia.chamados.domain.model.Colaborador;
import br.com.dunnastecnologia.chamados.domain.model.TipoChamado;
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
public interface ColaboradorRepository extends JpaRepository<Colaborador, UUID> {

    /**
     * Busca um colaborador pelo e-mail para autenticacao e validacoes cadastrais.
     */
    @Query("""
            select c
            from Colaborador c
            where lower(c.email) = lower(:email)
              and c.ativo = true
            """)
    Optional<Colaborador> findByEmail(@Param("email") String email);

    @Query("""
            select c
            from Colaborador c
            where c.ativo = true
            order by c.nome, c.id
            """)
    Page<Colaborador> findAllActive(Pageable pageable);

    @Query("""
            select c
            from Colaborador c
            where c.ativo = true
              and lower(c.email) like lower(concat(:prefixoEmail, '%'))
            order by c.nome, c.id
            """)
    Page<Colaborador> findByEmailStartingWithIgnoreCase(
            @Param("prefixoEmail") String prefixoEmail,
            Pageable pageable
    );

    @Query("""
            select distinct c
            from Colaborador c
            left join fetch c.tiposChamadoResponsaveis
            where c.id = :id
              and c.ativo = true
            """)
    Optional<Colaborador> findByIdAndAtivoTrue(@Param("id") UUID id);

    @Query("""
            select t
            from Colaborador c
            join c.tiposChamadoResponsaveis t
            where c.id = :colaboradorId
              and c.ativo = true
            order by t.titulo, t.id
            """)
    Page<TipoChamado> findTiposChamadoByColaboradorId(
            @Param("colaboradorId") UUID colaboradorId,
            Pageable pageable
    );

    @Query("""
            select t
            from Colaborador c
            join c.tiposChamadoResponsaveis t
            where c.id = :colaboradorId
              and c.ativo = true
            order by t.titulo, t.id
            """)
    List<TipoChamado> findTiposChamadoByColaboradorId(@Param("colaboradorId") UUID colaboradorId);

    boolean existsByIdAndAtivoTrue(UUID id);
}
