package org.example.hrsystem.SalaryRecord;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.hrsystem.Employee.Employee;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "salary_record")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalaryRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;
    @Column(name = "salary_year")
    private Short salaryYear;
    @Column(name = "salary_month")
    private Byte salaryMonth;
    @Column(name = "type")
    private String type;
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
}
