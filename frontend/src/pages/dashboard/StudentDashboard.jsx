import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getErrorMessage } from "@/api/client";
import { getGpa, getMyDocuments, getMyProfile } from "@/api/students";
import Alert from "@/components/ui/Alert";
import StatCard from "@/components/ui/StatCard";

const QUICK_LINKS = [
  { label: "My Profile", to: "/profile", text: "View and update your details" },
  { label: "Academic Records", to: "/academics", text: "Semester-wise marks and grades" },
  { label: "My Documents", to: "/documents", text: "Certificates and memos" },
];

function formatGpa(value) {
  return value === null || value === undefined ? "--" : Number(value).toFixed(2);
}

export default function StudentDashboard() {
  const [state, setState] = useState({ status: "loading" });

  useEffect(() => {
    let cancelled = false;

    async function load() {
      try {
        const profile = await getMyProfile();
        if (!profile) {
          if (!cancelled) setState({ status: "no-profile" });
          return;
        }

        const [gpaResult, docsResult] = await Promise.allSettled([
          getGpa(profile.id),
          getMyDocuments(),
        ]);
        if (cancelled) return;

        setState({
          status: "ready",
          profile,
          gpa: gpaResult.status === "fulfilled" ? gpaResult.value : null,
          documents: docsResult.status === "fulfilled" ? docsResult.value : null,
          partialError:
            gpaResult.status === "rejected" || docsResult.status === "rejected",
        });
      } catch (error) {
        if (!cancelled) {
          setState({ status: "error", message: getErrorMessage(error) });
        }
      }
    }

    load();
    return () => {
      cancelled = true;
    };
  }, []);

  if (state.status === "loading") {
    return <p className="text-slate-500">Loading your dashboard...</p>;
  }

  if (state.status === "error") {
    return <Alert>{state.message}</Alert>;
  }

  if (state.status === "no-profile") {
    return (
      <div className="mx-auto max-w-lg rounded-2xl bg-white p-8 text-center shadow">
        <h1 className="text-2xl font-bold text-slate-900">
          Complete your profile
        </h1>
        <p className="mt-2 text-slate-600">
          Add your student ID, course and semester to see your results and
          documents here.
        </p>
        <Link
          to="/profile"
          className="mt-6 inline-block rounded-lg bg-indigo-600 px-4 py-2.5 font-semibold text-white hover:bg-indigo-700"
        >
          Go to my profile
        </Link>
      </div>
    );
  }

  const { profile, gpa, documents, partialError } = state;
  const semesters = gpa?.semesters ?? [];
  const latest = semesters[semesters.length - 1];

  return (
    <div>
      <h1 className="text-2xl font-bold text-slate-900">
        Welcome, {profile.fullName}
      </h1>
      <p className="mt-1 text-slate-600">
        {profile.courseName} &middot; {profile.departmentName} &middot; Semester{" "}
        {profile.currentSemester} &middot; Year {profile.year}
      </p>

      {partialError && (
        <div className="mt-6">
          <Alert>Some information could not be loaded. Try refreshing the page.</Alert>
        </div>
      )}

      <div className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard
          label="CGPA"
          value={semesters.length ? formatGpa(gpa.cgpa) : "--"}
        />
        <StatCard
          label="Latest SGPA"
          value={latest ? formatGpa(latest.sgpa) : "--"}
          hint={latest ? `Semester ${latest.semester}` : "No results yet"}
        />
        <StatCard label="Semesters completed" value={semesters.length} />
        <StatCard
          label="Documents"
          value={documents ? documents.length : "--"}
          hint="Uploaded"
        />
      </div>

      <h2 className="mt-10 text-lg font-semibold text-slate-900">
        Semester results
      </h2>
      {semesters.length === 0 ? (
        <div className="mt-3 rounded-2xl border border-dashed border-slate-300 bg-white p-8 text-center text-slate-500">
          No results have been entered for you yet.
        </div>
      ) : (
        <ul className="mt-3 divide-y divide-slate-100 rounded-2xl bg-white shadow">
          {semesters.map((item) => (
            <li
              key={item.semester}
              className="flex items-center justify-between px-6 py-4"
            >
                           <span className="font-medium text-slate-700">
                Semester {item.semester}
                <span className="ml-2 text-sm font-normal text-slate-500">
                  {item.credits} credits
                </span>
              </span>
              <span className="font-semibold text-slate-900">
                SGPA {formatGpa(item.sgpa)}
              </span>
            </li>
          ))}
        </ul>
      )}

      <h2 className="mt-10 text-lg font-semibold text-slate-900">Quick links</h2>
      <div className="mt-3 grid gap-4 sm:grid-cols-3">
        {QUICK_LINKS.map((link) => (
          <Link
            key={link.to}
            to={link.to}
            className="rounded-2xl bg-white p-6 shadow transition hover:shadow-md"
          >
            <p className="font-semibold text-slate-900">{link.label}</p>
            <p className="mt-1 text-sm text-slate-500">{link.text}</p>
          </Link>
        ))}
      </div>
    </div>
  );
}
