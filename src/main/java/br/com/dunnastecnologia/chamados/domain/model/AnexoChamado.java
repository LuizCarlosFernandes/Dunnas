package br.com.dunnastecnologia.chamados.domain.model;

import br.com.dunnastecnologia.chamados.domain.validation.ValidationLimits;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "anexos_chamado")
@Getter
@Setter
public class AnexoChamado {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chamado_id")
    private Chamado chamado;

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
