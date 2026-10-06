export default function Pagination({
  page,
  totalPages,
  totalElements,
  noun = "item",
  loading,
  onChange,
}) {
  return (
    <div className="mt-4 flex flex-wrap items-center justify-between gap-3 text-sm text-slate-600">
      <span>
        {totalElements} {noun}
        {totalElements === 1 ? "" : "s"}
      </span>
      <div className="flex items-center gap-2">
        <button
          type="button"
          disabled={page === 0 || loading}
          onClick={() => onChange(page - 1)}
          className="rounded-lg border border-slate-300 px-3 py-1.5 hover:bg-slate-50 disabled:opacity-50"
        >
          Previous
        </button>
        <span>
          Page {page + 1} of {Math.max(totalPages, 1)}
        </span>
        <button
          type="button"
          disabled={page + 1 >= totalPages || loading}
          onClick={() => onChange(page + 1)}
          className="rounded-lg border border-slate-300 px-3 py-1.5 hover:bg-slate-50 disabled:opacity-50"
        >
          Next
        </button>
      </div>
    </div>
  );
}