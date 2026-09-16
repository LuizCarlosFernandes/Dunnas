package br.com.dunnastecnologia.chamados.domain.model;

import br.com.dunnastecnologia.chamados.domain.validation.ValidationLimits;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tipos_chamado")
@Getter
@Setter
public class TipoChamado {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = ValidationLimits.TIPO_CHAMADO_TITULO_MAX_LENGTH)
    private String titulo;

    private Integer prazoHoras;

    @ManyToMany(mappedBy = "tiposChamadoResponsaveis")
    private Set<Colaborador> colaboradoresResponsaveis = new HashSet<>();

}
