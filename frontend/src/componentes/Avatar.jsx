export function Avatar({ usuario, tamanho = 32, className = "" }) {
  return (
    <img
      className={("avatar-foto " + className).trim()}
      src={usuario?.foto || "/assets/images/avatar-padrao.svg"}
      alt=""
      width={tamanho}
      height={tamanho}
    />
  );
}
