import { NavLink } from "react-router-dom";
import useAuth from "@/context/useAuth";
import { navItemsForRole } from "@/config/navigation";

export default function Sidebar({ open, onClose }) {
  const { user } = useAuth();
  const items = navItemsForRole(user.role);

  return (
    <>
      {open && (
        <div
          className="fixed inset-0 z-20 bg-slate-900/40 md:hidden"
          onClick={onClose}
          aria-hidden="true"
        />
      )}

      <aside
        className={`fixed inset-y-0 left-0 z-30 w-64 bg-white shadow-lg transition-transform md:static md:translate-x-0 md:border-r md:border-slate-200 md:shadow-none ${
          open ? "translate-x-0" : "-translate-x-full"
        }`}
      >
        <div className="flex h-16 items-center px-6 text-xl font-bold text-slate-900">
          EduNexus
        </div>

        <nav className="space-y-1 px-3">
          {items.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              onClick={onClose}
              className={({ isActive }) =>
                `block rounded-lg px-3 py-2 text-sm font-medium transition ${
                  isActive
                    ? "bg-indigo-50 text-indigo-700"
                    : "text-slate-600 hover:bg-slate-100"
                }`
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>
    </>
  );
}
