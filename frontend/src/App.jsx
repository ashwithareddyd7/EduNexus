import { useEffect, useState } from "react";
import { checkBackendHealth } from "@/api/health";
import { getErrorMessage } from "@/api/client";

const STATUS_STYLES = {
  checking: "bg-amber-100 text-amber-800",
  up: "bg-emerald-100 text-emerald-800",
  down: "bg-red-100 text-red-800",
};

export default function App() {
  const [status, setStatus] = useState("checking");
  const [message, setMessage] = useState("Contacting backend...");

  useEffect(() => {
    checkBackendHealth()
      .then(() => {
        setStatus("up");
        setMessage("Backend connected");
      })
      .catch((error) => {
        setStatus("down");
        setMessage(getErrorMessage(error));
      });
  }, []);

  return (
    <main className="flex min-h-full items-center justify-center bg-slate-50 p-6">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-lg">
        <h1 className="text-3xl font-bold text-slate-900">EduNexus</h1>
        <p className="mt-2 text-slate-600">
          Academic records platform. Frontend setup complete.
        </p>
        <div
          className={`mt-6 rounded-lg px-4 py-3 text-sm font-medium ${STATUS_STYLES[status]}`}
        >
          {message}
        </div>
      </div>
    </main>
  );
}