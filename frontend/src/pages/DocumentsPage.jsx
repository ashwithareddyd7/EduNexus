import { useEffect, useState } from "react";
import Spinner from "@/components/ui/Spinner";
import { Link } from "react-router-dom";
import { fetchMyDocuments } from "@/api/documents";
import { getErrorMessage } from "@/api/client";
import UploadDocumentForm from "@/pages/documents/UploadDocumentForm";
import DocumentList from "@/pages/documents/DocumentList";

export default function DocumentsPage() {
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");
  const [needsProfile, setNeedsProfile] = useState(false);
  const [actionError, setActionError] = useState("");
  const [notice, setNotice] = useState("");

  useEffect(() => {
    let cancelled = false;
    fetchMyDocuments()
      .then((data) => {
        if (!cancelled) setDocuments([...data].sort((a, b) => b.id - a.id));
      })
      .catch((err) => {
        if (cancelled) return;
        if (err.response?.status === 404) setNeedsProfile(true);
        else setLoadError(getErrorMessage(err));
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  function handleUploaded(created) {
    setDocuments((current) => [created, ...current]);
    setActionError("");
    setNotice(`"${created.originalFilename}" uploaded.`);
  }

  function handleDeleted(id) {
    setDocuments((current) => current.filter((d) => d.id !== id));
    setNotice("Document deleted.");
  }

  function handleError(message) {
    setActionError(message);
    if (message) setNotice("");
  }

  return (
    <div className="mx-auto max-w-4xl space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-slate-900">My Documents</h1>
        <p className="mt-1 text-sm text-slate-500">
          Keep your certificates and memos here.
        </p>
      </div>

      {loading && <Spinner label="Loading documents..." />}

      {needsProfile && (
        <div className="rounded-xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-800">
          Create your profile before uploading documents.{" "}
          <Link to="/profile" className="font-medium underline">
            Go to My Profile
          </Link>
        </div>
      )}

      {loadError && (
        <p role="alert" className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
          {loadError}
        </p>
      )}

      {!loading && !needsProfile && !loadError && (
        <>
          <UploadDocumentForm onUploaded={handleUploaded} />

          {notice && (
            <p className="rounded-lg bg-green-50 px-3 py-2 text-sm text-green-700">
              {notice}
            </p>
          )}
          {actionError && (
            <p role="alert" className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
              {actionError}
            </p>
          )}

          <DocumentList
            documents={documents}
            onDeleted={handleDeleted}
            onError={handleError}
          />
        </>
      )}
    </div>
  );
}