-- Mais dados para a conta de demonstração (usuário 1); datas relativas ao dia da instalação

-- Nomes sem marcação de exemplo
UPDATE conta SET pessoa_nome = replace(pessoa_nome, ' (fictício)', '');
INSERT INTO conta(id,usuario_id,tipo,pessoa_id,pessoa_nome,categoria_id,categoria_versao,categoria_codigo,categoria_nome,categoria_natureza,categoria_ativo,numero_titulo,tipo_titulo,descricao,emissao,vencimento,valor,desconto_maximo,cancelada,motivo_cancelamento,cancelada_em,despesa_origem_id,origem_hash,criado_em) VALUES
 (500,1,'RECEBER',500,'Mercado Bom Preço',500,0,'1.02','Venda de produtos','RECEITA',true,'BP-071','DUPLICATA','Fornecimento mensal de produtos',CURRENT_DATE-75,CURRENT_DATE-60,4200.00,0,false,NULL,NULL,NULL,NULL,CURRENT_TIMESTAMP),
 (501,1,'RECEBER',500,'Mercado Bom Preço',500,0,'1.02','Venda de produtos','RECEITA',true,'BP-072','DUPLICATA','Fornecimento mensal de produtos',CURRENT_DATE-45,CURRENT_DATE-30,3900.00,0,false,NULL,NULL,NULL,NULL,CURRENT_TIMESTAMP),
 (502,1,'RECEBER',500,'Mercado Bom Preço',500,0,'1.02','Venda de produtos','RECEITA',true,'BP-073','DUPLICATA','Fornecimento mensal de produtos',CURRENT_DATE-15,CURRENT_DATE-1,4100.00,0,false,NULL,NULL,NULL,NULL,CURRENT_TIMESTAMP),
 (503,1,'RECEBER',500,'Mercado Bom Preço',500,0,'1.02','Venda de produtos','RECEITA',true,'BP-074','DUPLICATA','Fornecimento mensal de produtos',CURRENT_DATE-2,CURRENT_DATE+13,4300.00,0,false,NULL,NULL,NULL,NULL,CURRENT_TIMESTAMP),
 (504,1,'RECEBER',501,'Clínica Vida Plena',501,0,'1.03','Assinaturas e mensalidades','RECEITA',true,'VP-09','DUPLICATA','Mensalidade do sistema de agenda',CURRENT_DATE-65,CURRENT_DATE-60,1500.00,0,false,NULL,NULL,NULL,NULL,CURRENT_TIMESTAMP),
 (505,1,'RECEBER',501,'Clínica Vida Plena',501,0,'1.03','Assinaturas e mensalidades','RECEITA',true,'VP-10','DUPLICATA','Mensalidade do sistema de agenda',CURRENT_DATE-35,CURRENT_DATE-30,1500.00,0,false,NULL,NULL,NULL,NULL,CURRENT_TIMESTAMP),
 (506,1,'RECEBER',501,'Clínica Vida Plena',501,0,'1.03','Assinaturas e mensalidades','RECEITA',true,'VP-11','DUPLICATA','Mensalidade do sistema de agenda',CURRENT_DATE-5,CURRENT_DATE,1500.00,0,false,NULL,NULL,NULL,NULL,CURRENT_TIMESTAMP),
 (507,1,'RECEBER',502,'Escola Aprender',2,0,'1.01','Prestação de serviços','RECEITA',true,'EA-201','DUPLICATA','Oficina de planejamento financeiro',CURRENT_DATE-50,CURRENT_DATE-40,2600.00,0,false,NULL,NULL,NULL,NULL,CURRENT_TIMESTAMP),
 (508,1,'RECEBER',502,'Escola Aprender',2,0,'1.01','Prestação de serviços','RECEITA',true,'EA-202','DUPLICATA','Consultoria de matrículas',CURRENT_DATE-20,CURRENT_DATE-10,2600.00,0,false,NULL,NULL,NULL,NULL,CURRENT_TIMESTAMP),
 (509,1,'RECEBER',503,'Agência Pixel',2,0,'1.01','Prestação de serviços','RECEITA',true,'AP-33','DUPLICATA','Identidade visual da campanha',CURRENT_DATE-25,CURRENT_DATE-20,3200.00,0,false,NULL,NULL,NULL,NULL,CURRENT_TIMESTAMP),
 (510,1,'RECEBER',503,'Agência Pixel',2,0,'1.01','Prestação de serviços','RECEITA',true,'AP-34','DUPLICATA','Gestão de redes sociais',CURRENT_DATE-1,CURRENT_DATE+20,3500.00,0,false,NULL,NULL,NULL,NULL,CURRENT_TIMESTAMP),
 (511,1,'RECEBER',2,'Estúdio Ipê',2,0,'1.01','Prestação de serviços','RECEITA',true,'R-002','DUPLICATA','Revisão de portfólio',CURRENT_DATE-40,CURRENT_DATE-33,900.00,0,false,NULL,NULL,NULL,NULL,CURRENT_TIMESTAMP),
 (512,1,'RECEBER',3,'Papelaria Ponte',500,0,'1.02','Venda de produtos','RECEITA',true,'PP-58','DUPLICATA','Venda de papel reciclado',CURRENT_DATE-28,CURRENT_DATE-22,640.00,0,false,NULL,NULL,NULL,NULL,CURRENT_TIMESTAMP),
 (513,1,'RECEBER',508,'Gráfica Rápida',500,0,'1.02','Venda de produtos','RECEITA',true,'GR-R12','DUPLICATA','Venda de suprimentos de impressão',CURRENT_DATE-8,CURRENT_DATE+7,780.00,0,false,NULL,NULL,NULL,NULL,CURRENT_TIMESTAMP),
 (514,1,'RECEBER',3,'Papelaria Ponte',500,0,'1.02','Venda de produtos','RECEITA',true,'PP-41','DUPLICATA','Venda de cadernos',CURRENT_DATE-55,CURRENT_DATE-45,300.00,0,true,'Pedido devolvido',CURRENT_TIMESTAMP,NULL,NULL,CURRENT_TIMESTAMP);
INSERT INTO baixa(id,usuario_id,conta_id,conta_bancaria_id,conta_bancaria_descricao,chave_idempotencia,data,principal,acrescimo,desconto,forma,criado_em) VALUES
 (700,1,500,501,'Reserva de caixa / 98765-4','00000000-0000-4000-8000-000000000700',CURRENT_DATE-58,4200.00,0,0,'BOLETO',CURRENT_TIMESTAMP),
 (701,1,501,501,'Reserva de caixa / 98765-4','00000000-0000-4000-8000-000000000701',CURRENT_DATE-30,3900.00,0,0,'PIX',CURRENT_TIMESTAMP),
 (702,1,502,501,'Reserva de caixa / 98765-4','00000000-0000-4000-8000-000000000702',CURRENT_DATE-2,2000.00,0,0,'PIX',CURRENT_TIMESTAMP),
 (703,1,504,501,'Reserva de caixa / 98765-4','00000000-0000-4000-8000-000000000703',CURRENT_DATE-60,1500.00,0,0,'PIX_AUTOMATICO',CURRENT_TIMESTAMP),
 (704,1,505,501,'Reserva de caixa / 98765-4','00000000-0000-4000-8000-000000000704',CURRENT_DATE-30,1500.00,0,0,'PIX_AUTOMATICO',CURRENT_TIMESTAMP),
 (705,1,506,501,'Reserva de caixa / 98765-4','00000000-0000-4000-8000-000000000705',CURRENT_DATE,1500.00,0,0,'PIX_AUTOMATICO',CURRENT_TIMESTAMP),
 (706,1,507,NULL,NULL,'00000000-0000-4000-8000-000000000706',CURRENT_DATE-38,2600.00,0,0,'CARTAO_CREDITO',CURRENT_TIMESTAMP),
 (707,1,509,501,'Reserva de caixa / 98765-4','00000000-0000-4000-8000-000000000707',CURRENT_DATE-18,3200.00,0,0,'TRANSFERENCIA',CURRENT_TIMESTAMP),
 (708,1,511,NULL,NULL,'00000000-0000-4000-8000-000000000708',CURRENT_DATE-33,900.00,0,0,'CARTEIRA_DIGITAL',CURRENT_TIMESTAMP),
 (709,1,512,501,'Reserva de caixa / 98765-4','00000000-0000-4000-8000-000000000709',CURRENT_DATE-22,640.00,0,0,'CARTAO_DEBITO',CURRENT_TIMESTAMP);
