package br.com.dunnastecnologia.chamados.infrastructure.service;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.application.UserCase.MoradorUseCases;
import br.com.dunnastecnologia.chamados.application.pagination.PageResult;
import br.com.dunnastecnologia.chamados.domain.model.Chamado;
import br.com.dunnastecnologia.chamados.domain.model.Comentario;
import br.com.dunnastecnologia.chamados.domain.model.Unidade;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.AuthenticatedUserValidator;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.PageResultMapper;
import br.com.dunnastecnologia.chamados.infrastructure.repository.UnidadeRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class MoradorService implements MoradorUseCases {

    private final UnidadeRepository unidadeRepository;
    private final ChamadoService chamadoService;
    private final ComentarioService comentarioService;
    private final AuthenticatedUserValidator authenticatedUserValidator;

    public MoradorService(
            UnidadeRepository unidadeRepository,
            ChamadoService chamadoService,
            ComentarioService comentarioService,
            AuthenticatedUserValidator authenticatedUserValidator
    ) {
        this.unidadeRepository = unidadeRepository;
        this.chamadoService = chamadoService;
        this.comentarioService = comentarioService;
        this.authenticatedUserValidator = authenticatedUserValidator;
    }

    @Override
    public PageResult<Unidade> listarMinhasUnidades(AuthenticatedUser morador, PageRequest pageRequest) {
        authenticatedUserValidator.assertMorador(morador);
        return PageResultMapper.fromPage(unidadeRepository.findByMoradorId(morador.id(), pageRequest));
    }

    @Override
    @Transactional
    public Chamado abrirChamado(AuthenticatedUser morador, UUID unidadeId, UUID tipoChamadoId, String descricao) {
        return chamadoService.abrirChamado(morador, unidadeId, tipoChamadoId, descricao);
    }

    @Override
    public PageResult<Chamado> listarMeusChamados(
            AuthenticatedUser morador,
            UUID statusId,
            UUID unidadeId,
            UUID tipoChamadoId,
            LocalDate dataAbertura,
            PageRequest pageRequest
    ) {
        return chamadoService.listarChamadosDoMorador(morador, statusId, unidadeId, tipoChamadoId, dataAbertura, pageRequest);
    }

    @Override
    public Chamado buscarMeuChamadoPorId(AuthenticatedUser morador, UUID chamadoId) {
        return chamadoService.buscarChamadoDoMorador(morador, chamadoId);
    }

    @Override
    @Transactional
    public Comentario comentarChamado(AuthenticatedUser morador, UUID chamadoId, String mensagem) {
        return comentarioService.comentar(morador, chamadoId, mensagem);
    }

    @Override
    @Transactional
    public Chamado reabrirChamado(AuthenticatedUser morador, UUID chamadoId) {
        return chamadoService.reabrirComoMorador(morador, chamadoId);
    }
}
