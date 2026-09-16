package br.com.dunnastecnologia.chamados.domain.model;

import br.com.dunnastecnologia.chamados.domain.validation.ValidationLimits;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "unidades")
@Getter
@Setter
public class Unidade {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = ValidationLimits.DEFAULT_TEXT_MAX_LENGTH)
    private String identificacao;

    private Integer andar;

    @ManyToOne(fetch = FetchType.LAZY)
    private Bloco bloco;

}
