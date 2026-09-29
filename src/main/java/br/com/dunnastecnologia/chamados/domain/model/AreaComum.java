package br.com.dunnastecnologia.chamados.domain.model;

import br.com.dunnastecnologia.chamados.domain.validation.ValidationLimits;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Entity
@Table(name= "areas_comuns")
@Getter
@Setter

public class AreaComum{

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = ValidationLimits.AREA_COMUM_NOME_MAX_LENGTH)
    private String nome;

    @Column(nullable = false)
    private Boolean ativa = Boolean.TRUE;
}