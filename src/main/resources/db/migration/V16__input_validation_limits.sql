-- =========================================================
-- V16 - Limites de entrada e tamanho de anexos
-- Adiciona restricoes de tamanho para campos textuais e anexos
-- =========================================================

ALTER TABLE usuarios
    ALTER COLUMN nome SET NOT NULL,
    ALTER COLUMN email SET NOT NULL,
    ALTER COLUMN senha SET NOT NULL;

ALTER TABLE usuarios
    ADD CONSTRAINT chk_usuarios_nome_len
        CHECK (char_length(btrim(nome)) BETWEEN 1 AND 255),
    ADD CONSTRAINT chk_usuarios_email_len
        CHECK (char_length(btrim(email)) BETWEEN 1 AND 255),
    ADD CONSTRAINT chk_usuarios_senha_len
        CHECK (char_length(btrim(senha)) BETWEEN 1 AND 255);

ALTER TABLE blocos
    ALTER COLUMN identificacao SET NOT NULL;

ALTER TABLE blocos
    ADD CONSTRAINT chk_blocos_identificacao_len
        CHECK (char_length(btrim(identificacao)) BETWEEN 1 AND 255);

ALTER TABLE unidades
    ALTER COLUMN identificacao SET NOT NULL;

ALTER TABLE unidades
    ADD CONSTRAINT chk_unidades_identificacao_len
        CHECK (char_length(btrim(identificacao)) BETWEEN 1 AND 255);

ALTER TABLE tipos_chamado
    ALTER COLUMN titulo SET NOT NULL;

ALTER TABLE tipos_chamado
    ADD CONSTRAINT chk_tipos_chamado_titulo_len
        CHECK (char_length(btrim(titulo)) BETWEEN 1 AND 255);

ALTER TABLE status_chamado
    ALTER COLUMN nome SET NOT NULL;

ALTER TABLE status_chamado
    ADD CONSTRAINT chk_status_chamado_nome_len
        CHECK (char_length(btrim(nome)) BETWEEN 1 AND 255);

ALTER TABLE chamados
    ALTER COLUMN descricao SET NOT NULL;

ALTER TABLE chamados
    ADD CONSTRAINT chk_chamados_descricao_len
        CHECK (char_length(btrim(descricao)) BETWEEN 1 AND 255);

ALTER TABLE comentarios
    ALTER COLUMN mensagem SET NOT NULL;

ALTER TABLE comentarios
    ADD CONSTRAINT chk_comentarios_mensagem_len
        CHECK (char_length(btrim(mensagem)) BETWEEN 1 AND 255);

ALTER TABLE anexos_chamado
    ADD CONSTRAINT chk_anexos_chamado_nome_len
        CHECK (char_length(btrim(nome_arquivo)) BETWEEN 1 AND 255),
    ADD CONSTRAINT chk_anexos_chamado_content_type_len
        CHECK (char_length(btrim(content_type)) BETWEEN 1 AND 255),
    ADD CONSTRAINT chk_anexos_chamado_tamanho
        CHECK (tamanho_bytes BETWEEN 1 AND 5242880),
    ADD CONSTRAINT chk_anexos_chamado_conteudo
        CHECK (octet_length(conteudo) BETWEEN 1 AND 5242880 AND octet_length(conteudo) = tamanho_bytes);

ALTER TABLE anexos_comentario
    ADD CONSTRAINT chk_anexos_comentario_nome_len
        CHECK (char_length(btrim(nome_arquivo)) BETWEEN 1 AND 255),
    ADD CONSTRAINT chk_anexos_comentario_content_type_len
        CHECK (char_length(btrim(content_type)) BETWEEN 1 AND 255),
    ADD CONSTRAINT chk_anexos_comentario_tamanho
        CHECK (tamanho_bytes BETWEEN 1 AND 5242880),
    ADD CONSTRAINT chk_anexos_comentario_conteudo
        CHECK (octet_length(conteudo) BETWEEN 1 AND 5242880 AND octet_length(conteudo) = tamanho_bytes);
