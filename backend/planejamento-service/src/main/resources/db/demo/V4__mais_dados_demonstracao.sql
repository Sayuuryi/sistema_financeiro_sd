-- Mais dados para a conta de demonstração (usuário 1); datas relativas ao dia da instalação

-- Nomes sem marcação de exemplo
UPDATE despesa_prevista SET pessoa_nome = replace(pessoa_nome, ' (fictício)', '');
INSERT INTO despesa_prevista(id,usuario_id,pessoa_id,pessoa_nome,categoria_id,categoria_versao,categoria_codigo,categoria_nome,categoria_natureza,categoria_ativo,descricao,referencia,data_prevista,valor_previsto,ativo,conta_gerada_id) VALUES
 (501,1,506,'Nuvem Hospedagem',503,0,'2.04','Software e hospedagem','DESPESA',true,'Upgrade do plano de hospedagem','Orçamento NH-77',CURRENT_DATE+6,450.00,true,NULL),
 (502,1,508,'Gráfica Rápida',504,0,'2.05','Marketing','DESPESA',true,'Material da feira regional','Feira de dezembro',CURRENT_DATE+9,1300.00,true,NULL),
 (503,1,1,'Horizonte Suprimentos',1,0,'2.01','Materiais e suprimentos','DESPESA',true,'Reposição trimestral de materiais','Planejamento trimestral',CURRENT_DATE+18,950.00,true,NULL),
 (504,1,507,'Contabilidade Exata',506,0,'2.07','Impostos e taxas','DESPESA',true,'IRPJ do trimestre','Estimativa da contabilidade',CURRENT_DATE+25,2100.00,true,NULL),
 (505,1,1,'Horizonte Suprimentos',1,0,'2.01','Materiais e suprimentos','DESPESA',true,'Manutenção do ar-condicionado','Orçamento MA-01',CURRENT_DATE-6,600.00,true,516);
