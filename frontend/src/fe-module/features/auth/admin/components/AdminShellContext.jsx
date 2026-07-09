import { createContext, useContext, useEffect, useRef, useState } from "react";
import { AdminLogoutConfirmModal } from "../../components/AdminLogoutConfirmModal.jsx";
import { useAdminLogout } from "../../hooks/useAdminLogout.js";

const AdminShellContext = createContext(null);

export function AdminShellProvider({ children }) {
  const { performAdminLogout, isLoggingOut } = useAdminLogout();
  const [isConfirmOpen, setIsConfirmOpen] = useState(false);
  const searchRef = useRef(null);

  useEffect(() => {
    const onKeyDown = (event) => {
      if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === "k") {
        event.preventDefault();
        searchRef.current?.focus();
      }
    };

    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, []);

  const value = {
    searchRef,
    isLoggingOut,
    openLogoutConfirm: () => setIsConfirmOpen(true),
  };

  return (
    <AdminShellContext.Provider value={value}>
      {children}
      <AdminLogoutConfirmModal
        open={isConfirmOpen}
        isLoggingOut={isLoggingOut}
        onCancel={() => setIsConfirmOpen(false)}
        onConfirm={async () => {
          await performAdminLogout();
          setIsConfirmOpen(false);
        }}
      />
    </AdminShellContext.Provider>
  );
}

export function useAdminShell() {
  const context = useContext(AdminShellContext);
  if (!context) {
    throw new Error("useAdminShell must be used within AdminShellProvider");
  }
  return context;
}
