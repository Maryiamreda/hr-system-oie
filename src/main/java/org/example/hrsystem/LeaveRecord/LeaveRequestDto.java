package org.example.hrsystem.LeaveRecord;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import static org.example.hrsystem.utilities.EmployeeMessageConstants.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LeaveRequestDto {
    @NotNull(message = ERROR_NULL_LEAVE_DAYS)
    @Positive(message = ERROR_NONVALID_LEAVE_DAYS)
    private Integer days;
    @NotNull(message = ERROR_NULL_LEAVE_START_DATE)
    @PastOrPresent(message=ERROR_NONVALID_LEAVE_START_DATE)
    private LocalDate startDate;
    private String note;
}
