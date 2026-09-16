package br.com.dunnastecnologia.chamados.infrastructure.service;

import br.com.dunnastecnologia.chamados.infrastructure.repository.ChamadoRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(
        prefix = "app.chamado.atraso.scheduler",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class ChamadoAtrasoScheduler {

    private final ChamadoRepository chamadoRepository;

    public ChamadoAtrasoScheduler(ChamadoRepository chamadoRepository) {
        this.chamadoRepository = chamadoRepository;
    }

    @Scheduled(
            initialDelayString = "${app.chamado.atraso.scheduler.initial-delay-ms:30000}",
            fixedDelayString = "${app.chamado.atraso.scheduler.fixed-delay-ms:60000}"
    )
    @Transactional
    public void marcarChamadosAtrasados() {
        chamadoRepository.marcarChamadosAtrasados();
    }
}
