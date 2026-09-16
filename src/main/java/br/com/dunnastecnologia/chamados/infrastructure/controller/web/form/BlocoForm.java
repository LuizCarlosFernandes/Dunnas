package br.com.dunnastecnologia.chamados.infrastructure.controller.web.form;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlocoForm {
    private String identificacao;
    private Integer quantidadeAndares;
    private Integer apartamentosPorAndar;
}
