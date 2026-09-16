package br.com.dunnastecnologia.chamados.infrastructure.controller.web.form;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class VincularMoradorUnidadeForm {
    private UUID blocoId;
    private UUID unidadeId;
}
