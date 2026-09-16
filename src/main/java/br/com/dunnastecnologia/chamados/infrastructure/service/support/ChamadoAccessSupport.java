package br.com.dunnastecnologia.chamados.infrastructure.service.support;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.domain.model.Chamado;
import br.com.dunnastecnologia.chamados.infrastructure.exception.BusinessRuleException;
import br.com.dunnastecnologia.chamados.infrastructure.exception.ResourceNotFoundException;
import br.com.dunnastecnologia.chamados.infrastructure.repository.ChamadoRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ChamadoAccessSupport {

    private final ChamadoRepository chamadoRepository;
    private final AuthenticatedUserValidator authenticatedUserValidator;

    public ChamadoAccessSupport(
            ChamadoRepository chamadoRepository,
            AuthenticatedUserValidator authenticatedUserValidator
    ) {
        this.chamadoRepository = chamadoRepository;
        this.authenticatedUserValidator = authenticatedUserValidator;
    }

    public Chamado findAccessibleChamado(AuthenticatedUser user, UUID chamadoId) {
        if (authenticatedUserValidator.isAdministrador(user)) {
            authenticatedUserValidator.assertAdministrador(user);
            return chamadoRepository.findByIdAndAdminId(user.id(), chamadoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Chamado nao encontrado para o administrador"));
        }

        if (authenticatedUserValidator.isColaborador(user)) {
            authenticatedUserValidator.assertColaborador(user);
            return chamadoRepository.findByIdAndColaboradorId(user.id(), chamadoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Chamado nao encontrado para o colaborador"));
        }

        authenticatedUserValidator.assertMorador(user);
        return chamadoRepository.findByIdAndMoradorId(chamadoId, user.id())
                .orElseThrow(() -> new ResourceNotFoundException("Chamado nao encontrado para o morador"));
    }

    public Chamado findAccessibleChamadoEmAberto(AuthenticatedUser user, UUID chamadoId) {
        Chamado chamado = findAccessibleChamado(user, chamadoId);
        assertChamadoEmAberto(chamado);
        return chamado;
    }

    public void assertChamadoEmAberto(Chamado chamado) {
        if (chamado.getDataFinalizacao() != null) {
            throw new BusinessRuleException("Chamados finalizados nao podem ser alterados");
        }
    }
}
