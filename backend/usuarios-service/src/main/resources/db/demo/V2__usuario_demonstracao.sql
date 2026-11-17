-- Conta de demonstração: demo@fluxo.local / fluxo1234
INSERT INTO usuario(id,nome,email,senha_hash,criado_em) VALUES
 (1,'Conta Demonstração','demo@fluxo.local','$2a$10$4n9o9Ljas/tBgNWzfJBwMOzVpR84jfINn4xoTTN09vmDUEc8/k5UO',CURRENT_TIMESTAMP);
ALTER TABLE usuario ALTER COLUMN id RESTART WITH 1000;
