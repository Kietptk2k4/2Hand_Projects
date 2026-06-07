import { useCallback, useEffect, useId, useRef, useState } from "react";
import { searchInvestigationUsers } from "../api/userInvestigationApi.js";
import {
  INVESTIGATION_SEARCH_DEBOUNCE_MS,
  INVESTIGATION_SEARCH_LIMIT,
  INVESTIGATION_SEARCH_MIN_LENGTH,
} from "../constants/investigationSearchConstants.js";
import { AccountCard } from "../../../../../shared/ui/auth/authUi.jsx";

const UUID_RE = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

function formatUserSummary(user) {
  if (!user) return "";
  const name = user.display_name?.trim() || user.email;
  return `${user.email} — ${name}`;
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
      setQuery(formatUserSummary(selectedUser));
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
          setQuery(formatUserSummary(match));
        }
      } catch {
        if (!cancelled) {
          setQuery(userId);
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
    if (selectedUser && next !== formatUserSummary(selectedUser)) {
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
    setQuery(formatUserSummary(user));
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
    <AccountCard className="mb-6">
      <div ref={containerRef}>
      <label htmlFor="investigation-user-search" className="mb-1.5 block text-xs font-semibold text-on-surface">
        Người dùng điều tra
      </label>
      <div className="relative">
        <input
          id="investigation-user-search"
          type="search"
          value={query}
          onChange={handleInputChange}
          onFocus={() => {
            if (results.length > 0 || searchStatus === "loading") {
              setIsOpen(true);
            }
          }}
          placeholder="Tìm theo email hoặc UUID..."
          autoComplete="off"
          role="combobox"
          aria-expanded={showDropdown}
          aria-controls={listboxId}
          className="w-full rounded-lg border border-outline-variant bg-white px-3 py-2.5 pr-10 text-base outline-none focus:border-primary focus:ring-1 focus:ring-primary/30"
        />
        {userId ? (
          <button
            type="button"
            onClick={handleClear}
            className="absolute right-2 top-1/2 -translate-y-1/2 rounded-md px-2 py-1 text-xs font-medium text-on-surface-variant hover:bg-surface-container-high"
            aria-label="Xóa người dùng đã chọn"
          >
            Xóa
          </button>
        ) : null}

        {showDropdown ? (
          <ul
            id={listboxId}
            role="listbox"
            className="absolute z-20 mt-1 max-h-60 w-full overflow-auto rounded-lg border border-outline-variant bg-white py-1 shadow-lg"
          >
            {searchStatus === "loading" ? (
              <li className="px-3 py-2 text-sm text-on-surface-variant">Đang tìm...</li>
            ) : null}
            {searchStatus === "error" ? (
              <li className="px-3 py-2 text-sm text-error">{searchError}</li>
            ) : null}
            {searchStatus === "ready" && results.length === 0 ? (
              <li className="px-3 py-2 text-sm text-on-surface-variant">
                Không tìm thấy người dùng phù hợp.
              </li>
            ) : null}
            {results.map((user) => (
              <li key={user.user_id} role="option">
                <button
                  type="button"
                  onClick={() => handleSelectUser(user)}
                  className="w-full px-3 py-2 text-left text-sm hover:bg-surface-container-high"
                >
                  <span className="block font-medium text-on-surface">{formatUserSummary(user)}</span>
                  <span className="mt-0.5 block break-all text-xs text-on-surface-variant">
                    {user.user_id} · {user.status}
                  </span>
                </button>
              </li>
            ))}
          </ul>
        ) : null}
      </div>

      {selectedUser ? (
        <p className="mt-2 break-all text-xs text-on-surface-variant">
          User ID: {selectedUser.user_id}
        </p>
      ) : userId ? (
        <p className="mt-2 break-all text-xs text-on-surface-variant">User ID: {userId}</p>
      ) : (
        <p className="mt-2 text-xs text-on-surface-variant">
          Nhập ít nhất {INVESTIGATION_SEARCH_MIN_LENGTH} ký tự email hoặc dán UUID để tìm người dùng thật từ auth-service.
        </p>
      )}
      </div>
    </AccountCard>
  );
}
