import { Link, useParams } from "react-router-dom";
import { APP_ROUTES } from "../../../shared/constants/routes";

export function CommerceProductPlaceholderPage() {
  const { productId } = useParams();

  return (
    <section className="mx-auto max-w-lg rounded-2xl border border-outline-variant bg-surface-container-lowest p-8 text-center">
      <span className="material-symbols-outlined mb-3 text-5xl text-outline" aria-hidden="true">
        construction
      </span>
      <h1 className="text-headline-md font-semibold text-on-surface">Chi tiết sản phẩm</h1>
      <p className="mt-2 text-sm text-on-surface-variant">
        Trang chi tiết cho sản phẩm <span className="font-mono text-xs">{productId}</span> đang được
        phát triển.
      </p>
      <Link
        to={APP_ROUTES.commerceHome}
        className="mt-6 inline-block rounded-lg bg-primary px-4 py-2 text-sm font-medium text-on-primary hover:bg-[#0050cb]"
      >
        Quay lại Commerce
      </Link>
    </section>
  );
}
