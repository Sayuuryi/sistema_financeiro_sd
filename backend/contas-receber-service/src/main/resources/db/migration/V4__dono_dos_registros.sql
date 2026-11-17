-- Cada registro pertence a um usuário; o que já existia fica com a conta de demonstração (id 1)
ALTER TABLE conta ADD COLUMN usuario_id BIGINT;
UPDATE conta SET usuario_id = 1;
ALTER TABLE conta ALTER COLUMN usuario_id SET NOT NULL;
CREATE INDEX ix_conta_usuario ON conta(usuario_id);
ALTER TABLE baixa ADD COLUMN usuario_id BIGINT;
UPDATE baixa SET usuario_id = 1;
ALTER TABLE baixa ALTER COLUMN usuario_id SET NOT NULL;
CREATE INDEX ix_baixa_usuario ON baixa(usuario_id);
