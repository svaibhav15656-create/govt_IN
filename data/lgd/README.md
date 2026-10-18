# LGD Data Directory

Place downloaded LGD CSV files here for import.

See [docs/data-import.md](../../docs/data-import.md) for download instructions and expected format.

## Expected files

```
data/lgd/
├── states.csv
├── districts.csv
├── sub_districts.csv
├── villages.csv
└── pincodes.csv
```

## Where to download

- https://data.gov.in/ (search for "LGD")
- https://lgdirectory.gov.in/ (download directory)

**Do not commit real LGD data to this repository.**
The `.gitignore` excludes CSV files in this directory.
