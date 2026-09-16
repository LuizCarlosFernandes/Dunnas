package br.com.dunnastecnologia.chamados.infrastructure.service.support;

import br.com.dunnastecnologia.chamados.application.pagination.PageResult;
import org.springframework.data.domain.Page;

public final class PageResultMapper {

    private PageResultMapper() {
    }

    public static <T> PageResult<T> fromPage(Page<T> page) {
        return new PageResult<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize()
        );
    }
}
