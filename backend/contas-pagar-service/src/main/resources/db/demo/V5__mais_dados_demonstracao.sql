-- Mais dados para a conta de demonstração (usuário 1); datas relativas ao dia da instalação

-- Nomes sem marcação de exemplo
UPDATE conta SET pessoa_nome = replace(pessoa_nome, ' (fictício)', '');
INSERT INTO conta(id,usuario_id,tipo,pessoa_id,pessoa_nome,categoria_id,categoria_versao,categoria_codigo,categoria_nome,categoria_natureza,categoria_ativo,numero_titulo,tipo_titulo,descricao,emissao,vencimento,valor,desconto_maximo,cancelada,motivo_cancelamento,cancelada_em,despesa_origem_id,origem_hash,criado_em) VALUES
 (500,1,'PAGAR',504,'Imobiliária Central',502,0,'2.03','Aluguel','DESPESA',true,'AL-09','DUPLICATA','Aluguel do escritório',CURRENT_DATE-70,CURRENT_DATE-65,2800.00,0,false,NULL,NULL,NULL,NULL,CURRENT_TIMESTAMP),
 (501,1,'PAGAR',504,'Imobiliária Central',502,0,'2.03','Aluguel','DESPESA',true,'AL-10','DUPLICATA','Aluguel do escritório',CURRENT_DATE-40,CURRENT_DATE-35,2800.00,0,false,NULL,NULL,NULL,NULL,CURRENT_TIMESTAMP),
 (502,1,'PAGAR',504,'Imobiliária Central',502,0,'2.03','Aluguel','DESPESA',true,'AL-11','DUPLICATA','Aluguel do escritório',CURRENT_DATE-10,CURRENT_DATE-5,2800.00,0,false,NULL,NULL,NULL,NULL,CURRENT_TIMESTAMP),
 (503,1,'PAGAR',504,'Imobiliária Central',502,0,'2.03','Aluguel','DESPESA',true,'AL-12','DUPLICATA','Aluguel do escritório',CURRENT_DATE+20,CURRENT_DATE+25,2800.00,0,false,NULL,NULL,NULL,NULL,CURRENT_TIMESTAMP),
 (504,1,'PAGAR',505,'Energia Sul',3,0,'2.02','Energia e utilidades','DESPESA',true,'EN-0931','DUPLICATA','Conta de energia',CURRENT_DATE-60,CURRENT_DATE-50,640.00,0,false,NULL,NULL,NULL,NULL,CURRENT_TIMESTAMP),
 (505,1,'PAGAR',505,'Energia Sul',3,0,'2.02','Energia e utilidades','DESPESA',true,'EN-1031','DUPLICATA','Conta de energia',CURRENT_DATE-30,CURRENT_DATE-20,710.00,0,false,NULL,NULL,NULL,NULL,CURRENT_TIMESTAMP),
 (506,1,'PAGAR',505,'Energia Sul',3,0,'2.02','Energia e utilidades','DESPESA',true,'EN-1131','DUPLICATA','Conta de energia',CURRENT_DATE-2,CURRENT_DATE+8,690.00,0,false,NULL,NULL,NULL,NULL,CURRENT_TIMESTAMP),
 (507,1,'PAGAR',506,'Nuvem Hospedagem',503,0,'2.04','Software e hospedagem','DESPESA',true,'NH-2210','DUPLICATA','Plano de hospedagem',CURRENT_DATE-45,CURRENT_DATE-40,390.00,0,false,NULL,NULL,NULL,NULL,CURRENT_TIMESTAMP),
 (508,1,'PAGAR',506,'Nuvem Hospedagem',503,0,'2.04','Software e hospedagem','DESPESA',true,'NH-2211','DUPLICATA','Plano de hospedagem',CURRENT_DATE-15,CURRENT_DATE-10,390.00,0,false,NULL,NULL,NULL,NULL,CURRENT_TIMESTAMP),
 (509,1,'PAGAR',507,'Contabilidade Exata',505,0,'2.06','Serviços contábeis','DESPESA',true,'CE-310','DUPLICATA','Honorários contábeis',CURRENT_DATE-35,CURRENT_DATE-30,1200.00,0,false,NULL,NULL,NULL,NULL,CURRENT_TIMESTAMP),
 (510,1,'PAGAR',507,'Contabilidade Exata',505,0,'2.06','Serviços contábeis','DESPESA',true,'CE-311','DUPLICATA','Honorários contábeis',CURRENT_DATE-5,CURRENT_DATE-1,1200.00,0,false,NULL,NULL,NULL,NULL,CURRENT_TIMESTAMP),
 (511,1,'PAGAR',508,'Gráfica Rápida',504,0,'2.05','Marketing','DESPESA',true,'GR-118','DUPLICATA','Panfletos e banners',CURRENT_DATE-25,CURRENT_DATE-15,1500.00,0,false,NULL,NULL,NULL,NULL,CURRENT_TIMESTAMP),
 (512,1,'PAGAR',1,'Horizonte Suprimentos',1,0,'2.01','Materiais e suprimentos','DESPESA',true,'HS-450','DUPLICATA','Materiais de limpeza e copa',CURRENT_DATE-20,CURRENT_DATE-12,850.00,0,false,NULL,NULL,NULL,NULL,CURRENT_TIMESTAMP),
 (513,1,'PAGAR',508,'Gráfica Rápida',504,0,'2.05','Marketing','DESPESA',true,'GR-131','DUPLICATA','Cartões de visita e catálogo',CURRENT_DATE-3,CURRENT_DATE+12,980.00,0,false,NULL,NULL,NULL,NULL,CURRENT_TIMESTAMP),
 (514,1,'PAGAR',507,'Contabilidade Exata',506,0,'2.07','Impostos e taxas','DESPESA',true,'DAS-11','DUPLICATA','Simples Nacional',CURRENT_DATE-12,CURRENT_DATE+3,1750.00,0,false,NULL,NULL,NULL,NULL,CURRENT_TIMESTAMP),
 (515,1,'PAGAR',508,'Gráfica Rápida',504,0,'2.05','Marketing','DESPESA',true,'GR-099','DUPLICATA','Campanha de rádio',CURRENT_DATE-50,CURRENT_DATE-40,500.00,0,true,'Campanha suspensa pelo cliente',CURRENT_TIMESTAMP,NULL,NULL,CURRENT_TIMESTAMP),
 (516,1,'PAGAR',1,'Horizonte Suprimentos',1,0,'2.01','Materiais e suprimentos','DESPESA',true,'MA-01','DUPLICATA','Manutenção do ar-condicionado',CURRENT_DATE-6,CURRENT_DATE+4,640.00,0,false,NULL,NULL,505,'00000000000000000000000000000000000000000000000000000000000001f9',CURRENT_TIMESTAMP);
INSERT INTO baixa(id,usuario_id,conta_id,conta_bancaria_id,conta_bancaria_descricao,chave_idempotencia,data,principal,acrescimo,desconto,forma,criado_em) VALUES
 (600,1,500,500,'Recebimentos digitais / 55021-7','00000000-0000-4000-8000-000000000600',CURRENT_DATE-65,2800.00,0,0,'TRANSFERENCIA',CURRENT_TIMESTAMP),
 (601,1,501,500,'Recebimentos digitais / 55021-7','00000000-0000-4000-8000-000000000601',CURRENT_DATE-35,2800.00,0,0,'DEBITO_AUTOMATICO',CURRENT_TIMESTAMP),
 (602,1,502,500,'Recebimentos digitais / 55021-7','00000000-0000-4000-8000-000000000602',CURRENT_DATE-5,2800.00,0,0,'PIX',CURRENT_TIMESTAMP),
 (603,1,504,500,'Recebimentos digitais / 55021-7','00000000-0000-4000-8000-000000000603',CURRENT_DATE-50,640.00,0,0,'BOLETO',CURRENT_TIMESTAMP),
 (604,1,505,500,'Recebimentos digitais / 55021-7','00000000-0000-4000-8000-000000000604',CURRENT_DATE-20,710.00,0,0,'DEBITO_AUTOMATICO',CURRENT_TIMESTAMP),
 (605,1,507,NULL,NULL,'00000000-0000-4000-8000-000000000605',CURRENT_DATE-40,390.00,0,0,'CARTAO_CREDITO',CURRENT_TIMESTAMP),
 (606,1,508,NULL,NULL,'00000000-0000-4000-8000-000000000606',CURRENT_DATE-10,390.00,0,0,'CARTAO_CREDITO',CURRENT_TIMESTAMP),
 (607,1,509,500,'Recebimentos digitais / 55021-7','00000000-0000-4000-8000-000000000607',CURRENT_DATE-30,1200.00,0,0,'PIX',CURRENT_TIMESTAMP),
 (608,1,511,500,'Recebimentos digitais / 55021-7','00000000-0000-4000-8000-000000000608',CURRENT_DATE-14,600.00,0,0,'PIX',CURRENT_TIMESTAMP),
 (609,1,512,500,'Recebimentos digitais / 55021-7','00000000-0000-4000-8000-000000000609',CURRENT_DATE-12,850.00,0,0,'CARTAO_DEBITO',CURRENT_TIMESTAMP);
