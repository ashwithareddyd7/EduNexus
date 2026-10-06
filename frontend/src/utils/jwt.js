// Reads the expiry (in milliseconds) from a JWT without verifying it.
// The server still checks the signature on every request; this is only for a friendly logout.
export function getTokenExpiry(token) {
  try {
    const payload = token.split(".")[1];
    const json = atob(payload.replace(/-/g, "+").replace(/_/g, "/"));
    const { exp } = JSON.parse(json);
    return typeof exp === "number" ? exp * 1000 : null;
  } catch {
    return null;
  }
}