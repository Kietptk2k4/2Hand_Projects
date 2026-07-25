export function CommerceFooterTrustSection() {
  const TRUST_ITEMS = [
    {
      icon: "local_shipping",
      title: "GIAO HÀNG TOÀN QUỐC",
      desc: "Tích hợp đơn vị vận chuyển uy tín GHN & GHTK",
      color: "text-blue-600 bg-blue-50 border-blue-100",
    },
    {
      icon: "verified_user",
      title: "ĐẢM BẢO MUA BÁN 100%",
      desc: "Kiểm tra hàng trước khi nhận, hoàn tiền nếu lỗi",
      color: "text-emerald-600 bg-emerald-50 border-emerald-100",
    },
    {
      icon: "published_with_changes",
      title: "ĐỔI TRẢ TRONG 7 NGÀY",
      desc: "Quy trình khiếu nại & đổi trả minh bạch",
      color: "text-purple-600 bg-purple-50 border-purple-100",
    },
    {
      icon: "payments",
      title: "THANH TOÁN AN TOÀN",
      desc: "Hỗ trợ VNPay Sandbox, COD & Ví điện tử",
      color: "text-amber-600 bg-amber-50 border-amber-100",
    },
  ];

  return (
    <section className="mt-12 rounded-2xl border border-outline-variant bg-surface-container-lowest p-6 shadow-sm">
      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
        {TRUST_ITEMS.map((item, idx) => (
          <div key={idx} className="flex items-start gap-4">
            <div
              className={`flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl border ${item.color}`}
            >
              <span className="material-symbols-outlined text-2xl">{item.icon}</span>
            </div>
            <div>
              <h3 className="text-xs font-bold uppercase tracking-wider text-on-surface">
                {item.title}
              </h3>
              <p className="mt-1 text-xs text-on-surface-variant leading-relaxed">{item.desc}</p>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}
