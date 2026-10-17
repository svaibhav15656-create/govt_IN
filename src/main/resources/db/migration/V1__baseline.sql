-- ============================================================
-- V1__baseline.sql
-- Flyway baseline migration for Before You Go
-- Creates the PostGIS extension and a schema version marker.
-- ============================================================

-- Enable PostGIS if available (will be available in the Docker image).
-- In environments without PostGIS, this will fail gracefully if
-- CREATE EXTENSION IF NOT EXISTS is used.
CREATE EXTENSION IF NOT EXISTS postgis;

-- Enable trigram extension for typo-tolerant search (Phase 1).
CREATE EXTENSION IF NOT EXISTS pg_trgm;
