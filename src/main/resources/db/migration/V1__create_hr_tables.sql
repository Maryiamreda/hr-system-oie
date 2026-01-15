CREATE TABLE employee (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          gender VARCHAR(20),
                          birth_date DATE,
                          graduation_date DATE,
                          department VARCHAR(255) NOT NULL,
                          team VARCHAR(255),
                          gross_salary DECIMAL(10,2),
                          net_salary DECIMAL(10,2));