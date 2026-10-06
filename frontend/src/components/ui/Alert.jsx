const TONES = {
  error: "bg-red-50 text-red-700",
  success: "bg-emerald-50 text-emerald-700",
};

export default function Alert({ tone = "error", children }) {
  return (
    <div role="alert" className={`rounded-lg px-4 py-3 text-sm ${TONES[tone]}`}>
      {children}
    </div>
  );
}
