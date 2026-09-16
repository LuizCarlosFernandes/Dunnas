package br.com.dunnastecnologia.chamados.infrastructure.repository;

import br.com.dunnastecnologia.chamados.domain.model.AnexoChamado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnexoChamadoRepository extends JpaRepository<AnexoChamado, UUID> {

    /**
     * Lista os anexos vinculados a um chamado para exibicao paginada.
     */
    Page<AnexoChamado> findByChamadoId(UUID chamadoId, Pageable pageable);

    /**
     * Busca um anexo de um chamado especifico sem expor anexos de outros registros.
     */
    Optional<AnexoChamado> findByIdAndChamadoId(UUID id, UUID chamadoId);
}
