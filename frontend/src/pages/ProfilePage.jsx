import { useEffect, useState } from "react";
import Spinner from "@/components/ui/Spinner";
import { getMyProfile } from "@/api/students";
import { getErrorMessage } from "@/api/client";
import Alert from "@/components/ui/Alert";
import Button from "@/components/ui/Button";
import CreateProfileForm from "@/pages/profile/CreateProfileForm";
import EditProfileForm from "@/pages/profile/EditProfileForm";

function Detail({ label, value }) {
  return (
    <div className="py-3 sm:grid sm:grid-cols-3 sm:gap-4">
      <dt className="text-sm font-medium text-slate-500">{label}</dt>
      <dd className="mt-1 text-slate-900 sm:col-span-2 sm:mt-0">{value}</dd>
    </div>
  );
}

export default function ProfilePage() {
  const [state, setState] = useState({ status: "loading" });
  const [editing, setEditing] = useState(false);
  const [notice, setNotice] = useState("");

  useEffect(() => {
    let cancelled = false;
    getMyProfile()
      .then((profile) => {
        if (cancelled) return;
        setState(
          profile ? { status: "ready", profile } : { status: "no-profile" }
        );
      })
      .catch((error) => {
        if (!cancelled) {
          setState({ status: "error", message: getErrorMessage(error) });
        }
      });
    return () => {
      cancelled = true;
    };
  }, []);

  // Re-read the profile from the server after a create or update.
  async function refresh(message) {
    try {
      const profile = await getMyProfile();
      setState({ status: "ready", profile });
      setEditing(false);
      setNotice(message);
    } catch (error) {
      setState({ status: "error", message: getErrorMessage(error) });
    }
  }

  if (state.status === "loading") {
    return <Spinner label="Loading your profile..." />;
  }

  if (state.status === "error") {
    return <Alert>{state.message}</Alert>;
  }

  if (state.status === "no-profile") {
    return <CreateProfileForm onCreated={() => refresh("Profile created.")} />;
  }

  const { profile } = state;

  return (
    <div className="max-w-2xl">
      <h1 className="text-2xl font-bold text-slate-900">My Profile</h1>

      {notice && (
        <div className="mt-6">
          <Alert tone="success">{notice}</Alert>
        </div>
      )}

      <div className="mt-6 rounded-2xl bg-white p-6 shadow">
        {editing ? (
          <EditProfileForm
            profile={profile}
            onSaved={() => refresh("Profile updated.")}
            onCancel={() => setEditing(false)}
          />
        ) : (
          <>
            <dl className="divide-y divide-slate-100">
              <Detail label="Student ID" value={profile.studentId} />
              <Detail label="Full name" value={profile.fullName} />
              <Detail label="Email" value={profile.email} />
              <Detail label="Phone" value={profile.phone || "Not added"} />
              <Detail label="Course" value={profile.courseName} />
              <Detail label="Department" value={profile.departmentName} />
              <Detail label="Current semester" value={profile.currentSemester} />
              <Detail label="Year" value={profile.year} />
            </dl>
            <div className="mt-6">
              <Button
                onClick={() => {
                  setNotice("");
                  setEditing(true);
                }}
              >
                Edit name and phone
              </Button>
            </div>
            <p className="mt-4 text-xs text-slate-500">
              Student ID, course and semester cannot be changed from this page.
            </p>
          </>
        )}
      </div>
    </div>
  );
}
