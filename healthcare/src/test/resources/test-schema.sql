-- H2 schema initialization for integration tests
-- Creates all MySQL schemas as H2 schemas so JPA @Table(schema=...) resolves correctly

CREATE SCHEMA IF NOT EXISTS auth_schema;
CREATE SCHEMA IF NOT EXISTS patient_schema;
CREATE SCHEMA IF NOT EXISTS diagnosticcenter_schema;
CREATE SCHEMA IF NOT EXISTS diagnostictest_schema;
CREATE SCHEMA IF NOT EXISTS appointment_schema;
