package br.com.dunnastecnologia.chamados.infrastructure.service;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.application.UserCase.TipoChamadoUseCase;
import br.com.dunnastecnologia.chamados.application.pagination.PageResult;
import br.com.dunnastecnologia.chamados.domain.model.TipoChamado;
import br.com.dunnastecnologia.chamados.infrastructure.exception.BusinessRuleException;
import br.com.dunnastecnologia.chamados.infrastructure.exception.ResourceNotFoundException;
import br.com.dunnastecnologia.chamados.infrastructure.repository.TipoChamadoRepository;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.AuthenticatedUserValidator;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.InputValidationSupport;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.PageResultMapper;
import br.com.dunnastecnologia.chamados.domain.validation.ValidationLimits;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TipoChamadoService implements TipoChamadoUseCase {

    private final TipoChamadoRepository tipoChamadoRepository;
    private final AuthenticatedUserValidator authenticatedUserValidator;

    public TipoChamadoService(
            TipoChamadoRepository tipoChamadoRepository,
            AuthenticatedUserValidator authenticatedUserValidator
    ) {
        this.tipoChamadoRepository = tipoChamadoRepository;
        this.authenticatedUserValidator = authenticatedUserValidator;
    }

    @Override
    @Transactional
    public TipoChamado cadastrarTipoChamado(AuthenticatedUser admin, String titulo, int prazoHoras) {
        authenticatedUserValidator.assertAdministrador(admin);
        String tituloNormalizado = validateTipoChamado(titulo, prazoHoras);

        tipoChamadoRepository.findByTitulo(tituloNormalizado)
                .ifPresent(existing -> {
                    throw new BusinessRuleException("Ja existe um tipo de chamado com este titulo");
                });

        TipoChamado tipoChamado = new TipoChamado();
        tipoChamado.setTitulo(tituloNormalizado);
        tipoChamado.setPrazoHoras(prazoHoras);
        return tipoChamadoRepository.save(tipoChamado);
    }

    @Override
    public PageResult<TipoChamado> listarTiposChamado(PageRequest pageRequest) {
        return PageResultMapper.fromPage(tipoChamadoRepository.findAll(pageRequest));
    }

    @Override
    public TipoChamado buscarTipoChamadoPorId(UUID tipoId) {
        return tipoChamadoRepository.findById(tipoId)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de chamado nao encontrado"));
    }

    @Override
    @Transactional
    public TipoChamado atualizarTipoChamado(AuthenticatedUser admin, UUID tipoId, String titulo, int prazoHoras) {
        authenticatedUserValidator.assertAdministrador(admin);
        String tituloNormalizado = validateTipoChamado(titulo, prazoHoras);

        TipoChamado tipoChamado = buscarTipoChamadoPorId(tipoId);
        tipoChamadoRepository.findByTitulo(tituloNormalizado)
                .filter(existing -> !existing.getId().equals(tipoId))
                .ifPresent(existing -> {
                    throw new BusinessRuleException("Ja existe um tipo de chamado com este titulo");
                });

        tipoChamado.setTitulo(tituloNormalizado);
        tipoChamado.setPrazoHoras(prazoHoras);
        return tipoChamadoRepository.save(tipoChamado);
    }

    private String validateTipoChamado(String titulo, int prazoHoras) {
        String tituloNormalizado = InputValidationSupport.normalizeRequiredText(
                titulo,
                "Titulo do tipo de chamado e obrigatorio",
                "Titulo do tipo de chamado deve ter no maximo 255 caracteres",
                ValidationLimits.TIPO_CHAMADO_TITULO_MAX_LENGTH
        );
        if (prazoHoras <= 0) {
            throw new BusinessRuleException("Prazo do tipo de chamado deve ser maior que zero");
        }
        return tituloNormalizado;
    }
}
