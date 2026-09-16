-- =========================================================
-- V4 - Regras de negocio e autorizacao do colaborador
-- Define escopo, permissao de atendimento e consultas
-- =========================================================

-- Existencia e identidade do colaborador
CREATE OR REPLACE FUNCTION fn_colaborador_existe(
    p_colaborador_id UUID
)
RETURNS BOOLEAN
LANGUAGE sql
STABLE
AS $$
    SELECT EXISTS (
        SELECT 1
        FROM colaboradores c
        WHERE c.id = p_colaborador_id
    );
$$;


CREATE OR REPLACE FUNCTION fn_assert_colaborador_existe(
    p_colaborador_id UUID
)
RETURNS VOID
LANGUAGE plpgsql
STABLE
AS $$
BEGIN
    IF NOT fn_colaborador_existe(p_colaborador_id) THEN
        RAISE EXCEPTION
            USING ERRCODE = '42501',
                  MESSAGE = 'Usuario autenticado nao possui perfil de colaborador';
    END IF;
END;
$$;


-- Escopo operacional do colaborador
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
       AND (
           (p_chamado_id IS NULL AND p_unidade_id IS NULL)
           OR EXISTS (
               SELECT 1
               FROM chamados c
               WHERE (p_chamado_id IS NULL OR c.id = p_chamado_id)
                 AND (p_unidade_id IS NULL OR c.unidade_id = p_unidade_id)
           )
       );
$$;


CREATE OR REPLACE FUNCTION fn_colaborador_pode_buscar_chamados(
    p_colaborador_id UUID,
    p_status_id UUID DEFAULT NULL,
    p_unidade_id UUID DEFAULT NULL
)
RETURNS BOOLEAN
LANGUAGE sql
STABLE
AS $$
    SELECT fn_colaborador_existe(p_colaborador_id)
       AND (p_status_id IS NULL OR EXISTS (
           SELECT 1
           FROM status_chamado sc
           WHERE sc.id = p_status_id
       ))
       AND fn_colaborador_pode_acessar_escopo(p_colaborador_id, NULL, p_unidade_id);
$$;


CREATE OR REPLACE FUNCTION fn_assert_colaborador_pode_buscar_chamados(
    p_colaborador_id UUID,
    p_status_id UUID DEFAULT NULL,
    p_unidade_id UUID DEFAULT NULL
)
RETURNS VOID
LANGUAGE plpgsql
STABLE
AS $$
BEGIN
    IF NOT fn_colaborador_pode_buscar_chamados(p_colaborador_id, p_status_id, p_unidade_id) THEN
        RAISE EXCEPTION
            USING ERRCODE = '42501',
                  MESSAGE = 'Colaborador nao pode consultar chamados';
    END IF;
END;
$$;


-- Permissoes sobre o chamado
CREATE OR REPLACE FUNCTION fn_colaborador_pode_visualizar_chamado(
    p_colaborador_id UUID,
    p_chamado_id UUID
)
RETURNS BOOLEAN
LANGUAGE sql
STABLE
AS $$
    SELECT fn_colaborador_pode_acessar_escopo(p_colaborador_id, p_chamado_id, NULL);
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


CREATE OR REPLACE FUNCTION fn_colaborador_pode_atualizar_status_chamado(
    p_colaborador_id UUID,
    p_chamado_id UUID,
    p_status_id UUID
)
RETURNS BOOLEAN
LANGUAGE sql
STABLE
AS $$
    SELECT fn_colaborador_pode_visualizar_chamado(p_colaborador_id, p_chamado_id)
       AND EXISTS (
           SELECT 1
           FROM status_chamado sc
           WHERE sc.id = p_status_id
       )
       AND EXISTS (
           SELECT 1
           FROM chamados c
           WHERE c.id = p_chamado_id
             AND c.data_finalizacao IS NULL
       );
$$;


CREATE OR REPLACE FUNCTION fn_assert_colaborador_pode_atualizar_status_chamado(
    p_colaborador_id UUID,
    p_chamado_id UUID,
    p_status_id UUID
)
RETURNS VOID
LANGUAGE plpgsql
STABLE
AS $$
BEGIN
    IF NOT fn_colaborador_pode_atualizar_status_chamado(p_colaborador_id, p_chamado_id, p_status_id) THEN
        RAISE EXCEPTION
            USING ERRCODE = '42501',
                  MESSAGE = 'Colaborador nao pode atualizar o status deste chamado';
    END IF;
END;
$$;


CREATE OR REPLACE FUNCTION fn_colaborador_pode_finalizar_chamado(
    p_colaborador_id UUID,
    p_chamado_id UUID
)
RETURNS BOOLEAN
LANGUAGE sql
STABLE
AS $$
    SELECT fn_colaborador_pode_visualizar_chamado(p_colaborador_id, p_chamado_id)
       AND EXISTS (
           SELECT 1
           FROM chamados c
           WHERE c.id = p_chamado_id
             AND c.data_finalizacao IS NULL
       );
$$;


CREATE OR REPLACE FUNCTION fn_assert_colaborador_pode_finalizar_chamado(
    p_colaborador_id UUID,
    p_chamado_id UUID
)
RETURNS VOID
LANGUAGE plpgsql
STABLE
AS $$
BEGIN
    IF NOT fn_colaborador_pode_finalizar_chamado(p_colaborador_id, p_chamado_id) THEN
        RAISE EXCEPTION
            USING ERRCODE = '42501',
                  MESSAGE = 'Colaborador nao pode finalizar este chamado';
    END IF;
END;
$$;


CREATE OR REPLACE FUNCTION fn_colaborador_pode_comentar_chamado(
    p_colaborador_id UUID,
    p_chamado_id UUID
)
RETURNS BOOLEAN
LANGUAGE sql
STABLE
AS $$
    SELECT fn_colaborador_pode_visualizar_chamado(p_colaborador_id, p_chamado_id);
$$;


CREATE OR REPLACE FUNCTION fn_assert_colaborador_pode_comentar_chamado(
    p_colaborador_id UUID,
    p_chamado_id UUID
)
RETURNS VOID
LANGUAGE plpgsql
STABLE
AS $$
BEGIN
    IF NOT fn_colaborador_pode_comentar_chamado(p_colaborador_id, p_chamado_id) THEN
        RAISE EXCEPTION
            USING ERRCODE = '42501',
                  MESSAGE = 'Colaborador nao pode comentar este chamado';
    END IF;
END;
$$;


-- Consulta principal de chamados do colaborador
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
    ORDER BY c.data_abertura DESC NULLS LAST, c.id DESC;
END;
$$;


-- Consulta de detalhe de um chamado para o colaborador
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
    WHERE c.id = p_chamado_id;
END;
$$;
