-- Seed roles
INSERT INTO roles (name) VALUES
  ('ADMIN'),
  ('PROJECT_MANAGER'),
  ('DEVELOPER');

-- Seed modules
INSERT INTO modules (name) VALUES
  ('PROJECT'),
  ('TASK'),
  ('TEAM'),
  ('FRIEND'),
  ('ROLE'),
  ('USER');

-- Permissions for ADMIN: Full access to all modules
INSERT INTO permissions (role_id, module_id, can_view, can_create, can_update, can_delete)
SELECT r.id, m.id, TRUE, TRUE, TRUE, TRUE
FROM roles r, modules m
WHERE r.name = 'ADMIN';

-- Permissions for PROJECT_MANAGER
-- Full access on PROJECT, TASK, TEAM; view-only on USER; nothing on ROLE/FRIEND
INSERT INTO permissions (role_id, module_id, can_view, can_create, can_update, can_delete)
SELECT r.id, m.id,
       CASE WHEN m.name IN ('PROJECT', 'TASK', 'TEAM', 'USER') THEN TRUE ELSE FALSE END,
       CASE WHEN m.name IN ('PROJECT', 'TASK', 'TEAM') THEN TRUE ELSE FALSE END,
       CASE WHEN m.name IN ('PROJECT', 'TASK', 'TEAM') THEN TRUE ELSE FALSE END,
       CASE WHEN m.name IN ('PROJECT', 'TASK', 'TEAM') THEN FALSE ELSE FALSE END
FROM roles r, modules m
WHERE r.name = 'PROJECT_MANAGER';

-- Permissions for DEVELOPER
-- View and update access to TASK only
INSERT INTO permissions (role_id, module_id, can_view, can_create, can_update, can_delete)
SELECT r.id, m.id,
       CASE WHEN m.name = 'TASK' THEN TRUE ELSE FALSE END,
       FALSE,
       CASE WHEN m.name = 'TASK' THEN TRUE ELSE FALSE END,
       FALSE
FROM roles r, modules m
WHERE r.name = 'DEVELOPER';
