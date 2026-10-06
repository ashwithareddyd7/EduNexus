export default function PlaceholderPage({ title }) {
  return (
    <div>
      <h1 className="text-2xl font-bold text-slate-900">{title}</h1>
      <div className="mt-6 rounded-2xl border border-dashed border-slate-300 bg-white p-10 text-center text-slate-500">
        This page is built in a later phase.
      </div>
    </div>
  );
}
