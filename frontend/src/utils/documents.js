export const MAX_FILE_BYTES = 5 * 1024 * 1024; // matches the backend 5 MB limit
export const ALLOWED_EXTENSIONS = ["pdf", "jpg", "jpeg", "png"];
export const ACCEPT_ATTR = ".pdf,.jpg,.jpeg,.png";

// Values must match the backend DocumentType enum.
export const DOCUMENT_TYPES = [
  { value: "CERTIFICATE", label: "Certificate" },
  { value: "MARKS_MEMO", label: "Marks memo" },
  { value: "BONAFIDE", label: "Bonafide" },
  { value: "ID_DOCUMENT", label: "ID document" },
  { value: "OTHER", label: "Other" },
];

export function documentTypeLabel(value) {
  return DOCUMENT_TYPES.find((t) => t.value === value)?.label ?? value;
}

export function formatFileSize(bytes) {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

// Quick check for a friendly message; the server still validates the real file contents.
export function validateFile(file) {
  if (!file) return "Choose a file to upload.";
  const ext = file.name.split(".").pop()?.toLowerCase();
  if (!ALLOWED_EXTENSIONS.includes(ext)) {
    return "Only PDF, JPG and PNG files are allowed.";
  }
  if (file.size === 0) return "That file is empty.";
  if (file.size > MAX_FILE_BYTES) return "File exceeds the 5 MB limit.";
  return null;
}