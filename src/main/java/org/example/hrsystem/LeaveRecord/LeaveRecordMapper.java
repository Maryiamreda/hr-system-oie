package org.example.hrsystem.LeaveRecord;

import org.springframework.stereotype.Component;

@Component
public class LeaveRecordMapper {
    public LeaveRecord toEntity(LeaveRequestDto leaveRequestDto){
        return LeaveRecord.builder()
                .days(leaveRequestDto.getDays())
                .startDate(leaveRequestDto.getStartDate())
                .notes(leaveRequestDto.getNotes())
                .build();
    }
}
