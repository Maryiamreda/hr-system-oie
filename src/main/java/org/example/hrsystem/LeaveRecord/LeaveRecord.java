package org.example.hrsystem.LeaveRecord;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.hrsystem.Employee.Employee;

import java.time.LocalDate;

@Entity
@Data
@Builder
@Table(name = "leave_record")
@NoArgsConstructor
@AllArgsConstructor

public class LeaveRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "days")
    private int days;
    @Column(name = "total_leave_days")
    private int totalLeaveDays;
    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "end_date")
    private LocalDate endDate;
    private String note;
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

}


