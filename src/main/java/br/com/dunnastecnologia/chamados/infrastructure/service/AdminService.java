package br.com.dunnastecnologia.chamados.infrastructure.service;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.application.UserCase.AdminUseCases;
import br.com.dunnastecnologia.chamados.application.pagination.PageResult;
import br.com.dunnastecnologia.chamados.domain.model.Bloco;
import br.com.dunnastecnologia.chamados.domain.model.Chamado;
import br.com.dunnastecnologia.chamados.domain.model.Colaborador;
import br.com.dunnastecnologia.chamados.domain.model.Comentario;
import br.com.dunnastecnologia.chamados.domain.model.Morador;
import br.com.dunnastecnologia.chamados.domain.model.StatusChamado;
import br.com.dunnastecnologia.chamados.domain.model.TipoChamado;
import br.com.dunnastecnologia.chamados.domain.model.Unidade;
import br.com.dunnastecnologia.chamados.domain.model.Usuario;
import br.com.dunnastecnologia.chamados.infrastructure.exception.BusinessRuleException;
import br.com.dunnastecnologia.chamados.infrastructure.exception.ResourceNotFoundException;
import br.com.dunnastecnologia.chamados.infrastructure.repository.BlocoRepository;
import br.com.dunnastecnologia.chamados.infrastructure.repository.MoradorRepository;
import br.com.dunnastecnologia.chamados.infrastructure.repository.UnidadeRepository;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.AuthenticatedUserValidator;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.InputValidationSupport;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.PageResultMapper;
import br.com.dunnastecnologia.chamados.domain.validation.ValidationLimits;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AdminService implements AdminUseCases {

    private final BlocoRepository blocoRepository;
    private final MoradorRepository moradorRepository;
    private final UnidadeRepository unidadeRepository;
    private final UsuarioService usuarioService;
    private final TipoChamadoService tipoChamadoService;
    private final StatusChamadoService statusChamadoService;
    private final ChamadoService chamadoService;
    private final ComentarioService comentarioService;
    private final AuthenticatedUserValidator authenticatedUserValidator;

    public AdminService(
            BlocoRepository blocoRepository,
            MoradorRepository moradorRepository,
            UnidadeRepository unidadeRepository,
            UsuarioService usuarioService,
            TipoChamadoService tipoChamadoService,
            StatusChamadoService statusChamadoService,
            ChamadoService chamadoService,
            ComentarioService comentarioService,
            AuthenticatedUserValidator authenticatedUserValidator
    ) {
        this.blocoRepository = blocoRepository;
        this.moradorRepository = moradorRepository;
        this.unidadeRepository = unidadeRepository;
        this.usuarioService = usuarioService;
        this.tipoChamadoService = tipoChamadoService;
        this.statusChamadoService = statusChamadoService;
        this.chamadoService = chamadoService;
        this.comentarioService = comentarioService;
        this.authenticatedUserValidator = authenticatedUserValidator;
    }

    @Override
    @Transactional
    public Bloco cadastrarBloco(AuthenticatedUser admin, String identificacao, int quantidadeAndares, int apartamentosPorAndar) {
        authenticatedUserValidator.assertAdministrador(admin);
        String identificacaoNormalizada = InputValidationSupport.normalizeRequiredText(
                identificacao,
                "Identificacao do bloco e obrigatoria",
                "Identificacao do bloco deve ter no maximo 255 caracteres",
                ValidationLimits.BLOCO_IDENTIFICACAO_MAX_LENGTH
        );
        if (quantidadeAndares <= 0 || apartamentosPorAndar <= 0) {
            throw new BusinessRuleException("Quantidade de andares e apartamentos por andar deve ser maior que zero");
        }

        Bloco bloco = new Bloco();
        bloco.setIdentificacao(identificacaoNormalizada);
        bloco.setQuantidadeAndares(quantidadeAndares);
        bloco.setApartamentosPorAndar(apartamentosPorAndar);

        Bloco savedBloco = blocoRepository.save(bloco);
        blocoRepository.gerarUnidadesDoBloco(admin.id(), savedBloco.getId());
        return blocoRepository.findById(savedBloco.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Bloco nao encontrado apos cadastro"));
    }

    @Override
    public Bloco buscarBlocoPorId(UUID blocoId) {
        return blocoRepository.findById(blocoId)
                .orElseThrow(() -> new ResourceNotFoundException("Bloco nao encontrado"));
    }

    @Override
    public PageResult<Bloco> listarBlocos(PageRequest pageRequest) {
        return PageResultMapper.fromPage(blocoRepository.findAll(pageRequest));
    }

    @Override
    public PageResult<Unidade> listarUnidadesDoBloco(UUID blocoId, PageRequest pageRequest) {
        return PageResultMapper.fromPage(unidadeRepository.findByBlocoId(blocoId, pageRequest));
    }

    @Override
    public Map<UUID, List<Morador>> listarMoradoresPorUnidadeIds(List<UUID> unidadeIds) {
        if (unidadeIds == null || unidadeIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, List<Morador>> moradoresPorUnidade = new LinkedHashMap<>();
        for (Object[] row : moradorRepository.findActiveByUnidadeIds(unidadeIds)) {
            UUID unidadeId = (UUID) row[0];
            Morador morador = (Morador) row[1];
            moradoresPorUnidade.computeIfAbsent(unidadeId, ignored -> new ArrayList<>()).add(morador);
        }
        return moradoresPorUnidade;
    }

    @Override
    public Unidade buscarUnidadePorId(UUID unidadeId) {
        return unidadeRepository.findById(unidadeId)
                .orElseThrow(() -> new ResourceNotFoundException("Unidade nao encontrada"));
    }

    @Override
    @Transactional
    public Usuario cadastrarUsuario(AuthenticatedUser admin, Usuario usuario) {
        return usuarioService.cadastrarUsuario(admin, usuario);
    }

    @Override
    @Transactional
    public Usuario atualizarUsuario(AuthenticatedUser admin, UUID usuarioId, Usuario usuario) {
        return usuarioService.atualizarUsuario(admin, usuarioId, usuario);
    }

    @Override
    public PageResult<Usuario> listarUsuarios(PageRequest pageRequest) {
        return usuarioService.listarUsuarios(pageRequest);
    }

    @Override
    public PageResult<Morador> listarMoradores(PageRequest pageRequest) {
        return PageResultMapper.fromPage(moradorRepository.findAllActive(pageRequest));
    }

    @Override
    public PageResult<Morador> listarMoradoresPorPrefixoEmail(String prefixoEmail, PageRequest pageRequest) {
        if (prefixoEmail == null || prefixoEmail.isBlank()) {
            return listarMoradores(pageRequest);
        }
        return PageResultMapper.fromPage(moradorRepository.findByEmailStartingWithIgnoreCase(prefixoEmail.trim(), pageRequest));
    }

    @Override
    public PageResult<Morador> listarMoradoresSemUnidade(PageRequest pageRequest) {
        return PageResultMapper.fromPage(moradorRepository.findByUnidadesIsEmpty(pageRequest));
    }

    @Override
    public PageResult<Morador> listarMoradoresSemUnidadePorPrefixoEmail(String prefixoEmail, PageRequest pageRequest) {
        if (prefixoEmail == null || prefixoEmail.isBlank()) {
            return listarMoradoresSemUnidade(pageRequest);
        }
        return PageResultMapper.fromPage(
                moradorRepository.findByUnidadesIsEmptyAndEmailStartingWithIgnoreCase(prefixoEmail.trim(), pageRequest)
        );
    }

    @Override
    public PageResult<Colaborador> listarColaboradores(PageRequest pageRequest) {
        return usuarioService.listarColaboradores(pageRequest);
    }

    @Override
    public PageResult<Colaborador> listarColaboradoresPorPrefixoEmail(String prefixoEmail, PageRequest pageRequest) {
        return usuarioService.listarColaboradoresPorPrefixoEmail(prefixoEmail, pageRequest);
    }

    @Override
    public Usuario buscarUsuarioPorId(UUID usuarioId) {
        return usuarioService.buscarUsuarioPorId(usuarioId);
    }

    @Override
    @Transactional
    public void removerUsuario(AuthenticatedUser admin, UUID usuarioId) {
        usuarioService.removerUsuario(admin, usuarioId);
    }

    @Override
    @Transactional
    public void vincularMoradorUnidade(AuthenticatedUser admin, UUID moradorId, UUID unidadeId) {
        usuarioService.vincularMoradorUnidade(admin, moradorId, unidadeId);
    }

    @Override
    @Transactional
    public void desvincularMoradorUnidade(AuthenticatedUser admin, UUID moradorId, UUID unidadeId) {
        usuarioService.desvincularMoradorUnidade(admin, moradorId, unidadeId);
    }

    @Override
    @Transactional
    public void vincularColaboradorTipoChamado(AuthenticatedUser admin, UUID colaboradorId, UUID tipoChamadoId) {
        usuarioService.vincularColaboradorTipoChamado(admin, colaboradorId, tipoChamadoId);
    }

    @Override
    @Transactional
    public void desvincularColaboradorTipoChamado(AuthenticatedUser admin, UUID colaboradorId, UUID tipoChamadoId) {
        usuarioService.desvincularColaboradorTipoChamado(admin, colaboradorId, tipoChamadoId);
    }

    @Override
    public PageResult<TipoChamado> listarTiposChamadoDoColaborador(UUID colaboradorId, PageRequest pageRequest) {
        return usuarioService.listarTiposChamadoDoColaborador(colaboradorId, pageRequest);
    }

    @Override
    public PageResult<Unidade> listarUnidadesDoMorador(UUID moradorId, PageRequest pageRequest) {
        return PageResultMapper.fromPage(unidadeRepository.findByMoradorId(moradorId, pageRequest));
    }

    @Override
    @Transactional
    public TipoChamado cadastrarTipoChamado(AuthenticatedUser admin, String titulo, int prazoHoras) {
        return tipoChamadoService.cadastrarTipoChamado(admin, titulo, prazoHoras);
    }

    @Override
    public PageResult<TipoChamado> listarTiposChamado(PageRequest pageRequest) {
        return tipoChamadoService.listarTiposChamado(pageRequest);
    }

    @Override
    public TipoChamado buscarPorId(UUID tipoId) {
        return tipoChamadoService.buscarTipoChamadoPorId(tipoId);
    }

    @Override
    @Transactional
    public TipoChamado atualizarTipoChamado(AuthenticatedUser admin, UUID tipoId, String titulo, int prazoHoras) {
        return tipoChamadoService.atualizarTipoChamado(admin, tipoId, titulo, prazoHoras);
    }

    @Override
    @Transactional
    public StatusChamado cadastrarStatus(AuthenticatedUser admin, String nome) {
        return statusChamadoService.cadastrarStatus(admin, nome);
    }

    @Override
    public PageResult<StatusChamado> listarStatus(PageRequest pageRequest) {
        return statusChamadoService.listarStatus(pageRequest);
    }

    @Override
    public StatusChamado buscarStatusPorId(UUID statusId) {
        return statusChamadoService.buscarStatusPorId(statusId);
    }

    @Override
    @Transactional
    public StatusChamado atualizarStatus(AuthenticatedUser admin, UUID statusId, String nome) {
        return statusChamadoService.atualizarStatus(admin, statusId, nome);
    }

    @Override
    @Transactional
    public void definirStatusInicialPadrao(AuthenticatedUser admin, UUID statusId) {
        statusChamadoService.definirStatusInicialPadrao(admin, statusId);
    }

    @Override
    public PageResult<Chamado> buscarChamados(
            AuthenticatedUser admin,
            UUID statusId,
            String moradorNome,
            LocalDate dataAbertura,
            PageRequest pageRequest
    ) {
        return chamadoService.listarChamadosParaAdmin(admin, statusId, moradorNome, dataAbertura, pageRequest);
    }

    @Override
    public Chamado buscarChamadoPorId(AuthenticatedUser admin, UUID chamadoId) {
        return chamadoService.buscarChamadoParaAdmin(admin, chamadoId);
    }

    @Override
    @Transactional
    public Chamado atualizarStatusChamado(AuthenticatedUser admin, UUID chamadoId, UUID statusId) {
        return chamadoService.atualizarStatusComoAdmin(admin, chamadoId, statusId);
    }

    @Override
    @Transactional
    public Comentario comentarChamado(AuthenticatedUser admin, UUID chamadoId, String mensagem) {
        return comentarioService.comentar(admin, chamadoId, mensagem);
    }
}
