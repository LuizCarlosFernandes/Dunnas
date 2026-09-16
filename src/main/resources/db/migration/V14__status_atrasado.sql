-- =========================================================
-- V14 - Status automatico de atraso
-- Garante o status ATRASADO para chamados que excederem o SLA
-- =========================================================

INSERT INTO status_chamado (id, nome, inicial_padrao)
SELECT gen_random_uuid(), 'Atrasado', FALSE
WHERE NOT EXISTS (
    SELECT 1
    FROM status_chamado
    WHERE lower(nome) = lower('Atrasado')
);
