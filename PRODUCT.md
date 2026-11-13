# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Stack

Backend: Spring Boot 4.1 (Java 25, Gradle), Micrometer + Actuator, JPA + in-memory H2.
Frontend: React + TypeScript + Vite in `frontend/`, polling the backend every 2 s.
Hosting: backend on Render free tier (Docker), frontend on Vercel.

## Users

- **Primary:** a technical recruiter or tech lead arriving from Manuel Cerón's portfolio (manuelceron.vercel.app). They have about 30 seconds and want to see a real, instrumented Java backend, not a mock. Their job: press "Generate traffic" and watch RPS and p99 move live.
- **Secondary:** a developer who clones the repo and runs it locally with one command.

## Product Purpose

A live window into a Spring Boot API's own performance metrics: requests per second, per-endpoint latency percentiles (p50 / p99 / p99.5), status codes, and JVM CPU / heap / uptime, all read from Micrometer by the API itself. Success is the visitor understanding what it does, and that the numbers are real, within 30 seconds.

## Positioning

The metrics come from the running server measuring itself (Micrometer timers with histogram percentiles, exposed through `/api/metrics/summary` and Prometheus), and the visitor generates the traffic themselves from the browser. Nothing on screen is pre-recorded.

## Operating Context

Viewed in a desktop browser from a portfolio link, sometimes on a phone. The backend sleeps on Render's free tier after 15 minutes of inactivity and takes about 30–60 s to wake, so the first visit may hit a cold start; the UI must say so plainly rather than look broken.

## Capabilities and Constraints

- Endpoints: `/api/products` (CRUD), `/api/products/analytics`, `/api/transactions` (CRUD), `/api/transactions/report`, `/api/metrics/summary`, `/actuator/{health,metrics,prometheus}`.
- `/api/products/analytics` sleeps 400–900 ms on ~10% of requests; `/api/transactions/report` sleeps 800–1800 ms on ~5% and 300–600 ms on ~9.5% more. This tail latency is **simulated** and must be labeled as such in the UI.
- Percentiles are Micrometer client-side histograms over a rolling window; RPS is computed once per second by `RpsTracker` and includes the dashboard's own polling.
- Data is seeded synthetic data (500 products, 2000 transactions) and resets on restart.

## Brand Commitments

Must feel like the same author as the portfolio's "Night Transit" world without copying it: navy #0b0f16 / cobalt #0a1d3d, single sodium-yellow accent #ffb627, silver #d4d7de, window blue #7ba4ff; display Big Shoulders in uppercase, text Overpass; square corners, no generic cards. Copy in English.

## Evidence on Hand

Only the live metrics themselves. No users, benchmarks, load-test results, or production traffic exist; never invent throughput or latency claims.

## Product Principles

1. Real over impressive: show what the server actually measures, label what is simulated.
2. One action carries the demo: "Generate traffic" must be obvious and give visible feedback within seconds.
3. Readable in 30 seconds: the most important numbers (RPS, p99) dominate; detail is there for whoever looks.
4. Never look broken: cold start, errors, and empty states are explicit.
