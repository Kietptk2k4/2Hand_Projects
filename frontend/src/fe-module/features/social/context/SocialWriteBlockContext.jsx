import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import { useAuthSession } from "../../auth/hooks/useAuthSession.jsx";
import { FeedToast } from "../components/FeedToast";
import {
  clearSuspendedWriteBlock,
  registerSocialWriteBlockBridge,
} from "../utils/socialWriteBlockBridge";
import { DEFAULT_SUSPEND_MESSAGE } from "../utils/socialWriteErrors";

const SocialWriteBlockContext = createContext(null);

export function SocialWriteBlockProvider({ children }) {
  const { isAuthenticated } = useAuthSession();
  const [isWriteBlocked, setIsWriteBlocked] = useState(false);
  const [suspendMessage, setSuspendMessage] = useState(null);
  const [toastMessage, setToastMessage] = useState("");
  const hasShownSuspendToastRef = useRef(false);

  const clearWriteBlocked = useCallback(() => {
    setIsWriteBlocked(false);
    setSuspendMessage(null);
    hasShownSuspendToastRef.current = false;
  }, []);

  const setWriteBlocked = useCallback((message) => {
    const resolvedMessage = message?.trim() || DEFAULT_SUSPEND_MESSAGE;

    setIsWriteBlocked((wasBlocked) => {
      if (!wasBlocked && !hasShownSuspendToastRef.current) {
        hasShownSuspendToastRef.current = true;
        setToastMessage(resolvedMessage);
      }
      return true;
    });
    setSuspendMessage(resolvedMessage);
  }, []);

  useEffect(() => {
    registerSocialWriteBlockBridge({ setWriteBlocked, clearWriteBlocked });
    return () => {
      registerSocialWriteBlockBridge({
        setWriteBlocked: null,
        clearWriteBlocked: null,
      });
    };
  }, [clearWriteBlocked, setWriteBlocked]);

  useEffect(() => {
    if (!isAuthenticated) {
      clearWriteBlocked();
      clearSuspendedWriteBlock();
    }
  }, [clearWriteBlocked, isAuthenticated]);

  const dismissToast = useCallback(() => {
    setToastMessage("");
  }, []);

  const value = useMemo(
    () => ({
      isWriteBlocked,
      suspendMessage: suspendMessage || DEFAULT_SUSPEND_MESSAGE,
      setWriteBlocked,
      clearWriteBlocked,
    }),
    [clearWriteBlocked, isWriteBlocked, setWriteBlocked, suspendMessage]
  );

  return (
    <SocialWriteBlockContext.Provider value={value}>
      {children}
      <FeedToast message={toastMessage} onDismiss={dismissToast} />
    </SocialWriteBlockContext.Provider>
  );
}

export function useSocialWriteBlock() {
  const context = useContext(SocialWriteBlockContext);
  if (!context) {
    throw new Error("useSocialWriteBlock must be used inside SocialWriteBlockProvider");
  }
  return context;
}
