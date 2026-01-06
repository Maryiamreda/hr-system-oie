CREATE TABLE employee_expertise (



                                    employee_id BIGINT NOT NULL,
                                    expertise_id BIGINT NOT NULL,
                                    PRIMARY KEY (employee_id, expertise_id),
                                    FOREIGN KEY (employee_id) REFERENCES employee(id),
                                    FOREIGN KEY (expertise_id) REFERENCES expertise(id)
);

