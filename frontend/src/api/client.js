import axios from "axios";
import { tokenStorage } from "@/utils/tokenStorage";

// Fired when the backend rejects the token. AuthProvider listens for this
// and sends the user back to the login page.
export const UNAUTHORIZED_EVENT = "edunexus:unauthorized";

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000,
  headers: { "Content-Type": "application/json" },
});

// Attach the JWT to every outgoing request if we have one.
apiClient.interceptors.request.use((config) => {
  const token = tokenStorage.get();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// On 401, drop the stale token and tell the app.
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      tokenStorage.clear();
      window.dispatchEvent(new Event(UNAUTHORIZED_EVENT));
    }
    return Promise.reject(error);
  }
);

// Turns any Axios error into a message safe to show the user.
export function getErrorMessage(error) {
  const status = error.response?.status;
  const serverMessage = error.response?.data?.message;

  if (typeof serverMessage === "string" && serverMessage) return serverMessage;
  if (error.code === "ECONNABORTED") {
    return "The request timed out. Please try again.";
  }
  if (error.request && !error.response) {
    return "Cannot reach the server. Check your connection and try again.";
  }

  switch (status) {
    case 401:
      return "Your session has expired. Please sign in again.";
    case 403:
      return "You do not have permission to do that.";
    case 404:
      return "We could not find what you were looking for.";
    case 413:
      return "That file is too large.";
    default:
      break;
  }
  if (status >= 500) {
    return "The server ran into a problem. Please try again in a moment.";
  }
  return error.message || "Something went wrong.";
}

export default apiClient;