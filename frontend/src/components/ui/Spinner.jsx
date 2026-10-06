export default function Spinner({ label = "Loading...", className = "" }) {
  return (
    <div
      role="status"
      className={`flex items-center gap-3 text-sm text-slate-500 ${className}`}
    >
      <span
        className="h-5 w-5 animate-spin rounded-full border-2 border-slate-300 border-t-indigo-600"
        aria-hidden="true"
      />
      <span>{label}</span>
    </div>
  );
}