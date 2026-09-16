package br.com.dunnastecnologia.chamados.infrastructure.service;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.application.UserCase.StatusChamadoUseCase;
import br.com.dunnastecnologia.chamados.application.pagination.PageResult;
import br.com.dunnastecnologia.chamados.domain.model.StatusChamado;
import br.com.dunnastecnologia.chamados.infrastructure.exception.BusinessRuleException;
import br.com.dunnastecnologia.chamados.infrastructure.exception.ResourceNotFoundException;
import br.com.dunnastecnologia.chamados.infrastructure.repository.StatusChamadoRepository;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.AuthenticatedUserValidator;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.InputValidationSupport;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.PageResultMapper;
import br.com.dunnastecnologia.chamados.domain.validation.ValidationLimits;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class StatusChamadoService implements StatusChamadoUseCase {

    private static final Set<String> STATUS_RESERVADOS = Set.of("Finalizado", "Atrasado", "Solicitado");

    private final StatusChamadoRepository statusChamadoRepository;
    private final AuthenticatedUserValidator authenticatedUserValidator;

    public StatusChamadoService(
            StatusChamadoRepository statusChamadoRepository,
            AuthenticatedUserValidator authenticatedUserValidator
    ) {
        this.statusChamadoRepository = statusChamadoRepository;
        this.authenticatedUserValidator = authenticatedUserValidator;
    }

    @Override
    @Transactional
    public StatusChamado cadastrarStatus(AuthenticatedUser admin, String nome) {
        authenticatedUserValidator.assertAdministrador(admin);
        String nomeNormalizado = validateNome(nome);

        statusChamadoRepository.findByNome(nomeNormalizado)
                .ifPresent(existing -> {
                    throw new BusinessRuleException("Ja existe um status com este nome");
                });

        StatusChamado status = new StatusChamado();
        status.setNome(nomeNormalizado);
        status.setInicialPadrao(Boolean.FALSE);
        return statusChamadoRepository.save(status);
    }

    @Override
    public PageResult<StatusChamado> listarStatus(PageRequest pageRequest) {
        return PageResultMapper.fromPage(statusChamadoRepository.findAll(pageRequest));
    }

    @Override
    public StatusChamado buscarStatusPorId(UUID statusId) {
        return statusChamadoRepository.findById(statusId)
                .orElseThrow(() -> new ResourceNotFoundException("Status de chamado nao encontrado"));
    }

    @Override
    @Transactional
    public StatusChamado atualizarStatus(AuthenticatedUser admin, UUID statusId, String nome) {
        authenticatedUserValidator.assertAdministrador(admin);
        String nomeNormalizado = validateNome(nome);

        StatusChamado status = buscarStatusPorId(statusId);
        if (isStatusReservado(status.getNome())) {
            throw new BusinessRuleException("Nao e permitido editar os status reservados do sistema");
        }
        statusChamadoRepository.findByNome(nomeNormalizado)
                .filter(existing -> !existing.getId().equals(statusId))
                .ifPresent(existing -> {
                    throw new BusinessRuleException("Ja existe um status com este nome");
                });

        status.setNome(nomeNormalizado);
        return statusChamadoRepository.save(status);
    }

    @Override
    @Transactional
    public void definirStatusInicialPadrao(AuthenticatedUser admin, UUID statusId) {
        authenticatedUserValidator.assertAdministrador(admin);

        StatusChamado status = buscarStatusPorId(statusId);
        statusChamadoRepository.clearStatusInicialPadrao();
        status.setInicialPadrao(Boolean.TRUE);
        statusChamadoRepository.save(status);
    }

    private String validateNome(String nome) {
        return InputValidationSupport.normalizeRequiredText(
                nome,
                "Nome do status e obrigatorio",
                "Nome do status deve ter no maximo 255 caracteres",
                ValidationLimits.STATUS_CHAMADO_NOME_MAX_LENGTH
        );
    }

    private boolean isStatusReservado(String nome) {
        if (nome == null) {
            return false;
        }
        return STATUS_RESERVADOS.stream().anyMatch(reservado -> reservado.equalsIgnoreCase(nome));
    }
}
