-- =========================================================
-- V11 - Filtros operacionais do colaborador
-- Evolui a listagem para filtrar por tipo e identificacao da unidade
-- =========================================================

-- Remove a assinatura anterior antes de recriar a funcao
DROP FUNCTION IF EXISTS fn_listar_chamados_do_colaborador(uuid, uuid, uuid);

-- Recria a listagem do colaborador com filtros textuais e por tipo
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
        NULL
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
    WHERE (p_status_id IS NULL OR c.status_id = p_status_id)
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
