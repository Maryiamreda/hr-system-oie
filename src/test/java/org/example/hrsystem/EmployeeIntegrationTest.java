package org.example.hrsystem;

import com.jayway.jsonpath.JsonPath;
import org.example.hrsystem.Employee.Employee;
import org.example.hrsystem.Employee.EmployeeRequestDTO;
import org.example.hrsystem.Expertise.Expertise;
import org.example.hrsystem.Expertise.ExpertiseRepository;
import org.example.hrsystem.enums.Gender;
import org.example.hrsystem.Employee.EmployeeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class EmployeeIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private ExpertiseRepository expertiseRepository;
    private final String EMPLOYEE_NAME = "maryiam";

    private final float GROSS_SALARY = 5000F;
    private Expertise expertise1;
    private Expertise expertise2;
    private Employee mockUser;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        expertiseRepository.deleteAll();


        expertise1 = new Expertise();
        expertise1.setName("Java");
        expertise1 = expertiseRepository.save(expertise1);

        expertise2 = new Expertise();
        expertise2.setName("OOP");
        expertise2 = expertiseRepository.save(expertise2);

        mockUser = new Employee();
        mockUser.setName("Test User");
        mockUser.setDepartment("1");
        mockUser.setGrossSalary(10000F);
        mockUser = employeeRepository.save(mockUser);
        System.out.println("id is" + mockUser.getId());
    }

    @Test
    void addNewEmployee_WithAccurateDate_ReturnsCreatedStatus() throws Exception {
        //create new inputEmployee object
        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
                .name(EMPLOYEE_NAME)
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(2001, 8, 15))
                .graduationDate(LocalDate.of(2025, 8, 15))
                .team("1")
                .department("1")
                .grossSalary(GROSS_SALARY)
                .build();
        //send post request with the object
        MvcResult result = mockMvc.perform(post("/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputEmployee)))
                //expect output is successful
                .andExpect(status().isCreated())
                .andReturn();
        MockHttpServletResponse response = result.getResponse();
        Employee responseToEmployeeEntity = objectMapper.readValue(response.getContentAsString(), Employee.class);
        assertThat(responseToEmployeeEntity.getId()).isNotNull();
        Optional<Employee> dbEmployee = employeeRepository.findById(responseToEmployeeEntity.getId());
        assertThat(dbEmployee).isPresent();
        Employee actualEmployee = dbEmployee.get();
        assertThat(actualEmployee.getName()).isEqualTo(inputEmployee.getName());
        assertThat(actualEmployee.getBirthDate()).isEqualTo(inputEmployee.getBirthDate());
        assertThat(actualEmployee.getDepartment()).isEqualTo(inputEmployee.getDepartment());
        assertThat(actualEmployee.getGraduationDate()).isEqualTo(inputEmployee.getGraduationDate());
        assertThat(actualEmployee.getGrossSalary()).isEqualTo(inputEmployee.getGrossSalary());
//test json path results
        String json = result.getResponse().getContentAsString();
        String graduationDate = JsonPath.parse(json).read("$.graduationDate").toString();
        String birthDate = JsonPath.parse(json).read("$.birthDate").toString();
        String name = JsonPath.parse(json).read("$.name").toString();
        assertThat(name).isEqualTo(inputEmployee.getName());
        assertThat(graduationDate).isEqualTo(inputEmployee.getGraduationDate().toString());
        assertThat(birthDate).isEqualTo(inputEmployee.getBirthDate().toString());

    }

    @Test
    void addNewEmployee_WithNullName_ReturnsBadRequestStatus() throws Exception {
        String uuidDepartmentName = unique("department-name");

        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
                .name(null)
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(2001, 8, 15))
                .graduationDate(LocalDate.of(2025, 8, 15))
                .department(uuidDepartmentName)
                .team("1")
                .grossSalary(GROSS_SALARY)
                .build();
        mockMvc.perform(post("/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputEmployee)))
                .andExpect(status().isBadRequest());

        assertThat(employeeRepository.findByDepartment(uuidDepartmentName)).isEmpty();

    }

    @Test
    void addNewEmployee_WithNullDepartment_ReturnsBadRequestStatus() throws Exception {
        String uuidName = unique("unique-name");
        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
                .name(uuidName)
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(2001, 8, 15))
                .graduationDate(LocalDate.of(2025, 8, 15))
                .department(null)
                .team("1")
                .grossSalary(GROSS_SALARY)
                .build();
        mockMvc.perform(post("/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputEmployee)))
                .andExpect(status().isBadRequest());

        assertThat(employeeRepository.findByName(uuidName)).isEmpty();

    }

    @Test
    void addNewEmployee_WithNegativeSalary_ReturnsBadRequestStatus() throws Exception {
        String uuidName = unique("unique-name");
        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
                .name(uuidName)
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(2001, 8, 15))
                .graduationDate(LocalDate.of(2025, 8, 15))
                .department("1")
                .team("1")
                .grossSalary(-400F)
                .build();
        mockMvc.perform(post("/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputEmployee)))
                .andExpect(status().isBadRequest());
        assertThat(employeeRepository.findByName(uuidName)).isEmpty();
    }

    @Test
    void addNewEmployee_WithValidExpertise_ReturnsCreatedStatus() throws Exception {
        List<Long> expertises = List.of(expertise1.getId());
        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
                .name(EMPLOYEE_NAME)
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(2001, 8, 15))
                .graduationDate(LocalDate.of(2025, 8, 15))
                .team("1")
                .department("1")
                .grossSalary(GROSS_SALARY)
                .expertise(expertises)
                .build();
        MvcResult result = mockMvc.perform(post("/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputEmployee)))
                .andExpect(status().isCreated()).andReturn();
        MockHttpServletResponse response = result.getResponse();
        Employee responseToEmployeeEntity = objectMapper.readValue(response.getContentAsString(), Employee.class);
        assertThat(responseToEmployeeEntity.getId()).isNotNull();
        Optional<Employee> dbEmployee = employeeRepository.findById(responseToEmployeeEntity.getId());
        assertThat(dbEmployee).isPresent();
        Employee actualEmployee = dbEmployee.get();
        assertThat(actualEmployee.getExpertisesId()).hasSize(1);
        assertThat(actualEmployee.getExpertisesId().get(0).getId()).isEqualTo(expertises.get(0));
    }

    @Test
    void addNewEmployee_WithMultipleExpertises_ReturnsCreatedStatus() throws Exception {
        List<Long> expertises = List.of(expertise1.getId(), expertise2.getId());
        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
                .name(EMPLOYEE_NAME)
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(2001, 8, 15))
                .graduationDate(LocalDate.of(2025, 8, 15))
                .team("1")
                .department("1")
                .grossSalary(GROSS_SALARY)
                .expertise(expertises)
                .build();
        MvcResult result = mockMvc.perform(post("/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputEmployee)))
                .andExpect(status().isCreated()).andReturn();
        MockHttpServletResponse response = result.getResponse();
        Employee responseToEmployeeEntity = objectMapper.readValue(response.getContentAsString(), Employee.class);
        assertThat(responseToEmployeeEntity.getId()).isNotNull();
        Optional<Employee> dbEmployee = employeeRepository.findById(responseToEmployeeEntity.getId());
        assertThat(dbEmployee).isPresent();
        Employee actualEmployee = dbEmployee.get();
        assertThat(actualEmployee.getExpertisesId().get(0).getId()).isEqualTo(expertises.get(0));
        assertThat(actualEmployee.getExpertisesId().get(1).getId()).isEqualTo(expertises.get(1));
    }

    @Test
    public void addNewEmployee_WithExpertiseNotValid_ReturnsNotFoundStatus() throws Exception {
        String uuidName = unique("unique-name");
        List<Long> expertises = List.of(878L);
        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
                .name(uuidName)
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(2001, 8, 15))
                .graduationDate(LocalDate.of(2025, 8, 15))
                .team("1")
                .department("1")
                .grossSalary(GROSS_SALARY)
                .expertise(expertises)
                .build();
        mockMvc.perform(post("/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputEmployee)))
                .andExpect(status().isNotFound());
        assertThat(employeeRepository.findByName(uuidName)).isEmpty();
    }

    @Test
    public void addNewEmployee_WithManagerId_ReturnsCreatedStatus() throws Exception {
        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
                .name(EMPLOYEE_NAME)
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(2001, 8, 15))
                .graduationDate(LocalDate.of(2025, 8, 15))
                .team("1")
                .department("1")
                .grossSalary(GROSS_SALARY)
                .managerId(mockUser.getId())
                .build();
        MvcResult result = mockMvc.perform(post("/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputEmployee)))
                .andExpect(status().isCreated()).andReturn();
        MockHttpServletResponse response = result.getResponse();
        Employee responseToEmployeeEntity = objectMapper.readValue(response.getContentAsString(), Employee.class);
        assertThat(responseToEmployeeEntity.getId()).isNotNull();
        Optional<Employee> dbEmployee = employeeRepository.findById(responseToEmployeeEntity.getId());
        assertThat(dbEmployee).isPresent();
        Employee actualEmployee = dbEmployee.get();
        assertThat(actualEmployee.getManager().getId()).isEqualTo(inputEmployee.getManagerId());
    }

    @Test
    public void addNewEmployee_WithInValidManagerId_ReturnsNotFoundStatus() throws Exception {
        String uuidName = unique("unique-name");
        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
                .name(uuidName)
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(2001, 8, 15))
                .graduationDate(LocalDate.of(2025, 8, 15))
                .team("1")
                .department("1")
                .grossSalary(GROSS_SALARY)
                .managerId(-888L)
                .build();
        mockMvc.perform(post("/employee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputEmployee)))
                .andExpect(status().isNotFound());
        assertThat(employeeRepository.findByName(uuidName)).isEmpty();
    }

    private String unique(String name) {
        return name + "-" + UUID.randomUUID();
    }

    @AfterEach
    void tearDown() {
        employeeRepository.deleteAll();
    }
}

