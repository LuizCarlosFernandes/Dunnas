-- =========================================================
-- V17 - Filtro por data de abertura e ordenacao por antiguidade
-- Adiciona filtro opcional por data nas filas de admin/colaborador
-- e prioriza chamados mais antigos no topo da listagem.
-- =========================================================

DROP FUNCTION IF EXISTS fn_listar_chamados_para_admin(uuid, uuid, character varying);
DROP FUNCTION IF EXISTS fn_listar_chamados_do_colaborador(uuid, uuid, uuid, character varying);

CREATE OR REPLACE FUNCTION fn_listar_chamados_para_admin(
    p_admin_id UUID,
    p_status_id UUID DEFAULT NULL,
    p_morador_nome VARCHAR(255) DEFAULT NULL,
    p_data_abertura DATE DEFAULT NULL
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
    PERFORM fn_assert_admin_existe(p_admin_id);

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
    JOIN usuarios u ON u.id = c.morador_id
    WHERE (p_status_id IS NULL OR c.status_id = p_status_id)
      AND (
            p_morador_nome IS NULL
            OR trim(p_morador_nome) = ''
            OR lower(u.nome) LIKE lower(trim(p_morador_nome)) || '%'
      )
      AND (
            p_data_abertura IS NULL
            OR c.data_abertura::date = p_data_abertura
      )
    ORDER BY c.data_abertura ASC NULLS LAST, c.id ASC;
END;
$$;

CREATE OR REPLACE FUNCTION fn_listar_chamados_do_colaborador(
    p_colaborador_id UUID,
    p_status_id UUID DEFAULT NULL,
    p_tipo_chamado_id UUID DEFAULT NULL,
    p_unidade_identificacao VARCHAR(255) DEFAULT NULL,
    p_data_abertura DATE DEFAULT NULL
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
      AND (
            p_data_abertura IS NULL
            OR c.data_abertura::date = p_data_abertura
      )
      AND c.data_finalizacao IS NULL
    ORDER BY c.data_abertura ASC NULLS LAST, c.id ASC;
END;
$$;
