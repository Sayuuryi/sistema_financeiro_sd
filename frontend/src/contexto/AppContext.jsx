import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import { useQueryClient } from "@tanstack/react-query";
import { hojeSaoPaulo, periodoDoMes } from "../core/datas.js";

const AppContext = createContext(null);

export function useApp() {
  const contexto = useContext(AppContext);
  if (!contexto) {
    throw new Error("useApp deve ser usado dentro de <AppProvider>.");
  }
  return contexto;
}

// Estado compartilhado entre as telas
export function AppProvider({ children }) {
  const queryClient = useQueryClient();
  const [today] = useState(() => hojeSaoPaulo());
  const [period, setPeriodState] = useState(() => periodoDoMes(today));
  const [master, setMaster] = useState("pessoas");
  const [toastMessage, setToastMessage] = useState("");
  const [modal, setModal] = useState(null);
  const toastTimer = useRef();
  const modalKey = useRef(0);

  const toast = useCallback((message) => {
    setToastMessage(message);
    clearTimeout(toastTimer.current);
    toastTimer.current = setTimeout(() => setToastMessage(""), 5500);
  }, []);

  useEffect(() => () => clearTimeout(toastTimer.current), []);

  const openModal = useCallback((title, content) => {
    modalKey.current += 1;
    setModal({ title, content, key: modalKey.current });
  }, []);

  const closeModal = useCallback(() => setModal(null), []);

  const setPeriod = useCallback((from, to) => setPeriodState({ from, to }), []);

  const atualizar = useCallback(() => queryClient.invalidateQueries(), [queryClient]);

  const value = useMemo(
    () => ({
      today,
      from: period.from,
      to: period.to,
      setPeriod,
      master,
      setMaster,
      toast,
      toastMessage,
      modal,
      openModal,
      closeModal,
      atualizar,
    }),
    [
      today,
      period,
      setPeriod,
      master,
      toast,
      toastMessage,
      modal,
      openModal,
      closeModal,
      atualizar,
    ],
  );

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>;
}
