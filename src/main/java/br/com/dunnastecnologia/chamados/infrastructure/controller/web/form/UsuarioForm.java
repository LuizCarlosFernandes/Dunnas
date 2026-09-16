package br.com.dunnastecnologia.chamados.infrastructure.controller.web.form;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioForm {
    private String nome;
    private String email;
    private String senha;
    private String tipo;
}
