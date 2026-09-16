-- =========================================================
-- V6 - Consolidacao das regras de visibilidade dos chamados
-- Ajusta funcoes de listagem e visualizacao por perfil
-- =========================================================

-- Listagem do morador com validacao de unidade vinculada
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


-- Visualizacao do colaborador limitada a chamados em aberto
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
           WHERE c.id = p_chamado_id
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


-- Listagem operacional do colaborador
CREATE OR REPLACE FUNCTION fn_listar_chamados_do_colaborador(
    p_colaborador_id UUID,
    p_status_id UUID DEFAULT NULL,
    p_unidade_id UUID DEFAULT NULL
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
        p_unidade_id
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
    WHERE (p_status_id IS NULL OR c.status_id = p_status_id)
      AND (p_unidade_id IS NULL OR c.unidade_id = p_unidade_id)
      AND c.data_finalizacao IS NULL
    ORDER BY c.data_abertura DESC NULLS LAST, c.id DESC;
END;
$$;


-- Busca de detalhe para o colaborador
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


-- Listagem administrativa com filtros opcionais
CREATE OR REPLACE FUNCTION fn_listar_chamados_para_admin(
    p_admin_id UUID,
    p_status_id UUID DEFAULT NULL,
    p_unidade_id UUID DEFAULT NULL
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
      AND (p_unidade_id IS NULL OR c.unidade_id = p_unidade_id)
    ORDER BY c.data_abertura DESC NULLS LAST, c.id DESC;
END;
$$;
