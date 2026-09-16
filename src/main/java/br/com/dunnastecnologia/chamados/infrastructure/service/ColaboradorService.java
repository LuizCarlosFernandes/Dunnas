package br.com.dunnastecnologia.chamados.infrastructure.service;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.application.UserCase.ColaboradorUseCases;
import br.com.dunnastecnologia.chamados.application.pagination.PageResult;
import br.com.dunnastecnologia.chamados.domain.model.Chamado;
import br.com.dunnastecnologia.chamados.domain.model.Comentario;
import br.com.dunnastecnologia.chamados.domain.model.StatusChamado;
import br.com.dunnastecnologia.chamados.domain.model.TipoChamado;
import br.com.dunnastecnologia.chamados.infrastructure.repository.ColaboradorRepository;
import br.com.dunnastecnologia.chamados.infrastructure.repository.StatusChamadoRepository;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.AuthenticatedUserValidator;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.PageResultMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ColaboradorService implements ColaboradorUseCases {

    private final StatusChamadoRepository statusChamadoRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final ChamadoService chamadoService;
    private final ComentarioService comentarioService;
    private final AuthenticatedUserValidator authenticatedUserValidator;

    public ColaboradorService(
            StatusChamadoRepository statusChamadoRepository,
            ColaboradorRepository colaboradorRepository,
            ChamadoService chamadoService,
            ComentarioService comentarioService,
            AuthenticatedUserValidator authenticatedUserValidator
    ) {
        this.statusChamadoRepository = statusChamadoRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.chamadoService = chamadoService;
        this.comentarioService = comentarioService;
        this.authenticatedUserValidator = authenticatedUserValidator;
    }

    @Override
    public PageResult<StatusChamado> listarStatusDisponiveis(AuthenticatedUser colaborador, PageRequest pageRequest) {
        authenticatedUserValidator.assertColaborador(colaborador);
        return PageResultMapper.fromPage(statusChamadoRepository.findAll(pageRequest));
    }

    @Override
    public PageResult<TipoChamado> listarTiposChamadoDisponiveis(AuthenticatedUser colaborador, PageRequest pageRequest) {
        authenticatedUserValidator.assertColaborador(colaborador);
        return PageResultMapper.fromPage(colaboradorRepository.findTiposChamadoByColaboradorId(colaborador.id(), pageRequest));
    }

    @Override
    public PageResult<Chamado> buscarChamados(
            AuthenticatedUser colaborador,
            UUID statusId,
            UUID tipoChamadoId,
            String unidadeIdentificacao,
            LocalDate dataAbertura,
            PageRequest pageRequest
    ) {
        return chamadoService.listarChamadosParaColaborador(
                colaborador,
                statusId,
                tipoChamadoId,
                unidadeIdentificacao,
                dataAbertura,
                pageRequest
        );
    }

    @Override
    public Chamado buscarChamadoPorId(AuthenticatedUser colaborador, UUID chamadoId) {
        return chamadoService.buscarChamadoParaColaborador(colaborador, chamadoId);
    }

    @Override
    @Transactional
    public Chamado atualizarStatusChamado(AuthenticatedUser colaborador, UUID chamadoId, UUID statusId) {
        return chamadoService.atualizarStatusComoColaborador(colaborador, chamadoId, statusId);
    }

    @Override
    @Transactional
    public Comentario comentarChamado(AuthenticatedUser colaborador, UUID chamadoId, String mensagem) {
        return comentarioService.comentar(colaborador, chamadoId, mensagem);
    }
}
