import { useEffect, useRef, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { MIN_KEYWORD_LENGTH, SEARCH_DEBOUNCE_MS } from "../constants/productSearchConstants";
import { buildCommerceSearchPath } from "../utils/commerceSearchRoutes";
import { normalizeSearchKeyword } from "../utils/normalizeSearchKeyword";

export function CommerceSearchBar({ onInvalidKeyword, className = "" }) {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const urlQ = normalizeSearchKeyword(searchParams.get("q") ?? "");
  const [inputValue, setInputValue] = useState(urlQ);
  const debounceRef = useRef(null);

  useEffect(() => {
    setInputValue(urlQ);
  }, [urlQ]);

  const navigateWithKeyword = (raw) => {
    const normalized = normalizeSearchKeyword(raw);
    if (!normalized) {
      navigate(buildCommerceSearchPath(""));
      return;
    }
    if (normalized.length < MIN_KEYWORD_LENGTH) {
      onInvalidKeyword?.();
      return;
    }
    navigate(buildCommerceSearchPath(normalized));
  };

  const handleChange = (event) => {
    const next = event.target.value;
    setInputValue(next);

    if (debounceRef.current) {
      clearTimeout(debounceRef.current);
    }

    debounceRef.current = setTimeout(() => {
      const normalized = normalizeSearchKeyword(next);
      if (normalized.length >= MIN_KEYWORD_LENGTH) {
        navigate(buildCommerceSearchPath(normalized));
      }
    }, SEARCH_DEBOUNCE_MS);
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    if (debounceRef.current) {
      clearTimeout(debounceRef.current);
    }
    navigateWithKeyword(inputValue);
  };

  useEffect(() => {
    return () => {
      if (debounceRef.current) clearTimeout(debounceRef.current);
    };
  }, []);

  return (
    <form
      onSubmit={handleSubmit}
      className={`relative rounded-lg border border-outline-variant bg-surface shadow-sm ${className}`}
    >
      <span
        className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-outline"
        aria-hidden="true"
      >
        search
      </span>
      <input
        type="search"
        value={inputValue}
        onChange={handleChange}
        placeholder="Tìm sản phẩm, thương hiệu hoặc danh mục..."
        className="w-full rounded-lg bg-transparent py-3 pl-12 pr-28 text-body-md outline-none focus:ring-2 focus:ring-primary-container"
        aria-label="Tìm kiếm sản phẩm"
      />
      <button
        type="submit"
        className="absolute right-2 top-1/2 -translate-y-1/2 rounded-md bg-primary px-4 py-1.5 text-label-md text-on-primary transition-colors hover:bg-[#0050cb]"
      >
        Tìm kiếm
      </button>
    </form>
  );
}
