-- =========================================================
-- V8 - Estrutura de anexos dos chamados
-- Armazena metadados e conteudo binario dos arquivos
-- =========================================================

-- Tabela de anexos vinculados ao chamado
CREATE TABLE anexos_chamado (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chamado_id UUID NOT NULL,
    nome_arquivo VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    tamanho_bytes BIGINT NOT NULL,
    conteudo BYTEA NOT NULL,
    CONSTRAINT fk_anexos_chamado_chamado
        FOREIGN KEY (chamado_id) REFERENCES chamados (id)
);

-- Indice de apoio para listagem de anexos por chamado
CREATE INDEX idx_anexos_chamado_chamado_id
    ON anexos_chamado (chamado_id);
