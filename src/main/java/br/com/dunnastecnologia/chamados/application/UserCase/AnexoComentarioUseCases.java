package br.com.dunnastecnologia.chamados.application.UserCase;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;

import java.util.UUID;

public interface AnexoComentarioUseCases {

    AnexoComentarioInfo adicionarAnexoAoComentario(
            AuthenticatedUser usuario,
            UUID chamadoId,
            UUID comentarioId,
            String nomeArquivo,
            String contentType,
            long tamanhoBytes,
            byte[] conteudo
    );

    AnexoComentarioConteudo buscarAnexoPorId(
            AuthenticatedUser usuario,
            UUID chamadoId,
            UUID comentarioId,
            UUID anexoId
    );

    record AnexoComentarioInfo(
            UUID id,
            UUID comentarioId,
            String nomeArquivo,
            String contentType,
            long tamanhoBytes
    ) {
    }

    record AnexoComentarioConteudo(
            UUID id,
            UUID comentarioId,
            String nomeArquivo,
            String contentType,
            long tamanhoBytes,
            byte[] conteudo
    ) {
    }
}
