package br.com.dunnastecnologia.chamados.infrastructure.repository;

import br.com.dunnastecnologia.chamados.domain.model.StatusChamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StatusChamadoRepository extends JpaRepository<StatusChamado, UUID> {

    /**
     * Busca um status pelo nome para evitar duplicidade e apoiar regras de fluxo.
     */
    @Query("""
            select s
            from StatusChamado s
            where lower(s.nome) = lower(:nome)
            """)
    Optional<StatusChamado> findByNome(@Param("nome") String nome);

    /**
     * Busca o status inicial padrao definido para novos chamados.
     */
    Optional<StatusChamado> findByInicialPadraoTrue();

    /**
     * Limpa o status inicial atual antes de definir um novo padrao.
     */
    @Modifying
    @Query("""
            update StatusChamado s
            set s.inicialPadrao = false
            where s.inicialPadrao = true
            """)
    void clearStatusInicialPadrao();
}
