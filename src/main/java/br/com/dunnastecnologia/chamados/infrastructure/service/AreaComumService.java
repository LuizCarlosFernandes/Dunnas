package br.com.dunnastecnologia.chamados.infrastructure.service;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.application.UserCase.AdminAreaComumUseCases;
import br.com.dunnastecnologia.chamados.application.pagination.PageResult;
import br.com.dunnastecnologia.chamados.domain.model.AreaComum;
import br.com.dunnastecnologia.chamados.domain.validation.ValidationLimits;
import br.com.dunnastecnologia.chamados.infrastructure.exception.ResourceNotFoundException;
import br.com.dunnastecnologia.chamados.infrastructure.repository.AreaComumRepository;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.AuthenticatedUserValidator;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.InputValidationSupport;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.PageResultMapper;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AreaComumService implements AdminAreaComumUseCases {

    private final AreaComumRepository areaComumRepository;
    private final AuthenticatedUserValidator authenticatedUserValidator;

    public AreaComumService( AreaComumRepository areaComumRepository, AuthenticatedUserValidator authenticatedUserValidator) {
        this.areaComumRepository = areaComumRepository;
        this.authenticatedUserValidator = authenticatedUserValidator;
    }

    @Override
    @Transactional
    public AreaComum cadastrarAreaComum(AuthenticatedUser admin, String nome){
        authenticatedUserValidator.assertAdministrador(admin);
        AreaComum area = new AreaComum();
        area.setNome(normalizarNome(nome));
        area.setAtiva(Boolean.TRUE);
        return areaComumRepository.save(area);
    }

    @Override
    @Transactional
    public PageResult<AreaComum> listaAreasComum(AuthenticatedUser admin, PageRequest pageRequest){
        authenticatedUserValidator.assertAdministrador(admin);
        //findAll de propósito, admin precisa continuar gerenciado as áreas desativadas
        return PageResultMapper.fromPage(areaComumRepository.findAll(pageRequest));
    }

    @Override
    @Transactional
    public AreaComum buscarAreaComumPorId(AuthenticatedUser admin, UUID areaComumId){
        authenticatedUserValidator.assertAdministrador(admin);
        return buscarOuFalhar(areaComumId);
    }

    @Override
    @Transactional
    public AreaComum atualizarAreaComum(AuthenticatedUser admin, UUID areaComumId, String nome){
        authenticatedUserValidator.assertAdministrador(admin);

        AreaComum area = buscarOuFalhar(areaComumId);
        area.setNome(normalizarNome(nome));
        return areaComumRepository.save(area);
    }

    @Override
    @Transactional
    public AreaComum desativarAreaComum(AuthenticatedUser admin, UUID areaComumId){
        authenticatedUserValidator.assertAdministrador(admin);

        AreaComum area = buscarOuFalhar(areaComumId);

        //não deleta as reservas existentes, só bloqueia novas.
        area.setAtiva(Boolean.FALSE);
        return areaComumRepository.save(area);
    }

    @Override
    @Transactional
    public AreaComum reativarAreaComum(AuthenticatedUser admin, UUID areaComumId){
        authenticatedUserValidator.assertAdministrador(admin);

        AreaComum area = buscarOuFalhar(areaComumId);
        area.setAtiva(Boolean.TRUE);
        return areaComumRepository.save(area);
    }

    private AreaComum buscarOuFalhar(UUID areaComumId){
        return areaComumRepository.findById(areaComumId)
                .orElseThrow(()-> new ResourceNotFoundException("Área comum não encontrada."));
    }

    private String normalizarNome(String nome){
        return InputValidationSupport.normalizeRequiredText(
                nome,
                "Nome da área comum é obrigatório",
                 "Nome da área comum excede o tamanho máximo permitido",
                ValidationLimits.AREA_COMUM_NOME_MAX_LENGTH
        );
    }
}
