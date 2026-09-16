package br.com.dunnastecnologia.chamados.domain.model;

import br.com.dunnastecnologia.chamados.domain.validation.ValidationLimits;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "anexos_comentario")
@Getter
@Setter
public class AnexoComentario {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comentario_id")
    private Comentario comentario;

    @Column(nullable = false, length = ValidationLimits.ANEXO_NOME_ARQUIVO_MAX_LENGTH)
    private String nomeArquivo;

    @Column(nullable = false, length = ValidationLimits.ANEXO_CONTENT_TYPE_MAX_LENGTH)
    private String contentType;

    @Column(nullable = false)
    private Long tamanhoBytes;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "conteudo", nullable = false)
    private byte[] conteudo;
}
