import { useCallback, useEffect, useId, useRef, useState } from "react";
import {
  formatAuditAdminSummary,
  lookupAuditAdminById,
  searchAuditAdmins,
} from "../api/auditAdminApi.js";
import { AuditAdminPickerView } from "./AuditAdminPickerView.jsx";

const SEARCH_DEBOUNCE_MS = 350;
const SEARCH_MIN_LENGTH = 2;

export function AuditAdminPicker({ adminId, selectedAdmin, onAdminChange }) {
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
      /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(trimmed) ||
      trimmed.length >= SEARCH_MIN_LENGTH;

    if (!canSearch) {
      setResults([]);
      setSearchStatus("idle");
      setSearchError("");
      return;
    }

    setSearchStatus("loading");
    setSearchError("");

    try {
      const admins = await searchAuditAdmins(trimmed);
      setResults(admins);
      setSearchStatus("ready");
      setIsOpen(true);
    } catch (error) {
      setResults([]);
      setSearchStatus("error");
      setSearchError(error?.message || "Không tìm được admin.");
    }
  }, []);

  useEffect(() => {
    if (!adminId) {
      setQuery("");
      return;
    }

    if (selectedAdmin?.id === adminId) {
      setQuery("");
      return;
    }

    let cancelled = false;

    (async () => {
      try {
        const admin = await lookupAuditAdminById(adminId);
        if (!cancelled && admin) {
          onAdminChange?.({ adminId, admin });
          setQuery("");
        }
      } catch {
        if (!cancelled) setQuery("");
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [adminId, onAdminChange, selectedAdmin?.id]);

  useEffect(() => {
    const handlePointerDown = (event) => {
      if (!containerRef.current?.contains(event.target)) {
        setIsOpen(false);
      }
    };

    document.addEventListener("mousedown", handlePointerDown);
    return () => document.removeEventListener("mousedown", handlePointerDown);
  }, []);

  useEffect(
    () => () => {
      if (debounceRef.current) clearTimeout(debounceRef.current);
    },
    [],
  );

  const handleInputChange = (event) => {
    const next = event.target.value;
    setQuery(next);
    if (selectedAdmin) {
      onAdminChange?.({ adminId: "", admin: null });
    }
    setIsOpen(true);

    if (debounceRef.current) clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      runSearch(next);
    }, SEARCH_DEBOUNCE_MS);
  };

  const handleSelectAdmin = (admin) => {
    onAdminChange?.({ adminId: admin.id, admin });
    setQuery("");
    setResults([]);
    setIsOpen(false);
    setSearchStatus("idle");
    setSearchError("");
  };

  const handleClear = () => {
    onAdminChange?.({ adminId: "", admin: null });
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
    <AuditAdminPickerView
      listboxId={listboxId}
      containerRef={containerRef}
      query={query}
      adminId={adminId}
      selectedAdmin={selectedAdmin}
      searchStatus={searchStatus}
      searchError={searchError}
      showDropdown={showDropdown}
      results={results}
      minSearchLength={SEARCH_MIN_LENGTH}
      onInputChange={handleInputChange}
      onInputFocus={() => {
        if (results.length > 0 || searchStatus === "loading") {
          setIsOpen(true);
        }
      }}
      onSelectAdmin={handleSelectAdmin}
      onClear={handleClear}
      formatAdminSummary={formatAuditAdminSummary}
    />
  );
}
