package org.example.hrsystem;

import org.example.hrsystem.Employee.EmployeeRequestDTO;
import org.example.hrsystem.enums.Gender;
import org.example.hrsystem.Employee.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.LocalDate;

@SpringBootTest
@AutoConfigureMockMvc
public class EmployeeServiceTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    void addNewEmployee_WithAccurateDate_StatusIsCreated() throws Exception {
        //create new employee object
        EmployeeRequestDTO employee = generateEmployeeDTO();
        //send post request with the object
        ResultActions result = mockMvc.perform(post("/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                //expect output is successful
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(employee.getName()))
                .andExpect(jsonPath("$.birthDate").value(employee.getBirthDate().toString()))
                .andExpect(jsonPath("$.gender").value(employee.getGender().toString()))
                .andExpect(jsonPath("$.graduationDate").value(employee.getGraduationDate().toString()))
                .andExpect(jsonPath("$.grossSalary").value(employee.getGrossSalary()));
    }

    private EmployeeRequestDTO generateEmployeeDTO() {
        EmployeeRequestDTO dto = new EmployeeRequestDTO();
        dto.setName("maryiam");
        dto.setGender(Gender.FEMALE);
        dto.setBirthDate(LocalDate.of(2001, 8, 15));
        dto.setGraduationDate(LocalDate.of(2025, 8, 15));
        dto.setTeam("1");
        dto.setDepartment("1");
        dto.setGrossSalary(500F); //gross salary
        return dto;
    }

}
