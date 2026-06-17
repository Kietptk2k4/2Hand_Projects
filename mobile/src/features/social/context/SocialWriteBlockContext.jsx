import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import { useSocialToast } from "../../../shared/components/SocialToastProvider";
import { useCurrentUserId } from "../hooks/useCurrentUserId";
import {
  clearSuspendedWriteBlock,
  registerSocialWriteBlockBridge,
} from "../utils/socialWriteBlockBridge";
import { DEFAULT_SUSPEND_MESSAGE } from "../utils/socialWriteErrors";

const SocialWriteBlockContext = createContext(null);

export function SocialWriteBlockProvider({ children }) {
  const currentUserId = useCurrentUserId();
  const { showToast } = useSocialToast();
  const [isWriteBlocked, setIsWriteBlocked] = useState(false);
  const [suspendMessage, setSuspendMessage] = useState(null);
  const hasShownSuspendToastRef = useRef(false);

  const clearWriteBlocked = useCallback(() => {
    setIsWriteBlocked(false);
    setSuspendMessage(null);
    hasShownSuspendToastRef.current = false;
  }, []);

  const setWriteBlocked = useCallback(
    (message) => {
      const resolvedMessage = message?.trim() || DEFAULT_SUSPEND_MESSAGE;

      setIsWriteBlocked((wasBlocked) => {
        if (!wasBlocked && !hasShownSuspendToastRef.current) {
          hasShownSuspendToastRef.current = true;
          showToast(resolvedMessage, "error");
        }
        return true;
      });
      setSuspendMessage(resolvedMessage);
    },
    [showToast]
  );

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
    if (!currentUserId) {
      clearWriteBlocked();
      clearSuspendedWriteBlock();
    }
  }, [clearWriteBlocked, currentUserId]);

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
