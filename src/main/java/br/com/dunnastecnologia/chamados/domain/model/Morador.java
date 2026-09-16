package br.com.dunnastecnologia.chamados.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "moradores")
@Getter
@Setter
public class Morador extends Usuario {

    @ManyToMany
    @JoinTable(
            name = "morador_unidade",
            joinColumns = @JoinColumn(name = "morador_id"),
            inverseJoinColumns = @JoinColumn(name = "unidade_id")
    )
    private Set<Unidade> unidades = new HashSet<>();

    @Override
    public String getRole() {
        return "ROLE_MORADOR";
    }
}
