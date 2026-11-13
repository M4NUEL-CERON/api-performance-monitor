export const API_URL = (import.meta.env.VITE_API_URL ?? 'http://localhost:8080').replace(/\/$/, '')

// Mirrors com.example.apiperformancemonitor.model.MetricsSummary
export interface EndpointMetrics {
  method: string
  uri: string
  totalRequests: number
  meanLatencyMs: number
  maxLatencyMs: number
  p50Ms: number
  p99Ms: number
  p995Ms: number
  statusCodes: Record<string, number>
}

export interface MetricsSummary {
  timestamp: string
  rps: { currentRps: number; avgRps60s: number }
  system: {
    systemCpuPercent: number
    processCpuPercent: number
    heapUsedMb: number
    heapMaxMb: number
    heapUsedPercent: number
    nonHeapUsedMb: number
    availableProcessors: number
    uptimeSeconds: number
  }
  endpoints: EndpointMetrics[]
}

export async function fetchSummary(): Promise<MetricsSummary> {
  // Long timeout: a sleeping Render instance holds the request while it boots.
  const res = await fetch(`${API_URL}/api/metrics/summary`, { signal: AbortSignal.timeout(90_000) })
  if (!res.ok) throw new Error(`HTTP ${res.status}`)
  return res.json()
}

export const TRAFFIC_TOTAL = 400
export const TRAFFIC_CONCURRENCY = 8

const rand = (n: number) => 1 + Math.floor(Math.random() * n)

// Weighted mix: the two slow endpoints dominate so p99 moves; ~5% are intentional 404s.
const MIX: [weight: number, path: () => string][] = [
  [30, () => '/api/products/analytics'],
  [30, () => '/api/transactions/report'],
  [15, () => `/api/products/${rand(500)}`],
  [15, () => `/api/transactions/${rand(2000)}`],
  [5, () => '/api/products/999999'],
  [5, () => '/api/products'],
]
const MIX_TOTAL = MIX.reduce((sum, [w]) => sum + w, 0)

function pickPath(): string {
  let r = Math.random() * MIX_TOTAL
  for (const [w, path] of MIX) {
    if ((r -= w) < 0) return path()
  }
  return MIX[0][1]()
}

export interface TrafficProgress {
  done: number
  failed: number
}

/** Fires TRAFFIC_TOTAL real GET requests, TRAFFIC_CONCURRENCY at a time. A 404 counts as done, not failed. */
export async function generateTraffic(onProgress: (p: TrafficProgress) => void): Promise<TrafficProgress> {
  let started = 0
  const p: TrafficProgress = { done: 0, failed: 0 }
  const worker = async () => {
    while (started < TRAFFIC_TOTAL) {
      started++
      try {
        const res = await fetch(API_URL + pickPath())
        await res.arrayBuffer()
        if (res.status >= 500) p.failed++
      } catch {
        p.failed++
      }
      p.done++
      onProgress({ ...p })
    }
  }
  await Promise.all(Array.from({ length: TRAFFIC_CONCURRENCY }, worker))
  return p
}
