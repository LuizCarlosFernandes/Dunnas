package br.com.dunnastecnologia.chamados.application.UserCase;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.application.pagination.PageResult;
import org.springframework.data.domain.PageRequest;

import java.util.UUID;

public interface AnexoChamadoUseCases {
    /**
     * Existe para permitir que moradores, colaboradores e administradores anexem
     * arquivos a um chamado, complementando a descricao do problema com evidencias.
     */
    AnexoChamadoInfo adicionarAnexo(
            AuthenticatedUser usuario,
            UUID chamadoId,
            String nomeArquivo,
            String contentType,
            long tamanhoBytes,
            byte[] conteudo
    );

    /**
     * Existe para listar os anexos ja vinculados ao chamado, dando visibilidade
     * ao material de apoio usado durante abertura, atendimento e acompanhamento.
     */
    PageResult<AnexoChamadoInfo> listarAnexosDoChamado(
            AuthenticatedUser usuario,
            UUID chamadoId,
            PageRequest pageRequest
    );

    /**
     * Existe para recuperar um anexo especifico quando o usuario autorizado
     * precisar visualizar ou baixar uma evidencia vinculada ao chamado.
     */
    AnexoChamadoConteudo buscarAnexoPorId(
            AuthenticatedUser usuario,
            UUID chamadoId,
            UUID anexoId
    );

    record AnexoChamadoInfo(
            UUID id,
            UUID chamadoId,
            String nomeArquivo,
            String contentType,
            long tamanhoBytes
    ) {
    }

    record AnexoChamadoConteudo(
            UUID id,
            UUID chamadoId,
            String nomeArquivo,
            String contentType,
            long tamanhoBytes,
            byte[] conteudo
    ) {
    }
}
