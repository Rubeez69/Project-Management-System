-- 1. Insert sample users
INSERT INTO users (name, email, password, role_id)
SELECT 'Admin User', 'admin@example.com', 'admin123', r.id
FROM roles r WHERE r.name = 'ADMIN';

-- Insert 3 Project Managers
INSERT INTO users (name, email, password, role_id)
SELECT 'PM One', 'pm1@example.com', 'pm123', r.id FROM roles r WHERE r.name = 'PROJECT_MANAGER';
INSERT INTO users (name, email, password, role_id)
SELECT 'PM Two', 'pm2@example.com', 'pm123', r.id FROM roles r WHERE r.name = 'PROJECT_MANAGER';
INSERT INTO users (name, email, password, role_id)
SELECT 'PM Three', 'pm3@example.com', 'pm123', r.id FROM roles r WHERE r.name = 'PROJECT_MANAGER';

-- Insert 10 Developers
INSERT INTO users (name, email, password, role_id)
SELECT CONCAT('Dev ', n), CONCAT('dev', n, '@example.com'), 'dev123', r.id
FROM roles r, (SELECT 1 AS n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
               UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) AS nums
WHERE r.name = 'DEVELOPER';

-- 2. Create a sample project created by PM One (NOT Admin)
INSERT INTO projects (name, description, start_date, end_date, created_by)
SELECT 'Internal Tool Revamp', 'Building internal project management tool', '2025-06-01', '2025-12-01', u.id
FROM users u WHERE u.email = 'pm1@example.com';

-- 3. PM One (creator) is added as Project Lead
INSERT INTO team_members (user_id, project_id, specialization_id)
SELECT u.id, p.id, s.id
FROM users u, projects p, specializations s
WHERE u.email = 'pm1@example.com' AND p.name = 'Internal Tool Revamp' AND s.name = 'Project Lead';

-- PM Two is added as QA Engineer
INSERT INTO team_members (user_id, project_id, specialization_id)
SELECT u.id, p.id, s.id
FROM users u, projects p, specializations s
WHERE u.email = 'pm2@example.com' AND p.name = 'Internal Tool Revamp' AND s.name = 'QA Engineer';

-- First 6 Developers with assigned specializations
INSERT INTO team_members (user_id, project_id, specialization_id)
SELECT u.id, p.id, s.id
FROM users u
JOIN projects p ON p.name = 'Internal Tool Revamp'
JOIN specializations s ON s.name = CASE
    WHEN u.email = 'dev1@example.com' THEN 'Frontend Developer'
    WHEN u.email = 'dev2@example.com' THEN 'Backend Developer'
    WHEN u.email = 'dev3@example.com' THEN 'Fullstack Developer'
    WHEN u.email = 'dev4@example.com' THEN 'QA Engineer'
    WHEN u.email = 'dev5@example.com' THEN 'Backend Developer'
    WHEN u.email = 'dev6@example.com' THEN 'Frontend Developer'
    ELSE NULL
END
WHERE u.email IN ('dev1@example.com', 'dev2@example.com', 'dev3@example.com',
                  'dev4@example.com', 'dev5@example.com', 'dev6@example.com');
