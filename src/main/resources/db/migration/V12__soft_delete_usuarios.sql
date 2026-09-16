-- =========================================================
-- V12 - Soft delete de usuarios
-- Introduz indicador logico de atividade na tabela base de usuarios
-- =========================================================

-- Coluna de controle de exclusao logica
ALTER TABLE usuarios
    ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE;

-- Garantia de preenchimento para registros preexistentes
UPDATE usuarios
SET ativo = TRUE
WHERE ativo IS NULL;
