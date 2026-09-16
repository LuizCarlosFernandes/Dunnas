package br.com.dunnastecnologia.chamados.application.pagination;

public record PageRequest(
        int page,
        int size,
        String sortBy,
        String direction
) {}