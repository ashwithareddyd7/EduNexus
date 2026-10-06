import useAuth from "@/context/useAuth";

export default function DashboardPage() {
  const { user, logout } = useAuth();

  return (
    <main className="flex min-h-full items-center justify-center bg-slate-50 p-6">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-lg">
        <h1 className="text-2xl font-bold text-slate-900">
          Welcome, {user.name}
        </h1>
        <p className="mt-1 text-slate-600">{user.email}</p>
        <span className="mt-4 inline-block rounded-full bg-indigo-100 px-3 py-1 text-sm font-medium text-indigo-700">
          {user.role}
        </span>
        <p className="mt-6 text-sm text-slate-500">
          Role-specific dashboards are built in later phases.
        </p>
        <button
          type="button"
          onClick={logout}
          className="mt-6 w-full rounded-lg border border-slate-300 px-4 py-2.5 font-semibold text-slate-700 transition hover:bg-slate-100"
        >
          Log out
        </button>
      </div>
    </main>
  );
}