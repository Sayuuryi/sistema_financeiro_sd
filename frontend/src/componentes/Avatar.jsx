import { useQuadroEstatico } from "./QuadroEstatico.js";

export function Avatar({ usuario, tamanho = 32, className = "" }) {
  const foto = useQuadroEstatico(usuario?.foto);
  return (
    <img
      className={("avatar-foto " + className).trim()}
      src={foto || "/assets/images/avatar-padrao.svg"}
      alt=""
      width={tamanho}
      height={tamanho}
      decoding="async"
    />
  );
}
