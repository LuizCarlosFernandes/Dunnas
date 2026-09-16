-- =========================================================
-- V1 - Estrutura inicial do banco
-- Cria extensao UUID, tabelas base do dominio e indices
-- =========================================================

-- Extensoes necessarias
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Identidade e acesso
CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    senha VARCHAR(255)
);

CREATE TABLE administradores (
    id UUID PRIMARY KEY,
    CONSTRAINT fk_administradores_usuario
        FOREIGN KEY (id) REFERENCES usuarios (id)
);

CREATE TABLE colaboradores (
    id UUID PRIMARY KEY,
    CONSTRAINT fk_colaboradores_usuario
        FOREIGN KEY (id) REFERENCES usuarios (id)
);

CREATE TABLE moradores (
    id UUID PRIMARY KEY,
    CONSTRAINT fk_moradores_usuario
        FOREIGN KEY (id) REFERENCES usuarios (id)
);

-- Estrutura fisica do condominio
CREATE TABLE blocos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    identificacao VARCHAR(255),
    quantidade_andares INTEGER,
    apartamentos_por_andar INTEGER
);

CREATE TABLE unidades (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    identificacao VARCHAR(255),
    andar INTEGER,
    bloco_id UUID,
    CONSTRAINT fk_unidades_bloco
        FOREIGN KEY (bloco_id) REFERENCES blocos (id)
);

CREATE TABLE morador_unidade (
    morador_id UUID NOT NULL,
    unidade_id UUID NOT NULL,
    PRIMARY KEY (morador_id, unidade_id),
    CONSTRAINT fk_morador_unidade_morador
        FOREIGN KEY (morador_id) REFERENCES moradores (id),
    CONSTRAINT fk_morador_unidade_unidade
        FOREIGN KEY (unidade_id) REFERENCES unidades (id)
);

-- Catalogos operacionais
CREATE TABLE tipos_chamado (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titulo VARCHAR(255),
    prazo_horas INTEGER
);

CREATE TABLE status_chamado (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(255)
);

-- Operacao de chamados
CREATE TABLE chamados (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    descricao VARCHAR(255),
    data_abertura TIMESTAMP(6) WITHOUT TIME ZONE,
    data_finalizacao TIMESTAMP(6) WITHOUT TIME ZONE,
    morador_id UUID,
    unidade_id UUID,
    tipo_chamado_id UUID,
    status_id UUID,
    CONSTRAINT fk_chamados_morador
        FOREIGN KEY (morador_id) REFERENCES moradores (id),
    CONSTRAINT fk_chamados_unidade
        FOREIGN KEY (unidade_id) REFERENCES unidades (id),
    CONSTRAINT fk_chamados_tipo_chamado
        FOREIGN KEY (tipo_chamado_id) REFERENCES tipos_chamado (id),
    CONSTRAINT fk_chamados_status
        FOREIGN KEY (status_id) REFERENCES status_chamado (id)
);

-- Historico textual
CREATE TABLE comentarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mensagem VARCHAR(255),
    data_criacao TIMESTAMP(6) WITHOUT TIME ZONE,
    autor_id UUID,
    chamado_id UUID,
    CONSTRAINT fk_comentarios_autor
        FOREIGN KEY (autor_id) REFERENCES usuarios (id),
    CONSTRAINT fk_comentarios_chamado
        FOREIGN KEY (chamado_id) REFERENCES chamados (id)
);

-- Indices de apoio a consultas e relacionamentos
CREATE INDEX idx_unidades_bloco_id ON unidades (bloco_id);
CREATE INDEX idx_morador_unidade_unidade_id ON morador_unidade (unidade_id);
CREATE INDEX idx_chamados_morador_id ON chamados (morador_id);
CREATE INDEX idx_chamados_unidade_id ON chamados (unidade_id);
CREATE INDEX idx_chamados_tipo_chamado_id ON chamados (tipo_chamado_id);
CREATE INDEX idx_chamados_status_id ON chamados (status_id);
CREATE INDEX idx_comentarios_autor_id ON comentarios (autor_id);
CREATE INDEX idx_comentarios_chamado_id ON comentarios (chamado_id);
