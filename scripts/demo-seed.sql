-- Demo seed data for Galactic Hub Management System
-- Idempotent: safe to re-run (ON CONFLICT DO NOTHING). Does not delete existing rows.
-- Max 6 new rows per table (within the 10-row limit).

BEGIN;

-- ---------------------------------------------------------------------------
-- Spaceships first (6 new: 2 cargo, 2 scout, 2 fighter)
-- ---------------------------------------------------------------------------
INSERT INTO spaceship (id, name, fuel_level, hull_integrity, max_crew_capacity, is_docked, size)
VALUES
    ('d2000001-0001-4001-a001-000000000001'::uuid, 'Rocinante',     72, 88, 12, true,  'LARGE'),
    ('d2000001-0001-4001-a001-000000000002'::uuid, 'Mayflower One', 55, 91,  8, false, 'MEDIUM'),
    ('d2000001-0001-4001-a001-000000000003'::uuid, 'Pathfinder',    68, 95,  4, false, 'SMALL'),
    ('d2000001-0001-4001-a001-000000000004'::uuid, 'Hermes',        45, 78,  3, false, 'SMALL'),
    ('d2000001-0001-4001-a001-000000000005'::uuid, 'Bebop',         63, 82,  3, false, 'MEDIUM'),
    ('d2000001-0001-4001-a001-000000000006'::uuid, 'Excelsior',     38, 70,  2, false, 'LARGE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO cargo_ship (id, max_cargo_weight)
VALUES
    ('d2000001-0001-4001-a001-000000000001'::uuid, 45000),
    ('d2000001-0001-4001-a001-000000000002'::uuid, 12000)
ON CONFLICT (id) DO NOTHING;

INSERT INTO scout_ship (id, sensor_range, stealth_enabled)
VALUES
    ('d2000001-0001-4001-a001-000000000003'::uuid, 350, false),
    ('d2000001-0001-4001-a001-000000000004'::uuid, 280, true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO fighter_ship (id, primary_weapon, shield_strength, ammunition_count)
VALUES
    ('d2000001-0001-4001-a001-000000000005'::uuid, 'RAILGUN',  55, 18),
    ('d2000001-0001-4001-a001-000000000006'::uuid, 'TORPEDO',  72, 12)
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Docking bays 5–10 (6 new bays; bay 8 hosts Rocinante)
-- ---------------------------------------------------------------------------
INSERT INTO docking_bay (id, name, bay_size, bay_number, is_occupied, spaceship_id)
VALUES
    ('d1000001-0001-4001-a001-000000000001'::uuid, 'Transit-Small-A',   'SMALL',  5, false, NULL),
    ('d1000001-0001-4001-a001-000000000002'::uuid, 'Transit-Medium-B',  'MEDIUM', 6, false, NULL),
    ('d1000001-0001-4001-a001-000000000003'::uuid, 'Orbital-Large-C',   'LARGE',  7, false, NULL),
    ('d1000001-0001-4001-a001-000000000004'::uuid, 'Freight-Medium-D',  'MEDIUM', 8, true,  'd2000001-0001-4001-a001-000000000001'::uuid),
    ('d1000001-0001-4001-a001-000000000005'::uuid, 'Visitor-Small-E',   'SMALL',  9, false, NULL),
    ('d1000001-0001-4001-a001-000000000006'::uuid, 'Reserve-Small-F',   'SMALL', 10, false, NULL)
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Crew (10 new members across new and existing ships)
-- ---------------------------------------------------------------------------
INSERT INTO crew_member (id, name, rank, species, ship_id)
VALUES
    ('d3000001-0001-4001-a001-000000000001'::uuid, 'James Holden',  'COMMANDER',  'HUMAN',  'd2000001-0001-4001-a001-000000000001'::uuid),
    ('d3000001-0001-4001-a001-000000000002'::uuid, 'Naomi Nagata',  'ENGINEER',   'HUMAN',  'd2000001-0001-4001-a001-000000000001'::uuid),
    ('d3000001-0001-4001-a001-000000000003'::uuid, 'Alex Kamal',    'SPECIALIST', 'VYKEEN', 'd2000001-0001-4001-a001-000000000001'::uuid),
    ('d3000001-0001-4001-a001-000000000004'::uuid, 'Ellen Ripley',  'OFFICER',    'HUMAN',  'd2000001-0001-4001-a001-000000000002'::uuid),
    ('d3000001-0001-4001-a001-000000000005'::uuid, 'Bishop',        'SPECIALIST', 'ATLAS',  'd2000001-0001-4001-a001-000000000002'::uuid),
    ('d3000001-0001-4001-a001-000000000006'::uuid, 'Jana Sorell',   'SPECIALIST', 'KORVAX', 'd2000001-0001-4001-a001-000000000003'::uuid),
    ('d3000001-0001-4001-a001-000000000007'::uuid, 'Kaz Tanaka',    'OFFICER',    'HUMAN',  'd2000001-0001-4001-a001-000000000004'::uuid),
    ('d3000001-0001-4001-a001-000000000008'::uuid, 'Faye Valentine','CIVILIAN',   'HUMAN',  'd2000001-0001-4001-a001-000000000005'::uuid),
    ('d3000001-0001-4001-a001-000000000009'::uuid, 'Jet Black',     'OFFICER',    'HUMAN',  'd2000001-0001-4001-a001-000000000005'::uuid),
    ('d3000001-0001-4001-a001-00000000000a'::uuid, 'Cole Anderson', 'OFFICER',    'HUMAN',  'af6e5215-d8d2-48f9-821d-25249eab2960'::uuid)
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Cargo items (6 new, mixed types)
-- ---------------------------------------------------------------------------
INSERT INTO cargo_item (id, name, weight, description)
VALUES
    ('d4000001-0001-4001-a001-000000000001'::uuid, 'Titanium Ore',       15.0, 'Refined ore for station fabrication'),
    ('d4000001-0001-4001-a001-000000000002'::uuid, 'Kelp Pods',           8.0, 'Fresh hydroponic produce'),
    ('d4000001-0001-4001-a001-000000000003'::uuid, 'Nav Computer Mk-IV',  2.5, 'Encrypted navigation core'),
    ('d4000001-0001-4001-a001-000000000004'::uuid, 'Reactor Coolant',    12.0, 'Sealed coolant canisters'),
    ('d4000001-0001-4001-a001-000000000005'::uuid, 'Helium-3 Canister',   6.0, 'Fusion fuel reserve'),
    ('d4000001-0001-4001-a001-000000000006'::uuid, 'Synth-Wheat Crates', 20.0, 'Bulk foodstuffs for colony resupply')
ON CONFLICT (id) DO NOTHING;

INSERT INTO raw_material_cargo (id, stateofmatter, purity_percentage)
VALUES
    ('d4000001-0001-4001-a001-000000000001'::uuid, 'SOLID', 92.5),
    ('d4000001-0001-4001-a001-000000000005'::uuid, 'GAS',   88.0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO agricultural_cargo (id, requires_refrigeration, until_spoilage)
VALUES
    ('d4000001-0001-4001-a001-000000000002'::uuid, true,  14),
    ('d4000001-0001-4001-a001-000000000006'::uuid, false, 30)
ON CONFLICT (id) DO NOTHING;

INSERT INTO manufactured_cargo (id, fragil_index)
VALUES
    ('d4000001-0001-4001-a001-000000000003'::uuid, 7)
ON CONFLICT (id) DO NOTHING;

INSERT INTO hazardous_cargo (id, radiation_level, containment_type)
VALUES
    ('d4000001-0001-4001-a001-000000000004'::uuid, 4, 'Cryogenic')
ON CONFLICT (id) DO NOTHING;

-- ---------------------------------------------------------------------------
-- Cargo manifest entries (5 new loads)
-- ---------------------------------------------------------------------------
INSERT INTO cargo_manifest_entry (id, cargo_ship_id, cargo_item_id, quantity)
VALUES
    ('d5000001-0001-4001-a001-000000000001'::uuid, 'd2000001-0001-4001-a001-000000000001'::uuid, 'd4000001-0001-4001-a001-000000000001'::uuid, 3),
    ('d5000001-0001-4001-a001-000000000002'::uuid, 'd2000001-0001-4001-a001-000000000001'::uuid, 'd4000001-0001-4001-a001-000000000002'::uuid, 2),
    ('d5000001-0001-4001-a001-000000000003'::uuid, 'd2000001-0001-4001-a001-000000000002'::uuid, 'd4000001-0001-4001-a001-000000000003'::uuid, 1),
    ('d5000001-0001-4001-a001-000000000004'::uuid, 'd2000001-0001-4001-a001-000000000002'::uuid, 'd4000001-0001-4001-a001-000000000005'::uuid, 4),
    ('d5000001-0001-4001-a001-000000000005'::uuid, '68ff10a7-321a-4c25-8607-4b3bcc275be2'::uuid, 'd4000001-0001-4001-a001-000000000004'::uuid, 1)
ON CONFLICT (id) DO NOTHING;

COMMIT;
