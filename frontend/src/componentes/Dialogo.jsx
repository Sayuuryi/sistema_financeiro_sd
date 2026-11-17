import { useEffect, useRef } from "react";
import { X } from "lucide-react";
import { useApp } from "../contexto/AppContext.jsx";

export function Toast() {
  const { toastMessage } = useApp();
  return (
    <div id="toast" role="status" aria-live="polite" hidden={!toastMessage}>
      {toastMessage}
    </div>
  );
}

export function Modal() {
  const { modal, closeModal } = useApp();
  const ref = useRef(null);

  useEffect(() => {
    const dialog = ref.current;
    if (!dialog) return;
    if (modal && !dialog.open) {
      dialog.showModal?.();
    } else if (!modal && dialog.open) {
      dialog.close();
    }
  }, [modal]);

  return (
    <dialog id="modal" ref={ref} onClose={closeModal}>
      <div className="dialog-head">
        <div>
          <h2 id="modal-title">{modal?.title}</h2>
        </div>
        <button
          className="icon-button"
          id="close-modal"
          aria-label="Fechar janela"
          onClick={closeModal}>
          <X size={20} aria-hidden="true" />
        </button>
      </div>
      <div id="modal-body">
        {modal ? (
          <div className="dialog-content" key={modal.key}>
            {modal.content}
          </div>
        ) : null}
      </div>
    </dialog>
  );
}
