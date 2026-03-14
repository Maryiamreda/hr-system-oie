package org.example.hrsystem.LeaveRecord;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeaveRequestDto {
    private int days;
    private LocalDate startDate;
    private String notes;
}
