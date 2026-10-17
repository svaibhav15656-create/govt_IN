# Before You Go (India): Agent Brief

## Kickoff prompt

> Read `AGENT_BRIEF_BEFORE_YOU_GO.md` fully. Work ONLY on the phase I name. Before coding, show a
> short plan (files you will add/change). After coding, run all tests, then report: what changed,
> how to verify, and anything you were unsure about. Never invent government rules, fees,
> document lists, office addresses or URLs: if you cannot verify something from an official
> source, leave it as `NOT_VERIFIED` and tell me. Do not start the next phase until I say so.
> Start with Phase 0.

---

## 1. Product goal

Before You Go is a website that tells a person exactly what to prepare before visiting a government office anywhere in India.

The user:

- Picks a service (for example passport, Aadhaar update, PAN, voter ID, EPF claim, sale-deed registration, income certificate, driving licence).
- Picks their location: state → district → sub-district → village/town, or enters a pincode.
- Answers 3 to 6 situation questions (for example: minor applicant? name change? property type? buyer or seller?).

The site returns one guide page containing:

- **Documents to carry**: originals and photocopies, with "any ONE of these" groups so people know what they can substitute.
- **Official money needed**: fixed fees, or a calculated total (for example stamp duty plus registration fee), with the formula shown.
- **Steps and expected time**: what happens, in what order.
- **Which office to go to, and nearby offices**: the office responsible for the user's location, and nearby alternatives where the service allows any office.
- **Common reasons for rejection** (for example name spelling mismatch), so people avoid repeat trips.
- **The "Before you leave" note** (see section 3), always shown.
- **Sources and last-verified date** next to every rule.

**Target audience**: ordinary citizens in any part of India, including low-bandwidth mobile users and people who are not fluent in English.

---

## 2. Non-negotiable rules

1. **Accuracy over coverage.** Wrong information costs people trips and money. Never ship a rule that has no source and no verification date.
2. **Honest coverage.** Every (service × state) pair has a status: `VERIFIED`, `IN_PROGRESS`, or `NOT_VERIFIED`. For `NOT_VERIFIED`, the site says so and links to that state's official portal. It never shows guessed rules.
3. **Provenance on every rule**: `source_url`, `source_title`, `verified_on`, `verified_by`, `valid_from`, `next_review_due`.
4. **Rules expire.** When `next_review_due` passes, the rule shows a visible "needs recheck" warning and is flagged in the admin dashboard.
5. **Official fees only.** Show only fees published by the government. Never include agent or "facilitation" charges, and never suggest paying anyone unofficially.
6. **No scraping** of government websites or portals. Use official downloadable datasets, official documents entered as rules by a human reviewer, or link-outs to official locators.
7. **No personal data stored.** Nothing the user enters (location, answers, property value) is persisted or logged with identifiers. Calculate in the browser where practical.
8. **Disclaimer on every guide**: informational only, not legal advice, rules change, confirm with the official source before you go.
9. **No fabricated data.** Example values in code, tests and docs must be clearly marked `EXAMPLE ONLY`.
10. **Rules are data, not code.** Adding a state or service must not require changing application logic.
11. **Do not edit applied database migrations.** Add new versions only.
12. **No secrets in git.** `.env` is ignored; `.env.example` has placeholders only.

---

## 3. The "Before you leave" note (required on every guide page)

Every guide page, and the printable/shareable version, must show this note in a clearly visible box near the end of the checklist, and the API response must include it as `preparedness_note`.

**Canonical English text:**

> Before you leave: Carry some extra cash for the unexpected: extra photocopies or passport-size photos, a small difference in fees, a change in travel plans, or an urgent issue on the day. Pay only the official fee, at the official counter or portal, and always take a receipt. Keep your cash and documents safe.

**Rules for this note:**

- It is always shown, for every service and every state, including `NOT_VERIFIED` ones.
- It must never suggest paying anyone beyond official fees, or paying an agent or middleman.
- It does not state a rupee amount by default. A service may add an optional, reviewer-verified `extra_cash_hint` (for example "photocopies and photos usually cost a small amount near the office") with its own source or reviewer note. Do not invent amounts.
- Where verified, also show `payment_modes` for the office/service (cash, online/UPI, demand draft) so people know what is accepted. If unknown, show "Payment modes not verified: ask the office or check the official portal."
- Where verified, show an official helpline or grievance link for the service as part of the urgency guidance.
- Translations live in i18n files like all other text. Draft Hindi and Tamil below must be reviewed by a native speaker before launch.

**Hindi (draft, needs native review):**

> निकलने से पहले: अचानक आने वाली ज़रूरतों के लिए कुछ अतिरिक्त नकद साथ रखें, जैसे अतिरिक्त फोटोकॉपी या पासपोर्ट-साइज़ फोटो, शुल्क में छोटा अंतर, यात्रा में बदलाव, या उस दिन कोई ज़रूरी समस्या। केवल आधिकारिक शुल्क ही आधिकारिक काउंटर या पोर्टल पर जमा करें और रसीद ज़रूर लें। नकद और दस्तावेज़ सुरक्षित रखें।

**Tamil (draft, needs native review):**

> புறப்படுவதற்கு முன்: எதிர்பாராத தேவைகளுக்காக சிறிது கூடுதல் பணத்தை உடன் வைத்திருங்கள்: கூடுதல் ஜெராக்ஸ் அல்லது பாஸ்போர்ட் அளவு புகைப்படங்கள், கட்டணத்தில் சிறிய வேறுபாடு, பயணத் திட்டத்தில் மாற்றம் அல்லது அன்றைய அவசரப் பிரச்சினை. அதிகாரப்பூர்வ கட்டணத்தை மட்டும், அதிகாரப்பூர்வ கவுண்டர் அல்லது இணையதளத்தில் செலுத்துங்கள்; ரசீதைக் கண்டிப்பாகப் பெறுங்கள். பணத்தையும் ஆவணங்களையும் பாதுகாப்பாக வைத்திருங்கள்.

---

## 4. Coverage model (whole India, in layers)

| Layer | What it covers | Rollout |
|-------|---------------|---------|
| **A. Central services** | Rules that are the same everywhere: passport, Aadhaar, PAN, voter ID, EPF | Launch for all of India early |
| **B. State services** | Stamp duty and registration, income/caste certificates, driving licence, ration card, land records | One state at a time, fully verified |
| **C. Office directories** | Which office serves a location | Official directories, state by state; link-outs to official locators where no open data exists |

- "Every corner of India" is achieved for location lookup from day one (every village via LGD).
- Verified rules grow state by state, and a public coverage map shows which (service × state) pairs are verified.
- A state overlay can override a central default (for example fee differences, extra documents).
- Default first five central services (the owner may change this list): passport, Aadhaar update/enrolment, PAN, voter ID, EPF claim.
- Default pilot state: Tamil Nadu. Pilot services: sale-deed registration (with stamp duty and registration fee calculation), income certificate, community certificate, driving licence, ration card.

---

## 5. Architecture

```
Rules repo (YAML files, reviewed via pull requests)     LGD + pincode datasets (official)
              │ validate (JSON Schema + custom checks)            │ import (monthly)
              ▼                                                   ▼
        PostgreSQL (+ PostGIS)  ◄─────────────  Locations: states → districts → sub-districts → villages
              │                                  Offices (coordinates, jurisdiction)
              ▼
   Read-only API  (/api/locations, /api/services, /api/guide, /api/offices, /api/coverage)
              ▼
   Fast, mobile-first web app (i18n, printable, offline-capable PWA)
```

**Principles:**

- **Git is the source of truth for rules.** Each rule file is human-readable YAML, validated in CI, and merged only after a reviewer confirms it against the official source. A deploy step loads validated rules into the database.
- **The request path never calls external services.** No maps API, no government API, no AI call on a user request.
- **Fee calculations use a declarative format only.** Allowed components: `fixed`, `percent_of` (with optional `min`/`max`), `slab_table`, `sum`, `round`. Never use `eval` or arbitrary expressions.
- **Nearest-office search** uses Postgres (PostGIS, or a bounding-box plus distance query if PostGIS is not available). "Open in Maps" is a plain link built from coordinates, so no maps API key is needed.
- **Static-first delivery.** Landing pages per service and per state are pre-rendered for speed, SEO and low bandwidth.

**Suggested stack** (agent may propose changes with reasons):

Java 21 + Spring Boot 3, PostgreSQL 16 with PostGIS, Flyway, a lightweight front end (vanilla JS or a small framework) with JSON i18n files, Docker Compose, Caddy (or Nginx) for TLS, GitHub Actions for CI.

---

## 6. Data model (Flyway migrations)

### Locations (from LGD):

- `states(lgd_code PK, name_en, name_local, kind)`
- `districts(lgd_code PK, state_code FK, name_en, name_local)`
- `sub_districts(lgd_code PK, district_code FK, name_en, name_local)`
- `villages(lgd_code PK, sub_district_code FK, name_en, name_local)`
- `pincodes(pincode, village_code or district_code, source)`; trigram indexes on names for typo-tolerant typeahead

### Rules and content:

- `services(id, slug, name_i18n, layer, jurisdiction_type)` where `jurisdiction_type` is one of `ANY_OFFICE`, `NEAREST`, `BY_ADDRESS`, `BY_PROPERTY_LOCATION`
- `service_questions(service_id, key, type, options_i18n)` for the situation questions
- `document_groups(id, service_id, variant_condition, title_i18n, rule: ALL | ANY_ONE, copies_needed, original_needed)` and `documents(group_id, name_i18n, notes_i18n)`
- `fee_rules(id, service_id, state_code NULL for central, variant_condition, components JSONB, currency)`
- `steps(service_id, state_code NULL, order, text_i18n, typical_duration_note)`
- `rejection_reasons(service_id, state_code NULL, text_i18n)`
- `rule_sources(rule_ref, source_url, source_title, verified_on, verified_by, valid_from, next_review_due)`
- `coverage(service_id, state_code, status ENUM(VERIFIED, IN_PROGRESS, NOT_VERIFIED), updated_on)`

### Offices:

- `offices(id, type, name_i18n, address, state_code, district_code, geom, official_page_url, hours_note, payment_modes, verified_on, verified_by)`
- `office_services(office_id, service_id)`
- `office_jurisdiction(office_id, area_kind, area_code)` (which villages/sub-districts/districts an office serves)

### Operations:

- `issue_reports(id, rule_ref, category, message, created_at)`: no email, name or IP stored
- `ingestion_runs` for dataset imports

---

## 7. Example rule file (EXAMPLE ONLY: not real rates or documents)

```yaml
# rules/central/example-service.yml   (EXAMPLE ONLY - do not ship these values)
service: example-service
layer: A
jurisdiction_type: ANY_OFFICE
questions:
  - key: applicant_is_minor
    type: boolean
document_groups:
  - id: identity
    rule: ANY_ONE
    original_needed: true
    copies_needed: 1
    documents: ["<verified document name>", "<verified document name>"]
    when: always
fees:
  - id: base_fee
    components:
      - type: fixed
        amount: "<from official fee schedule>"
    when: always
steps:
  - "<verified step>"
sources:
  - url: "<official government URL>"
    title: "<official title>"
    verified_on: "YYYY-MM-DD"
    verified_by: "<reviewer id>"
    next_review_due: "YYYY-MM-DD"
extra_cash_hint: null      # optional, reviewer-verified text only
payment_modes: unknown     # cash | online | dd | unknown
helpline_url: null
```

---

## 8. API contract (read-only, rate-limited, no PII)

| Endpoint | Description |
|----------|-------------|
| `GET /api/locations/search?q=&level=` | Typeahead over states, districts, sub-districts, villages and pincodes |
| `GET /api/services` | List all services |
| `GET /api/services/{slug}/questions` | Questions for a service |
| `POST /api/guide` | Body: `{service, location_code, answers}` (not logged, not stored) → guide response |
| `GET /api/offices/nearest?service=&lat=&lng=` or `&location_code=` | Nearest offices |
| `GET /api/coverage` | Status matrix for the coverage map |
| `POST /api/issue-reports` | Anonymous "this looks wrong" reports |

### Example `/api/guide` response shape:

```json
{
  "service": "example-service",
  "coverage_status": "VERIFIED",
  "documents": [{"group": "identity", "rule": "ANY_ONE", "items": ["..."], "originals": true, "copies": 1}],
  "money": {"total": 0, "currency": "INR", "breakdown": [{"label": "...", "amount": 0}], "formula_note": "..."},
  "steps": ["..."],
  "rejection_reasons": ["..."],
  "office": {"mode": "BY_ADDRESS", "primary": {}, "nearby": [], "confirm_before_visit_url": "..."},
  "preparedness_note": "<localized 'Before you leave' text from section 3>",
  "payment_modes": "unknown",
  "helpline_url": null,
  "sources": [{"title": "...", "url": "...", "verified_on": "...", "needs_recheck": false}],
  "disclaimer": "<localized>"
}
```

When `coverage_status` is `NOT_VERIFIED`, return no rules. Return only: the status, the official portal link for that state, the `preparedness_note`, and the disclaimer.

---

## 9. UI requirements

- **Flow**: choose service → choose location → answer questions → guide page.
- **Guide page order**: documents (checklist with tick boxes) → money → steps → office(s) → common rejection reasons → "Before you leave" note → sources and last-verified dates → "Report a problem" button.
- **Print and share**: one-click print layout and a plain-text/WhatsApp-friendly share summary. Both must include the "Before you leave" note.
- **Languages**: English first, Hindi and Tamil next, built so any scheduled language can be added by adding a translation file. A test fails if any key is missing in a shipped language.
- **Mobile-first**, works on slow connections (small pages, no heavy scripts, no third-party trackers). Offline-capable: the last viewed guide can be reopened without network.
- **Accessibility**: WCAG 2.1 AA basics (labels, contrast, keyboard use, screen-reader text, text resize).
- Always show the "last verified" date and the "needs recheck" warning when due.
- **Coverage map page**: states coloured by status per service.

---

## 10. Data sources (verify each before use; record terms in `docs/sources/<name>.md`)

| Need | Source | Notes |
|------|--------|-------|
| States, districts, sub-districts, villages | LGD (Local Government Directory, Ministry of Panchayati Raj) via its download directory and data.gov.in | Monthly updates; includes local-language names; confirm licence and attribution wording |
| Villages with pincodes | LGD "villages with PIN codes" dataset on data.gov.in | Used to map pincode → location |
| Pincode directory | India Post dataset on data.gov.in | Verify freshness and licence |
| Rules, fees, documents | Official government orders, notifications, department portals | Entered by a human reviewer; never scraped |
| Offices | State/department directories, official locators | Link out where no open data exists; geocode offline once and check by hand |
| Geocoding | A geocoder whose terms allow bulk/offline use, or manual coordinates | Check usage terms first; never geocode on user requests |

---

## 11. Phases (complete one at a time; each has acceptance criteria)

### Phase 0: Project setup

Repo, Maven/Gradle wrapper, Docker Compose (app + Postgres/PostGIS), CI, lint, `.gitignore` (`.env` only), `.env.example`, health endpoint. **Accept**: `docker compose up` works; CI passes.

### Phase 1: Location backbone

Import LGD (states, districts, sub-districts, villages, local names) and pincode mapping. Typeahead search with typo tolerance in English and local scripts. **Accept**: all states/UTs present; village count matches the downloaded file; typeahead returns correct results for sample names and pincodes; re-import is idempotent.

### Phase 2: Rules engine and validation

YAML schema, validator (required sources, verification dates, no unknown fee component types, no eval), loader into DB, expiry handling, coverage table, admin dashboard of due and expired rules. **Accept**: invalid rule files fail CI with clear messages; expired rules flip to "needs recheck".

### Phase 3: Guide engine, API, result page, "Before you leave" note

Implement `/api/guide`, fee calculator with golden tests, document-group logic, `NOT_VERIFIED` behaviour, the preparedness note in all outputs, print and share views. **Accept**: golden tests pass for every fee example; the note appears in API, page, print and share output for every status; no personal data in logs or DB.

### Phase 4: Layer A (central services)

Add the five default central services with verified sources. **Accept**: each has sources, verification dates, documents, fees, steps and rejection reasons, reviewed by a human.

### Phase 5: Offices and nearest-office logic

Office model, jurisdiction types, nearest search, "Open in Maps" links, "confirm before visiting" links, payment modes. **Accept**: `BY_ADDRESS` and `BY_PROPERTY_LOCATION` services never show "nearest" as if it were the correct office; tests cover each jurisdiction type.

### Phase 6: Pilot state (Tamil Nadu) fully verified

Add state-specific services and overlays, offices and local-language content. **Accept**: coverage status `VERIFIED` only after reviewer sign-off per service; stamp duty calculator matches official worked examples.

### Phase 7: Languages

Hindi and Tamil translations (native review required), i18n completeness test, local-script place names. **Accept**: guide, coverage map and note fully translated and reviewed.

### Phase 8: Contributor and review workflow

Contribution guide, PR template with required source evidence, reviewer checklist, "Report a problem" intake and triage. **Accept**: a new rule cannot merge without a source and verification date.

### Phase 9: Production deployment

Reverse proxy with TLS, security headers and CSP, rate limiting, backups and restore test, monitoring and alerts (including expired rules and failed imports), privacy policy, terms, cookie notice, pre-rendered SEO pages per service × state. **Accept**: fresh deploy from documented steps; HTTPS only; DB not publicly exposed; Lighthouse performance and accessibility checks pass on mid-range mobile.

### Phase 10: State rollout playbook

A repeatable checklist and template for adding each new state (services to cover, sources to collect, offices, reviewers, translation, go-live criteria). **Accept**: a second state onboarded using only the playbook and rule files, with no application code changes.

---

## 12. Testing standards

- Golden tests for every fee rule using worked examples from official sources.
- Rule validator tests (missing source, expired date, unknown component).
- Location import and typeahead tests (including local-script names).
- Nearest-office and jurisdiction tests.
- i18n completeness test.
- Privacy test: no user input persisted; logs contain no query payloads or full IPs.
- Contract test: the "Before you leave" note is present for every coverage status and every output format.
- Accessibility checks in CI.
- **Run the full suite before every report. Report failures honestly. Never weaken or skip tests to pass.**

---

## 13. Deployment notes

Docker Compose for local and single-server deploys. Caddy or Nginx in front for TLS. Managed or backed-up PostgreSQL with a documented restore. Static assets behind a CDN if traffic grows. Secrets via the host's secret store. A monthly job re-imports LGD, and a daily job flags rules past their review date.

---

## 14. Definition of done

- A user anywhere in India can find their location, and for every `VERIFIED` service sees documents, official fees, steps, the correct office(s), rejection reasons, sources with dates, and the "Before you leave" note.
- Unverified areas say so honestly and link to the official portal, and the note still appears.
- No scraping, no stored personal data, no unsourced rules, no secrets in git.
- Deployed over HTTPS with backups, monitoring and a working review process.

---

## 15. Out of scope

- Legal, tax or financial advice.
- Applying on the user's behalf, submitting forms, or taking payments.
- Agent, middleman or "express service" listings, or anything encouraging unofficial payments.
- Scraping government websites.
- Storing user documents or identity numbers.
