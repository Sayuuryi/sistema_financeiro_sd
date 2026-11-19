-- Conta de demonstração: demo@fluxo.com / 123456
UPDATE usuario
   SET email = 'demo@fluxo.com',
       senha_hash = '$2a$10$Em6Nu9jrbaAZFurks5z4newW.VlhNEbZbRaeDlaR77LlYWfo/kdsW',
       nome = 'Usuário Demo',
       cargo = 'Gestor financeiro',
       empresa = 'Empresa Demo'
 WHERE id = 1;
