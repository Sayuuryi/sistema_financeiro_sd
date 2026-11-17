// Área central da imagem que preenche o destino sem distorcer
export function recorte(largura, altura, destinoLargura, destinoAltura) {
  const escala = Math.max(destinoLargura / largura, destinoAltura / altura);
  const sw = Math.round(destinoLargura / escala);
  const sh = Math.round(destinoAltura / escala);
  return { sx: Math.round((largura - sw) / 2), sy: Math.round((altura - sh) / 2), sw, sh };
}

export const FUNDOS = {
  "fundo:teal": { nome: "Fluxo", css: "linear-gradient(120deg, #122d36, #087f76)" },
  "fundo:oceano": { nome: "Oceano", css: "linear-gradient(120deg, #0f2f4a, #1f7a8c)" },
  "fundo:aurora": { nome: "Aurora", css: "linear-gradient(120deg, #087f76, #5ed0b4 65%, #c9efe2)" },
  "fundo:areia": { nome: "Areia", css: "linear-gradient(120deg, #8a5a14, #d29a44 60%, #f3d9a6)" },
  "fundo:grafite": { nome: "Grafite", css: "linear-gradient(120deg, #1b2226, #4a5a61)" },
};

export function estiloCapa(capa) {
  if (capa?.startsWith("data:image/")) {
    return {
      backgroundImage: `url("${capa}")`,
      backgroundSize: "cover",
      backgroundPosition: "center",
    };
  }
  return { background: (FUNDOS[capa] ?? FUNDOS["fundo:teal"]).css };
}

const TIPOS = ["image/png", "image/jpeg", "image/webp"];

// Recorta e reduz no navegador para enviar poucos KB ao servidor
export async function reduzirImagem(arquivo, largura, altura) {
  if (!TIPOS.includes(arquivo.type)) {
    throw new Error("Escolha uma imagem PNG, JPEG ou WebP.");
  }
  if (arquivo.size > 15 * 1024 * 1024) {
    throw new Error("A imagem passa de 15 MB.");
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
  return canvas.toDataURL("image/jpeg", 0.86);
}
