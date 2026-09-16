-- =========================================================
-- V13 - Escopo do colaborador por tipo de chamado
-- Cria o vinculo N:N entre colaborador e tipo de chamado e
-- atualiza as funcoes SQL para respeitar esse escopo.
-- =========================================================

-- Relacionamento entre colaboradores e tipos de chamado responsaveis
CREATE TABLE colaborador_tipo_chamado (
    colaborador_id UUID NOT NULL,
    tipo_chamado_id UUID NOT NULL,
    PRIMARY KEY (colaborador_id, tipo_chamado_id),
    CONSTRAINT fk_colaborador_tipo_chamado_colaborador
        FOREIGN KEY (colaborador_id) REFERENCES colaboradores (id),
    CONSTRAINT fk_colaborador_tipo_chamado_tipo
        FOREIGN KEY (tipo_chamado_id) REFERENCES tipos_chamado (id)
);

CREATE INDEX idx_colaborador_tipo_chamado_tipo_id
    ON colaborador_tipo_chamado (tipo_chamado_id);

-- Valida se o colaborador possui pelo menos um tipo de chamado atribuIdo
CREATE OR REPLACE FUNCTION fn_colaborador_tem_tipo_chamado(
    p_colaborador_id UUID,
    p_tipo_chamado_id UUID
)
RETURNS BOOLEAN
LANGUAGE sql
STABLE
AS $$
    SELECT EXISTS (
        SELECT 1
        FROM colaborador_tipo_chamado ctc
        WHERE ctc.colaborador_id = p_colaborador_id
          AND ctc.tipo_chamado_id = p_tipo_chamado_id
    );
$$;

-- Valida o escopo do colaborador sobre chamados e unidades, sempre limitado
-- aos tipos de chamado sob sua responsabilidade
CREATE OR REPLACE FUNCTION fn_colaborador_pode_acessar_escopo(
    p_colaborador_id UUID,
    p_chamado_id UUID DEFAULT NULL,
    p_unidade_id UUID DEFAULT NULL
)
RETURNS BOOLEAN
LANGUAGE sql
STABLE
AS $$
    SELECT fn_colaborador_existe(p_colaborador_id)
       AND EXISTS (
           SELECT 1
           FROM colaborador_tipo_chamado ctc
           JOIN chamados c ON c.tipo_chamado_id = ctc.tipo_chamado_id
           WHERE ctc.colaborador_id = p_colaborador_id
             AND (p_chamado_id IS NULL OR c.id = p_chamado_id)
             AND (p_unidade_id IS NULL OR c.unidade_id = p_unidade_id)
       );
$$;

-- Permissao de busca de chamados limitada ao tipo atribuido ao colaborador
-- As funcoes antigas usavam o terceiro parametro como unidade. Em PostgreSQL,
-- a mudanca do nome do parametro exige a remocao previa da assinatura.
DROP FUNCTION IF EXISTS fn_assert_colaborador_pode_buscar_chamados(uuid, uuid, uuid);
DROP FUNCTION IF EXISTS fn_colaborador_pode_buscar_chamados(uuid, uuid, uuid);

CREATE OR REPLACE FUNCTION fn_colaborador_pode_buscar_chamados(
    p_colaborador_id UUID,
    p_status_id UUID DEFAULT NULL,
    p_tipo_chamado_id UUID DEFAULT NULL
)
RETURNS BOOLEAN
LANGUAGE sql
STABLE
AS $$
    SELECT fn_colaborador_existe(p_colaborador_id)
       AND EXISTS (
           SELECT 1
           FROM colaborador_tipo_chamado ctc
           WHERE ctc.colaborador_id = p_colaborador_id
       )
       AND (p_status_id IS NULL OR EXISTS (
           SELECT 1
           FROM status_chamado sc
           WHERE sc.id = p_status_id
       ))
       AND (
           p_tipo_chamado_id IS NULL
           OR fn_colaborador_tem_tipo_chamado(p_colaborador_id, p_tipo_chamado_id)
       );
$$;

CREATE OR REPLACE FUNCTION fn_assert_colaborador_pode_buscar_chamados(
    p_colaborador_id UUID,
    p_status_id UUID DEFAULT NULL,
    p_tipo_chamado_id UUID DEFAULT NULL
)
RETURNS VOID
LANGUAGE plpgsql
STABLE
AS $$
BEGIN
    IF NOT fn_colaborador_pode_buscar_chamados(p_colaborador_id, p_status_id, p_tipo_chamado_id) THEN
        RAISE EXCEPTION
            USING ERRCODE = '42501',
                  MESSAGE = 'Colaborador nao pode consultar chamados fora do seu escopo';
    END IF;
END;
$$;

-- Visualizacao de detalhe somente para chamados cujo tipo pertence ao colaborador
CREATE OR REPLACE FUNCTION fn_colaborador_pode_visualizar_chamado(
    p_colaborador_id UUID,
    p_chamado_id UUID
)
RETURNS BOOLEAN
LANGUAGE sql
STABLE
AS $$
    SELECT fn_colaborador_existe(p_colaborador_id)
       AND EXISTS (
           SELECT 1
           FROM chamados c
           JOIN colaborador_tipo_chamado ctc ON ctc.tipo_chamado_id = c.tipo_chamado_id
           WHERE ctc.colaborador_id = p_colaborador_id
             AND c.id = p_chamado_id
             AND c.data_finalizacao IS NULL
       );
$$;

CREATE OR REPLACE FUNCTION fn_assert_colaborador_pode_visualizar_chamado(
    p_colaborador_id UUID,
    p_chamado_id UUID
)
RETURNS VOID
LANGUAGE plpgsql
STABLE
AS $$
BEGIN
    IF NOT fn_colaborador_pode_visualizar_chamado(p_colaborador_id, p_chamado_id) THEN
        RAISE EXCEPTION
            USING ERRCODE = '42501',
                  MESSAGE = 'Colaborador nao pode acessar este chamado';
    END IF;
END;
$$;

-- Listagem operacional de chamados respeitando o escopo por tipo
CREATE OR REPLACE FUNCTION fn_listar_chamados_do_colaborador(
    p_colaborador_id UUID,
    p_status_id UUID DEFAULT NULL,
    p_tipo_chamado_id UUID DEFAULT NULL,
    p_unidade_identificacao VARCHAR(255) DEFAULT NULL
)
RETURNS TABLE (
    id UUID,
    descricao VARCHAR(255),
    data_abertura TIMESTAMP(6) WITHOUT TIME ZONE,
    data_finalizacao TIMESTAMP(6) WITHOUT TIME ZONE,
    morador_id UUID,
    unidade_id UUID,
    tipo_chamado_id UUID,
    status_id UUID
)
LANGUAGE plpgsql
STABLE
AS $$
BEGIN
    PERFORM fn_assert_colaborador_pode_buscar_chamados(
        p_colaborador_id,
        p_status_id,
        p_tipo_chamado_id
    );

    RETURN QUERY
    SELECT c.id,
           c.descricao,
           c.data_abertura,
           c.data_finalizacao,
           c.morador_id,
           c.unidade_id,
           c.tipo_chamado_id,
           c.status_id
    FROM chamados c
    JOIN unidades u ON u.id = c.unidade_id
    JOIN colaborador_tipo_chamado ctc ON ctc.tipo_chamado_id = c.tipo_chamado_id
    WHERE ctc.colaborador_id = p_colaborador_id
      AND (p_status_id IS NULL OR c.status_id = p_status_id)
      AND (p_tipo_chamado_id IS NULL OR c.tipo_chamado_id = p_tipo_chamado_id)
      AND (
            p_unidade_identificacao IS NULL
            OR trim(p_unidade_identificacao) = ''
            OR lower(u.identificacao) LIKE '%' || lower(trim(p_unidade_identificacao)) || '%'
      )
      AND c.data_finalizacao IS NULL
    ORDER BY c.data_abertura DESC NULLS LAST, c.id DESC;
END;
$$;

-- Busca de detalhe no contexto do colaborador
CREATE OR REPLACE FUNCTION fn_buscar_chamado_para_colaborador(
    p_colaborador_id UUID,
    p_chamado_id UUID
)
RETURNS TABLE (
    id UUID,
    descricao VARCHAR(255),
    data_abertura TIMESTAMP(6) WITHOUT TIME ZONE,
    data_finalizacao TIMESTAMP(6) WITHOUT TIME ZONE,
    morador_id UUID,
    unidade_id UUID,
    tipo_chamado_id UUID,
    status_id UUID
)
LANGUAGE plpgsql
STABLE
AS $$
BEGIN
    PERFORM fn_assert_colaborador_pode_visualizar_chamado(p_colaborador_id, p_chamado_id);

    RETURN QUERY
    SELECT c.id,
           c.descricao,
           c.data_abertura,
           c.data_finalizacao,
           c.morador_id,
           c.unidade_id,
           c.tipo_chamado_id,
           c.status_id
    FROM chamados c
    WHERE c.id = p_chamado_id
      AND c.data_finalizacao IS NULL;
END;
$$;
