import { useEffect, useRef, useState } from 'react'
import {
  API_URL,
  TRAFFIC_CONCURRENCY,
  TRAFFIC_TOTAL,
  fetchSummary,
  generateTraffic,
  type EndpointMetrics,
  type MetricsSummary,
  type TrafficProgress,
} from './api'

const REPO_URL = 'https://github.com/M4NUEL-CERON/api-performance-monitor'
const PORTFOLIO_URL = 'https://manuelceron.vercel.app'
const POLL_MS = 2000
const HISTORY = 60 // samples = 2 minutes
const WAKE_GIVE_UP_S = 120

const NOTES: Record<string, string> = {
  '/api/products/analytics': 'Simulated tail: 10% of requests sleep 400–900 ms',
  '/api/transactions/report': 'Simulated tail: 5% sleep 0.8–1.8 s, ~10% sleep 300–600 ms',
  '/api/metrics/summary': "This dashboard's own polling",
}

type Connection = 'connecting' | 'live' | 'waking' | 'reconnecting' | 'offline'

function usePolling() {
  const [summary, setSummary] = useState<MetricsSummary | null>(null)
  const [history, setHistory] = useState<number[]>([])
  const [failing, setFailing] = useState(false)
  const [startedAt] = useState(() => Date.now())

  useEffect(() => {
    let stopped = false
    let timer: number | undefined
    const poll = async () => {
      try {
        const s = await fetchSummary()
        if (stopped) return
        setSummary(s)
        setFailing(false)
        setHistory((h) => [...h, s.rps.currentRps].slice(-HISTORY))
      } catch {
        if (!stopped) setFailing(true)
      }
      if (!stopped) timer = window.setTimeout(poll, POLL_MS)
    }
    poll()
    return () => {
      stopped = true
      window.clearTimeout(timer)
    }
  }, [])

  return { summary, history, failing, startedAt }
}

// Re-renders every second while `active`, for elapsed-time counters.
function useClock(active: boolean) {
  const [now, setNow] = useState(() => Date.now())
  useEffect(() => {
    if (!active) return
    const id = window.setInterval(() => setNow(Date.now()), 1000)
    return () => window.clearInterval(id)
  }, [active])
  return now
}

export default function App() {
  const { summary, history, failing, startedAt } = usePolling()
  const now = useClock(!summary || failing)
  const waitedS = Math.floor((now - startedAt) / 1000)

  let connection: Connection = 'live'
  if (!summary) connection = waitedS >= WAKE_GIVE_UP_S ? 'offline' : waitedS >= 3 ? 'waking' : 'connecting'
  else if (failing) connection = 'reconnecting'

  return (
    <div className="page">
      <header className="topbar">
        <h1>API Performance Monitor</h1>
        <Status connection={connection} />
        <nav aria-label="Links">
          <a href={REPO_URL}>GitHub</a>
          <a href={`${API_URL}/actuator/prometheus`}>Prometheus</a>
        </nav>
      </header>

      <main>
        <section className="band hero" aria-labelledby="rps-title">
          <RpsPanel summary={summary} history={history} connection={connection} waitedS={waitedS} />
          <TrafficPanel disabled={!summary} />
        </section>

        <section className="band" aria-labelledby="endpoints-title">
          <div className="band-head">
            <h2 id="endpoints-title">Endpoints</h2>
            <Legend />
          </div>
          <EndpointTable endpoints={summary?.endpoints} />
        </section>

        <section className="band" aria-labelledby="jvm-title">
          <h2 id="jvm-title">JVM</h2>
          <JvmStrip system={summary?.system} />
        </section>
      </main>

      <footer className="footer">
        <p>
          Spring Boot 4 · Micrometer timers with histogram percentiles over a rolling 2-minute window. Data is
          synthetic (500 products, 2,000 transactions) and resets when the server restarts.
        </p>
        <p>
          Built by <a href={PORTFOLIO_URL}>Manuel Cerón</a>
        </p>
      </footer>
    </div>
  )
}

function Status({ connection }: { connection: Connection }) {
  const label = {
    connecting: 'Connecting',
    live: `Live · every ${POLL_MS / 1000} s`,
    waking: 'Waking server',
    reconnecting: 'Reconnecting',
    offline: 'Offline',
  }[connection]
  return (
    <p className={`status status-${connection}`} role="status">
      <span className="status-dot" aria-hidden="true" />
      {label}
    </p>
  )
}

function RpsPanel({
  summary,
  history,
  connection,
  waitedS,
}: {
  summary: MetricsSummary | null
  history: number[]
  connection: Connection
  waitedS: number
}) {
  return (
    <div className="rps">
      <h2 id="rps-title">Requests / sec</h2>
      {summary ? (
        <>
          <p className="rps-numbers">
            <span className="rps-current">{summary.rps.currentRps.toFixed(0)}</span>
            <span className="rps-avg">
              <span className="unit">avg 60 s</span> {summary.rps.avgRps60s.toFixed(1)}
            </span>
          </p>
          <RpsChart history={history} />
        </>
      ) : (
        <ColdStart connection={connection} waitedS={waitedS} />
      )}
    </div>
  )
}

function ColdStart({ connection, waitedS }: { connection: Connection; waitedS: number }) {
  if (connection === 'offline') {
    return (
      <div className="coldstart">
        <p className="coldstart-title">The API is not answering.</p>
        <p>
          Tried for {WAKE_GIVE_UP_S} s at <code>{API_URL}</code>. The free instance may be redeploying; reload in a
          minute, or run it locally with <code>docker compose up</code>.
        </p>
      </div>
    )
  }
  return (
    <div className="coldstart">
      <p className="coldstart-title">
        Waking up the server<span className="coldstart-time"> · {waitedS} s</span>
      </p>
      <p>
        The API runs on Render's free tier, which sleeps after 15 minutes without traffic. The first request boots
        the JVM; this usually takes 30–60 seconds.
      </p>
    </div>
  )
}

function RpsChart({ history }: { history: number[] }) {
  const W = 600
  const H = 160
  const max = Math.max(5, ...history)
  const top = niceCeil(max)
  const step = W / (HISTORY - 1)
  const offset = HISTORY - history.length
  const pts = history.map((v, i) => [(offset + i) * step, H - (v / top) * H] as const)
  const line = pts.map(([x, y], i) => `${i ? 'L' : 'M'}${x.toFixed(1)},${y.toFixed(1)}`).join('')
  const area = pts.length ? `${line}L${W},${H}L${pts[0][0].toFixed(1)},${H}Z` : ''

  return (
    <figure className="chart">
      <div className="chart-plot">
        <svg viewBox={`0 0 ${W} ${H}`} preserveAspectRatio="none" role="img" aria-label="Requests per second, last 2 minutes">
          {[0.5, 1].map((f) => (
            <line key={f} x1="0" x2={W} y1={H - f * H} y2={H - f * H} className="chart-grid" vectorEffect="non-scaling-stroke" />
          ))}
          <path d={area} className="chart-area" />
          <path d={line} className="chart-line" vectorEffect="non-scaling-stroke" />
        </svg>
        <span className="chart-y chart-y-top">{top}</span>
        <span className="chart-y chart-y-mid">{top / 2}</span>
      </div>
      <figcaption>
        <span>−2 min</span>
        <span>sampled every {POLL_MS / 1000} s</span>
        <span>now</span>
      </figcaption>
    </figure>
  )
}

function TrafficPanel({ disabled }: { disabled: boolean }) {
  const [progress, setProgress] = useState<TrafficProgress | null>(null)
  const [running, setRunning] = useState(false)
  const [lastRun, setLastRun] = useState<{ seconds: number; failed: number } | null>(null)
  const runningRef = useRef(false)

  const run = async () => {
    if (runningRef.current) return
    runningRef.current = true
    setRunning(true)
    setLastRun(null)
    setProgress({ done: 0, failed: 0 })
    const t0 = performance.now()
    const result = await generateTraffic(setProgress)
    setLastRun({ seconds: (performance.now() - t0) / 1000, failed: result.failed })
    setRunning(false)
    runningRef.current = false
  }

  const pct = progress ? (progress.done / TRAFFIC_TOTAL) * 100 : 0

  return (
    <div className="traffic">
      <button type="button" className="btn-primary" onClick={run} disabled={disabled || running}>
        {running ? 'Sending traffic…' : 'Generate traffic'}
      </button>
      <p className="traffic-copy">
        Sends {TRAFFIC_TOTAL} real requests from your browser, {TRAFFIC_CONCURRENCY} at a time, mostly to the two
        slow endpoints. About 5% hit a missing product on purpose, so a 404 shows up.
      </p>
      <div className="traffic-progress" aria-live="polite">
        <div
          className="meter"
          role="progressbar"
          aria-label="Requests sent"
          aria-valuemin={0}
          aria-valuemax={TRAFFIC_TOTAL}
          aria-valuenow={progress?.done ?? 0}
        >
          <div className="meter-fill" style={{ width: `${pct}%` }} />
        </div>
        <p className="traffic-count">
          {lastRun ? (
            <>
              Done: {TRAFFIC_TOTAL} requests in {lastRun.seconds.toFixed(1)} s
              {lastRun.failed > 0 && <span className="bad"> · {lastRun.failed} failed</span>}. Watch p99 below.
            </>
          ) : progress ? (
            <>
              {progress.done} / {TRAFFIC_TOTAL} sent
              {progress.failed > 0 && <span className="bad"> · {progress.failed} failed</span>}
            </>
          ) : disabled ? (
            'Available once the API is connected.'
          ) : (
            'Idle.'
          )}
        </p>
      </div>
    </div>
  )
}

function Legend() {
  return (
    <ul className="legend" aria-label="Latency line legend">
      <li><span className="station station-p50" aria-hidden="true" />p50</li>
      <li><span className="station station-p99" aria-hidden="true" />p99</li>
      <li><span className="station station-p995" aria-hidden="true" />p99.5</li>
      <li className="legend-scale">log scale, ticks at the SLO buckets</li>
    </ul>
  )
}

function EndpointTable({ endpoints }: { endpoints?: EndpointMetrics[] }) {
  if (!endpoints) return <div className="table-skeleton" aria-hidden="true" />
  const hasTraffic = endpoints.some((e) => e.uri !== '/api/metrics/summary')

  return (
    <>
      <table className="endpoints">
        <thead>
          <tr>
            <th scope="col">Endpoint</th>
            <th scope="col" className="col-line">Latency</th>
            <th scope="col" className="num">Req</th>
            <th scope="col" className="num">p50</th>
            <th scope="col" className="num">p99</th>
            <th scope="col" className="num">p99.5</th>
            <th scope="col">Status</th>
          </tr>
        </thead>
        <tbody>
          {endpoints.map((e) => (
            <tr key={`${e.method} ${e.uri}`}>
              <th scope="row" className="ep">
                <span className="ep-method">{e.method}</span> <span className="ep-uri">{e.uri}</span>
                {NOTES[e.uri] && <span className="ep-note">{NOTES[e.uri]}</span>}
              </th>
              <td className="col-line"><LatencyLine e={e} /></td>
              <td className="num" data-label="Req">{e.totalRequests.toLocaleString('en-US')}</td>
              <td className="num" data-label="p50">{ms(e.p50Ms)}</td>
              <td className="num p99" data-label="p99">{ms(e.p99Ms)}</td>
              <td className="num" data-label="p99.5">{ms(e.p995Ms)}</td>
              <td className="codes" data-label="Status">
                {Object.entries(e.statusCodes).map(([code, n]) => (
                  <span key={code} className={`code code-${code[0]}xx`}>
                    {code}<span className="code-n"> ×{n.toLocaleString('en-US')}</span>
                  </span>
                ))}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {!hasTraffic && (
        <p className="empty">
          Only the dashboard's own polling so far. Press <strong>Generate traffic</strong> and each endpoint gets a
          latency line here: watch the p99 station on the two slow endpoints slide past 500 ms.
        </p>
      )}
    </>
  )
}

const LOG_MAX_MS = 5000 // cold JIT on the free tier pushes the first run past 2 s
const TICKS: [number, string][] = [[50, '50'], [100, '100'], [200, '200'], [500, '500'], [1000, '1 s']]
const pos = (msVal: number) => (Math.log10(Math.min(Math.max(msVal, 1), LOG_MAX_MS)) / Math.log10(LOG_MAX_MS)) * 100

function LatencyLine({ e }: { e: EndpointMetrics }) {
  // Micrometer percentiles decay to 0 once the 2-minute window holds no samples; the count stays cumulative.
  if (!e.p99Ms) {
    return (
      <div className="line line-idle">
        <div className="line-rail" />
        <span className="line-idle-label">No requests in the last 2 min</span>
      </div>
    )
  }
  const a = pos(e.p50Ms)
  const b = pos(e.p995Ms)
  return (
    <div className="line" role="img" aria-label={`p50 ${ms(e.p50Ms)}, p99 ${ms(e.p99Ms)}, p99.5 ${ms(e.p995Ms)}`}>
      <div className="line-rail" />
      {TICKS.map(([v, label]) => (
        <span key={v} className="line-tick" style={{ left: `${pos(v)}%` }}>
          <span className="line-tick-label">{label}</span>
        </span>
      ))}
      <span className="line-end line-end-start">1 ms</span>
      <span className="line-end line-end-stop">5 s</span>
      <div className="line-span" style={{ left: `${a}%`, width: `${Math.max(b - a, 0)}%` }} />
      <span className="station station-p50" style={{ left: `${a}%` }} />
      <span className="station station-p995" style={{ left: `${b}%` }} />
      <span className="station station-p99" style={{ left: `${pos(e.p99Ms)}%` }} />
    </div>
  )
}

function JvmStrip({ system }: { system?: MetricsSummary['system'] }) {
  if (!system) return <div className="jvm-skeleton" aria-hidden="true" />
  const cpu = system.processCpuPercent
  return (
    <dl className="jvm">
      <div>
        <dt>Process CPU</dt>
        <dd>{cpu >= 0 ? `${cpu.toFixed(1)}%` : 'n/a'}</dd>
      </div>
      <div className="jvm-heap">
        <dt>Heap</dt>
        <dd>
          {system.heapUsedMb.toFixed(0)} <span className="unit">/ {system.heapMaxMb.toFixed(0)} MB</span>
          <span className="meter meter-thin" aria-hidden="true">
            <span className="meter-fill meter-fill-data" style={{ width: `${Math.min(system.heapUsedPercent, 100)}%` }} />
          </span>
        </dd>
      </div>
      <div>
        <dt>Non-heap</dt>
        <dd>{system.nonHeapUsedMb.toFixed(0)} <span className="unit">MB</span></dd>
      </div>
      <div>
        <dt>Uptime</dt>
        <dd>{uptime(system.uptimeSeconds)}</dd>
      </div>
      <div>
        <dt>Cores</dt>
        <dd>{system.availableProcessors}</dd>
      </div>
    </dl>
  )
}

function ms(v: number): string {
  if (!v) return '—'
  if (v >= 1000) return `${(v / 1000).toFixed(2)} s`
  if (v >= 10) return `${v.toFixed(0)} ms`
  return `${v.toFixed(1)} ms`
}

function uptime(s: number): string {
  const h = Math.floor(s / 3600)
  const m = Math.floor((s % 3600) / 60)
  if (h) return `${h}h ${m}m`
  if (m) return `${m}m ${s % 60}s`
  return `${s}s`
}

function niceCeil(v: number): number {
  const p = 10 ** Math.floor(Math.log10(v))
  return [1, 2, 5, 10].map((m) => m * p).find((n) => n >= v) ?? v
}
