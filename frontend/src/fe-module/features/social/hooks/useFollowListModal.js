import { useCallback, useState } from "react";

export function useFollowListModal() {
  const [isOpen, setIsOpen] = useState(false);
  const [activeType, setActiveType] = useState("followers");

  const openFollowList = useCallback((type) => {
    setActiveType(type === "following" ? "following" : "followers");
    setIsOpen(true);
  }, []);

  const closeFollowList = useCallback(() => {
    setIsOpen(false);
  }, []);

  return {
    isOpen,
    activeType,
    openFollowList,
    closeFollowList,
    setActiveType,
  };
}
