import { Link } from "react-router-dom";

export default function NotFoundPage() {
  return (
    <main className="flex min-h-full flex-col items-center justify-center gap-4 bg-slate-50 p-6">
      <h1 className="text-4xl font-bold text-slate-900">404</h1>
      <p className="text-slate-600">That page does not exist.</p>
      <Link to="/dashboard" className="font-medium text-indigo-600 hover:underline">
        Go to dashboard
      </Link>
    </main>
  );
}