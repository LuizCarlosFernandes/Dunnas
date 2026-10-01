package br.com.dunnastecnologia.chamados.infrastructure.repository;

import br.com.dunnastecnologia.chamados.domain.model.AreaComum;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AreaComumRepository extends JpaRepository<AreaComum, UUID>{
    //RF-02 - área que o morador pode escolher para solicitar reserva.
    Page<AreaComum> findByAtivaTrue(Pageable pageable);

    /**
     * Trava a linha da área até o fim da transação atual
     * Só deve ser chamado dentro de um métod @Transacional que decide uma aprovação
     */

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({ @QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000") })
    @Query("select a from AreaComum a where a.id = :id")
    Optional<AreaComum> buscarComLockParaDecisao(@Param("id") UUID id);
}
