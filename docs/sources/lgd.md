# LGD (Local Government Directory) — Data Source

## Source

- **Name**: Local Government Directory (LGD)
- **Publisher**: Ministry of Panchayati Raj, Government of India
- **Website**: https://lgdirectory.gov.in/
- **Data Portal**: https://data.gov.in/ (search for "LGD")

## Datasets Used

| Dataset | Purpose | Typical File |
|---------|---------|--------------|
| State list | All states and union territories with LGD codes | `states.csv` |
| District list | All districts with state mapping | `districts.csv` |
| Sub-district list | All sub-districts (taluk/tehsil/block) with district mapping | `sub_districts.csv` |
| Village list | All villages/towns with sub-district mapping | `villages.csv` |
| Villages with PIN codes | Pincode-to-village mapping | `pincodes.csv` |

## Licence and Attribution

- **Licence**: Government Open Data License – India (GODL)
- **Attribution**: "Source: Local Government Directory, Ministry of Panchayati Raj, Government of India"
- **Terms checked on**: NOT_VERIFIED — must be confirmed before production use
- **Usage restrictions**: Verify terms at https://data.gov.in/government-open-data-license-india

## Update Frequency

- LGD data is updated periodically (approximately monthly)
- The import should be re-run monthly to pick up new villages, renamed entities, etc.
- Re-import is idempotent: existing records are updated, new records are inserted

## Important Notes

1. **No scraping**: We download official CSV/Excel files from data.gov.in or the LGD download directory. We do NOT scrape the LGD website.
2. **Local-language names**: LGD datasets include names in local scripts (Hindi, Tamil, Telugu, etc.)
3. **LGD codes**: Each entity has a unique LGD code that is stable across updates
4. **Census codes**: Some datasets include Census 2011 codes for cross-referencing
