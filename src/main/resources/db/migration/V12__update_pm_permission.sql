UPDATE permissions
SET can_delete = TRUE
WHERE role_id = (SELECT id FROM roles WHERE name = 'PROJECT_MANAGER')
  AND module_id = (SELECT id FROM modules WHERE name = 'TEAM');