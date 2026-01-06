package org.example.hrsystem;

import org.example.hrsystem.Employee.Employee;
import org.example.hrsystem.Employee.EmployeeRequestDTO;
import org.example.hrsystem.Expertise.Expertise;
import org.example.hrsystem.enums.Gender;
import org.example.hrsystem.Employee.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc

public class EmployeeIntegrationTest {
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

        MvcResult result = mockMvc.perform(post("/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                //expect output is successful
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(employee.getName()))
                .andExpect(jsonPath("$.birthDate").value(employee.getBirthDate().toString()))
                .andExpect(jsonPath("$.graduationDate").value(employee.getGraduationDate().toString()))
                .andExpect(jsonPath("$.grossSalary").value(employee.getGrossSalary()))
                .andReturn();

//    checkdb status
        List<Employee> employees = employeeRepository.findAll();
        assertThat(employees).hasSize(1);
        Employee savedEmployee = employees.getFirst();
        assertThat(savedEmployee.getName()).isEqualTo("maryiam");
        assertThat(savedEmployee.getBirthDate()).isEqualTo(LocalDate.of(2001, 8, 15));
        assertThat(savedEmployee.getGrossSalary()).isEqualTo(500F);
        assertThat(savedEmployee.getDepartment()).isEqualTo(employee.getDepartment());
        assertThat(savedEmployee.getGraduationDate()).isEqualTo(employee.getGraduationDate());
        assertThat(savedEmployee.getGrossSalary()).isEqualTo(employee.getGrossSalary());
    }

    @Test
    void addNewEmployee_WithNullName_ReturnsBadRequest() throws Exception {
        EmployeeRequestDTO dto = EmployeeRequestDTO.builder()
                .name(null)
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(2001, 8, 15))
                .graduationDate(LocalDate.of(2025, 8, 15))
                .department("1")
                .team("1")
                .grossSalary(500F)
                .build();

        mockMvc.perform(post("/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
        assertThat(employeeRepository.findAll()).isEmpty();
    }

    @Test
    void addNewEmployee_WithNullDepartment_ReturnsBadRequest() throws Exception {
        EmployeeRequestDTO dto = EmployeeRequestDTO.builder()
                .name("maryiam")
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(2001, 8, 15))
                .graduationDate(LocalDate.of(2025, 8, 15))
                .department(null)
                .team("1")
                .grossSalary(500F)
                .build();

        mockMvc.perform(post("/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
        assertThat(employeeRepository.findAll()).isEmpty();
    }

    @Test
    void addNewEmployee_WithNegativeSalary_ReturnsBadRequest() throws Exception {
        EmployeeRequestDTO dto = EmployeeRequestDTO.builder()
                .name("maryiam")
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(2001, 8, 15))
                .graduationDate(LocalDate.of(2025, 8, 15))
                .department("1")
                .team("1")
                .grossSalary(-400F)
                .build();

        mockMvc.perform(post("/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        assertThat(employeeRepository.findAll()).isEmpty();
    }

    @Test
    void addNewEmployee_WithValidExpertise_StatusIsCreated() throws Exception {
        List<Long> expertises = List.of(1L);
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
        mockMvc.perform(post("/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
        assertThat(employeeRepository.findAll()).hasSize(1);
        List<Employee> employees = employeeRepository.findAll();
        assertThat(employees).hasSize(1);

        Employee savedEmployee = employees.get(0);
        assertThat(savedEmployee.getExpertisesId()).hasSize(1);
        assertThat(savedEmployee.getExpertisesId().get(0).getId()).isEqualTo(expertises.get(0));
    }

    @Test
    void addNewEmployee_WithMultipleExpertises_ReturnIsCreated() throws Exception {
        List<Long> expertises = List.of(1L, 2L);
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
        mockMvc.perform(post("/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
        Employee savedEmployee = employeeRepository.findAll().get(0);
        assertThat(savedEmployee.getExpertisesId()).hasSize(2);
        assertThat(savedEmployee.getExpertisesId().get(0).getId()).isEqualTo(expertises.get(0));
        assertThat(savedEmployee.getExpertisesId().get(1).getId()).isEqualTo(expertises.get(1));

    }

    @Test
    public void addNewEmployee_WithExpertiseNotValid_ExpectNotFound() throws Exception {
        List<Long> expertises = List.of(878L);
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
        mockMvc.perform(post("/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
        assertThat(employeeRepository.findAll()).isEmpty();
    }


}
