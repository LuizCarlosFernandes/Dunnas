-- =========================================================
-- V5 - Funcoes de apoio aos repositories
-- Centraliza consultas usadas pela camada de persistencia
-- =========================================================

-- Busca detalhada de chamado no contexto do morador
CREATE OR REPLACE FUNCTION fn_buscar_chamado_do_morador(
    p_morador_id UUID,
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
    PERFORM fn_assert_morador_pode_ver_chamado(p_morador_id, p_chamado_id);

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
    WHERE c.id = p_chamado_id;
END;
$$;


-- Busca detalhada de chamado no contexto do administrador
CREATE OR REPLACE FUNCTION fn_buscar_chamado_para_admin(
    p_admin_id UUID,
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
    PERFORM fn_assert_admin_pode_visualizar_chamado(p_admin_id, p_chamado_id);

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
    WHERE c.id = p_chamado_id;
END;
$$;


-- Consulta de unidades pertencentes a um bloco
CREATE OR REPLACE FUNCTION fn_listar_unidades_do_bloco(
    p_bloco_id UUID
)
RETURNS TABLE (
    id UUID,
    identificacao VARCHAR(255),
    andar INTEGER,
    bloco_id UUID
)
LANGUAGE sql
STABLE
AS $$
    SELECT u.id,
           u.identificacao,
           u.andar,
           u.bloco_id
    FROM unidades u
    WHERE u.bloco_id = p_bloco_id
    ORDER BY u.andar, u.identificacao, u.id;
$$;


-- Consulta de unidades vinculadas a um morador
CREATE OR REPLACE FUNCTION fn_listar_unidades_do_morador(
    p_morador_id UUID
)
RETURNS TABLE (
    id UUID,
    identificacao VARCHAR(255),
    andar INTEGER,
    bloco_id UUID
)
LANGUAGE sql
STABLE
AS $$
    SELECT u.id,
           u.identificacao,
           u.andar,
           u.bloco_id
    FROM morador_unidade mu
    JOIN unidades u ON u.id = mu.unidade_id
    WHERE mu.morador_id = p_morador_id
    ORDER BY u.andar, u.identificacao, u.id;
$$;


-- Consulta cronologica de comentarios de um chamado
CREATE OR REPLACE FUNCTION fn_listar_comentarios_do_chamado(
    p_chamado_id UUID
)
RETURNS TABLE (
    id UUID,
    mensagem VARCHAR(255),
    data_criacao TIMESTAMP(6) WITHOUT TIME ZONE,
    autor_id UUID,
    chamado_id UUID
)
LANGUAGE sql
STABLE
AS $$
    SELECT c.id,
           c.mensagem,
           c.data_criacao,
           c.autor_id,
           c.chamado_id
    FROM comentarios c
    WHERE c.chamado_id = p_chamado_id
    ORDER BY c.data_criacao ASC NULLS LAST, c.id ASC;
$$;
