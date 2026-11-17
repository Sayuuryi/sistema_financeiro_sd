INSERT INTO despesa_prevista(id,pessoa_id,pessoa_nome,categoria_id,categoria_versao,categoria_codigo,categoria_nome,categoria_natureza,categoria_ativo,descricao,referencia,data_prevista,valor_previsto,ativo) VALUES
 (500,1,'Horizonte Suprimentos (fictício)',1,0,'2.01','Materiais e suprimentos','DESPESA',true,'Reposição planejada de materiais','Planejamento mensal',CURRENT_DATE+5,300.00,true);
ALTER TABLE despesa_prevista ALTER COLUMN id RESTART WITH 1000;
