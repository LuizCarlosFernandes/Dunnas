package br.com.dunnastecnologia.chamados.infrastructure.repository;

import br.com.dunnastecnologia.chamados.domain.model.AnexoComentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnexoComentarioRepository extends JpaRepository<AnexoComentario, UUID> {

    Optional<AnexoComentario> findByIdAndComentarioId(UUID id, UUID comentarioId);
}
