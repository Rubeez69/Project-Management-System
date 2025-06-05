UPDATE tasks
SET 
    start_date = DATE_ADD(start_date, INTERVAL 2 YEAR),
    due_date = DATE_ADD(due_date, INTERVAL 2 YEAR)
WHERE 
    YEAR(start_date) = 2023 OR YEAR(due_date) = 2023;
