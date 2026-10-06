import Spinner from "@/components/ui/Spinner";

// A white card with a title that shows its own loading and error state.
export default function Section({ title, subtitle, loading, error, children }) {
  return (
    <section className="rounded-2xl bg-white p-6 shadow">
      <h2 className="text-lg font-semibold text-slate-900">{title}</h2>
      {subtitle && <p className="mt-1 text-sm text-slate-500">{subtitle}</p>}
      <div className="mt-4">
        {error ? (
          <p role="alert" className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
            {error}
          </p>
        ) : loading ? (
          <Spinner />
        ) : (
          children
        )}
      </div>
    </section>
  );
}