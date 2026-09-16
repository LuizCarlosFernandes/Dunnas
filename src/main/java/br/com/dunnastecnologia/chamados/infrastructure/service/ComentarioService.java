package br.com.dunnastecnologia.chamados.infrastructure.service;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.application.UserCase.ComentarioUseCase;
import br.com.dunnastecnologia.chamados.application.pagination.PageResult;
import br.com.dunnastecnologia.chamados.domain.model.Chamado;
import br.com.dunnastecnologia.chamados.domain.model.Comentario;
import br.com.dunnastecnologia.chamados.domain.model.Usuario;
import br.com.dunnastecnologia.chamados.infrastructure.exception.BusinessRuleException;
import br.com.dunnastecnologia.chamados.infrastructure.exception.ResourceNotFoundException;
import br.com.dunnastecnologia.chamados.infrastructure.repository.ComentarioRepository;
import br.com.dunnastecnologia.chamados.infrastructure.repository.UsuarioRepository;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.ChamadoAccessSupport;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.InputValidationSupport;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.PageResultMapper;
import br.com.dunnastecnologia.chamados.domain.validation.ValidationLimits;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ComentarioService implements ComentarioUseCase {

    private final ComentarioRepository comentarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ChamadoAccessSupport chamadoAccessSupport;

    public ComentarioService(
            ComentarioRepository comentarioRepository,
            UsuarioRepository usuarioRepository,
            ChamadoAccessSupport chamadoAccessSupport
    ) {
        this.comentarioRepository = comentarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.chamadoAccessSupport = chamadoAccessSupport;
    }

    @Override
    @Transactional
    public Comentario comentar(AuthenticatedUser usuario, UUID chamadoId, String mensagem) {
        String mensagemNormalizada = InputValidationSupport.normalizeRequiredText(
                mensagem,
                "Mensagem do comentario e obrigatoria",
                "Mensagem do comentario deve ter no maximo 255 caracteres",
                ValidationLimits.COMENTARIO_MENSAGEM_MAX_LENGTH
        );

        Usuario autor = usuarioRepository.findByIdAndAtivoTrue(usuario.id())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autor nao encontrado"));
        Chamado chamado = chamadoAccessSupport.findAccessibleChamadoEmAberto(usuario, chamadoId);

        Comentario comentario = new Comentario();
        comentario.setAutor(autor);
        comentario.setChamado(chamado);
        comentario.setMensagem(mensagemNormalizada);
        comentario.setDataCriacao(LocalDateTime.now());
        return comentarioRepository.save(comentario);
    }

    @Override
    public PageResult<Comentario> listarComentariosDoChamado(
            AuthenticatedUser usuario,
            UUID chamadoId,
            PageRequest pageRequest
    ) {
        chamadoAccessSupport.findAccessibleChamado(usuario, chamadoId);
        return PageResultMapper.fromPage(comentarioRepository.findByChamadoId(chamadoId, pageRequest));
    }
}
