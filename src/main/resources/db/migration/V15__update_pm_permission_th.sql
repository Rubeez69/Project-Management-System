INSERT INTO permissions (
    role_id, module_id, can_view, can_create, can_update, can_delete
)
SELECT 2, 7, TRUE, FALSE, FALSE, FALSE
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE role_id = 2 AND module_id = 7
);



