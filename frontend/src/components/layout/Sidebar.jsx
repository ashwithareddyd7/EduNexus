import { useEffect } from "react";
import { NavLink } from "react-router-dom";
import useAuth from "@/context/useAuth";
import { navItemsForRole } from "@/config/navigation";

// Below 1024px the sidebar is a slide-in drawer; from 1024px up it is always visible.
export default function Sidebar({ open, onClose }) {
  const { user } = useAuth();
  const items = navItemsForRole(user.role);

  // Close the drawer with the Escape key.
  useEffect(() => {
    if (!open) return undefined;
    const onKeyDown = (event) => {
      if (event.key === "Escape") onClose();
    };
    document.addEventListener("keydown", onKeyDown);
    return () => document.removeEventListener("keydown", onKeyDown);
  }, [open, onClose]);

  return (
    <>
      {open && (
        <div
          className="fixed inset-0 z-20 bg-slate-900/40 lg:hidden"
          onClick={onClose}
          aria-hidden="true"
        />
      )}

      <aside
        className={`fixed inset-y-0 left-0 z-30 w-64 overflow-y-auto bg-white shadow-lg transition-transform lg:static lg:shrink-0 lg:translate-x-0 lg:border-r lg:border-slate-200 lg:shadow-none ${
          open ? "translate-x-0" : "-translate-x-full"
        }`}
      >
        <div className="flex h-16 items-center justify-between px-6 text-xl font-bold text-slate-900">
          EduNexus
          <button
            type="button"
            onClick={onClose}
            aria-label="Close menu"
            className="rounded-lg p-2 text-2xl leading-none text-slate-500 hover:bg-slate-100 lg:hidden"
          >
            &times;
          </button>
        </div>

        <nav className="space-y-1 px-3 pb-6">
          {items.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              onClick={onClose}
              className={({ isActive }) =>
                `block rounded-lg px-3 py-2.5 text-sm font-medium transition ${
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