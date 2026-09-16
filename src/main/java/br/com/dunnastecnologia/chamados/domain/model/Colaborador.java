package br.com.dunnastecnologia.chamados.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "colaboradores")
@Getter
@Setter
public class Colaborador extends Usuario {

    @ManyToMany
    @JoinTable(
            name = "colaborador_tipo_chamado",
            joinColumns = @JoinColumn(name = "colaborador_id"),
            inverseJoinColumns = @JoinColumn(name = "tipo_chamado_id")
    )
    private Set<TipoChamado> tiposChamadoResponsaveis = new HashSet<>();

    @Override
    public String getRole() {
        return "ROLE_COLABORADOR";
    }
}
