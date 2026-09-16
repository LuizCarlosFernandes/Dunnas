-- =========================================================
-- V9 - Filtro administrativo por morador
-- Ajusta a funcao de listagem do admin para receber morador_id
-- =========================================================

-- Remove assinatura anterior para recriar a funcao com novo filtro
DROP FUNCTION IF EXISTS fn_listar_chamados_para_admin(uuid, uuid, uuid);

-- Recria a listagem do admin com filtro opcional por morador
CREATE OR REPLACE FUNCTION fn_listar_chamados_para_admin(
    p_admin_id UUID,
    p_status_id UUID DEFAULT NULL,
    p_morador_id UUID DEFAULT NULL
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
    WHERE (p_status_id IS NULL OR c.status_id = p_status_id)
      AND (p_morador_id IS NULL OR c.morador_id = p_morador_id)
    ORDER BY c.data_abertura DESC NULLS LAST, c.id DESC;
END;
$$;
