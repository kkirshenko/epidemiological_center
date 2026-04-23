-- ─── Hygiene and Epidemiology Center Database Schema for PostgreSQL ───
--  This migration creates the full data model for a sanitary-epidemiological
--  center management system. The schema is normalized to 3NF with proper
--  PK/FK constraints.
--
--  ## Tables
--  - profiles: Extended user profiles
--  - organization_types: Lookup table for organization types
--  - organizations: Organizations subject to sanitary inspection
--  - inspection_types: Lookup table for inspection categories
--  - inspections: Core inspection records
--  - violations: Individual violations found during inspections
-- ──────────────────────────────────────────────────────────────────────────────

-- ─── PROFILES ────────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS profiles (
  id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  username    text NOT NULL UNIQUE,
  password    text NOT NULL,
  full_name   text NOT NULL DEFAULT '',
  role        text NOT NULL DEFAULT 'ROLE_INSPECTOR' CHECK (role IN ('ROLE_ADMIN','ROLE_INSPECTOR','ROLE_LABORANT')),
  phone       text NOT NULL DEFAULT '',
  position    text NOT NULL DEFAULT '',
  is_active   boolean NOT NULL DEFAULT true,
  created_at  timestamptz NOT NULL DEFAULT now(),
  updated_at  timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_profiles_role ON profiles(role);
CREATE INDEX IF NOT EXISTS idx_profiles_is_active ON profiles(is_active);

-- ─── ORGANIZATION TYPES ───────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS organization_types (
  id          serial PRIMARY KEY,
  name        text NOT NULL UNIQUE,
  description text NOT NULL DEFAULT '',
  created_at  timestamptz NOT NULL DEFAULT now()
);

-- ─── ORGANIZATIONS ────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS organizations (
  id                    uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  name                  text NOT NULL,
  short_name            text NOT NULL DEFAULT '',
  registration_number   text UNIQUE,
  type_id               integer NOT NULL REFERENCES organization_types(id),
  address               text NOT NULL DEFAULT '',
  city                  text NOT NULL DEFAULT '',
  director_name         text NOT NULL DEFAULT '',
  phone                 text NOT NULL DEFAULT '',
  email                 text NOT NULL DEFAULT '',
  employee_count        integer NOT NULL DEFAULT 0 CHECK (employee_count >= 0),
  risk_category         text NOT NULL DEFAULT 'medium' CHECK (risk_category IN ('low','medium','high','critical')),
  is_active             boolean NOT NULL DEFAULT true,
  notes                 text NOT NULL DEFAULT '',
  created_at            timestamptz NOT NULL DEFAULT now(),
  updated_at            timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_organizations_name ON organizations(name);
CREATE INDEX IF NOT EXISTS idx_organizations_registration_number ON organizations(registration_number);
CREATE INDEX IF NOT EXISTS idx_organizations_type_id ON organizations(type_id);
CREATE INDEX IF NOT EXISTS idx_organizations_risk_category ON organizations(risk_category);

-- ─── INSPECTION TYPES ─────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS inspection_types (
  id          serial PRIMARY KEY,
  name        text NOT NULL UNIQUE,
  code        text NOT NULL UNIQUE,
  description text NOT NULL DEFAULT '',
  created_at  timestamptz NOT NULL DEFAULT now()
);

-- ─── INSPECTIONS ──────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS inspections (
  id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  organization_id     uuid NOT NULL REFERENCES organizations(id),
  type_id             integer NOT NULL REFERENCES inspection_types(id),
  inspector_id        uuid NOT NULL REFERENCES profiles(id),
  scheduled_date      date NOT NULL,
  start_date          date,
  end_date            date,
  status              text NOT NULL DEFAULT 'planned' CHECK (status IN ('planned','in_progress','completed','cancelled')),
  result              text NOT NULL DEFAULT 'pending' CHECK (result IN ('pending','satisfactory','unsatisfactory','critical')),
  findings_summary    text NOT NULL DEFAULT '',
  recommendations     text NOT NULL DEFAULT '',
  act_number          text UNIQUE,
  created_at          timestamptz NOT NULL DEFAULT now(),
  updated_at          timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_inspections_organization_id ON inspections(organization_id);
CREATE INDEX IF NOT EXISTS idx_inspections_inspector_id ON inspections(inspector_id);
CREATE INDEX IF NOT EXISTS idx_inspections_status ON inspections(status);
CREATE INDEX IF NOT EXISTS idx_inspections_scheduled_date ON inspections(scheduled_date);
CREATE INDEX IF NOT EXISTS idx_inspections_result ON inspections(result);

-- ─── VIOLATIONS ───────────────────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS violations (
  id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  inspection_id       uuid NOT NULL REFERENCES inspections(id) ON DELETE CASCADE,
  code                text NOT NULL DEFAULT '',
  description         text NOT NULL,
  severity            text NOT NULL DEFAULT 'minor' CHECK (severity IN ('minor','moderate','major','critical')),
  article_reference   text NOT NULL DEFAULT '',
  correction_deadline date,
  resolved            boolean NOT NULL DEFAULT false,
  resolution_notes    text NOT NULL DEFAULT '',
  created_at          timestamptz NOT NULL DEFAULT now(),
  updated_at          timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_violations_inspection_id ON violations(inspection_id);
CREATE INDEX IF NOT EXISTS idx_violations_resolved ON violations(resolved);
CREATE INDEX IF NOT EXISTS idx_violations_severity ON violations(severity);

-- ─── SEED LOOKUP DATA ─────────────────────────────────────────────────────────

INSERT INTO organization_types (name, description) VALUES
  ('Предприятие общественного питания', 'Рестораны, кафе, столовые, буфеты'),
  ('Пищевое производство', 'Заводы и цеха по производству продуктов питания'),
  ('Медицинское учреждение', 'Больницы, поликлиники, лаборатории'),
  ('Детское учреждение', 'Детские сады, школы, лагеря'),
  ('Промышленное предприятие', 'Заводы, фабрики, производственные объекты'),
  ('Торговое предприятие', 'Магазины, супермаркеты, рынки'),
  ('Объект водоснабжения', 'Водозаборы, очистные станции, сети'),
  ('Прочее', 'Иные объекты санитарного надзора')
ON CONFLICT (name) DO NOTHING;

INSERT INTO inspection_types (name, code, description) VALUES
  ('Плановая проверка', 'PLAN', 'Проверка согласно утверждённому плану'),
  ('Внеплановая проверка', 'UNPLAN', 'Проверка по жалобам или обращениям'),
  ('Повторная проверка', 'REPEAT', 'Проверка устранения ранее выявленных нарушений'),
  ('Рейдовая проверка', 'RAID', 'Массовые проверки однотипных объектов'),
  ('Мониторинговая проверка', 'MONITOR', 'Систематический сбор данных без санкций')
ON CONFLICT (code) DO NOTHING;


INSERT INTO profiles (id, username, password, full_name, role, phone, position, is_active)
VALUES (
  gen_random_uuid(),
  'admin1',
  'admin1',
  'Системный администратор',
  'ROLE_ADMIN',
  '',
  'Администратор',
  true
)
ON CONFLICT (username) DO NOTHING;

-- ─── TRIGGER: auto-update updated_at ──────────────────────────────────────────

CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_profiles_updated_at') THEN
    CREATE TRIGGER trg_profiles_updated_at BEFORE UPDATE ON profiles FOR EACH ROW EXECUTE FUNCTION update_updated_at();
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_organizations_updated_at') THEN
    CREATE TRIGGER trg_organizations_updated_at BEFORE UPDATE ON organizations FOR EACH ROW EXECUTE FUNCTION update_updated_at();
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_inspections_updated_at') THEN
    CREATE TRIGGER trg_inspections_updated_at BEFORE UPDATE ON inspections FOR EACH ROW EXECUTE FUNCTION update_updated_at();
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_violations_updated_at') THEN
    CREATE TRIGGER trg_violations_updated_at BEFORE UPDATE ON violations FOR EACH ROW EXECUTE FUNCTION update_updated_at();
  END IF;
END $$;
