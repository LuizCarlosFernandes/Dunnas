package br.com.dunnastecnologia.chamados.infrastructure.service;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.application.UserCase.AnexoComentarioUseCases;
import br.com.dunnastecnologia.chamados.domain.model.AnexoComentario;
import br.com.dunnastecnologia.chamados.domain.model.Comentario;
import br.com.dunnastecnologia.chamados.infrastructure.exception.BusinessRuleException;
import br.com.dunnastecnologia.chamados.infrastructure.exception.ResourceNotFoundException;
import br.com.dunnastecnologia.chamados.infrastructure.repository.AnexoComentarioRepository;
import br.com.dunnastecnologia.chamados.infrastructure.repository.ComentarioRepository;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.ChamadoAccessSupport;
import br.com.dunnastecnologia.chamados.infrastructure.service.support.InputValidationSupport;
import br.com.dunnastecnologia.chamados.domain.validation.ValidationLimits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AnexoComentarioService implements AnexoComentarioUseCases {

    private final AnexoComentarioRepository anexoComentarioRepository;
    private final ComentarioRepository comentarioRepository;
    private final ChamadoAccessSupport chamadoAccessSupport;

    public AnexoComentarioService(
            AnexoComentarioRepository anexoComentarioRepository,
            ComentarioRepository comentarioRepository,
            ChamadoAccessSupport chamadoAccessSupport
    ) {
        this.anexoComentarioRepository = anexoComentarioRepository;
        this.comentarioRepository = comentarioRepository;
        this.chamadoAccessSupport = chamadoAccessSupport;
    }

    @Override
    @Transactional
    public AnexoComentarioInfo adicionarAnexoAoComentario(
            AuthenticatedUser usuario,
            UUID chamadoId,
            UUID comentarioId,
            String nomeArquivo,
            String contentType,
            long tamanhoBytes,
            byte[] conteudo
    ) {
        String nomeArquivoNormalizado = InputValidationSupport.normalizeRequiredText(
                nomeArquivo,
                "Nome do arquivo do comentario e obrigatorio",
                "Nome do arquivo do comentario deve ter no maximo 255 caracteres",
                ValidationLimits.ANEXO_NOME_ARQUIVO_MAX_LENGTH
        );


        String contentTypeNormalizado = InputValidationSupport.normalizeRequiredText(
                contentType,
                "Content type do anexo do comentario e obrigatorio",
                "Content type do anexo do comentario deve ter no maximo 255 caracteres",
                ValidationLimits.ANEXO_CONTENT_TYPE_MAX_LENGTH
        );


        validarArquivo(tamanhoBytes, conteudo);
        chamadoAccessSupport.findAccessibleChamadoEmAberto(usuario, chamadoId);

        Comentario comentario = comentarioRepository.findByIdAndChamadoId(comentarioId, chamadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario nao encontrado para o chamado"));

        AnexoComentario anexoComentario = new AnexoComentario();
        anexoComentario.setComentario(comentario);
        anexoComentario.setNomeArquivo(nomeArquivoNormalizado);
        anexoComentario.setContentType(contentTypeNormalizado);
        anexoComentario.setTamanhoBytes(tamanhoBytes);
        anexoComentario.setConteudo(conteudo);

        AnexoComentario salvo = anexoComentarioRepository.save(anexoComentario);
        return new AnexoComentarioInfo(
                salvo.getId(),
                comentarioId,
                salvo.getNomeArquivo(),
                salvo.getContentType(),
                salvo.getTamanhoBytes()
        );
    }

    @Override
    public AnexoComentarioConteudo buscarAnexoPorId(
            AuthenticatedUser usuario,
            UUID chamadoId,
            UUID comentarioId,
            UUID anexoId
    ) {
        chamadoAccessSupport.findAccessibleChamado(usuario, chamadoId);
        comentarioRepository.findByIdAndChamadoId(comentarioId, chamadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario nao encontrado para o chamado"));

        AnexoComentario anexoComentario = anexoComentarioRepository.findByIdAndComentarioId(anexoId, comentarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Anexo do comentario nao encontrado"));

        return new AnexoComentarioConteudo(
                anexoComentario.getId(),
                comentarioId,
                anexoComentario.getNomeArquivo(),
                anexoComentario.getContentType(),
                anexoComentario.getTamanhoBytes(),
                anexoComentario.getConteudo()
        );
    }

    private void validarArquivo(long tamanhoBytes, byte[] conteudo) {
        if (tamanhoBytes <= 0 || conteudo == null || conteudo.length == 0) {
            throw new BusinessRuleException("Conteudo do anexo do comentario e obrigatorio");
        }
        if (conteudo.length != tamanhoBytes) {
            throw new BusinessRuleException("Tamanho do anexo do comentario informado e diferente do conteudo enviado");
        }
        InputValidationSupport.validateMaxBytes(
                tamanhoBytes,
                ValidationLimits.ANEXO_TAMANHO_MAX_BYTES,
                "Anexo do comentario deve ter no maximo 5 MB"
        );
    }
}
