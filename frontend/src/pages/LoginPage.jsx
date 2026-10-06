import { useState } from "react";
import { Link, Navigate, useLocation } from "react-router-dom";
import useAuth from "@/context/useAuth";
import { getErrorMessage } from "@/api/client";
import Alert from "@/components/ui/Alert";
import Button from "@/components/ui/Button";
import TextField from "@/components/ui/TextField";

export default function LoginPage() {
  const { isAuthenticated, login, sessionExpired } = useAuth();
  const location = useLocation();

  // Set by the register page after a successful sign-up.
  const registeredEmail = location.state?.registeredEmail;

  const [form, setForm] = useState({
    email: registeredEmail ?? "",
    password: "",
  });
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const redirectTo = location.state?.from?.pathname ?? "/dashboard";

  if (isAuthenticated) {
    return <Navigate to={redirectTo} replace />;
  }

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((previous) => ({ ...previous, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      await login({ email: form.email.trim(), password: form.password });
    } catch (err) {
      setError(getErrorMessage(err));
      setSubmitting(false);
    }
  };

  return (
    <main className="flex min-h-full items-center justify-center bg-slate-50 p-6">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-lg">
        <h1 className="text-3xl font-bold text-slate-900">EduNexus</h1>
        <p className="mt-1 text-slate-600">Sign in to your account</p>

        {sessionExpired && !error && (
          <div className="mt-6">
            <Alert>Your session has expired. Please sign in again.</Alert>
          </div>
        )}

        {registeredEmail && !error && !sessionExpired && (
          <div className="mt-6">
            <Alert tone="success">
              Account created. Please sign in.
            </Alert>
          </div>
        )}

        {error && (
          <div className="mt-6">
            <Alert>{error}</Alert>
          </div>
        )}

        <form onSubmit={handleSubmit} className="mt-6 space-y-5">
          <TextField
            id="email"
            label="Email"
            type="email"
            autoComplete="username"
            required
            value={form.email}
            onChange={handleChange}
          />
          <TextField
            id="password"
            label="Password"
            type="password"
            autoComplete="current-password"
            required
            value={form.password}
            onChange={handleChange}
          />
          <Button type="submit" disabled={submitting} className="w-full">
            {submitting ? "Signing in..." : "Sign in"}
          </Button>
        </form>

        <p className="mt-6 text-center text-sm text-slate-600">
          New student?{" "}
          <Link to="/register" className="font-medium text-indigo-600 hover:underline">
            Create an account
          </Link>
        </p>
      </div>
    </main>
  );
}