-- =========================================================
-- V10 - Filtro administrativo por prefixo do nome do morador
-- Evolui a listagem do admin de UUID para busca textual
-- =========================================================

-- Remove a assinatura anterior antes de recriar a funcao
DROP FUNCTION IF EXISTS fn_listar_chamados_para_admin(uuid, uuid, uuid);

-- Recria a listagem do admin com filtro opcional por prefixo de nome
CREATE OR REPLACE FUNCTION fn_listar_chamados_para_admin(
    p_admin_id UUID,
    p_status_id UUID DEFAULT NULL,
    p_morador_nome VARCHAR(255) DEFAULT NULL
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
    ORDER BY c.data_abertura DESC NULLS LAST, c.id DESC;
END;
$$;
