-- =========================================================
-- V3 - Regras de negocio e autorizacao do administrador
-- Define funcoes de validacao, geracao de unidades e consultas
-- =========================================================

-- Existencia e identidade do administrador
CREATE OR REPLACE FUNCTION fn_admin_existe(
    p_admin_id UUID
)
RETURNS BOOLEAN
LANGUAGE sql
STABLE
AS $$
    SELECT EXISTS (
        SELECT 1
        FROM administradores a
        WHERE a.id = p_admin_id
    );
$$;


CREATE OR REPLACE FUNCTION fn_assert_admin_existe(
    p_admin_id UUID
)
RETURNS VOID
LANGUAGE plpgsql
STABLE
AS $$
BEGIN
    IF NOT fn_admin_existe(p_admin_id) THEN
        RAISE EXCEPTION
            USING ERRCODE = '42501',
                  MESSAGE = 'Usuario autenticado nao possui perfil de administrador';
    END IF;
END;
$$;


-- Permissoes administrativas por contexto
CREATE OR REPLACE FUNCTION fn_admin_pode_gerenciar_estrutura(
    p_admin_id UUID
)
RETURNS BOOLEAN
LANGUAGE sql
STABLE
AS $$
    SELECT fn_admin_existe(p_admin_id);
$$;


CREATE OR REPLACE FUNCTION fn_assert_admin_pode_gerenciar_estrutura(
    p_admin_id UUID
)
RETURNS VOID
LANGUAGE plpgsql
STABLE
AS $$
BEGIN
    IF NOT fn_admin_pode_gerenciar_estrutura(p_admin_id) THEN
        RAISE EXCEPTION
            USING ERRCODE = '42501',
                  MESSAGE = 'Administrador nao pode gerenciar a estrutura do condominio';
    END IF;
END;
$$;


CREATE OR REPLACE FUNCTION fn_admin_pode_gerenciar_usuarios(
    p_admin_id UUID
)
RETURNS BOOLEAN
LANGUAGE sql
STABLE
AS $$
    SELECT fn_admin_existe(p_admin_id);
$$;


CREATE OR REPLACE FUNCTION fn_assert_admin_pode_gerenciar_usuarios(
    p_admin_id UUID
)
RETURNS VOID
LANGUAGE plpgsql
STABLE
AS $$
BEGIN
    IF NOT fn_admin_pode_gerenciar_usuarios(p_admin_id) THEN
        RAISE EXCEPTION
            USING ERRCODE = '42501',
                  MESSAGE = 'Administrador nao pode gerenciar usuarios';
    END IF;
END;
$$;


CREATE OR REPLACE FUNCTION fn_admin_pode_gerenciar_catalogos(
    p_admin_id UUID
)
RETURNS BOOLEAN
LANGUAGE sql
STABLE
AS $$
    SELECT fn_admin_existe(p_admin_id);
$$;


CREATE OR REPLACE FUNCTION fn_assert_admin_pode_gerenciar_catalogos(
    p_admin_id UUID
)
RETURNS VOID
LANGUAGE plpgsql
STABLE
AS $$
BEGIN
    IF NOT fn_admin_pode_gerenciar_catalogos(p_admin_id) THEN
        RAISE EXCEPTION
            USING ERRCODE = '42501',
                  MESSAGE = 'Administrador nao pode gerenciar tipos e status de chamado';
    END IF;
END;
$$;


-- Permissoes sobre chamados
CREATE OR REPLACE FUNCTION fn_admin_pode_visualizar_chamado(
    p_admin_id UUID,
    p_chamado_id UUID
)
RETURNS BOOLEAN
LANGUAGE sql
STABLE
AS $$
    SELECT fn_admin_existe(p_admin_id)
       AND EXISTS (
           SELECT 1
           FROM chamados c
           WHERE c.id = p_chamado_id
       );
$$;


CREATE OR REPLACE FUNCTION fn_assert_admin_pode_visualizar_chamado(
    p_admin_id UUID,
    p_chamado_id UUID
)
RETURNS VOID
LANGUAGE plpgsql
STABLE
AS $$
BEGIN
    IF NOT fn_admin_pode_visualizar_chamado(p_admin_id, p_chamado_id) THEN
        RAISE EXCEPTION
            USING ERRCODE = '42501',
                  MESSAGE = 'Administrador nao pode acessar este chamado';
    END IF;
END;
$$;


CREATE OR REPLACE FUNCTION fn_admin_pode_atualizar_status_chamado(
    p_admin_id UUID,
    p_chamado_id UUID,
    p_status_id UUID
)
RETURNS BOOLEAN
LANGUAGE sql
STABLE
AS $$
    SELECT fn_admin_existe(p_admin_id)
       AND EXISTS (
           SELECT 1
           FROM chamados c
           JOIN status_chamado sc ON sc.id = p_status_id
           WHERE c.id = p_chamado_id
             AND c.data_finalizacao IS NULL
       );
$$;


CREATE OR REPLACE FUNCTION fn_assert_admin_pode_atualizar_status_chamado(
    p_admin_id UUID,
    p_chamado_id UUID,
    p_status_id UUID
)
RETURNS VOID
LANGUAGE plpgsql
STABLE
AS $$
BEGIN
    IF NOT fn_admin_pode_atualizar_status_chamado(p_admin_id, p_chamado_id, p_status_id) THEN
        RAISE EXCEPTION
            USING ERRCODE = '42501',
                  MESSAGE = 'Administrador nao pode atualizar o status deste chamado';
    END IF;
END;
$$;


CREATE OR REPLACE FUNCTION fn_admin_pode_finalizar_chamado(
    p_admin_id UUID,
    p_chamado_id UUID
)
RETURNS BOOLEAN
LANGUAGE sql
STABLE
AS $$
    SELECT fn_admin_existe(p_admin_id)
       AND EXISTS (
           SELECT 1
           FROM chamados c
           WHERE c.id = p_chamado_id
             AND c.data_finalizacao IS NULL
       );
$$;


CREATE OR REPLACE FUNCTION fn_assert_admin_pode_finalizar_chamado(
    p_admin_id UUID,
    p_chamado_id UUID
)
RETURNS VOID
LANGUAGE plpgsql
STABLE
AS $$
BEGIN
    IF NOT fn_admin_pode_finalizar_chamado(p_admin_id, p_chamado_id) THEN
        RAISE EXCEPTION
            USING ERRCODE = '42501',
                  MESSAGE = 'Administrador nao pode finalizar este chamado';
    END IF;
END;
$$;


CREATE OR REPLACE FUNCTION fn_admin_pode_comentar_chamado(
    p_admin_id UUID,
    p_chamado_id UUID
)
RETURNS BOOLEAN
LANGUAGE sql
STABLE
AS $$
    SELECT fn_admin_pode_visualizar_chamado(p_admin_id, p_chamado_id);
$$;


CREATE OR REPLACE FUNCTION fn_assert_admin_pode_comentar_chamado(
    p_admin_id UUID,
    p_chamado_id UUID
)
RETURNS VOID
LANGUAGE plpgsql
STABLE
AS $$
BEGIN
    IF NOT fn_admin_pode_comentar_chamado(p_admin_id, p_chamado_id) THEN
        RAISE EXCEPTION
            USING ERRCODE = '42501',
                  MESSAGE = 'Administrador nao pode comentar este chamado';
    END IF;
END;
$$;


-- Geracao automatica de identificacao das unidades
CREATE OR REPLACE FUNCTION fn_gerar_identificacao_unidade(
    p_bloco_identificacao VARCHAR(255),
    p_andar INTEGER,
    p_apartamento_no_andar INTEGER
)
RETURNS VARCHAR(255)
LANGUAGE sql
IMMUTABLE
AS $$
    SELECT CONCAT(
        p_bloco_identificacao,
        '-',
        LPAD(p_andar::TEXT, 2, '0'),
        LPAD(p_apartamento_no_andar::TEXT, 2, '0')
    );
$$;


-- Geracao automatica das unidades de um bloco
CREATE OR REPLACE FUNCTION fn_gerar_unidades_bloco(
    p_admin_id UUID,
    p_bloco_id UUID
)
RETURNS INTEGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_bloco_identificacao VARCHAR(255);
    v_quantidade_andares INTEGER;
    v_apartamentos_por_andar INTEGER;
    v_andar INTEGER;
    v_apartamento INTEGER;
    v_identificacao VARCHAR(255);
    v_inseridos INTEGER := 0;
BEGIN
    PERFORM fn_assert_admin_pode_gerenciar_estrutura(p_admin_id);

    SELECT b.identificacao,
           b.quantidade_andares,
           b.apartamentos_por_andar
    INTO v_bloco_identificacao,
         v_quantidade_andares,
         v_apartamentos_por_andar
    FROM blocos b
    WHERE b.id = p_bloco_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION
            USING ERRCODE = 'P0002',
                  MESSAGE = 'Bloco nao encontrado';
    END IF;

    IF v_quantidade_andares IS NULL OR v_quantidade_andares <= 0 THEN
        RAISE EXCEPTION
            USING ERRCODE = '22023',
                  MESSAGE = 'Quantidade de andares deve ser maior que zero';
    END IF;

    IF v_apartamentos_por_andar IS NULL OR v_apartamentos_por_andar <= 0 THEN
        RAISE EXCEPTION
            USING ERRCODE = '22023',
                  MESSAGE = 'Apartamentos por andar deve ser maior que zero';
    END IF;

    FOR v_andar IN 1..v_quantidade_andares LOOP
        FOR v_apartamento IN 1..v_apartamentos_por_andar LOOP
            v_identificacao := fn_gerar_identificacao_unidade(
                v_bloco_identificacao,
                v_andar,
                v_apartamento
            );

            INSERT INTO unidades (
                id,
                identificacao,
                andar,
                bloco_id
            )
            SELECT gen_random_uuid(),
                   v_identificacao,
                   v_andar,
                   p_bloco_id
            WHERE NOT EXISTS (
                SELECT 1
                FROM unidades u
                WHERE u.bloco_id = p_bloco_id
                  AND u.identificacao = v_identificacao
            );

            IF FOUND THEN
                v_inseridos := v_inseridos + 1;
            END IF;
        END LOOP;
    END LOOP;

    RETURN v_inseridos;
END;
$$;


-- Consulta administrativa de chamados
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
