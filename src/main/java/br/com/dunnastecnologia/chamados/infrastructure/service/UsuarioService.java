package br.com.dunnastecnologia.chamados.infrastructure.service;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.application.UserCase.UsuarioUseCase;
import br.com.dunnastecnologia.chamados.application.pagination.PageResult;
import br.com.dunnastecnologia.chamados.domain.model.Colaborador;
import br.com.dunnastecnologia.chamados.domain.model.Morador;
import br.com.dunnastecnologia.chamados.domain.model.TipoChamado;
import br.com.dunnastecnologia.chamados.domain.model.Unidade;
import br.com.dunnastecnologia.chamados.domain.model.Usuario;
import br.com.dunnastecnologia.chamados.infrastructure.repository.ColaboradorRepository;
import br.com.dunnastecnologia.chamados.infrastructure.exception.BusinessRuleException;
import br.com.dunnastecnologia.chamados.infrastructure.exception.ResourceNotFoundException;
import br.com.dunnastecnologia.chamados.infrastructure.repository.MoradorRepository;
import br.com.dunnastecnologia.chamados.infrastructure.repository.TipoChamadoRepository;
import br.com.dunnastecnologia.chamados.infrastructure.repository.UnidadeRepository;
import br.com.dunnastecnologia.chamados.infrastructure.repository.UsuarioRepository;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.AuthenticatedUserValidator;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.InputValidationSupport;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.PageResultMapper;
import br.com.dunnastecnologia.chamados.domain.validation.ValidationLimits;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UsuarioService implements UsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final ColaboradorRepository colaboradorRepository;
    private final MoradorRepository moradorRepository;
    private final UnidadeRepository unidadeRepository;
    private final TipoChamadoRepository tipoChamadoRepository;
    private final AuthenticatedUserValidator authenticatedUserValidator;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            ColaboradorRepository colaboradorRepository,
            MoradorRepository moradorRepository,
            UnidadeRepository unidadeRepository,
            TipoChamadoRepository tipoChamadoRepository,
            AuthenticatedUserValidator authenticatedUserValidator,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.colaboradorRepository = colaboradorRepository;
        this.moradorRepository = moradorRepository;
        this.unidadeRepository = unidadeRepository;
        this.tipoChamadoRepository = tipoChamadoRepository;
        this.authenticatedUserValidator = authenticatedUserValidator;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Usuario cadastrarUsuario(AuthenticatedUser admin, Usuario usuario) {
        authenticatedUserValidator.assertAdministrador(admin);
        validateUsuario(usuario);
        validateUniqueEmail(usuario.getEmail(), null);
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public Usuario atualizarUsuario(AuthenticatedUser admin, UUID usuarioId, Usuario usuario) {
        authenticatedUserValidator.assertAdministrador(admin);
        validateUsuario(usuario);

        Usuario persistedUsuario = buscarUsuarioPorId(usuarioId);
        validateUniqueEmail(usuario.getEmail(), usuarioId);

        if (!persistedUsuario.getClass().equals(usuario.getClass())) {
            throw new BusinessRuleException("Nao e permitido alterar o tipo do usuario");
        }

        persistedUsuario.setNome(usuario.getNome());
        persistedUsuario.setEmail(usuario.getEmail());
        persistedUsuario.setSenha(passwordEncoder.encode(usuario.getSenha()));

        if (persistedUsuario instanceof Morador persistedMorador && usuario instanceof Morador sourceMorador) {
            Set<Unidade> unidades = sourceMorador.getUnidades() == null
                    ? new HashSet<>()
                    : new HashSet<>(sourceMorador.getUnidades());
            persistedMorador.setUnidades(unidades);
        }

        return usuarioRepository.save(persistedUsuario);
    }

    @Override
    public PageResult<Usuario> listarUsuarios(PageRequest pageRequest) {
        return PageResultMapper.fromPage(usuarioRepository.findAllActive(pageRequest));
    }

    @Override
    public Usuario buscarUsuarioPorId(UUID usuarioId) {
        return usuarioRepository.findByIdAndAtivoTrue(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
    }

    @Override
    @Transactional
    public void removerUsuario(AuthenticatedUser admin, UUID usuarioId) {
        authenticatedUserValidator.assertAdministrador(admin);
        Usuario usuario = buscarUsuarioPorId(usuarioId);
        usuario.setAtivo(Boolean.FALSE);
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void vincularMoradorUnidade(AuthenticatedUser admin, UUID moradorId, UUID unidadeId) {
        authenticatedUserValidator.assertAdministrador(admin);
        Morador morador = moradorRepository.findByIdAndAtivoTrue(moradorId)
                .orElseThrow(() -> new ResourceNotFoundException("Morador nao encontrado"));
        Unidade unidade = unidadeRepository.findById(unidadeId)
                .orElseThrow(() -> new ResourceNotFoundException("Unidade nao encontrada"));

        morador.getUnidades().add(unidade);
        moradorRepository.save(morador);
    }

    @Override
    @Transactional
    public void desvincularMoradorUnidade(AuthenticatedUser admin, UUID moradorId, UUID unidadeId) {
        authenticatedUserValidator.assertAdministrador(admin);
        Morador morador = moradorRepository.findByIdAndAtivoTrue(moradorId)
                .orElseThrow(() -> new ResourceNotFoundException("Morador nao encontrado"));

        morador.getUnidades().removeIf(unidade -> unidade.getId().equals(unidadeId));
        moradorRepository.save(morador);
    }

    @Override
    @Transactional
    public void vincularColaboradorTipoChamado(AuthenticatedUser admin, UUID colaboradorId, UUID tipoChamadoId) {
        authenticatedUserValidator.assertAdministrador(admin);
        Colaborador colaborador = colaboradorRepository.findByIdAndAtivoTrue(colaboradorId)
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador nao encontrado"));
        TipoChamado tipoChamado = tipoChamadoRepository.findById(tipoChamadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de chamado nao encontrado"));

        colaborador.getTiposChamadoResponsaveis().add(tipoChamado);
        colaboradorRepository.save(colaborador);
    }

    @Override
    @Transactional
    public void desvincularColaboradorTipoChamado(AuthenticatedUser admin, UUID colaboradorId, UUID tipoChamadoId) {
        authenticatedUserValidator.assertAdministrador(admin);
        Colaborador colaborador = colaboradorRepository.findByIdAndAtivoTrue(colaboradorId)
                .orElseThrow(() -> new ResourceNotFoundException("Colaborador nao encontrado"));

        colaborador.getTiposChamadoResponsaveis().removeIf(tipoChamado -> tipoChamado.getId().equals(tipoChamadoId));
        colaboradorRepository.save(colaborador);
    }

    @Override
    public PageResult<TipoChamado> listarTiposChamadoDoColaborador(UUID colaboradorId, PageRequest pageRequest) {
        return PageResultMapper.fromPage(colaboradorRepository.findTiposChamadoByColaboradorId(colaboradorId, pageRequest));
    }

    @Override
    public PageResult<Colaborador> listarColaboradores(PageRequest pageRequest) {
        return PageResultMapper.fromPage(colaboradorRepository.findAllActive(pageRequest));
    }

    @Override
    public PageResult<Colaborador> listarColaboradoresPorPrefixoEmail(String prefixoEmail, PageRequest pageRequest) {
        if (prefixoEmail == null || prefixoEmail.isBlank()) {
            return listarColaboradores(pageRequest);
        }
        return PageResultMapper.fromPage(
                colaboradorRepository.findByEmailStartingWithIgnoreCase(prefixoEmail.trim(), pageRequest)
        );
    }

    private void validateUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new BusinessRuleException("Usuario e obrigatorio");
        }
        usuario.setNome(InputValidationSupport.normalizeRequiredText(
                usuario.getNome(),
                "Nome do usuario e obrigatorio",
                "Nome do usuario deve ter no maximo 255 caracteres",
                ValidationLimits.USUARIO_NOME_MAX_LENGTH
        ));
        usuario.setEmail(InputValidationSupport.normalizeRequiredText(
                usuario.getEmail(),
                "Email do usuario e obrigatorio",
                "Email do usuario deve ter no maximo 255 caracteres",
                ValidationLimits.USUARIO_EMAIL_MAX_LENGTH
        ));
        usuario.setSenha(InputValidationSupport.normalizeRequiredText(
                usuario.getSenha(),
                "Senha do usuario e obrigatoria",
                "Senha do usuario deve ter no maximo 255 caracteres",
                ValidationLimits.USUARIO_SENHA_MAX_LENGTH
        ));
    }

    private void validateUniqueEmail(String email, UUID currentUserId) {
        usuarioRepository.findByEmail(email)
                .filter(existing -> currentUserId == null || !existing.getId().equals(currentUserId))
                .ifPresent(existing -> {
                    throw new BusinessRuleException("Ja existe um usuario com este email");
                });
    }
}
