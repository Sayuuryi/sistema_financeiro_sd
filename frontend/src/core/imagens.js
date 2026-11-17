import { services } from "../config.js";

const KB = 1024;

// Mesmos limites do serviço de usuários; GIF vai sem redução para manter a animação
export const LIMITES = {
  foto: { gif: 1024 * KB, outros: 400 * KB },
  capa: { gif: 4096 * KB, outros: 1024 * KB },
};

const TIPOS = ["image/png", "image/jpeg", "image/webp", "image/gif"];

// Área central da imagem que preenche o destino sem distorcer
export function recorte(largura, altura, destinoLargura, destinoAltura) {
  const escala = Math.max(destinoLargura / largura, destinoAltura / altura);
  const sw = Math.round(destinoLargura / escala);
  const sh = Math.round(destinoAltura / escala);
  return { sx: Math.round((largura - sw) / 2), sy: Math.round((altura - sh) / 2), sw, sh };
}

export function conferirArquivo(arquivo, destino) {
  if (!TIPOS.includes(arquivo.type)) {
    throw new Error("Escolha uma imagem PNG, JPEG, WebP ou GIF.");
  }
  if (arquivo.type === "image/gif" && arquivo.size > LIMITES[destino].gif) {
    throw new Error(
      "O GIF passa de " + LIMITES[destino].gif / (1024 * KB) + " MB. Escolha um arquivo menor.",
    );
  }
  if (arquivo.size > 15 * 1024 * KB) {
    throw new Error("A imagem passa de 15 MB.");
  }
}

export const FUNDOS = {
  "fundo:teal": { nome: "Fluxo", css: "linear-gradient(120deg, #122d36, #087f76)" },
  "fundo:oceano": { nome: "Oceano", css: "linear-gradient(120deg, #0f2f4a, #1f7a8c)" },
  "fundo:aurora": { nome: "Aurora", css: "linear-gradient(120deg, #087f76, #5ed0b4 65%, #c9efe2)" },
  "fundo:areia": { nome: "Areia", css: "linear-gradient(120deg, #8a5a14, #d29a44 60%, #f3d9a6)" },
  "fundo:grafite": { nome: "Grafite", css: "linear-gradient(120deg, #1b2226, #4a5a61)" },
};

// O serviço devolve o caminho; a imagem é servida pelo próprio serviço de usuários
export const urlImagem = (caminho) =>
  caminho?.startsWith("/api/") ? services.usuarios + caminho : caminho || null;

export const ehImagem = (valor) => /^(\/api\/|data:|https?:)/.test(valor ?? "");

export const ehAnimada = (valor) => /\.gif$/i.test(valor ?? "");

export function estiloCapa(capa) {
  if (ehImagem(capa)) {
    return {
      backgroundImage: `url("${urlImagem(capa)}")`,
      backgroundSize: "cover",
      backgroundPosition: "center",
    };
  }
  return { background: (FUNDOS[capa] ?? FUNDOS["fundo:teal"]).css };
}

// Estáticas são recortadas e reduzidas aqui; GIF segue como veio
export async function prepararImagem(arquivo, destino, largura, altura) {
  conferirArquivo(arquivo, destino);
  if (arquivo.type === "image/gif") {
    return arquivo;
  }
  let bitmap;
  try {
    bitmap = await createImageBitmap(arquivo);
  } catch {
    throw new Error("Não foi possível abrir esta imagem.");
  }
  const { sx, sy, sw, sh } = recorte(bitmap.width, bitmap.height, largura, altura);
  const canvas = document.createElement("canvas");
  canvas.width = largura;
  canvas.height = altura;
  const ctx = canvas.getContext("2d");
  ctx.imageSmoothingQuality = "high";
  ctx.drawImage(bitmap, sx, sy, sw, sh, 0, 0, largura, altura);
  bitmap.close?.();
  return new Promise((resolve, reject) =>
    canvas.toBlob(
      (blob) => (blob ? resolve(blob) : reject(new Error("Não foi possível preparar a imagem."))),
      "image/jpeg",
      0.86,
    ),
  );
}
