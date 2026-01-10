ALTER TABLE employee ADD manager_id BIGINT;
ALTER TABLE employee
    ADD CONSTRAINT fk_manager
        FOREIGN KEY (manager_id) REFERENCES employee(id);
-- ALTER TABLE employee
--     ADD manager_id BIGINT,
-- ADD CONSTRAINT fk_manager
-- FOREIGN KEY(manager_id) REFERENCES employee(id);
