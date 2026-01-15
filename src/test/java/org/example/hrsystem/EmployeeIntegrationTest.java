package org.example.hrsystem;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.example.hrsystem.Department.Department;
import org.example.hrsystem.Employee.Employee;
import org.example.hrsystem.Employee.dto.EmployeeRequestDTO;
import org.example.hrsystem.enums.Gender;
import org.example.hrsystem.Employee.EmployeeRepository;
import org.example.hrsystem.Department.DepartmentRepository;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.Optional;

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
public class EmployeeIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private  ObjectMapper objectMapper;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private DepartmentRepository departmentRepository;

    private static final String EMPLOYEE_NAME = "maryiam";
    private static final String EMPLOYEE_ROOT_MANAGER_NAME = "ROOT_MANAGER";
    private static final String UNIQUE_DEPARTMENT_NAME = "UNIQUE_DEPARTMENT_NAME";
    private static final String EMPLOYEE_API = "/hr/api/employee";
    private static final BigDecimal GROSS_SALARY = new BigDecimal("5000.00");


    @BeforeEach
    void setUp() {

    }

    @Test
    @DatabaseSetup(value = "/dataset/addNewEmployee_WithValidDepartment.xml", type = DatabaseOperation.CLEAN_INSERT)
    void addNewEmployee_WithAccurateDate_ReturnsCreatedStatus() throws Exception {
        //create new inputEmployee object
        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
                .name(EMPLOYEE_NAME)
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(2001, 8, 15))
                .graduationDate(LocalDate.of(2025, 8, 15))
                .team("1")
                .departmentName(UNIQUE_DEPARTMENT_NAME)
                .grossSalary(GROSS_SALARY)
                .build();
        //send post request with the object
        MvcResult result = mockMvc.perform(post(EMPLOYEE_API)
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
        assertThat(actualEmployee.getDepartment().getName()).isEqualTo(inputEmployee.getDepartmentName());
        assertThat(actualEmployee.getTeam()).isEqualTo(inputEmployee.getTeam());

    }

    @Test
    @DatabaseSetup(value = "/dataset/addNewEmployee_WithValidDepartment.xml", type = DatabaseOperation.INSERT)
    void addNewEmployee_WithNullName_ReturnsBadRequestStatus() throws Exception {
        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
                .name(null)
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(2001, 8, 15))
                .graduationDate(LocalDate.of(2025, 8, 15))
                .departmentName(UNIQUE_DEPARTMENT_NAME)
                .team("1")
                .grossSalary(GROSS_SALARY)
                .build();
        mockMvc.perform(post(EMPLOYEE_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputEmployee)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", containsInAnyOrder("Employee name cannot be empty")));
        Optional<Department> department = departmentRepository.findByName(UNIQUE_DEPARTMENT_NAME);
        assertThat(department).isPresent();
        assertThat(employeeRepository.findByDepartment(department.get())).isEmpty();
    }

    @Test
    @DatabaseSetup(value = "/dataset/addNewEmployee_WithValidDepartment.xml", type = DatabaseOperation.INSERT)
    void addNewEmployee_WithNegativeSalary_ReturnsBadRequestStatus() throws Exception {

        String uuidName = unique("unique-name");
        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
                .name(uuidName)
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(2001, 8, 15))
                .graduationDate(LocalDate.of(2025, 8, 15))
                .departmentName(UNIQUE_DEPARTMENT_NAME)
                .team("1")
                .grossSalary(new BigDecimal("-123.45"))
                .build();
        mockMvc.perform(post(EMPLOYEE_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputEmployee)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", containsInAnyOrder("Salary must be positive")));
        assertThat(employeeRepository.findByName(uuidName)).isEmpty();
    }

    @Test
    @DatabaseSetup(value = "/dataset/addNewEmployee_WithValidDepartment.xml", type = DatabaseOperation.CLEAN_INSERT)
    void addNewEmployee_WithValidDepartment_ReturnsCreatedStatus() throws Exception {
        //create new inputEmployee object
        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
                .name(EMPLOYEE_NAME)
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(2001, 8, 15))
                .graduationDate(LocalDate.of(2025, 8, 15))
                .team("1")
                .departmentName(UNIQUE_DEPARTMENT_NAME)
                .grossSalary(GROSS_SALARY)
                .build();
        //send post request with the object
        MvcResult result = mockMvc.perform(post(EMPLOYEE_API)
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
        assertThat(actualEmployee.getDepartment().getName()).isEqualTo(UNIQUE_DEPARTMENT_NAME);

    }


    @Test
    void addNewEmployee_WithNullDepartment_ReturnsBadRequestStatus() throws Exception {
        String uuidName = unique("unique-name");
        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
                .name(uuidName)
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(2001, 8, 15))
                .graduationDate(LocalDate.of(2025, 8, 15))
                .departmentName(null)
                .team("1")
                .grossSalary(GROSS_SALARY)
                .build();
        mockMvc.perform(post(EMPLOYEE_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputEmployee)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", containsInAnyOrder("Department name be empty")));


        assertThat(employeeRepository.findByName(uuidName)).isEmpty();

    }

    @Test
    @DatabaseSetup(value = "/dataset/addNewEmployee_WithValidDepartment.xml", type = DatabaseOperation.CLEAN_INSERT)
    void addNewEmployee_WithNonExistenceDepartmentName_ReturnsNotFoundStatus() throws Exception {
        String inValidIdDepartmentName = "NON EXISTENCE NAME ";
        String uuidName = unique("unique-name");
        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
                .name(uuidName)
                .gender(Gender.FEMALE)
                .birthDate(LocalDate.of(2001, 8, 15))
                .graduationDate(LocalDate.of(2025, 8, 15))
                .departmentName(inValidIdDepartmentName)
                .team("1")
                .grossSalary(GROSS_SALARY)
                .build();
        mockMvc.perform(post(EMPLOYEE_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputEmployee)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Department Doesn't Exist"));
        assertThat(employeeRepository.findByName(uuidName)).isEmpty();

    }

//    @Test
//    @DatabaseSetup(value = "/dataset/addNewEmployee_WithValidExpertise.xml", type = DatabaseOperation.CLEAN_INSERT)
//    void addNewEmployee_WithValidExpertise_ReturnsCreatedStatus() throws Exception {
//   Expertise expertise1
//        Department department = departmentRepository.findByName(UNIQUE_DEPARTMENT_NAME);
//        List<Long> expertises = List.of(expertise1.getId());
//        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
//                .name(EMPLOYEE_NAME)
//                .gender(Gender.FEMALE)
//                .birthDate(LocalDate.of(2001, 8, 15))
//                .graduationDate(LocalDate.of(2025, 8, 15))
//                .team("1")
//                .departmentId(department.getId())
//                .grossSalary(GROSS_SALARY)
//                .expertise(expertises)
//                .build();
//        MvcResult result = mockMvc.perform(post(EMPLOYEE_API)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(inputEmployee)))
//                .andExpect(status().isCreated()).andReturn();
//        MockHttpServletResponse response = result.getResponse();
//        Employee responseToEmployeeEntity = objectMapper.readValue(response.getContentAsString(), Employee.class);
//        assertThat(responseToEmployeeEntity.getId()).isNotNull();
//        Optional<Employee> dbEmployee = employeeRepository.findById(responseToEmployeeEntity.getId());
//        assertThat(dbEmployee).isPresent();
//        Employee actualEmployee = dbEmployee.get();
//        assertThat(actualEmployee.getExpertisesId()).hasSize(1);
//        assertThat(actualEmployee.getExpertisesId().get(0).getId()).isEqualTo(expertises.get(0));
//    }

//    @Test
//    @DatabaseSetup(value = "/dataset/addNewEmployee_WithValidExpertise.xml", type = DatabaseOperation.CLEAN_INSERT)
//    void addNewEmployee_WithMultipleExpertises_ReturnsCreatedStatus() throws Exception {
//        List<Long> expertises = List.of(expertise1.getId(), expertise2.getId());
//        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
//                .name(EMPLOYEE_NAME)
//                .gender(Gender.FEMALE)
//                .birthDate(LocalDate.of(2001, 8, 15))
//                .graduationDate(LocalDate.of(2025, 8, 15))
//                .team("1")
//                .department("1")
//                .grossSalary(GROSS_SALARY)
//                .expertise(expertises)
//                .build();
//        MvcResult result = mockMvc.perform(post(EMPLOYEE_API)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(inputEmployee)))
//                .andExpect(status().isCreated()).andReturn();
//        MockHttpServletResponse response = result.getResponse();
//        Employee responseToEmployeeEntity = objectMapper.readValue(response.getContentAsString(), Employee.class);
//        assertThat(responseToEmployeeEntity.getId()).isNotNull();
//        Optional<Employee> dbEmployee = employeeRepository.findById(responseToEmployeeEntity.getId());
//        assertThat(dbEmployee).isPresent();
//        Employee actualEmployee = dbEmployee.get();
//        assertThat(actualEmployee.getExpertisesId()
//                .stream()
//                .map(Expertise::getId)
//                .collect(Collectors.toList()))
//                .containsExactlyInAnyOrderElementsOf(expertises);

    /// /        assertThat(actualEmployee.getExpertisesId().get(0).getId()).isEqualTo(expertises.get(0));
    /// /        assertThat(actualEmployee.getExpertisesId().get(1).getId()).isEqualTo(expertises.get(1));
//    }
//
//    @Test
//    public void addNewEmployee_WithExpertiseNotValid_ReturnsNotFoundStatus() throws Exception {
//        String uuidName = unique("unique-name");
//        List<Long> expertises = List.of(878L);
//        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
//                .name(uuidName)
//                .gender(Gender.FEMALE)
//                .birthDate(LocalDate.of(2001, 8, 15))
//                .graduationDate(LocalDate.of(2025, 8, 15))
//                .team("1")
//                .department("1")
//                .grossSalary(GROSS_SALARY)
//                .expertise(expertises)
//                .build();
//        mockMvc.perform(post(EMPLOYEE_API)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(inputEmployee)))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.message").value("Expert Doesn't Exist"));
//        assertThat(employeeRepository.findByName(uuidName)).isEmpty();
//    }
//
//    @Test
//    @DatabaseSetup(value = "/dataset/addNewEmployee_WithManagerId.xml", type = DatabaseOperation.CLEAN_INSERT)
//    public void addNewEmployee_WithManagerId_ReturnsCreatedStatus() throws Exception {
//        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
//                .name(EMPLOYEE_NAME)
//                .gender(Gender.FEMALE)
//                .birthDate(LocalDate.of(2001, 8, 15))
//                .graduationDate(LocalDate.of(2025, 8, 15))
//                .team("1")
//                .department("1")
//                .grossSalary(GROSS_SALARY)
//                .managerId(mockUser.getId())
//                .build();
//        MvcResult result = mockMvc.perform(post(EMPLOYEE_API)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(inputEmployee)))
//                .andExpect(status().isCreated()).andReturn();
//        MockHttpServletResponse response = result.getResponse();
//        Employee responseToEmployeeEntity = objectMapper.readValue(response.getContentAsString(), Employee.class);
//        assertThat(responseToEmployeeEntity.getId()).isNotNull();
//        Optional<Employee> dbEmployee = employeeRepository.findById(responseToEmployeeEntity.getId());
//        assertThat(dbEmployee).isPresent();
//        Employee actualEmployee = dbEmployee.get();
//        assertThat(actualEmployee.getManager().getId()).isEqualTo(inputEmployee.getManagerId());
//    }
//
//    @Test
//    public void addNewEmployee_WithInValidManagerId_ReturnsNotFoundStatus() throws Exception {
//        String uuidName = unique("unique-name");
//        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
//                .name(uuidName)
//                .gender(Gender.FEMALE)
//                .birthDate(LocalDate.of(2001, 8, 15))
//                .graduationDate(LocalDate.of(2025, 8, 15))
//                .team("1")
//                .department("1")
//                .grossSalary(GROSS_SALARY)
//                .managerId(-888L)
//                .build();
//        mockMvc.perform(post(EMPLOYEE_API)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(inputEmployee)))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.message").value("Manager Doesn't Exist"));
//        assertThat(employeeRepository.findByName(uuidName)).isEmpty();
//    }
//
//    //given-arrange when-act then-asset
//    @Test
//    @DatabaseSetup(value = "/dataset/get_employee_info.xml", type = DatabaseOperation.CLEAN_INSERT)
//
//    public void getEmployee_WithValidId_ReturnEmployeeInfo() throws Exception {
//        //given
//        Long employeeId = mockUser.getId();
//        // act
//        MvcResult result = mockMvc.perform(get(EMPLOYEE_API + "/" + employeeId)).andExpect(status().isOk()).andReturn();
//        //assert
//        MockHttpServletResponse response = result.getResponse();
//        Employee responseToEmployeeEntity = objectMapper.readValue(response.getContentAsString(), Employee.class);
//        assertThat(responseToEmployeeEntity.getName()).isEqualTo(mockUser.getName());
//        assertThat(responseToEmployeeEntity.getDepartment()).isEqualTo(mockUser.getDepartment());
//        assertThat(responseToEmployeeEntity.getGrossSalary()).isEqualTo(mockUser.getGrossSalary());
//
//    }
//
//    @Test
//    @DatabaseSetup(value = "/dataset/get_employee_info.xml", type = DatabaseOperation.CLEAN_INSERT)
//    public void getEmployee_WithInvalidId_ReturnNotFoundStatus() throws Exception {
//        //given
//        Long employeeId = 9999L;
//        // act
//        mockMvc.perform(get(EMPLOYEE_API + "/" + employeeId))
//                .andDo(print())
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.message").value("Employee Doesn't Exist"))
//                .andReturn();
//        //assert
//        Optional<Employee> dbEmployee = employeeRepository.findById(employeeId);
//        assertThat(dbEmployee).isEmpty();
//    }
//
//    @Test
//    @DatabaseSetup(value = "/dataset/employee_with_expertises.xml", type = DatabaseOperation.CLEAN_INSERT)
//    public void getEmployee_WithMultipleExpertises_ReturnsAllExpertises() throws Exception {
//
//        Long employeeId = mockUser.getId();
//        // when
//        mockMvc.perform(get(EMPLOYEE_API + "/" + employeeId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.expertisesId").isArray())
//                .andExpect(jsonPath("$.expertisesId.length()").value(2))
//                .andExpect(jsonPath("$.expertisesId[*].id", containsInAnyOrder(10, 20)))
//                .andExpect(jsonPath("$.expertisesId[*].name", containsInAnyOrder(expertise1.getName(), expertise2.getName())));
//
//    }
    private String unique(String name) {
        return name + "-" + UUID.randomUUID();
    }
}

//
//


