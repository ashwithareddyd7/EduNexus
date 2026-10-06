import Button from "@/components/ui/Button";
import useAuth from "@/context/useAuth";
import { ROLE_LABELS } from "@/config/navigation";

export default function Topbar({ onMenuClick }) {
  const { user, logout } = useAuth();

  return (
    <header className="flex h-16 shrink-0 items-center justify-between gap-3 border-b border-slate-200 bg-white px-4 lg:px-6">
      <button
        type="button"
        onClick={onMenuClick}
        aria-label="Open menu"
        className="shrink-0 rounded-lg p-2 text-xl leading-none text-slate-600 hover:bg-slate-100 lg:hidden"
      >
        &#9776;
      </button>
      <div className="hidden lg:block" />

      <div className="flex min-w-0 items-center gap-3 sm:gap-4">
        <div className="min-w-0 text-right">
          <p className="truncate text-sm font-semibold text-slate-900">{user.name}</p>
          <p className="hidden text-xs text-slate-500 sm:block">
            {ROLE_LABELS[user.role] ?? user.role}
          </p>
        </div>
        <Button variant="secondary" size="sm" onClick={logout} className="shrink-0">
          Log out
        </Button>
      </div>
    </header>
  );
}