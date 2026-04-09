import { useState, useRef } from "react";
import type { Endpoint, Field } from "./types";
import { ENDPOINTS, GROUPS } from "./endpoints";
import MethodBadge from "./MethodBadge";

function FieldInput({ field, prefix }: { field: Field; prefix: string }) {
  const id = field.id ?? `${prefix}-${field.key}`;
  const base =
    "w-full bg-zinc-900 border border-zinc-700 rounded-md px-3 py-2 text-sm text-zinc-100 placeholder-zinc-600 focus:outline-none focus:border-violet-500 font-mono";
  if (field.type === "select") {
    return (
      <select id={id} className={base}>
        {field.options?.map((o) => (
          <option key={o} value={o}>
            {o}
          </option>
        ))}
      </select>
    );
  }
  if (field.type === "textarea") {
    return (
      <textarea
        id={id}
        placeholder={field.placeholder}
        className={`${base} min-h-[72px] resize-y`}
      />
    );
  }
  return (
    <input
      id={id}
      type={
        field.type === "password"
          ? "password"
          : field.type === "number"
            ? "number"
            : "text"
      }
      placeholder={field.placeholder}
      className={base}
    />
  );
}

function SectionHeading({ children }: { children: React.ReactNode }) {
  return (
    <p className="text-[10px] uppercase tracking-widest text-zinc-500 mt-4 mb-2 first:mt-0">
      {children}
    </p>
  );
}

function getValue(id: string): string {
  const el = document.getElementById(id) as
    | HTMLInputElement
    | HTMLSelectElement
    | HTMLTextAreaElement
    | null;
  return el?.value ?? "";
}

export default function App() {
  const [activeKey, setActiveKey] = useState<string | null>(null);
  const [token, setToken] = useState("");
  const [responseText, setResponseText] = useState<string>("");
  const [statusInfo, setStatusInfo] = useState<{
    code: number;
    ok: boolean;
  } | null>(null);
  const [loading, setLoading] = useState(false);

  const ep: Endpoint | null = activeKey ? ENDPOINTS[activeKey] : null;

  const tokenHandler = useRef((e: Event) => {
    setToken((e as CustomEvent<string>).detail);
  });
  useState(() => {
    window.addEventListener("token-received", tokenHandler.current);
    return () =>
      window.removeEventListener("token-received", tokenHandler.current);
  });

  function selectEndpoint(key: string) {
    setActiveKey(key);
    setResponseText("");
    setStatusInfo(null);
    const e = ENDPOINTS[key];
    if (e.onOpen) setTimeout(e.onOpen, 50);
  }

  async function sendRequest() {
    if (!ep || !activeKey) return;
    setLoading(true);
    setResponseText("");
    setStatusInfo(null);

    let path = ep.path;
    for (const p of ep.pathParams ?? []) {
      const val = getValue(`path-${p.key}`);
      path = path.replace(`{${p.key}}`, encodeURIComponent(val));
    }

    const qs = (ep.queryParams ?? [])
      .map((p) => {
        const v = getValue(`query-${p.key}`);
        return v
          ? `${encodeURIComponent(p.key)}=${encodeURIComponent(v)}`
          : null;
      })
      .filter(Boolean)
      .join("&");

    const url = path + (qs ? `?${qs}` : "");
    const headers: Record<string, string> = {
      "Content-Type": "application/json",
    };
    if (token) headers["Authorization"] = `Bearer ${token}`;

    let body: string | undefined;
    if (ep.method !== "GET" && ep.method !== "DELETE" && ep.bodyFields) {
      const obj: Record<string, unknown> = {};
      for (const f of ep.bodyFields) {
        const raw = getValue(`body-${f.key}`);
        if (f.key === "testIds") {
          try {
            obj[f.key] = JSON.parse(raw);
          } catch {
            obj[f.key] = raw;
          }
        } else if (f.type === "array") {
          obj[f.key] = raw
            ? raw
                .split(",")
                .map((s: string) => s.trim())
                .filter(Boolean)
            : [];
        } else if (f.type === "number") {
          obj[f.key] = raw !== "" ? Number(raw) : undefined;
        } else {
          obj[f.key] = raw;
        }
      }
      body = JSON.stringify(obj);
    }

    try {
      const res = await fetch(url, { method: ep.method, headers, body });
      const text = await res.text();
      let pretty = text;
      try {
        pretty = JSON.stringify(JSON.parse(text), null, 2);
      } catch {
        /* leave as-is */
      }
      setResponseText(pretty);
      setStatusInfo({ code: res.status, ok: res.ok });
      if (res.ok && ep.onSuccess) {
        try {
          ep.onSuccess(JSON.parse(text));
        } catch {
          /* ignore */
        }
      }
    } catch (err) {
      setResponseText(`Network error: ${(err as Error).message}`);
      setStatusInfo({ code: 0, ok: false });
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="flex h-screen bg-zinc-950 text-zinc-100 overflow-hidden">
      <aside className="w-56 min-w-56 bg-zinc-900 border-r border-zinc-800 flex flex-col overflow-y-auto">
        <div className="px-4 py-3 border-b border-zinc-800">
          <h1 className="text-xs font-bold uppercase tracking-widest text-violet-400">
            Healthcare API
          </h1>
        </div>
        {GROUPS.map((group) => (
          <div key={group.label}>
            <p className="px-4 pt-3 pb-1 text-[10px] uppercase tracking-widest text-zinc-600">
              {group.label}
            </p>
            {group.keys.map((key) => {
              const e = ENDPOINTS[key];
              return (
                <button
                  key={key}
                  onClick={() => selectEndpoint(key)}
                  className={`w-full flex items-center gap-2 px-4 py-1.5 text-xs text-left transition-colors
                    ${activeKey === key ? "bg-zinc-800 text-zinc-100" : "text-zinc-400 hover:bg-zinc-800/50 hover:text-zinc-200"}`}
                >
                  <MethodBadge method={e.method} small />
                  <span className="truncate">{e.title}</span>
                </button>
              );
            })}
          </div>
        ))}
      </aside>

      <div className="flex-1 flex flex-col overflow-hidden">
        <div className="flex items-center gap-3 px-5 py-2 bg-zinc-900 border-b border-zinc-800 flex-shrink-0">
          <span className="text-xs text-zinc-500 whitespace-nowrap">
            Access Token
          </span>
          <input
            value={token}
            onChange={(e) => setToken(e.target.value)}
            placeholder="Paste JWT here, or login to auto-fill"
            className="flex-1 bg-zinc-950 border border-zinc-700 rounded px-2 py-1 text-xs font-mono text-zinc-200 placeholder-zinc-600 focus:outline-none focus:border-violet-500"
          />
          {token ? (
            <span className="text-[11px] bg-emerald-900/50 text-emerald-400 px-2 py-0.5 rounded whitespace-nowrap">
              ✓ Token set
            </span>
          ) : (
            <span className="text-[11px] bg-zinc-800 text-zinc-500 px-2 py-0.5 rounded whitespace-nowrap">
              No token
            </span>
          )}
          {token && (
            <button
              onClick={() => setToken("")}
              className="text-xs text-zinc-500 hover:text-zinc-300"
            >
              Clear
            </button>
          )}
        </div>

        <div className="flex-1 flex overflow-hidden">
          <div className="w-1/2 border-r border-zinc-800 flex flex-col overflow-hidden">
            {ep ? (
              <>
                <div className="px-5 py-3 border-b border-zinc-800 flex-shrink-0">
                  <div className="flex items-center gap-2 mb-1">
                    <MethodBadge method={ep.method} />
                    <h2 className="font-semibold text-sm">{ep.title}</h2>
                  </div>
                  {ep.desc && (
                    <p className="text-xs text-zinc-500">{ep.desc}</p>
                  )}
                  <p className="mt-1 font-mono text-xs text-blue-400 truncate">
                    {ep.path}
                  </p>
                </div>
                <div className="flex-1 overflow-y-auto px-5 py-3">
                  {(ep.pathParams?.length ?? 0) > 0 && (
                    <>
                      <SectionHeading>Path Parameters</SectionHeading>
                      {ep.pathParams!.map((f) => (
                        <div key={f.key} className="mb-3">
                          <label className="block text-[11px] text-zinc-400 mb-1">
                            {f.label}
                          </label>
                          <FieldInput field={f} prefix="path" />
                        </div>
                      ))}
                    </>
                  )}
                  {(ep.queryParams?.length ?? 0) > 0 && (
                    <>
                      <SectionHeading>Query Parameters</SectionHeading>
                      {ep.queryParams!.map((f) => (
                        <div key={f.key} className="mb-3">
                          <label className="block text-[11px] text-zinc-400 mb-1">
                            {f.label}
                          </label>
                          <FieldInput field={f} prefix="query" />
                        </div>
                      ))}
                    </>
                  )}
                  {(ep.bodyFields?.length ?? 0) > 0 && (
                    <>
                      <SectionHeading>Request Body</SectionHeading>
                      {ep.bodyFields!.map((f) => (
                        <div key={f.key} className="mb-3">
                          <label className="block text-[11px] text-zinc-400 mb-1">
                            {f.label}
                          </label>
                          <FieldInput field={f} prefix="body" />
                        </div>
                      ))}
                    </>
                  )}
                  {ep.auth && (
                    <p className="mt-4 text-[11px] text-zinc-600">
                      ��� Requires a valid JWT in the token bar above.
                    </p>
                  )}
                </div>
                <div className="px-5 pb-4 flex-shrink-0">
                  <button
                    onClick={sendRequest}
                    disabled={loading}
                    className="w-full py-2.5 bg-violet-600 hover:bg-violet-500 disabled:bg-zinc-700 disabled:cursor-not-allowed text-white font-semibold text-sm rounded-md transition-colors"
                  >
                    {loading ? "Sending…" : "Send Request"}
                  </button>
                </div>
              </>
            ) : (
              <div className="flex-1 flex items-center justify-center text-zinc-600 text-sm">
                ← Select an endpoint
              </div>
            )}
          </div>

          <div className="flex-1 flex flex-col overflow-hidden">
            <div className="px-5 py-3 border-b border-zinc-800 flex items-center gap-3 flex-shrink-0">
              <h2 className="font-semibold text-sm">Response</h2>
              {statusInfo && (
                <span
                  className={`text-xs font-bold px-2 py-0.5 rounded ${statusInfo.ok ? "bg-emerald-900/50 text-emerald-400" : "bg-red-900/50 text-red-400"}`}
                >
                  {statusInfo.code === 0 ? "Network Error" : statusInfo.code}
                </span>
              )}
            </div>
            <pre
              className={`flex-1 overflow-auto px-5 py-4 text-xs font-mono leading-relaxed whitespace-pre-wrap ${statusInfo?.ok ? "text-cyan-300" : statusInfo ? "text-red-300" : "text-zinc-600"}`}
            >
              {responseText || "// Response will appear here"}
            </pre>
          </div>
        </div>
      </div>
    </div>
  );
}
