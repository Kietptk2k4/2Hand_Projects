export function SearchResultsHeader({ keyword, totalElements }) {
  if (!keyword) {
    return (
      <header className="border-b border-outline-variant pb-4">
        <h1 className="text-2xl font-semibold text-on-surface md:text-[32px] md:leading-10">
          Tìm kiếm bài viết
        </h1>
        <p className="mt-1 text-base text-on-surface-variant">
          Nhập từ khóa để tìm bài viết
        </p>
      </header>
    );
  }

  return (
    <header className="border-b border-outline-variant pb-4">
      <h1 className="text-2xl font-semibold text-on-surface md:text-[32px] md:leading-10">
        Kết quả cho &quot;{keyword}&quot;
      </h1>
      <p className="mt-1 text-base text-on-surface-variant">{totalElements} kết quả</p>
    </header>
  );
}
