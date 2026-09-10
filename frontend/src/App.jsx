import { useState } from "react";

function App() {
  const [query, setQuery] = useState("");
  const [field, setField] = useState("service");
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(false);

  const searchLogs = async () => {
    if (!query.trim()) {
      setLogs([]);
      return;
    }

    setLoading(true);

    try {
      const response = await fetch(
        `http://localhost:8080/api/logs/search?field=${field}&query=${encodeURIComponent(query)}`
      );

      if (!response.ok) {
        throw new Error("Search request failed");
      }

      const data = await response.json();
      setLogs(data);

    } catch (error) {
      console.error("Search error:", error);
      setLogs([]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: "30px", fontFamily: "Arial" }}>

      <h1>LogStream</h1>

      <p>
        Distributed Log Analytics & Alerting Platform
      </p>

      <div style={{ marginBottom: "20px" }}>

        <select
          value={field}
          onChange={(e) => setField(e.target.value)}
          style={{ padding: "8px", marginRight: "10px" }}
        >
          <option value="service">Service</option>
          <option value="level">Log Level</option>
          <option value="message">Message</option>
        </select>

        <input
          type="text"
          placeholder="Search logs..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter") {
              searchLogs();
            }
          }}
          style={{ padding: "8px", width: "250px" }}
        />

        <button
          onClick={searchLogs}
          style={{ padding: "8px 15px", marginLeft: "10px" }}
        >
          Search
        </button>

      </div>

      <h2>Log Results</h2>

      {loading && <p>Searching...</p>}

      {!loading && logs.length === 0 && (
        <p>No logs found.</p>
      )}

      {logs.map((log, index) => (
        <div
          key={index}
          style={{
            border: "1px solid #ccc",
            padding: "15px",
            marginBottom: "10px",
            borderRadius: "5px"
          }}
        >
          <p><strong>Timestamp:</strong> {log.timestamp}</p>
          <p><strong>Level:</strong> {log.level}</p>
          <p><strong>Service:</strong> {log.service}</p>
          <p><strong>Message:</strong> {log.message}</p>
          <p><strong>Response Time:</strong> {log.responseTime} ms</p>
        </div>
      ))}

    </div>
  );
}

export default App;