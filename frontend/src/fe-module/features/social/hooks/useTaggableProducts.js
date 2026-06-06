import { useEffect, useState } from "react";
import { fetchTaggableSellerProducts } from "../api/postProductTagApi";

export function useTaggableProducts(searchQuery = "") {
  const [products, setProducts] = useState([]);
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    let cancelled = false;

    async function load() {
      setStatus("loading");
      setErrorMessage("");

      try {
        const items = await fetchTaggableSellerProducts({ q: searchQuery });
        if (cancelled) return;
        setProducts(items);
        setStatus("ready");
      } catch (error) {
        if (cancelled) return;
        setProducts([]);
        setStatus("error");
        setErrorMessage(error?.message || "Không tải được sản phẩm của shop.");
      }
    }

    load();

    return () => {
      cancelled = true;
    };
  }, [searchQuery]);

  return { products, status, errorMessage };
}