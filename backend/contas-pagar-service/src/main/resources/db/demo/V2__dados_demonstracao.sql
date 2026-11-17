INSERT INTO conta(id,tipo,pessoa_id,pessoa_nome,categoria_id,categoria_versao,categoria_codigo,categoria_nome,categoria_natureza,categoria_ativo,numero_titulo,tipo_titulo,descricao,emissao,vencimento,valor,desconto_maximo,validade_desconto,criado_em) VALUES
 (100,'PAGAR',1,'Horizonte Suprimentos (fictício)',1,0,'2.01','Materiais e suprimentos','DESPESA',true,'P-001','DUPLICATA','Compra de equipamentos',CURRENT_DATE,CURRENT_DATE+7,1000.00,20.00,CURRENT_DATE+7,CURRENT_TIMESTAMP),
 (200,'RECEBER',2,'Estúdio Ipê (fictício)',2,0,'1.01','Prestação de serviços','RECEITA',true,'R-001','DUPLICATA','Serviço de design',CURRENT_DATE-5,CURRENT_DATE-1,300.00,0,NULL,CURRENT_TIMESTAMP),
 (300,'PAGAR',1,'Horizonte Suprimentos (fictício)',1,0,'2.01','Materiais e suprimentos','DESPESA',true,'P-002','DUPLICATA','Materiais de escritório',CURRENT_DATE,CURRENT_DATE+10,100.00,5.00,CURRENT_DATE+10,CURRENT_TIMESTAMP);
INSERT INTO baixa(id,conta_id,conta_bancaria_id,conta_bancaria_descricao,chave_idempotencia,data,principal,acrescimo,desconto,forma,criado_em) VALUES
 (400,300,1,'Conta operacional / 12345-6','1053cdb5-dd18-4979-819f-60d1bc93c142',CURRENT_DATE,40.00,2.00,5.00,'PIX',CURRENT_TIMESTAMP);
ALTER TABLE conta ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE baixa ALTER COLUMN id RESTART WITH 1000;
