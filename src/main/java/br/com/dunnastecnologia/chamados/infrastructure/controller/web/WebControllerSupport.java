package br.com.dunnastecnologia.chamados.infrastructure.controller.web;

import br.com.dunnastecnologia.chamados.application.Security.AuthenticatedUser;
import br.com.dunnastecnologia.chamados.application.pagination.PageResult;
import br.com.dunnastecnologia.chamados.domain.model.*;
import br.com.dunnastecnologia.chamados.infrastructure.exception.UnauthorizedOperationException;
import br.com.dunnastecnologia.chamados.infrastructure.security.adapter.UserDetailsImpl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;

@Component
public class WebControllerSupport {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public AuthenticatedUser authenticatedUser(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || !(authentication.getPrincipal() instanceof UserDetailsImpl userDetails)) {
            throw new UnauthorizedOperationException("Usuario nao autenticado");
        }

        Usuario usuario = userDetails.getUsuario();
        return new AuthenticatedUser(usuario.getId(), usuario.getEmail(), usuario.getRole());
    }

    public boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.getPrincipal() instanceof UserDetailsImpl;
    }

    public org.springframework.data.domain.PageRequest pageRequest(Integer page, Integer size) {
        int resolvedPage = page == null || page < 0
                ? DEFAULT_PAGE
                : page;

        int resolvedSize = size == null || size <= 0
                ? DEFAULT_SIZE
                : Math.min(size, MAX_SIZE);

        return org.springframework.data.domain.PageRequest.of(resolvedPage, resolvedSize);
    }

    public Map<String, Object> pageMetadata(PageResult<?> pageResult) {
        return Map.of(
                "page", pageResult.page(),
                "size", pageResult.size(),
                "totalElements", pageResult.totalElements(),
                "totalPages", pageResult.totalPages(),
                "hasPrevious", pageResult.page() > 0,
                "hasNext", pageResult.page() + 1 < pageResult.totalPages()
        );
    }

    public String homePathForRole(String role) {
        return switch (role) {
            case "ROLE_ADMINISTRADOR" -> "/admin";
            case "ROLE_COLABORADOR" -> "/colaborador";
            case "ROLE_MORADOR" -> "/morador";
            default -> "/";
        };
    }

    public String roleLabel(String role) {
        return switch (role) {
            case "ROLE_ADMINISTRADOR" -> "Administrador";
            case "ROLE_COLABORADOR" -> "Colaborador";
            case "ROLE_MORADOR" -> "Morador";
            default -> role;
        };
    }

    public String userTypeLabel(Usuario usuario) {
        return roleLabel(usuario.getRole());
    }

    public <T> List<Map<String, Object>> mapContent(List<T> content, Function<T, Map<String, Object>> mapper) {
        return content.stream().map(mapper).toList();
    }

    public Map<String, Object> toBlocoMap(Bloco bloco) {
        return Map.of(
                "id", bloco.getId(),
                "identificacao", bloco.getIdentificacao(),
                "quantidadeAndares", bloco.getQuantidadeAndares(),
                "apartamentosPorAndar", bloco.getApartamentosPorAndar()
        );
    }

    public Map<String, Object> toUnidadeMap(Unidade unidade) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("id", unidade.getId());
        values.put("identificacao", unidade.getIdentificacao());
        values.put("andar", unidade.getAndar());
        values.put("blocoId", unidade.getBloco() == null ? null : unidade.getBloco().getId());
        values.put("blocoIdentificacao", unidade.getBloco() == null ? null : unidade.getBloco().getIdentificacao());
        return values;
    }

    public Map<String, Object> toUsuarioMap(Usuario usuario) {
        return Map.of(
                "id", usuario.getId(),
                "nome", usuario.getNome(),
                "email", usuario.getEmail(),
                "role", usuario.getRole(),
                "tipo", userTypeLabel(usuario)
        );
    }

    public Map<String, Object> toTipoChamadoMap(TipoChamado tipoChamado) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("id", tipoChamado.getId());
        values.put("titulo", tipoChamado.getTitulo());
        values.put("prazoHoras", tipoChamado.getPrazoHoras());
        return values;
    }

    public Map<String, Object> toStatusChamadoMap(StatusChamado statusChamado) {
        return Map.of(
                "id", statusChamado.getId(),
                "nome", statusChamado.getNome(),
                "inicialPadrao", Boolean.TRUE.equals(statusChamado.getInicialPadrao())
        );
    }

    public Map<String, Object> toChamadoMap(Chamado chamado) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("id", chamado.getId());
        values.put("descricao", chamado.getDescricao());
        values.put("dataAbertura", chamado.getDataAbertura());
        values.put("dataAberturaFormatada", formatDateTime(chamado.getDataAbertura()));
        values.put("dataFinalizacao", chamado.getDataFinalizacao());
        values.put("dataFinalizacaoFormatada", formatDateTime(chamado.getDataFinalizacao()));
        values.put("finalizado", chamado.getDataFinalizacao() != null);
        values.put("moradorId", chamado.getMorador() == null ? null : chamado.getMorador().getId());
        values.put("moradorNome", chamado.getMorador() == null ? null : chamado.getMorador().getNome());
        values.put("unidadeId", chamado.getUnidade() == null ? null : chamado.getUnidade().getId());
        values.put("unidadeIdentificacao", chamado.getUnidade() == null ? null : chamado.getUnidade().getIdentificacao());
        values.put(
                "blocoIdentificacao",
                chamado.getUnidade() == null || chamado.getUnidade().getBloco() == null
                        ? null
                        : chamado.getUnidade().getBloco().getIdentificacao()
        );
        values.put("tipoChamadoId", chamado.getTipoChamado() == null ? null : chamado.getTipoChamado().getId());
        values.put(
                "tipoChamadoTitulo",
                chamado.getTipoChamado() == null ? null : chamado.getTipoChamado().getTitulo()
        );
        values.put("statusId", chamado.getStatus() == null ? null : chamado.getStatus().getId());
        values.put("statusNome", chamado.getStatus() == null ? null : chamado.getStatus().getNome());
        return values;
    }

    public Map<String, Object> toComentarioMap(Comentario comentario) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("id", comentario.getId());
        values.put("mensagem", comentario.getMensagem());
        values.put("dataCriacao", comentario.getDataCriacao());
        values.put("dataCriacaoFormatada", formatDateTime(comentario.getDataCriacao()));
        values.put("autorId", comentario.getAutor() == null ? null : comentario.getAutor().getId());
        values.put("autorNome", comentario.getAutor() == null ? null : comentario.getAutor().getNome());
        values.put("autorRole", comentario.getAutor() == null ? null : roleLabel(comentario.getAutor().getRole()));
        values.put(
                "anexos",
                comentario.getAnexos().stream()
                        .map(anexo -> {
                            Map<String, Object> anexoValues = new LinkedHashMap<>();
                            anexoValues.put("id", anexo.getId());
                            anexoValues.put("nomeArquivo", anexo.getNomeArquivo());
                            anexoValues.put("contentType", anexo.getContentType());
                            anexoValues.put("tamanhoBytes", anexo.getTamanhoBytes());
                            anexoValues.put("tamanhoFormatado", formatBytes(anexo.getTamanhoBytes()));
                            return anexoValues;
                        })
                        .toList()
        );
        return values;
    }

    public Map<String, Object> toAnexoMap(
            UUID id,
            String nomeArquivo,
            String contentType,
            long tamanhoBytes
    ) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("id", id);
        values.put("nomeArquivo", nomeArquivo);
        values.put("contentType", contentType);
        values.put("tamanhoBytes", tamanhoBytes);
        values.put("tamanhoFormatado", formatBytes(tamanhoBytes));
        return values;
    }

    public Map<String, Object> toAreaComumMap(AreaComum area){
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("id", area.getId());
        values.put("nome",area.getNome());
        values.put("ativa", area.getAtiva());
        return values;
    }

    public Map<String, Object> toReservaMap(Reserva reserva){
        Map<String, Object> values = new HashMap<>();
        values.put("id", reserva.getId());
        values.put("area", toAreaComumMap(reserva.getArea()));
        values.put("moradorNome", reserva.getMorador().getNome());
        values.put("data", reserva.getData());
        values.put("horaInicio", reserva.getHoraInicio());
        values.put("horaFim", reserva.getHoraFim());
        values.put("status", reserva.getStatus());
        values.put("statusCodigo", reserva.getStatus() == null? "" : reserva.getStatus().name());
        values.put("statusLabel", reservaStatusLabel(reserva.getStatus()));
        values.put("motivoNegacao", reserva.getMotivoNegacao());


        //Campos já formatados para as telas
        values.put("dataFormatada", reserva.getData() == null ? null : DATE_FORMATTER.format(reserva.getData()));
        values.put("horaInicioFormatada", reserva.getHoraInicio() == null ? null : TIME_FORMATTER.format(reserva.getHoraInicio()));
        values.put("horaFimFormatada", reserva.getHoraFim() == null ? null : TIME_FORMATTER.format(reserva.getHoraFim()));
        values.put("dataCriacaoFormatada", formatDateTime(reserva.getDataCriacao()));
        values.put("dataCriacao", formatDateTime(reserva.getDataCriacao()));

        //RN-01-11 / RN-01-12 / RN-01-14 - Só SOLICITADA/APROVADA e antes do inicio podem ser canceladas
        boolean statusCancelavel = reserva.getStatus() == StatusReserva.SOLICITADA
                || reserva.getStatus() == StatusReserva.APROVADA;
        boolean antesDoInicio = reserva.getData() != null
                && reserva.getHoraInicio() != null
                && LocalDateTime.of(reserva.getData(), reserva.getHoraInicio()).isAfter(LocalDateTime.now());
        values.put("cancelavel", statusCancelavel && antesDoInicio);
        return values;
    }

    public UploadedFileData optionalUploadedFile(MultipartFile arquivo, String errorMessage) {
        if (arquivo == null || arquivo.isEmpty()) {
            return null;
        }
        return uploadedFile(arquivo, errorMessage);
    }

    public UploadedFileData requiredUploadedFile(
            MultipartFile arquivo,
            String emptyFileMessage,
            String errorMessage
    ) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException(emptyFileMessage);
        }
        return uploadedFile(arquivo, errorMessage);
    }

    public ResponseEntity<byte[]> downloadResponse(
            String nomeArquivo,
            String contentType,
            long tamanhoBytes,
            byte[] conteudo
    ) {
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (contentType != null && !contentType.isBlank()) {
            mediaType = MediaType.parseMediaType(contentType);
        }

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nomeArquivo).build().toString()
                )
                .contentType(mediaType)
                .contentLength(tamanhoBytes)
                .body(conteudo);
    }

    private String reservaStatusLabel(StatusReserva status){
        if (status == null){
            return "";
        }
        return switch(status){
            case SOLICITADA -> "Solicitada";
            case APROVADA -> "Aprovada";
            case NEGADA -> "Negada";
            case CANCELADA -> "Cancelada";
        };
    }

    private String formatDateTime(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return DATE_TIME_FORMATTER.format(value);
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        double kilobytes = bytes / 1024.0;
        if (kilobytes < 1024) {
            return String.format(Locale.US, "%.1f KB", kilobytes);
        }
        double megabytes = kilobytes / 1024.0;
        return String.format(Locale.US, "%.1f MB", megabytes);
    }

    private UploadedFileData uploadedFile(MultipartFile arquivo, String errorMessage) {
        try {
            return new UploadedFileData(
                    arquivo.getOriginalFilename(),
                    arquivo.getContentType(),
                    arquivo.getSize(),
                    arquivo.getBytes()
            );
        } catch (Exception exception) {
            throw new IllegalArgumentException(errorMessage, exception);
        }
    }

    public record UploadedFileData(
            String nomeArquivo,
            String contentType,
            long tamanhoBytes,
            byte[] conteudo
    ) {
    }
}
