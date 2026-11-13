---
version: 1
slug: "frontend-src-app-tsx"
primary_target: "frontend/src/App.tsx"
related_targets: []
---

## Scope

Dashboard (`frontend/src/App.tsx`), single screen. Mode: Operate. Build path: code-led.

## Surface

Audience: technical recruiter / tech lead from the portfolio, ~30 s. Task: press Generate traffic (400 real requests, 8 concurrent, ~5% intentional 404s) and watch RPS and p99 move. States: live, cold start (Render free tier, 30–60 s wake), offline/error, empty (no traffic yet), generating. Simulated tail latency on /analytics and /report is labeled in the UI.

## Direction contract

THESIS: A live instrument panel for one Spring Boot API where latency is read as a transit line, not a grid of KPI cards. Refuses the hero-metric + card-grid admin template.

OWN-WORLD: Night Transit, same author as the portfolio: night #0b0f16 ground, cobalt #0a1d3d top band, street #111826 secondary layer, rules #233049. Sodium #ffb627 is the only action/emphasis colour (button, p99 station, live dot, focus, selection). Window blue #7ba4ff and silver #d4d7de carry data only (chart line, p50 / p99.5 stations). Big Shoulders caps for headings, Overpass for UI and tabular numerals. Square corners; round only for stations. Flat bands split by 1px rules, no cards.

STORY: The visitor sees requests/sec and a latency line per endpoint, presses Generate traffic, watches RPS climb and the simulated-tail endpoints' p99 stations slide right past the 500 ms tick, and understands the server is measuring itself.

FIRST VIEWPORT: Top bar: title, live status, GitHub/Prometheus links. Band 1, two columns: left, RPS headline + 60 s average over a 2-minute SVG area chart; right, Generate traffic button with its explanation and progress (sent / total / errors). Band 2: endpoint table, each row with method+URI, request count, p50/p99/p99.5, status codes, and the latency line. Band 3: JVM strip (CPU, heap bar, non-heap, uptime, cores).

FORM: Brief-pinned world and first surface. seed: none (pinned) — "Mundo 'Night Transit': fondo navy #0b0f16 / cobalt #0a1d3d, acento único amarillo sodio #ffb627, plata #d4d7de y azul ventana #7ba4ff; display: Big Shoulders en mayúsculas; texto: Overpass; esquinas rectas y sin tarjetas genéricas", layout confirmed by the user from the ASCII preview. Signature move: latency line — log-scale rail 1 ms → 2 s with ticks at the real SLO buckets (50/100/200/500 ms, 1 s) and round stations at p50, p99, p99.5; motion: stations glide to new positions on each 2 s poll (200 ms ease-out).

FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, DESIGN.md, and every shipping raster carrying its provenance
