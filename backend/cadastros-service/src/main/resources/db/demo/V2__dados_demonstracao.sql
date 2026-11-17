INSERT INTO pessoa(id,nome,tipo,ativo) VALUES
 (1,'Horizonte Suprimentos (fictício)','FORNECEDOR',true),
 (2,'Estúdio Ipê (fictício)','CLIENTE',true),
 (3,'Papelaria Ponte (fictício)','AMBOS',true);
INSERT INTO categoria(id,codigo,nome,natureza,ativo) VALUES
 (1,'2.01','Materiais e suprimentos','DESPESA',true),
 (2,'1.01','Prestação de serviços','RECEITA',true),
 (3,'2.02','Energia e utilidades','DESPESA',true);
INSERT INTO banco(id,codigo,nome,ativo) VALUES (1,'999','Banco Demonstração (fictício)',true);
INSERT INTO agencia(id,banco_id,numero,nome,ativo) VALUES (1,1,'0001','Agência de demonstração',true);
INSERT INTO conta_bancaria(id,agencia_id,numero,descricao,saldo_inicial,ativo) VALUES (1,1,'12345-6','Conta operacional',15000.00,true);
ALTER TABLE pessoa ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE categoria ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE banco ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE agencia ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE conta_bancaria ALTER COLUMN id RESTART WITH 1000;
