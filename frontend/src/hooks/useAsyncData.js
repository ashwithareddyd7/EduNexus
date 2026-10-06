import { useEffect, useState } from "react";
import { getErrorMessage } from "@/api/client";

// Runs fetcher() whenever deps change and tracks { data, loading, error }.
export default function useAsyncData(fetcher, deps) {
  const [state, setState] = useState({ data: null, loading: true, error: "" });

  useEffect(() => {
    let cancelled = false;
    setState((s) => ({ ...s, loading: true, error: "" }));
    fetcher()
      .then((data) => {
        if (!cancelled) setState({ data, loading: false, error: "" });
      })
      .catch((err) => {
        if (!cancelled) {
          setState({ data: null, loading: false, error: getErrorMessage(err) });
        }
      });
    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);

  return state;
}