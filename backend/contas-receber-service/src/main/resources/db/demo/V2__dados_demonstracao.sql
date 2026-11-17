INSERT INTO conta(id,tipo,pessoa_id,pessoa_nome,categoria_id,categoria_versao,categoria_codigo,categoria_nome,categoria_natureza,categoria_ativo,numero_titulo,tipo_titulo,descricao,emissao,vencimento,valor,desconto_maximo,validade_desconto,criado_em) VALUES
 (200,'RECEBER',2,'Estúdio Ipê',2,0,'1.01','Prestação de serviços','RECEITA',true,'R-001','DUPLICATA','Serviço de design',CURRENT_DATE-5,CURRENT_DATE-1,300.00,0,NULL,CURRENT_TIMESTAMP);
ALTER TABLE conta ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE baixa ALTER COLUMN id RESTART WITH 1000;
