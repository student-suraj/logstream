import { useMemo, useState } from "react";
import "./App.css";

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || "http://localhost:8080").replace(/\/+$/, "");

const fields = [
  { value: "service", label: "Service name", hint: "e.g. payment-service" },
  { value: "level", label: "Log level", hint: "e.g. ERROR or INFO" },
  { value: "message", label: "Message text", hint: "e.g. payment processed" },
];

function App() {
  const [query, setQuery] = useState("");
  const [field, setField] = useState("service");
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [hasSearched, setHasSearched] = useState(false);

  const activeField = fields.find((item) => item.value === field) || fields[0];
  const errorCount = useMemo(
    () => logs.filter((log) => String(log.level || "").toUpperCase() === "ERROR").length,
    [logs]
  );
  const warningCount = useMemo(
    () => logs.filter((log) => String(log.level || "").toUpperCase() === "WARN" || String(log.level || "").toUpperCase() === "WARNING").length,
    [logs]
  );

  async function searchLogs(event) {
    event?.preventDefault();
    const cleanQuery = query.trim();
    if (!cleanQuery) {
      setError("Enter a search value to find matching logs.");
      setLogs([]);
      setHasSearched(false);
      return;
    }

    setLoading(true);
    setError("");
    setHasSearched(true);

    try {
      const params = new URLSearchParams({ field, query: cleanQuery });
      const response = await fetch(`${API_BASE_URL}/api/logs/search?${params.toString()}`);
      const data = await response.json().catch(() => null);

      if (!response.ok) {
        throw new Error(data?.message || data?.error || `Search failed (HTTP ${response.status}).`);
      }
      if (!Array.isArray(data)) {
        throw new Error("The server returned an unexpected response.");
      }
      setLogs(data);
    } catch (searchError) {
      setLogs([]);
      setError(
        searchError instanceof TypeError
          ? "Could not reach the backend. Start the Spring Boot app on port 8080 and try again."
          : searchError.message || "Something went wrong while searching."
      );
    } finally {
      setLoading(false);
    }
  }

  function clearSearch() {
    setQuery("");
    setLogs([]);
    setError("");
    setHasSearched(false);
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <a className="brand" href="/" aria-label="LogStream home">
          <span className="brand-mark" aria-hidden="true"><span /><span /><span /></span>
          <span>Log<span className="brand-accent">Stream</span></span>
        </a>
        <div className="topbar-meta">
          <span className="status-dot" />
          <span>Log analytics workspace</span>
          <span className="topbar-divider" />
          <span className="environment-label">LOCAL ENVIRONMENT</span>
        </div>
      </header>

      <main className="main-content">
        <section className="page-intro">
          <div className="eyebrow"><span className="eyebrow-line" /> OBSERVABILITY / LOG EXPLORER</div>
          <h1>Find the signal<br /><span>inside your logs.</span></h1>
          <p className="intro-copy">
            Search structured application events in one place. Explore service activity,
            inspect errors, and investigate what happened.
          </p>
        </section>

        <section className="stats-grid" aria-label="Search overview">
          <div className="stat-card">
            <div className="stat-label"><span className="stat-icon icon-blue">⌕</span> MATCHING EVENTS</div>
            <div className="stat-value">{hasSearched && !loading ? logs.length : "—"}</div>
            <div className="stat-foot">{hasSearched ? "Results in current search" : "Run a search to see results"}</div>
          </div>
          <div className="stat-card">
            <div className="stat-label"><span className="stat-icon icon-red">!</span> ERRORS FOUND</div>
            <div className="stat-value">{hasSearched && !loading ? errorCount : "—"}</div>
            <div className="stat-foot">ERROR-level matches</div>
          </div>
          <div className="stat-card">
            <div className="stat-label"><span className="stat-icon icon-amber">△</span> WARNINGS FOUND</div>
            <div className="stat-value">{hasSearched && !loading ? warningCount : "—"}</div>
            <div className="stat-foot">WARN-level matches</div>
          </div>
        </section>

        <section className="workspace-card">
          <div className="section-heading">
            <div>
              <div className="section-kicker">QUERY BUILDER</div>
              <h2>Search log events</h2>
              <p>Choose a field and enter a value to look for matching records.</p>
            </div>
            <span className="api-chip"><span className="api-chip-dot" /> REST API</span>
          </div>

          <form className="search-form" onSubmit={searchLogs}>
            <label className="form-field">
              <span>SEARCH FIELD</span>
              <select value={field} onChange={(event) => setField(event.target.value)}>
                {fields.map((item) => <option key={item.value} value={item.value}>{item.label}</option>)}
              </select>
            </label>
            <label className="form-field query-field">
              <span>SEARCH VALUE</span>
              <div className="input-wrap">
                <span className="input-search-icon" aria-hidden="true">⌕</span>
                <input
                  value={query}
                  onChange={(event) => setQuery(event.target.value)}
                  placeholder={activeField.hint}
                  aria-label="Search value"
                />
                {query && <button type="button" className="input-clear" onClick={() => setQuery("")} aria-label="Clear search value">×</button>}
              </div>
            </label>
            <button className="search-button" type="submit" disabled={loading}>
              {loading ? <><span className="spinner" /> Searching</> : <><span aria-hidden="true">⌕</span> Search logs</>}
            </button>
          </form>
          <div className="query-helper">
            <span>TIP</span>
            <p>For an exact service or level match, try <button type="button" onClick={() => { setField("service"); setQuery("payment-service"); }}>payment-service</button> or <button type="button" onClick={() => { setField("level"); setQuery("ERROR"); }}>ERROR</button>.</p>
          </div>
        </section>

        <section className="results-section">
          <div className="results-heading">
            <div>
              <div className="section-kicker">SEARCH OUTPUT</div>
              <h2>Log entries <span className="result-count">{hasSearched && !loading ? logs.length : ""}</span></h2>
            </div>
            {hasSearched && <button type="button" className="text-button" onClick={clearSearch}>Clear search</button>}
          </div>

          {loading ? (
            <div className="state-card"><span className="spinner spinner-dark" /><div><strong>Searching indexed logs</strong><p>Contacting the LogStream API…</p></div></div>
          ) : error ? (
            <div className="state-card state-error"><span className="state-symbol">!</span><div><strong>Search could not be completed</strong><p>{error}</p></div></div>
          ) : !hasSearched ? (
            <div className="empty-state">
              <div className="empty-graphic"><span className="empty-ring ring-one" /><span className="empty-ring ring-two" /><span className="empty-search">⌕</span></div>
              <h3>Your next investigation starts here</h3>
              <p>Search by service, log level, or message to inspect events stored in the Lucene index.</p>
              <span className="empty-code">GET /api/logs/search</span>
            </div>
          ) : logs.length === 0 ? (
            <div className="empty-state compact-empty">
              <div className="empty-search small-search">⌕</div>
              <h3>No matching logs found</h3>
              <p>Try a different value or send a sample log to the gRPC ingestion service on port 9090.</p>
            </div>
          ) : (
            <div className="logs-table-wrap">
              <table className="logs-table">
                <thead><tr><th>SEVERITY</th><th>TIMESTAMP</th><th>SERVICE</th><th>MESSAGE</th><th className="response-col">RESPONSE</th></tr></thead>
                <tbody>
                  {logs.map((log, index) => {
                    const level = String(log.level || "UNKNOWN").toUpperCase();
                    const levelClass = level === "ERROR" ? "level-error" : level === "WARN" || level === "WARNING" ? "level-warning" : level === "INFO" ? "level-info" : "level-neutral";
                    return (
                      <tr key={`${log.timestamp}-${log.service}-${index}`}>
                        <td><span className={`level-pill ${levelClass}`}><span />{level}</span></td>
                        <td className="timestamp-cell">{log.timestamp || "—"}</td>
                        <td><span className="service-name">{log.service || "—"}</span></td>
                        <td className="message-cell">{log.message || "—"}</td>
                        <td className="response-cell">{log.responseTime ? <>{log.responseTime}<small> ms</small></> : log.responseTime === "0" ? <>0<small> ms</small></> : "—"}</td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
              <div className="table-footer"><span><span className="status-dot" /> Query completed</span><span>Showing up to 20 matching events</span></div>
            </div>
          )}
        </section>
        <footer className="page-footer">
          <span>LOGSTREAM <span className="footer-separator">/</span> DISTRIBUTED LOG ANALYTICS</span>
          <span>Built with Spring Boot · gRPC · Apache Lucene · React</span>
        </footer>
      </main>
    </div>
  );
}

export default App;
