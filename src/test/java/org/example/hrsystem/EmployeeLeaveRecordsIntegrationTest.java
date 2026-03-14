package org.example.hrsystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.example.hrsystem.Employee.Employee;
import org.example.hrsystem.Employee.EmployeeRepository;
import org.example.hrsystem.LeaveRecord.LeaveRecord;
import org.example.hrsystem.LeaveRecord.LeaveRepository;
import org.example.hrsystem.LeaveRecord.LeaveRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class
})
@DbUnitConfiguration(databaseConnection = "dataSource")
public class EmployeeLeaveRecordsIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private LeaveRepository leaveRepository;
    private static final String EMPLOYEE_API = "/hr/api/employee";

    private static final String EMPLOYEE_REQUEST_LEAVE_NATIONAL_ID = "ROOT_MANAGER-ID-111111111";
    private static final int EMPLOYEE_LEAVE_REQUEST_DAYS = 2;
    private static final int EMPLOYEE_LEAVE_REQUEST_DAYS_WITH_WEEKENDS= 5;
    private static final int EMPLOYEE_EXPECTED_TOTAL_LEAVE_DAYS = 4;
    private static final LocalDate EMPLOYEE_LEAVE_REQUEST_START_DATE = LocalDate.of(2026, 3, 16);
    private static final LocalDate EMPLOYEE_LEAVE_REQUEST_START_DATE_FRIDAY = LocalDate.of(2026, 3, 20);

    private static final LocalDate EMPLOYEE_LEAVE_REQUEST_EXPECTED_END_DATE = LocalDate.of(2026, 3, 18);
    private static final LocalDate EMPLOYEE_LEAVE_REQUEST_END_DATE_MONDAY = LocalDate.of(2026, 3, 23);

    private static final LocalDate EMPLOYEE_LEAVE_REQUEST_EXPECTED_END_DATE_WITH_WEEKENDS = LocalDate.of(2026, 3, 23);


    @Test
    @DatabaseSetup(value = "/dataset/addNewLeaveForEmployee.xml")
    void addNewLeaveForEmployee_WithValidIdAndLeaveDaysLeft_ReturnCreatedStatus() throws Exception {
        Employee employee = employeeRepository.findByNationalId(EMPLOYEE_REQUEST_LEAVE_NATIONAL_ID).get();
        LeaveRequestDto leaveRequestDto = LeaveRequestDto.builder().
                days(EMPLOYEE_LEAVE_REQUEST_DAYS)
                .startDate(EMPLOYEE_LEAVE_REQUEST_START_DATE)
                .build();
        MvcResult result = mockMvc.perform(post(EMPLOYEE_API + "/" + employee.getId() + "/leave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(leaveRequestDto))
                )
                .andExpect(status().isCreated()).andReturn();
        MockHttpServletResponse response = result.getResponse();
        LeaveRecord leaveRecord = objectMapper.readValue(response.getContentAsString(), LeaveRecord.class);
        //get the first record with the employee national id from the leaves repository
        assertThat(leaveRecord.getId()).isNotNull();
        Optional<LeaveRecord> dbLeaveRecord = leaveRepository.findById(leaveRecord.getId());
        assertThat(dbLeaveRecord).isPresent();
        LeaveRecord actualLeaveRecord = dbLeaveRecord.get();
        //make sure the record with this data and with the expected end date and total days also
        assertThat(actualLeaveRecord.getDays()).isEqualTo(EMPLOYEE_LEAVE_REQUEST_DAYS);
        assertThat(actualLeaveRecord.getStartDate()).isEqualTo(EMPLOYEE_LEAVE_REQUEST_START_DATE);
        assertThat(actualLeaveRecord.getTotalLeaveDays()).isEqualTo(EMPLOYEE_EXPECTED_TOTAL_LEAVE_DAYS);
        assertThat(actualLeaveRecord.getEndDate()).isEqualTo(EMPLOYEE_LEAVE_REQUEST_EXPECTED_END_DATE);
    }

    @Test
    @DatabaseSetup(value = "/dataset/addNewLeaveForEmployee.xml")
    void addNewLeaveForEmployee_WithLeaveThatHasWeekendsBetween_ReturnCreatedStatus() throws Exception {
        Employee employee = employeeRepository.findByNationalId(EMPLOYEE_REQUEST_LEAVE_NATIONAL_ID).get();
        LeaveRequestDto leaveRequestDto = LeaveRequestDto.builder().
                days(EMPLOYEE_LEAVE_REQUEST_DAYS_WITH_WEEKENDS)
                .startDate(EMPLOYEE_LEAVE_REQUEST_START_DATE)
                .build();
        MvcResult result = mockMvc.perform(post(EMPLOYEE_API + "/" + employee.getId() + "/leave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(leaveRequestDto))
                )
                .andExpect(status().isCreated()).andReturn();
        MockHttpServletResponse response = result.getResponse();
        LeaveRecord leaveRecord = objectMapper.readValue(response.getContentAsString(), LeaveRecord.class);
        //get the first record with the employee national id from the leaves repository
        assertThat(leaveRecord.getId()).isNotNull();
        Optional<LeaveRecord> dbLeaveRecord = leaveRepository.findById(leaveRecord.getId());
        assertThat(dbLeaveRecord).isPresent();
        LeaveRecord actualLeaveRecord = dbLeaveRecord.get();
        //make sure the record with this data and with the expected end date and total days also
       assertThat(actualLeaveRecord.getEndDate()).isEqualTo(EMPLOYEE_LEAVE_REQUEST_EXPECTED_END_DATE_WITH_WEEKENDS);

    }
    @Test
    @DatabaseSetup(value = "/dataset/addNewLeaveForEmployee.xml")
    void addNewLeaveForEmployee_OneDayLeaveStartOnFridayEndOnSunday_ReturnCreatedStatus() throws Exception {
        Employee employee = employeeRepository.findByNationalId(EMPLOYEE_REQUEST_LEAVE_NATIONAL_ID).get();
        LeaveRequestDto leaveRequestDto = LeaveRequestDto.builder().
                days(1)
                .startDate(EMPLOYEE_LEAVE_REQUEST_START_DATE_FRIDAY)
                .build();
        MvcResult result = mockMvc.perform(post(EMPLOYEE_API + "/" + employee.getId() + "/leave")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(leaveRequestDto))
                )
                .andExpect(status().isCreated()).andReturn();
        MockHttpServletResponse response = result.getResponse();
        LeaveRecord leaveRecord = objectMapper.readValue(response.getContentAsString(), LeaveRecord.class);
        //get the first record with the employee national id from the leaves repository
        assertThat(leaveRecord.getId()).isNotNull();
        Optional<LeaveRecord> dbLeaveRecord = leaveRepository.findById(leaveRecord.getId());
        assertThat(dbLeaveRecord).isPresent();
        LeaveRecord actualLeaveRecord = dbLeaveRecord.get();
        //make sure the record with this data and with the expected end date and total days also
        assertThat(actualLeaveRecord.getEndDate()).isEqualTo(EMPLOYEE_LEAVE_REQUEST_END_DATE_MONDAY);
    }
}
