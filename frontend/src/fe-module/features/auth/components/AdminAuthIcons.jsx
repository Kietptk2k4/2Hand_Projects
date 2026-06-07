export function AdminShieldIcon({ className = "h-12 w-12 text-primary" }) {
  return (
    <svg className={className} fill="currentColor" viewBox="0 0 24 24" aria-hidden="true">
      <path d="M12 1 3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm0 2.18 7 3.11v5.71c0 4.54-3.07 8.78-7 9.93-3.93-1.15-7-5.39-7-9.93V6.29l7-3.11zM12 7a4 4 0 00-4 4v1H7v5h10v-5h-1v-1a4 4 0 00-4-4zm0 2a2 2 0 012 2v1h-4v-1a2 2 0 012-2z" />
    </svg>
  );
}

export function LogoutIcon({ className = "h-5 w-5" }) {
  return (
    <svg className={className} fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24" aria-hidden="true">
      <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4M16 17l5-5-5-5M21 12H9" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

export function ArrowBackIcon({ className = "h-4 w-4" }) {
  return (
    <svg className={className} fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24" aria-hidden="true">
      <path d="M19 12H5M12 19l-7-7 7-7" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}
