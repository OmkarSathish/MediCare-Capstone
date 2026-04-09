import type { HttpMethod } from "./types";

export const METHOD_STYLES: Record<HttpMethod, string> = {
  GET: "bg-emerald-900/60 text-emerald-400",
  POST: "bg-blue-900/60 text-blue-400",
  PUT: "bg-orange-900/60 text-orange-400",
  DELETE: "bg-red-900/60 text-red-400",
};

interface MethodBadgeProps {
  method: HttpMethod;
  small?: boolean;
}

export default function MethodBadge({ method, small }: MethodBadgeProps) {
  return (
    <span
      className={`font-mono font-bold rounded px-1.5 ${small ? "text-[10px] py-0.5" : "text-xs py-1 px-2"} ${METHOD_STYLES[method]}`}
    >
      {method === "DELETE" ? "DEL" : method}
    </span>
  );
}
