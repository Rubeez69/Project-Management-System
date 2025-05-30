UPDATE permissions
SET can_view = TRUE
WHERE role_id = (SELECT id FROM roles WHERE name = 'DEVELOPER')
  AND module_id IN (
      SELECT id FROM modules WHERE name IN ('PROJECT', 'TEAM')
  );



