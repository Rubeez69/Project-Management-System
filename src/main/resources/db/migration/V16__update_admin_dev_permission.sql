INSERT INTO permissions (
    role_id, module_id, can_view, can_create, can_update, can_delete
)
SELECT 1, 7, TRUE, FALSE, FALSE, FALSE
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE role_id = 1 AND module_id = 7
);

INSERT INTO permissions (
    role_id, module_id, can_view, can_create, can_update, can_delete
)
SELECT 3, 7, FALSE, FALSE, FALSE, FALSE
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE role_id = 3 AND module_id = 7
);
