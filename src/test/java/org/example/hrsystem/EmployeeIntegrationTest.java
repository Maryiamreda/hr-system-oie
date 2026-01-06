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
                .andReturn();
        //Mock implementation of the HttpServletResponse interface.
//        MockHttpServletResponse response = result.getResponse();
//        Employee responseToEmployeeEntity = objectMapper.readValue(response.getContentAsString(), Employee.class);
//        assertThat(responseToEmployeeEntity.getId()).isNotNull();
//        Optional<Employee> dbEmployee = employeeRepository.findById(responseToEmployeeEntity.getId());
//        assertThat(dbEmployee).isPresent();
//        Employee employee1 = dbEmployee.get();
//        assertThat(employee1.getName()).isEqualTo(employee.getName());
//        assertThat(employee1.getBirthDate()).isEqualTo(employee.getBirthDate());
//        assertThat(employee1.getDepartment()).isEqualTo(employee.getDepartment());
//        db status
        List<Employee> employees = employeeRepository.findAll();
        assertThat(employees).hasSize(1);
        Employee savedEmployee = employees.get(0);
        assertThat(savedEmployee.getName()).isEqualTo("maryiam");
        assertThat(savedEmployee.getBirthDate()).isEqualTo(LocalDate.of(2001, 8, 15));
        assertThat(savedEmployee.getGrossSalary()).isEqualTo(500F);

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
        List<Long> expertises = List.of(1L,2L);
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
