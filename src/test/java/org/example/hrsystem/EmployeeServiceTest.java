package org.example.hrsystem;

import org.example.hrsystem.Employee.EmployeeRequestDTO;
import org.example.hrsystem.enums.Gender;
import org.example.hrsystem.Employee.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc

public class EmployeeServiceTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setup() {
        employeeRepository.deleteAll();
    }

    @Test
    void addNewEmployee_WithAccurateDate_StatusIsCreated() throws Exception {
        //create new employee object
        EmployeeRequestDTO employee = EmployeeRequestDTO.builder()
                .name("maryiam")
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(2001, 8, 15))
                .graduationDate(LocalDate.of(2025, 8, 15))
                .team("1")
                .department("1")
                .grossSalary(500F)
                .build();
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
        assertThat(employeeRepository.findAll()).hasSize(1);

    }

    @Test
    void addNewEmployee_WithInvalidData_ReturnsBadRequest() throws Exception {
        EmployeeRequestDTO dto = EmployeeRequestDTO.builder()
                .name(null)
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(2001, 8, 15))
                .graduationDate(LocalDate.of(2025, 8, 15))
                .team(null)
                .department("1")
                .grossSalary(-100F)
                .build();

        mockMvc.perform(post("/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
        assertThat(employeeRepository.findAll()).isEmpty();
    }

    @Test
    void addNewEmployee_WithListOfExpertise_StatusIsCreated() throws Exception {
        List<String> expertises = List.of("1", "2");
        EmployeeRequestDTO dto = EmployeeRequestDTO.builder()
                .name("maryiam")
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(2001, 8, 15))
                .graduationDate(LocalDate.of(2025, 8, 15))
                .team("1")
                .department("1")
                .grossSalary(500F)
                .expertise(expertises)
                .build();
        ResultActions result = mockMvc.perform(post("/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
        assertThat(employeeRepository.findAll()).hasSize(1);

    }

}
