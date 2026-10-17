-- ============================================================
-- V2__location_tables.sql
-- Location tables for LGD data: states, districts,
-- sub-districts, villages, pincodes.
-- Trigram (pg_trgm) GIN indexes for typo-tolerant typeahead.
-- ============================================================

-- ========================
-- States and Union Territories
-- ========================
CREATE TABLE states (
    lgd_code    INTEGER PRIMARY KEY,
    name_en     VARCHAR(255) NOT NULL,
    name_local  VARCHAR(255),
    kind        VARCHAR(50) NOT NULL DEFAULT 'STATE',
    census_2011_code VARCHAR(10),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ========================
-- Districts
-- ========================
CREATE TABLE districts (
    lgd_code    INTEGER PRIMARY KEY,
    state_code  INTEGER NOT NULL REFERENCES states(lgd_code),
    name_en     VARCHAR(255) NOT NULL,
    name_local  VARCHAR(255),
    census_2011_code VARCHAR(10),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_districts_state_code ON districts(state_code);

-- ========================
-- Sub-districts (Taluk / Tehsil / Block)
-- ========================
CREATE TABLE sub_districts (
    lgd_code    INTEGER PRIMARY KEY,
    district_code INTEGER NOT NULL REFERENCES districts(lgd_code),
    name_en     VARCHAR(255) NOT NULL,
    name_local  VARCHAR(255),
    census_2011_code VARCHAR(10),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sub_districts_district_code ON sub_districts(district_code);

-- ========================
-- Villages / Towns
-- ========================
CREATE TABLE villages (
    lgd_code        INTEGER PRIMARY KEY,
    sub_district_code INTEGER NOT NULL REFERENCES sub_districts(lgd_code),
    name_en         VARCHAR(255) NOT NULL,
    name_local      VARCHAR(255),
    census_2011_code VARCHAR(10),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_villages_sub_district_code ON villages(sub_district_code);

-- ========================
-- Pincode mappings
-- ========================
CREATE TABLE pincodes (
    id              BIGSERIAL PRIMARY KEY,
    pincode         VARCHAR(6) NOT NULL,
    village_code    INTEGER REFERENCES villages(lgd_code),
    district_code   INTEGER REFERENCES districts(lgd_code),
    source          VARCHAR(255),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(pincode, village_code)
);

CREATE INDEX idx_pincodes_pincode ON pincodes(pincode);
CREATE INDEX idx_pincodes_village_code ON pincodes(village_code);
CREATE INDEX idx_pincodes_district_code ON pincodes(district_code);

-- ========================
-- Ingestion run tracking
-- ========================
CREATE TABLE ingestion_runs (
    id              BIGSERIAL PRIMARY KEY,
    run_type        VARCHAR(100) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'RUNNING',
    records_processed INTEGER DEFAULT 0,
    records_skipped   INTEGER DEFAULT 0,
    source_file     VARCHAR(500),
    error_message   TEXT,
    started_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at    TIMESTAMP
);

-- ========================
-- Trigram GIN indexes for typo-tolerant search
-- (pg_trgm was enabled in V1__baseline.sql)
-- These indexes accelerate both LIKE '%query%' and
-- similarity() searches on name columns.
-- ========================
CREATE INDEX idx_states_name_en_trgm ON states USING GIN (name_en gin_trgm_ops);
CREATE INDEX idx_states_name_local_trgm ON states USING GIN (name_local gin_trgm_ops);

CREATE INDEX idx_districts_name_en_trgm ON districts USING GIN (name_en gin_trgm_ops);
CREATE INDEX idx_districts_name_local_trgm ON districts USING GIN (name_local gin_trgm_ops);

CREATE INDEX idx_sub_districts_name_en_trgm ON sub_districts USING GIN (name_en gin_trgm_ops);
CREATE INDEX idx_sub_districts_name_local_trgm ON sub_districts USING GIN (name_local gin_trgm_ops);

CREATE INDEX idx_villages_name_en_trgm ON villages USING GIN (name_en gin_trgm_ops);
CREATE INDEX idx_villages_name_local_trgm ON villages USING GIN (name_local gin_trgm_ops);
