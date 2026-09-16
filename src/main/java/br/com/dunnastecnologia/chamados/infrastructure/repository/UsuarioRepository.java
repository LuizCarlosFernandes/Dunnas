package br.com.dunnastecnologia.chamados.infrastructure.repository;

import br.com.dunnastecnologia.chamados.domain.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    /**
     * Busca um usuario pelo e-mail para autenticacao e garantia de unicidade.
     */
    @Query("""
            select u
            from Usuario u
            where lower(u.email) = lower(:email)
              and u.ativo = true
            """)
    Optional<Usuario> findActiveByEmail(@Param("email") String email);

    @Query("""
            select u
            from Usuario u
            where lower(u.email) = lower(:email)
            """)
    Optional<Usuario> findByEmail(@Param("email") String email);

    @Query("""
            select u
            from Usuario u
            where u.ativo = true
            """)
    Page<Usuario> findAllActive(Pageable pageable);

    @Query("""
            select u
            from Usuario u
            where u.id = :usuarioId
              and u.ativo = true
            """)
    Optional<Usuario> findByIdAndAtivoTrue(@Param("usuarioId") UUID usuarioId);

    @Query("""
            select count(a) > 0
            from Administrador a
            where a.ativo = true
            """)
    boolean existsAdministrador();

    boolean existsByEmailIgnoreCase(String email);
}
