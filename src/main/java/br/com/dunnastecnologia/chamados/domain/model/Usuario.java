package br.com.dunnastecnologia.chamados.domain.model;

import br.com.dunnastecnologia.chamados.domain.validation.ValidationLimits;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Column;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public abstract class Usuario {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = ValidationLimits.USUARIO_NOME_MAX_LENGTH)
    private String nome;

    @Column(unique = true, nullable = false, length = ValidationLimits.USUARIO_EMAIL_MAX_LENGTH)
    private String email;

    @Column(nullable = false, length = ValidationLimits.USUARIO_SENHA_MAX_LENGTH)
    private String senha;

    private Boolean ativo = Boolean.TRUE;

    public abstract String getRole();
}
