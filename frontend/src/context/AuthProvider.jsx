import { useCallback, useEffect, useMemo, useState } from "react";
import { AuthContext } from "@/context/authContext";
import { loginRequest } from "@/api/auth";
import { UNAUTHORIZED_EVENT } from "@/api/client";
import { tokenStorage } from "@/utils/tokenStorage";
import { getTokenExpiry } from "@/utils/jwt";

const MAX_TIMEOUT = 2147483647; // the longest delay setTimeout accepts

// Restore the session after a page refresh, unless the token has already expired.
function readStoredSession() {
  const token = tokenStorage.get();
  if (!token) return { user: null, sessionExpired: false };

  const expiry = getTokenExpiry(token);
  if (expiry !== null && expiry <= Date.now()) {
    tokenStorage.clear();
    return { user: null, sessionExpired: true };
  }
  return { user: tokenStorage.getUser(), sessionExpired: false };
}

export default function AuthProvider({ children }) {
  const [session, setSession] = useState(readStoredSession);
  const { user, sessionExpired } = session;

  const login = useCallback(async (credentials) => {
    const { token, user: loggedInUser } = await loginRequest(credentials);
    tokenStorage.set(token);
    tokenStorage.setUser(loggedInUser);
    setSession({ user: loggedInUser, sessionExpired: false });
    return loggedInUser;
  }, []);

  const logout = useCallback(() => {
    tokenStorage.clear();
    setSession({ user: null, sessionExpired: false });
  }, []);

  // Used when the token expires or the server rejects it. The "expired" notice only
  // shows if someone was actually signed in (a wrong password also returns 401).
  const expireSession = useCallback(() => {
    tokenStorage.clear();
    setSession((current) => ({
      user: null,
      sessionExpired: current.user ? true : current.sessionExpired,
    }));
  }, []);

  // The Axios client fires this when the backend returns 401.
  useEffect(() => {
    window.addEventListener(UNAUTHORIZED_EVENT, expireSession);
    return () => window.removeEventListener(UNAUTHORIZED_EVENT, expireSession);
  }, [expireSession]);

  // Log out the moment the token expires, even if the user is idle.
  useEffect(() => {
    if (!user) return undefined;
    const token = tokenStorage.get();
    const expiry = token ? getTokenExpiry(token) : null;
    if (expiry === null) return undefined;

    const check = () => {
      if (Date.now() >= expiry) expireSession();
    };
    const delay = Math.min(Math.max(expiry - Date.now(), 0), MAX_TIMEOUT);
    const timer = setTimeout(check, delay);
    // Timers can pause while a laptop sleeps, so check again when the tab comes back.
    document.addEventListener("visibilitychange", check);
    return () => {
      clearTimeout(timer);
      document.removeEventListener("visibilitychange", check);
    };
  }, [user, expireSession]);

  const value = useMemo(
    () => ({ user, sessionExpired, isAuthenticated: Boolean(user), login, logout }),
    [user, sessionExpired, login, logout]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}