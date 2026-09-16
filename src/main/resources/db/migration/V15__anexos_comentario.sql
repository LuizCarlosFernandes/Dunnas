-- =========================================================
-- V15 - Anexos em comentarios de moradores
-- Cria estrutura de anexos vinculados a comentarios
-- =========================================================

CREATE TABLE anexos_comentario (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    comentario_id UUID NOT NULL,
    nome_arquivo VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    tamanho_bytes BIGINT NOT NULL,
    conteudo BYTEA NOT NULL,
    CONSTRAINT fk_anexos_comentario_comentario
        FOREIGN KEY (comentario_id) REFERENCES comentarios (id)
);

CREATE INDEX idx_anexos_comentario_comentario_id
    ON anexos_comentario (comentario_id);
