// Saves a Blob as a file. A plain link can't send the JWT, so callers fetch a blob first.
export default function saveBlob(blob, filename) {
  const url = URL.createObjectURL(blob);
  const link = window.document.createElement("a");
  link.href = url;
  link.download = filename;
  window.document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}