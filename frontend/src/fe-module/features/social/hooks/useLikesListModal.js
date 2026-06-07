import { useCallback, useState } from "react";

export function useLikesListModal() {
  const [isOpen, setIsOpen] = useState(false);
  const [targetType, setTargetType] = useState(null);
  const [targetId, setTargetId] = useState(null);
  const [likeCount, setLikeCount] = useState(0);

  const openLikesList = useCallback(({ type, targetId: id, likeCount: count = 0 }) => {
    const normalizedCount = Number(count) || 0;
    if (!type || !id || normalizedCount <= 0) return;
    setTargetType(type);
    setTargetId(id);
    setLikeCount(normalizedCount);
    setIsOpen(true);
  }, []);

  const closeLikesList = useCallback(() => {
    setIsOpen(false);
  }, []);

  return {
    isOpen,
    targetType,
    targetId,
    likeCount,
    openLikesList,
    closeLikesList,
  };
}