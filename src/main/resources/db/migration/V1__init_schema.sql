-- Roles table
CREATE TABLE roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Users table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role_id INT NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Projects table
CREATE TABLE projects (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATE,
    end_date DATE,
    status ENUM('ACTIVE', 'ON_HOLD', 'COMPLETED') NOT NULL DEFAULT 'ACTIVE',
    created_by INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Team members table (linking users to projects)
CREATE TABLE team_members (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    project_id INT NOT NULL,
    specialization VARCHAR(100),
    added_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY (user_id, project_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (project_id) REFERENCES projects(id)
);

-- Tasks table
CREATE TABLE tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    priority ENUM('LOW', 'MEDIUM', 'HIGH') NOT NULL DEFAULT 'MEDIUM',
    status ENUM('UNASSIGNED', 'TODO', 'IN_PROGRESS', 'COMPLETED', 'OVERDUE', 'ARCHIVED') NOT NULL DEFAULT 'UNASSIGNED',
    start_date DATE,
    due_date DATE,
    project_id INT NOT NULL,
    assignee_id INT DEFAULT NULL,
    created_by INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id),
    FOREIGN KEY (assignee_id) REFERENCES users(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Optional: Task history for auditing
CREATE TABLE task_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    task_id INT NOT NULL,
    old_status ENUM('UNASSIGNED', 'TODO', 'IN_PROGRESS', 'COMPLETED', 'OVERDUE', 'ARCHIVED'),
    new_status ENUM('UNASSIGNED', 'TODO', 'IN_PROGRESS', 'COMPLETED', 'OVERDUE', 'ARCHIVED'),
    changed_by INT NOT NULL,
    changed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id),
    FOREIGN KEY (changed_by) REFERENCES users(id)
);

-- Modules table

CREATE TABLE modules (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- Permission table

CREATE TABLE permissions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    role_id INT NOT NULL,
    module_id INT NOT NULL,
    can_view BOOLEAN DEFAULT FALSE,
    can_create BOOLEAN DEFAULT FALSE,
    can_update BOOLEAN DEFAULT FALSE,
    can_delete BOOLEAN DEFAULT FALSE,
    UNIQUE KEY (role_id, module_id),
    FOREIGN KEY (role_id) REFERENCES roles(id),
    FOREIGN KEY (module_id) REFERENCES modules(id)
);