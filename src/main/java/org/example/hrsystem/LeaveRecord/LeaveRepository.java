package org.example.hrsystem.LeaveRecord;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LeaveRepository extends JpaRepository<LeaveRecord,Long> {
    @Query("SELECT l FROM LeaveRecord l WHERE l.employee.id = :employeeId ORDER BY l.id DESC LIMIT 1")
    Optional<LeaveRecord> findLastRecordByEmployeeId(Long employeeId);

    List<LeaveRecord> findByEmployeeId(Long employeeId);

    List<LeaveRecord> findByNote(String leaveNote);
}
