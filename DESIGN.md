---
name: API Performance Monitor
description: A live instrument panel where a Spring Boot API's latency is read as a night transit line.
colors:
  night: "#0b0f16"
  cobalt: "#0a1d3d"
  street: "#111826"
  tower: "#070a10"
  rule: "#233049"
  sodium: "#ffb627"
  window: "#7ba4ff"
  silver: "#d4d7de"
  text: "#e9ebf0"
  lamplit: "#c3cad6"
  muted: "#9aa6ba"
  bad: "#ff7a6b"
typography:
  display:
    fontFamily: "'Big Shoulders Variable', 'Arial Narrow', sans-serif"
    fontSize: "5.5rem"
    fontWeight: 300
    lineHeight: 0.85
    fontFeature: "'tnum'"
  headline:
    fontFamily: "'Big Shoulders Variable', 'Arial Narrow', sans-serif"
    fontSize: "2.5rem"
    fontWeight: 300
    lineHeight: 1
  title:
    fontFamily: "'Big Shoulders Variable', 'Arial Narrow', sans-serif"
    fontSize: "1.75rem"
    fontWeight: 700
    lineHeight: 1
    letterSpacing: "0.04em"
  section:
    fontFamily: "'Big Shoulders Variable', 'Arial Narrow', sans-serif"
    fontSize: "1.25rem"
    fontWeight: 600
    lineHeight: 1
    letterSpacing: "0.04em"
  readout:
    fontFamily: "'Overpass Variable', system-ui, sans-serif"
    fontSize: "1.5rem"
    fontWeight: 400
    lineHeight: 1.5
    fontFeature: "'tnum'"
  body:
    fontFamily: "'Overpass Variable', system-ui, sans-serif"
    fontSize: "1rem"
    fontWeight: 400
    lineHeight: 1.5
    fontFeature: "'tnum'"
  label:
    fontFamily: "'Overpass Variable', system-ui, sans-serif"
    fontSize: "0.7rem"
    fontWeight: 600
    lineHeight: 1.5
    letterSpacing: "0.16em"
rounded:
  none: "0"
  station: "50%"
spacing:
  gutter: "clamp(1rem, 4vw, 3rem)"
  band-y: "2rem"
  band-head: "1.25rem"
  column-gap: "3rem"
  cell: "0.95rem 0.75rem"
components:
  button-primary:
    backgroundColor: "{colors.sodium}"
    textColor: "{colors.tower}"
    rounded: "{rounded.none}"
    padding: "0.95rem 1.4rem 0.8rem"
  button-primary-disabled:
    backgroundColor: "{colors.street}"
    textColor: "{colors.muted}"
    rounded: "{rounded.none}"
  topbar:
    backgroundColor: "{colors.cobalt}"
    textColor: "{colors.text}"
    padding: "1.1rem clamp(1rem, 4vw, 3rem) 1rem"
  nav-link:
    textColor: "{colors.lamplit}"
  nav-link-hover:
    textColor: "{colors.sodium}"
  endpoint-row-hover:
    backgroundColor: "{colors.street}"
  meter:
    backgroundColor: "{colors.street}"
    height: "0.5rem"
    rounded: "{rounded.none}"
  meter-fill:
    backgroundColor: "{colors.sodium}"
  meter-fill-data:
    backgroundColor: "{colors.window}"
  station-p50:
    backgroundColor: "{colors.night}"
    rounded: "{rounded.station}"
    size: "0.8rem"
  station-p99:
    backgroundColor: "{colors.sodium}"
    rounded: "{rounded.station}"
    size: "0.95rem"
  station-p995:
    backgroundColor: "{colors.window}"
    width: "3px"
    height: "1.15rem"
---

# Design System: API Performance Monitor

## Overview

**Creative North Star: "The Night Line"**

The dashboard is a transit map read at night. A dark street ground, a cobalt band across the top like a station canopy, and one sodium lamp that marks whatever matters or can be pressed. Each endpoint is a line on a log-scale rail from 1 ms to 5 s, and its percentiles are stations along it: p50 a hollow silver ring, p99 a lit sodium stop, p99.5 a window-blue stem rising off the rail. The numbers are what the server measures about itself, so the surface stays quiet and lets them move.

It belongs to the same family as the author's portfolio world ("Night Transit": same night/cobalt/sodium/window/silver palette, Big Shoulders caps over Overpass) without copying it. What this build adds is the instrument: a single scrolling page of flat horizontal bands split by 1px rules, tabular numerals everywhere, and a signature latency line instead of KPI cards. Density is medium-high and calm; motion is limited to stations gliding on each 2 s poll, a pulsing live dot, and a skeleton shimmer, all removed under reduced motion.

The confirmed rejection is the admin template: no hero-metric-plus-card-grid, no rounded panels, no decorative charts.

**Key Characteristics:**
- Dark only (`color-scheme: dark`), night ground with a cobalt top band.
- One sodium accent for action and emphasis; window blue and silver for data.
- Big Shoulders uppercase for headings and the big readouts; Overpass with tabular numerals for everything else.
- Flat bands divided by 1px rules; no cards, no container backgrounds.
- Square corners everywhere except the round stations and the status dot.
- The latency line is the signature: log rail, SLO-bucket ticks, three percentile stations.

## Colors

A near-black blue city at night, lit by one warm sodium lamp, with cool window light reserved for data.

### Primary
- **Sodium Lamp** (sodium): the only action and emphasis colour. The Generate traffic button, the p99 station and the p99 column, the live status dot, link hover, nav hover, keyboard focus outline, text selection, the bold call to action in the empty state, the traffic progress fill, and 4xx status codes.

### Secondary
- **Window Light** (window): data, never action. The RPS chart line and its 14% area fill, the p99.5 station, the heap meter fill, and the waiting states (the connecting/waking/reconnecting status dot and the cold-start elapsed-seconds counter).
- **Facade Silver** (silver): the p50 station ring, the p50-to-p99.5 span on the latency line, section headings, and inline code.

### Tertiary
- **Warning Coral** (bad): failure only. 5xx codes, failed-request counts, the offline status dot.

### Neutral
- **Night Ground** (night): page background; also the knock-out fill inside the p50 ring and the 3px gap ring around the p99 station.
- **Cobalt Canopy** (cobalt): the top bar only; also the browser `theme-color`.
- **Street Layer** (street): the one raised surface value. Meter tracks, disabled button, skeletons, endpoint row hover, inline code background.
- **Tower Ink** (tower): text on sodium (button label, selection text).
- **Rail Rule** (rule): every divider, table rule, chart gridline, latency rail, tick mark, and the inset 1px ring on meters and the disabled button.
- **Platform White** (text): primary text and big numerals.
- **Lamplit Grey** (lamplit): secondary copy, nav links, status label, legend, status codes.
- **Muted Steel** (muted): units, table headers, tick labels, method names, axis labels, footer.

### Named Rules
**The One Lamp Rule.** Sodium is the only colour that means "press this" or "look here". If a new element is clickable, focused or the headline number in its row, it is sodium; nothing else competes for that job.

**The Data Light Rule.** Window blue and silver mark measured values and the server's own process state. They never fill a button, link or focus state.

## Typography

**Display Font:** Big Shoulders Variable (with 'Arial Narrow', sans-serif), self-hosted via @fontsource-variable.
**Body Font:** Overpass Variable (with system-ui, sans-serif), self-hosted via @fontsource-variable.

**Character:** Condensed civic signage over a highway-sign text face. Big Shoulders names things in caps; Overpass carries the numbers, set tabular on `:root` so readouts never jitter on a 2 s poll.

### Hierarchy
- **Display** (300, 5.5rem, 0.85; 4rem at ≤720px): the current requests-per-second numeral. Thin, huge, alone.
- **Headline** (300, 2.5rem, 1, uppercase): full-state messages that replace the RPS block (cold start "Waking up the server", offline).
- **Title** (700, 1.75rem, 0.04em, uppercase; 1.5rem at ≤720px): the product name in the top bar.
- **Section** (600, 1.25rem, 0.04em, uppercase, silver): band headings (Requests / sec, Endpoints, JVM).
- **Readout** (Overpass 400, 1.5rem): JVM values. The 60 s average sits at 1.125rem in lamplit.
- **Body** (Overpass 400, 1rem, 1.5): explanatory copy, table cells. Long copy capped at 65–75ch.
- **Label** (Overpass 600, 0.7rem, 0.16em, uppercase, muted): table headers and JVM terms. Nav links and the status label use the same voice at 0.75rem / 0.18em; chart captions at 0.7rem / 0.1em.
- **Scale text** (Overpass, 0.625rem, muted): tick labels and rail ends on the latency line.

### Named Rules
**The Caps Signage Rule.** Every Big Shoulders setting is uppercase. Thin (300) for the large readouts and full-state messages, heavy (600–700) for anything that names a thing (product, section, button).

**The Tabular Rule.** Numerals are tabular everywhere (`font-variant-numeric: tabular-nums` on `:root`). Units sit beside numbers in muted at 0.8em.

## Layout

A single full-width column of horizontal bands, each padded `2rem` vertically and by the fluid gutter (`clamp(1rem, 4vw, 3rem)`) horizontally, separated by a 1px rule. No max-width container: the bands run edge to edge.

- **Top bar:** title left, live status, then nav links; wraps on narrow screens with the title taking the full row at ≤720px.
- **Hero band:** two columns, `minmax(0, 1fr)` and `minmax(16rem, 22rem)`, gap `2rem 3rem`. The right column (traffic) is set off by a 1px left rule and 2rem padding, not a box. At ≤960px it stacks and the rule moves to the top.
- **Band heads:** heading and legend on one baseline, space-between, wrapping.
- **Endpoint table:** full width, rows padded `0.95rem 0.75rem` with flush first and last cells; the latency column takes 34% (min 14rem). At ≤720px each row becomes a 4-column grid with no card treatment: endpoint, latency line and status codes span the row, numbers sit four across with their labels drawn from `data-label`.
- **JVM strip:** a 5-column definition grid (3 at ≤960px, 2 at ≤720px with heap spanning both), each item topped by a 1px rule.
- **Footer:** muted, two paragraphs, space-between, no rule below the last band.

## Elevation & Depth

Flat. Depth comes from tonal layering (night ground, cobalt canopy, street layer for tracks and hover) and 1px rules, never from lifted surfaces. The only shadows are light emitted by sodium, plus inset 1px rings that act as strokes.

### Shadow Vocabulary
- **Lamp glow, station** (`box-shadow: 0 0 0 3px var(--night), 0 0 0.75rem rgb(255 182 39 / 0.45)`): the p99 station; the gap ring switches to `--street` on row hover.
- **Lamp glow, live dot** (`box-shadow: 0 0 0.6rem rgb(255 182 39 / 0.7)`): the status dot while live.
- **Lamp glow, button hover** (`box-shadow: 0 0.4rem 1.5rem rgb(255 182 39 / 0.35)`): the primary button on hover, with a 1px lift.
- **Rule ring** (`box-shadow: inset 0 0 0 1px var(--rule)`): meter tracks and the disabled button. A stroke, not elevation.

### Named Rules
**The Only Light Glows Rule.** A blurred shadow exists only where sodium light is emitted. Neutral surfaces never get drop shadows.

## Shapes

Square by default: `border-radius: 0` on the button, meters, skeletons and every band. Round is reserved for points on the map: the percentile stations (50%) and the status dot. The p99.5 station is the one exception to round-only: a 3px square stem rising 1.15rem above the rail, capped by a 0.5rem round head, so it stays visible when it lands on p99. Lines are 1px rules and 2px rails; the idle rail is dashed (4px on, 4px off).

## Components

### Buttons
Heavy, lit, and alone: there is one button and it carries the demo.
- **Shape:** square (0).
- **Primary:** sodium fill, tower text, Big Shoulders 700 at 1.5rem, 0.05em tracking, uppercase, padding `0.95rem 1.4rem 0.8rem` (bottom trimmed for the caps' optical centre).
- **Hover / Active:** lamp glow and a 1px lift on hover; 1px press and no glow on active. Transitions 150–200ms on the house ease.
- **Disabled:** street fill, muted text, inset rule ring, not-allowed cursor.
- **Focus:** the global 2px sodium outline, 3px offset.

### Navigation
- **Top bar:** cobalt band with a 1px rule below. Nav links are 0.75rem uppercase Overpass at 0.18em tracking in lamplit, no underline, sodium on hover.
- **Inline links:** text colour with a rule-coloured underline offset 0.25em; both turn sodium on hover.

### Status
A 0.6rem round dot plus an uppercase label. Live: sodium with glow, slow 2s pulse. Connecting/waking/reconnecting: window blue, faster 1s pulse. Offline: coral, still. Announced through `role="status"`.

### Meter
A 0.5rem street track with an inset rule ring and a flat fill that eases its width over 200ms. Sodium for user-driven progress (traffic sent), window blue for measured data (heap, 0.3rem thin variant).

### Endpoint Table
Rules only, no zebra or cards. Method in muted 0.7rem bold caps before the URI; a muted note under endpoints with simulated latency. Numbers right-aligned; the p99 column is sodium 600. Status codes inline: 2xx lamplit, 4xx sodium, 5xx coral, counts muted. Row hover (pointer devices only) fills street.

### Latency Line (signature)
A log-scale rail from 1 ms to 5 s: 2px rule-coloured rail, 1px ticks at the SLO buckets (50, 100, 200, 500 ms, 1 s) with 0.625rem labels, end labels "1 ms" and "5 s". A silver 2px span joins p50 to p99.5. Stations: p50 a 0.8rem hollow silver ring (night fill, 2px inset), p99 a 0.95rem sodium disc with night gap ring and glow, p99.5 a window-blue stem with round head. Stations glide over 200ms on the house ease on each poll. With no samples in the window the rail turns dashed and reads "No requests in the last 2 min". The line carries an `aria-label` with all three values; the legend above the table repeats the station shapes.

### RPS Chart
An SVG area chart, 10rem tall, bottom rule, dashed (2 4) rule-coloured gridlines, window-blue 2px line over a 14% window-blue fill. Y labels and the caption in muted small caps.

### JVM Strip
A definition list: label-voice terms over 1.5rem readouts, each item topped by a 1px rule. Heap adds a thin window-blue meter.

### Loading and Full States
Skeleton blocks in street with a 1.6s linear shimmer. Cold start and offline replace the RPS block with a thin Big Shoulders headline and lamplit copy, the elapsed seconds in window blue.

## Do's and Don'ts

### Do:
- **Do** keep sodium (#ffb627) as the single action and emphasis colour: one button, the p99 station and column, focus, selection, live dot.
- **Do** split content into full-width bands with 1px rule dividers and the fluid gutter (`clamp(1rem, 4vw, 3rem)`).
- **Do** set every Big Shoulders heading in uppercase: thin 300 for big readouts, 600–700 for names.
- **Do** keep numerals tabular and units muted at 0.8em.
- **Do** use round shapes only for stations and status dots; everything else stays square.
- **Do** ease movement with `cubic-bezier(0.16, 1, 0.3, 1)` at 150–200ms, and drop all animation under `prefers-reduced-motion`.
- **Do** label simulated latency and make cold start, offline, empty and loading states explicit.

### Don't:
- **Don't** build KPI cards, rounded panels, or a hero-metric-plus-card-grid layout.
- **Don't** use window blue or silver for buttons, links, focus or selection.
- **Don't** add drop shadows to neutral surfaces; only sodium elements glow.
- **Don't** set Big Shoulders in sentence case or use it for body copy.
- **Don't** round the corners of buttons, meters, tables or bands.
