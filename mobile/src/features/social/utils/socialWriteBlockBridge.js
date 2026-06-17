let bridgeHandlers = {
  setWriteBlocked: null,
  clearWriteBlocked: null,
};

export function registerSocialWriteBlockBridge(handlers) {
  bridgeHandlers = {
    setWriteBlocked: handlers.setWriteBlocked ?? null,
    clearWriteBlocked: handlers.clearWriteBlocked ?? null,
  };
}

export function notifySuspendedWrite(message) {
  bridgeHandlers.setWriteBlocked?.(message);
}

export function clearSuspendedWriteBlock() {
  bridgeHandlers.clearWriteBlocked?.();
}
