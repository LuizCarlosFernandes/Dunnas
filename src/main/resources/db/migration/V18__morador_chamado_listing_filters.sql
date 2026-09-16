-- =========================================================
-- V18 - Filtros de listagem para meus chamados
-- Adiciona filtros opcionais por status, unidade, tipo e data
-- na listagem do morador, preservando a regra de visibilidade.
-- =========================================================

DROP FUNCTION IF EXISTS fn_listar_chamados_do_morador(uuid);

CREATE OR REPLACE FUNCTION fn_listar_chamados_do_morador(
    p_morador_id UUID,
    p_status_id UUID DEFAULT NULL,
    p_unidade_id UUID DEFAULT NULL,
    p_tipo_chamado_id UUID DEFAULT NULL,
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
      AND (p_status_id IS NULL OR c.status_id = p_status_id)
      AND (p_unidade_id IS NULL OR c.unidade_id = p_unidade_id)
      AND (p_tipo_chamado_id IS NULL OR c.tipo_chamado_id = p_tipo_chamado_id)
      AND (p_data_abertura IS NULL OR c.data_abertura::date = p_data_abertura)
    ORDER BY c.data_abertura DESC NULLS LAST, c.id DESC;
$$;
