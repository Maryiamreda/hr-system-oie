package org.example.hrsystem.SalaryRecord;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaryRecordRepository extends JpaRepository<SalaryRecord,Long> {
    List<SalaryRecord> findByEmployeeIdAndTypeAndSalaryMonthAndSalaryYear(Long id, String salaryRecordType, int monthValue, int year);
}
