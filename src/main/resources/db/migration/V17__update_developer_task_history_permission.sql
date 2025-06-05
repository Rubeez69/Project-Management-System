-- Update developer permissions to allow viewing task history
UPDATE permissions
SET can_view = TRUE
WHERE role_id = 3 -- DEVELOPER role
  AND module_id = 7; -- TASK_HISTORY module 