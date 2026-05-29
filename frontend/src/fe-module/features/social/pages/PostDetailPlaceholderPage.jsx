import { Link, useParams } from "react-router-dom";
import { APP_ROUTES } from "../../../shared/constants/routes";

export function PostDetailPlaceholderPage() {
  const { postId } = useParams();

  return (
    <section className="mx-auto max-w-2xl rounded-xl border border-outline-variant bg-surface-container-lowest p-8 text-center shadow-sm">
      <h1 className="text-2xl font-semibold text-on-surface">Chi tiết bài viết</h1>
      <p className="mt-2 text-sm text-on-surface-variant">
        Màn chi tiết bài viết (<code className="text-xs">{postId}</code>) sẽ được triển khai theo spec
        View Post Detail.
      </p>
      <Link to={APP_ROUTES.socialFeed} className="mt-6 inline-block text-sm font-medium text-primary hover:underline">
        ← Quay lại feed
      </Link>
    </section>
  );
}
