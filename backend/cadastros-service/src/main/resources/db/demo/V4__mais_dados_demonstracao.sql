-- Mais dados para a conta de demonstração (usuário 1); datas relativas ao dia da instalação

-- Nomes sem marcação de exemplo
UPDATE pessoa SET nome = replace(nome, ' (fictício)', '') WHERE id IN (1, 2, 3);
UPDATE banco SET nome = 'Banco Aurora' WHERE id = 1;
UPDATE agencia SET nome = 'Agência Paulista' WHERE id = 1;
INSERT INTO pessoa(id,usuario_id,nome,tipo,email,ativo) VALUES
 (500,1,'Mercado Bom Preço','CLIENTE','financeiro@bompreco.exemplo',true),
 (501,1,'Clínica Vida Plena','CLIENTE','contato@vidaplena.exemplo',true),
 (502,1,'Escola Aprender','CLIENTE','secretaria@aprender.exemplo',true),
 (503,1,'Agência Pixel','CLIENTE','ola@pixel.exemplo',true),
 (504,1,'Imobiliária Central','FORNECEDOR','aluguel@central.exemplo',true),
 (505,1,'Energia Sul','FORNECEDOR',NULL,true),
 (506,1,'Nuvem Hospedagem','FORNECEDOR','faturas@nuvem.exemplo',true),
 (507,1,'Contabilidade Exata','FORNECEDOR','escritorio@exata.exemplo',true),
 (508,1,'Gráfica Rápida','AMBOS','pedidos@grafica.exemplo',true);
INSERT INTO categoria(id,usuario_id,codigo,nome,natureza,ativo) VALUES
 (500,1,'1.02','Venda de produtos','RECEITA',true),
 (501,1,'1.03','Assinaturas e mensalidades','RECEITA',true),
 (502,1,'2.03','Aluguel','DESPESA',true),
 (503,1,'2.04','Software e hospedagem','DESPESA',true),
 (504,1,'2.05','Marketing','DESPESA',true),
 (505,1,'2.06','Serviços contábeis','DESPESA',true),
 (506,1,'2.07','Impostos e taxas','DESPESA',true);
INSERT INTO banco(id,usuario_id,codigo,nome,ativo) VALUES (500,1,'901','Banco Horizonte Digital',true);
INSERT INTO agencia(id,usuario_id,banco_id,numero,nome,ativo) VALUES (500,1,500,'0042','Agência Centro',true);
INSERT INTO conta_bancaria(id,usuario_id,agencia_id,numero,descricao,saldo_inicial,ativo) VALUES
 (500,1,500,'55021-7','Recebimentos digitais',8000.00,true),
 (501,1,1,'98765-4','Reserva de caixa',20000.00,true);
