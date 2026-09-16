-- =========================================================
-- V7 - Suporte a status inicial padrao
-- Permite definir o status automatico na abertura do chamado
-- =========================================================

ALTER TABLE status_chamado
    ADD COLUMN IF NOT EXISTS inicial_padrao BOOLEAN NOT NULL DEFAULT FALSE;
