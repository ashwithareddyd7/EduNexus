// Simple horizontal bars: items = [{ label, value }]. max defaults to the largest value.
export default function BarChart({ items, max, format = String }) {
  if (items.length === 0) {
    return <p className="text-sm text-slate-500">No data yet.</p>;
  }
  const top = max ?? Math.max(...items.map((i) => i.value), 1);

  return (
    <ul className="space-y-3">
      {items.map((item) => (
        <li key={item.label}>
          <div className="flex justify-between text-sm">
            <span className="truncate text-slate-700">{item.label}</span>
            <span className="ml-3 shrink-0 font-medium text-slate-900">
              {format(item.value)}
            </span>
          </div>
          <div className="mt-1 h-2 rounded-full bg-slate-100">
            <div
              className="h-2 rounded-full bg-indigo-500"
              style={{ width: `${Math.min(100, (item.value / top) * 100)}%` }}
            />
          </div>
        </li>
      ))}
    </ul>
  );
}