-- =========================================================
-- V2 - Regras de autorizacao do morador
-- Define funcoes de permissao, assertivas e listagem
-- =========================================================

-- Vinculo entre morador e unidade
CREATE OR REPLACE FUNCTION fn_morador_possui_unidade(
    p_morador_id UUID,
    p_unidade_id UUID
)
RETURNS BOOLEAN
LANGUAGE sql
STABLE
AS $$
    SELECT EXISTS (
        SELECT 1
        FROM morador_unidade mu
        WHERE mu.morador_id = p_morador_id
          AND mu.unidade_id = p_unidade_id
    );
$$;


-- Permissao para abertura de chamado
CREATE OR REPLACE FUNCTION fn_morador_pode_abrir_chamado(
    p_morador_id UUID,
    p_unidade_id UUID
)
RETURNS BOOLEAN
LANGUAGE sql
STABLE
AS $$
    SELECT fn_morador_possui_unidade(p_morador_id, p_unidade_id);
$$;


-- Permissao para visualizacao de chamado
CREATE OR REPLACE FUNCTION fn_morador_pode_ver_chamado(
    p_morador_id UUID,
    p_chamado_id UUID
)
RETURNS BOOLEAN
LANGUAGE sql
STABLE
AS $$
    SELECT EXISTS (
        SELECT 1
        FROM chamados c
        WHERE c.id = p_chamado_id
          AND c.morador_id = p_morador_id
          AND fn_morador_possui_unidade(p_morador_id, c.unidade_id)
    );
$$;


-- Permissao para comentario em chamado
CREATE OR REPLACE FUNCTION fn_morador_pode_comentar_chamado(
    p_morador_id UUID,
    p_chamado_id UUID
)
RETURNS BOOLEAN
LANGUAGE sql
STABLE
AS $$
    SELECT fn_morador_pode_ver_chamado(p_morador_id, p_chamado_id);
$$;


-- Assertivas de seguranca para uso em queries e procedures
CREATE OR REPLACE FUNCTION fn_assert_morador_pode_abrir_chamado(
    p_morador_id UUID,
    p_unidade_id UUID
)
RETURNS VOID
LANGUAGE plpgsql
STABLE
AS $$
BEGIN
    IF NOT fn_morador_pode_abrir_chamado(p_morador_id, p_unidade_id) THEN
        RAISE EXCEPTION
            USING ERRCODE = '42501',
                  MESSAGE = 'Morador nao pode abrir chamado para esta unidade';
    END IF;
END;
$$;


CREATE OR REPLACE FUNCTION fn_assert_morador_pode_ver_chamado(
    p_morador_id UUID,
    p_chamado_id UUID
)
RETURNS VOID
LANGUAGE plpgsql
STABLE
AS $$
BEGIN
    IF NOT fn_morador_pode_ver_chamado(p_morador_id, p_chamado_id) THEN
        RAISE EXCEPTION
            USING ERRCODE = '42501',
                  MESSAGE = 'Morador nao pode acessar este chamado';
    END IF;
END;
$$;


CREATE OR REPLACE FUNCTION fn_assert_morador_pode_comentar_chamado(
    p_morador_id UUID,
    p_chamado_id UUID
)
RETURNS VOID
LANGUAGE plpgsql
STABLE
AS $$
BEGIN
    IF NOT fn_morador_pode_comentar_chamado(p_morador_id, p_chamado_id) THEN
        RAISE EXCEPTION
            USING ERRCODE = '42501',
                  MESSAGE = 'Morador nao pode comentar este chamado';
    END IF;
END;
$$;


-- Consulta principal de chamados do morador
CREATE OR REPLACE FUNCTION fn_listar_chamados_do_morador(
    p_morador_id UUID
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
LANGUAGE sql
STABLE
AS $$
    SELECT c.id,
           c.descricao,
           c.data_abertura,
           c.data_finalizacao,
           c.morador_id,
           c.unidade_id,
           c.tipo_chamado_id,
           c.status_id
    FROM chamados c
    WHERE c.morador_id = p_morador_id
      AND fn_morador_possui_unidade(p_morador_id, c.unidade_id)
    ORDER BY c.data_abertura DESC NULLS LAST, c.id DESC;
$$;


-- Indice composto de apoio a visibilidade do morador
CREATE INDEX idx_chamados_morador_unidade
    ON chamados (morador_id, unidade_id);
