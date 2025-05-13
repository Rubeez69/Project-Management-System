-- 1. Create the specializations lookup table
CREATE TABLE specializations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- 2. Add specialization_id to team_members
ALTER TABLE team_members
ADD COLUMN specialization_id INT;

-- 3. Add foreign key constraint
ALTER TABLE team_members
ADD CONSTRAINT fk_specialization
FOREIGN KEY (specialization_id) REFERENCES specializations(id);

-- 4. Drop the old VARCHAR specialization column
ALTER TABLE team_members
DROP COLUMN specialization;

-- 5. (Optional) Seed common specializations
INSERT INTO specializations (name) VALUES
  ('Frontend Developer'),
  ('Backend Developer'),
  ('Fullstack Developer'),
  ('QA Engineer'),
  ('Project Leader');
