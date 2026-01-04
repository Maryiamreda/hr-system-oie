CREATE TABLE employee (
                          id BIGINT AUTO_INCREMENTPRIMARYKEY,
                          name VARCHAR(255)NOTNULL,
                          gender VARCHAR(20),
                          birth_date DATE,
                          graduation_date DATE,
                          department VARCHAR(255) NOTNULL,
                          team VARCHAR(255),
                          gross_salary DECIMAL(10,2),
                          net_salary DECIMAL(10,2));