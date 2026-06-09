import { useCallback, useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";

export function useRouteToastMessage(stateKey = "message") {
  const location = useLocation();
  const navigate = useNavigate();
  const [toastMessage, setToastMessage] = useState("");

  useEffect(() => {
    const message = location.state?.[stateKey];
    if (!message || typeof message !== "string") return;

    setToastMessage(message);
    navigate(
      { pathname: location.pathname, search: location.search },
      { replace: true, state: {} },
    );
  }, [location.pathname, location.search, location.state, navigate, stateKey]);

  const dismissToast = useCallback(() => setToastMessage(""), []);

  return { toastMessage, setToastMessage, dismissToast };
}
