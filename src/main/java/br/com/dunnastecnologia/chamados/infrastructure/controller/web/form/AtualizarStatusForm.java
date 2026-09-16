package br.com.dunnastecnologia.chamados.infrastructure.controller.web.form;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AtualizarStatusForm {
    private UUID statusId;
}
