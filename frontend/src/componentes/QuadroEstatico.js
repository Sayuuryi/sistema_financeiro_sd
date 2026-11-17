import { useEffect, useState } from "react";
import { ehAnimada, urlImagem } from "../core/imagens.js";

const reduzirMovimento = () =>
  typeof window !== "undefined" && window.matchMedia?.("(prefers-reduced-motion: reduce)").matches;

// Com "reduzir movimento" ligado, GIF aparece parado no primeiro quadro
export function useQuadroEstatico(caminho) {
  const url = urlImagem(caminho);
  const parar = ehAnimada(caminho) && reduzirMovimento();
  const [quadro, setQuadro] = useState(null);

  useEffect(() => {
    if (!parar) return;
    let ativo = true;
    const img = new Image();
    img.crossOrigin = "anonymous";
    img.onload = () => {
      try {
        const canvas = document.createElement("canvas");
        canvas.width = img.naturalWidth;
        canvas.height = img.naturalHeight;
        canvas.getContext("2d").drawImage(img, 0, 0);
        if (ativo) setQuadro(canvas.toDataURL("image/png"));
      } catch {}
    };
    img.src = url;
    return () => {
      ativo = false;
    };
  }, [url, parar]);

  return parar ? quadro : url;
}
