import { useCallback, useEffect, useId, useRef, useState } from "react";
import { searchInvestigationUsers } from "../api/userInvestigationApi.js";
import {
  INVESTIGATION_SEARCH_DEBOUNCE_MS,
  INVESTIGATION_SEARCH_LIMIT,
  INVESTIGATION_SEARCH_MIN_LENGTH,
} from "../constants/investigationSearchConstants.js";
import { AdminUserTargetBarView } from "./AdminUserTargetBarView.jsx";

const UUID_RE = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

function formatUserSummary(user) {
  if (!user) return "";
  const name = user.display_name?.trim();
  if (name && name !== user.email) {
    return `${user.email} — ${name}`;
  }
  return user.email || "";
}

function isUuid(value) {
  return UUID_RE.test((value || "").trim());
}

export function AdminUserTargetBar({ userId, selectedUser, onTargetChange }) {
  const listboxId = useId();
  const containerRef = useRef(null);
  const debounceRef = useRef(null);
  const [query, setQuery] = useState("");
  const [results, setResults] = useState([]);
  const [searchStatus, setSearchStatus] = useState("idle");
  const [searchError, setSearchError] = useState("");
  const [isOpen, setIsOpen] = useState(false);

  const runSearch = useCallback(async (rawQuery) => {
    const trimmed = rawQuery.trim();
    if (!trimmed) {
      setResults([]);
      setSearchStatus("idle");
      setSearchError("");
      return;
    }

    const canSearch =
      isUuid(trimmed) || trimmed.length >= INVESTIGATION_SEARCH_MIN_LENGTH;
    if (!canSearch) {
      setResults([]);
      setSearchStatus("idle");
      setSearchError("");
      return;
    }

    setSearchStatus("loading");
    setSearchError("");

    try {
      const data = await searchInvestigationUsers(trimmed, INVESTIGATION_SEARCH_LIMIT);
      setResults(data?.users || []);
      setSearchStatus("ready");
      setIsOpen(true);
    } catch (error) {
      setResults([]);
      setSearchStatus("error");
      setSearchError(error?.message || "Không tìm được người dùng.");
    }
  }, []);

  useEffect(() => {
    if (!userId) {
      setQuery("");
      return;
    }

    if (selectedUser?.user_id === userId) {
      // Identity strip shows the target; keep search empty for “Đổi người dùng”.
      setQuery("");
      return;
    }

    let cancelled = false;

    (async () => {
      try {
        const data = await searchInvestigationUsers(userId, 1);
        const match =
          data?.users?.find((user) => user.user_id === userId) || data?.users?.[0];
        if (!cancelled && match) {
          onTargetChange({ userId, user: match });
          setQuery("");
        }
      } catch {
        if (!cancelled) {
          setQuery("");
        }
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [userId, selectedUser?.user_id, onTargetChange]);

  useEffect(() => {
    const handlePointerDown = (event) => {
      if (!containerRef.current?.contains(event.target)) {
        setIsOpen(false);
      }
    };

    document.addEventListener("mousedown", handlePointerDown);
    return () => document.removeEventListener("mousedown", handlePointerDown);
  }, []);

  useEffect(() => {
    return () => {
      if (debounceRef.current) {
        clearTimeout(debounceRef.current);
      }
    };
  }, []);

  const handleInputChange = (event) => {
    const next = event.target.value;
    setQuery(next);
    if (selectedUser) {
      onTargetChange({ userId: "", user: null });
    }
    setIsOpen(true);

    if (debounceRef.current) {
      clearTimeout(debounceRef.current);
    }

    debounceRef.current = setTimeout(() => {
      runSearch(next);
    }, INVESTIGATION_SEARCH_DEBOUNCE_MS);
  };

  const handleSelectUser = (user) => {
    onTargetChange({ userId: user.user_id, user });
    setQuery("");
    setResults([]);
    setIsOpen(false);
    setSearchStatus("idle");
    setSearchError("");
  };

  const handleClear = () => {
    onTargetChange({ userId: "", user: null });
    setQuery("");
    setResults([]);
    setIsOpen(false);
    setSearchStatus("idle");
    setSearchError("");
  };

  const showDropdown =
    isOpen &&
    (searchStatus === "loading" ||
      searchStatus === "error" ||
      (searchStatus === "ready" && query.trim().length > 0));

  return (
    <AdminUserTargetBarView
      listboxId={listboxId}
      containerRef={containerRef}
      query={query}
      userId={userId}
      selectedUser={selectedUser}
      searchStatus={searchStatus}
      searchError={searchError}
      showDropdown={showDropdown}
      results={results}
      minSearchLength={INVESTIGATION_SEARCH_MIN_LENGTH}
      onInputChange={handleInputChange}
      onInputFocus={() => {
        if (results.length > 0 || searchStatus === "loading") {
          setIsOpen(true);
        }
      }}
      onSelectUser={handleSelectUser}
      onClear={handleClear}
      formatUserSummary={formatUserSummary}
    />
  );
}
