import { Link } from "react-router-dom";
import useAuth from "@/context/useAuth";
import { ROLE_LABELS, navItemsForRole } from "@/config/navigation";

export default function DashboardPage() {
  const { user } = useAuth();
  const shortcuts = navItemsForRole(user.role).filter(
    (item) => item.to !== "/dashboard"
  );

  return (
    <div>
      <h1 className="text-2xl font-bold text-slate-900">
        Welcome, {user.name}
      </h1>
      <p className="mt-1 text-slate-600">
        {ROLE_LABELS[user.role] ?? user.role} · {user.email}
      </p>

      <div className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {shortcuts.map((item) => (
          <Link
            key={item.to}
            to={item.to}
            className="rounded-2xl bg-white p-6 shadow transition hover:shadow-md"
          >
            <p className="font-semibold text-slate-900">{item.label}</p>
            <p className="mt-1 text-sm text-indigo-600">Open →</p>
          </Link>
        ))}
      </div>
    </div>
  );
}
