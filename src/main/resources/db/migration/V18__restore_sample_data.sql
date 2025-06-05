-- Insert sample projects
INSERT INTO projects (name, description, start_date, end_date, status, created_by, created_at, updated_at)
SELECT 'Sample Project 1', 'This is a sample project for testing', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'ACTIVE', 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM projects WHERE name = 'Sample Project 1');

-- Insert sample tasks
INSERT INTO tasks (title, description, priority, status, start_date, due_date, project_id, assignee_id, created_by, created_at, updated_at)
SELECT 'Sample Task 1', 'This is a sample task for testing', 'MEDIUM', 'TODO', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 
       (SELECT id FROM projects WHERE name = 'Sample Project 1' LIMIT 1), 
       2, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM tasks WHERE title = 'Sample Task 1');

-- Insert sample task history
INSERT INTO task_history (task_id, old_status, new_status, changed_by, changed_at)
SELECT 
    (SELECT id FROM tasks WHERE title = 'Sample Task 1' LIMIT 1),
    'UNASSIGNED', 'TODO', 1, DATE_SUB(NOW(), INTERVAL 1 DAY)
WHERE EXISTS (SELECT 1 FROM tasks WHERE title = 'Sample Task 1'); 