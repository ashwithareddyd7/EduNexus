import { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import useAuth from "@/context/useAuth";
import { registerRequest } from "@/api/auth";
import { getErrorMessage } from "@/api/client";
import Alert from "@/components/ui/Alert";
import Button from "@/components/ui/Button";
import TextField from "@/components/ui/TextField";

const MIN_PASSWORD_LENGTH = 8;

function validate(form) {
  if (!form.fullName.trim()) return "Please enter your full name.";
  if (form.password.length < MIN_PASSWORD_LENGTH) {
    return `Password must be at least ${MIN_PASSWORD_LENGTH} characters.`;
  }
  if (form.password !== form.confirmPassword) return "Passwords do not match.";
  return "";
}

export default function RegisterPage() {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    fullName: "",
    email: "",
    password: "",
    confirmPassword: "",
  });
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((previous) => ({ ...previous, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    const validationMessage = validate(form);
    if (validationMessage) {
      setError(validationMessage);
      return;
    }

    setError("");
    setSubmitting(true);
    try {
      const email = form.email.trim();
      await registerRequest({
        fullName: form.fullName.trim(),
        email,
        password: form.password,
      });
      navigate("/login", { replace: true, state: { registeredEmail: email } });
    } catch (err) {
      setError(getErrorMessage(err));
      setSubmitting(false);
    }
  };

  return (
    <main className="flex min-h-full items-center justify-center bg-slate-50 p-6">
      <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-lg sm:p-8">
        <h1 className="text-3xl font-bold text-slate-900">EduNexus</h1>
        <p className="mt-1 text-slate-600">Create your student account</p>

        {error && (
          <div className="mt-6">
            <Alert>{error}</Alert>
          </div>
        )}

        <form onSubmit={handleSubmit} className="mt-6 space-y-5">
          <TextField
            id="fullName"
            label="Full name"
            type="text"
            autoComplete="name"
            required
            value={form.fullName}
            onChange={handleChange}
          />
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
            autoComplete="new-password"
            required
            value={form.password}
            onChange={handleChange}
          />
          <TextField
            id="confirmPassword"
            label="Confirm password"
            type="password"
            autoComplete="new-password"
            required
            value={form.confirmPassword}
            onChange={handleChange}
          />
          <Button type="submit" disabled={submitting} className="w-full">
            {submitting ? "Creating account..." : "Create account"}
          </Button>
        </form>

        <p className="mt-6 text-center text-sm text-slate-600">
          Already have an account?{" "}
          <Link to="/login" className="font-medium text-indigo-600 hover:underline">
            Sign in
          </Link>
        </p>
      </div>
    </main>
  );
}