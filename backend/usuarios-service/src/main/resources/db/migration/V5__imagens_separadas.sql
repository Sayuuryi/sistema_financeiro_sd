-- Foto e capa saem do perfil: ficam em bytes, e o perfil leva só o endereço
CREATE TABLE imagem_usuario (
 usuario_id BIGINT NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
 tipo VARCHAR(10) NOT NULL CHECK (tipo IN ('FOTO','CAPA')),
 conteudo BYTEA NOT NULL,
 media_type VARCHAR(30) NOT NULL,
 hash CHAR(64) NOT NULL,
 atualizado_em TIMESTAMP WITH TIME ZONE NOT NULL,
 PRIMARY KEY (usuario_id, tipo)
);
INSERT INTO imagem_usuario(usuario_id, tipo, conteudo, media_type, hash, atualizado_em)
SELECT id, 'FOTO', bytes, tipo, encode(sha256(bytes), 'hex'), CURRENT_TIMESTAMP FROM (
 SELECT id, decode(split_part(foto, ',', 2), 'base64') AS bytes, substring(foto FROM '^data:([^;]+);') AS tipo
 FROM usuario WHERE foto LIKE 'data:image/%') AS antigas;
INSERT INTO imagem_usuario(usuario_id, tipo, conteudo, media_type, hash, atualizado_em)
SELECT id, 'CAPA', bytes, tipo, encode(sha256(bytes), 'hex'), CURRENT_TIMESTAMP FROM (
 SELECT id, decode(split_part(capa, ',', 2), 'base64') AS bytes, substring(capa FROM '^data:([^;]+);') AS tipo
 FROM usuario WHERE capa LIKE 'data:image/%') AS antigas;
UPDATE usuario SET capa = NULL WHERE capa LIKE 'data:%';
ALTER TABLE usuario DROP COLUMN foto;
