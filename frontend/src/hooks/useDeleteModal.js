import { useState } from "react";

const useDeleteModal = (deleteAction, refreshAction) => {
  const [show, setShow] = useState(false);
  const [itemToDelete, setItemToDelete] = useState(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const askDelete = (item) => {
    setItemToDelete(item);
    setShow(true);
  };

  const confirmDelete = async () => {
    if (!itemToDelete) return;

    setIsDeleting(true);
    try {
      await deleteAction(itemToDelete);
      if (refreshAction) await refreshAction();
    } catch (error) {
      console.error("Błąd usuwania:", error);
    } finally {
      setIsDeleting(false);
      setShow(false);
      setItemToDelete(null);
    }
  };

  return {
    show,
    setShow,
    itemToDelete,
    askDelete,
    confirmDelete,
    isDeleting,
  };
};

export default useDeleteModal;
