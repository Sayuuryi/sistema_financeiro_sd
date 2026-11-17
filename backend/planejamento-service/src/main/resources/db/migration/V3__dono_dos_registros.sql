-- Cada registro pertence a um usuário; o que já existia fica com a conta de demonstração (id 1)
ALTER TABLE despesa_prevista ADD COLUMN usuario_id BIGINT;
UPDATE despesa_prevista SET usuario_id = 1;
ALTER TABLE despesa_prevista ALTER COLUMN usuario_id SET NOT NULL;
CREATE INDEX ix_despesa_prevista_usuario ON despesa_prevista(usuario_id);
