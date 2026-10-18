# LGD Data Import Guide

## Overview

The Before You Go location backbone is powered by data from the
**Local Government Directory (LGD)**, published by the Ministry of Panchayati Raj.

The import is **idempotent**: you can run it repeatedly without duplicating data.
Each run is tracked in the `ingestion_runs` table.

## Step 1: Download the Data

Download CSV files from [data.gov.in](https://data.gov.in/) or the
[LGD download directory](https://lgdirectory.gov.in/).

Search for these datasets:
- **State list** (states and union territories)
- **District list** (with state codes)
- **Sub-district list** (with district codes)
- **Village list** (with sub-district codes)
- **Villages with PIN codes** (pincode mapping)

## Step 2: Prepare CSV Files

Rename the downloaded files and place them in `data/lgd/`:

```
data/lgd/
├── states.csv
├── districts.csv
├── sub_districts.csv
├── villages.csv
└── pincodes.csv
```

### Expected Column Headers

| File | Required Columns |
|------|-----------------|
| `states.csv` | `State Code`, `State Name (In English)`, `State Name (In Local language)`, `State or UT` |
| `districts.csv` | `State Code`, `District Code`, `District Name (In English)`, `District Name (In Local language)` |
| `sub_districts.csv` | `District Code`, `Sub-district Code`, `Sub-district Name (In English)`, `Sub-district Name (In Local language)` |
| `villages.csv` | `Sub-district Code`, `Village Code`, `Village Name (In English)`, `Village Name (In Local language)` |
| `pincodes.csv` | `Village Code`, `Pincode` |

> **Note**: Column headers must match exactly (case-sensitive).
> If your download has different headers, rename them to match.

## Step 3: Run the Import

Import must be run **in order** (states first, then districts, etc.):

```bash
# Via the import API (when the app is running):
curl -X POST http://localhost:8080/api/admin/import?dataDir=data/lgd

# Or via the full import command (imports all in order):
# This will be available as a CLI command in a future update.
```

## Step 4: Verify

```bash
curl http://localhost:8080/api/locations/counts
# Expected: {"states":36,"districts":~780,"subDistricts":~7000,"villages":~650000,"pincodes":~...}
```

Check that:
- **States**: 36 (28 states + 8 union territories as of 2024)
- **Villages**: Count matches the downloaded file's row count (minus header)
- **Typeahead**: Test with sample queries

```bash
curl "http://localhost:8080/api/locations/search?q=Chennai"
curl "http://localhost:8080/api/locations/search?q=600044"
curl "http://localhost:8080/api/locations/search?q=தமிழ்"
```

## Re-importing (Monthly)

Download fresh data and re-run the import. Existing records are updated;
new records are inserted. Run monthly to stay current with LGD updates.
