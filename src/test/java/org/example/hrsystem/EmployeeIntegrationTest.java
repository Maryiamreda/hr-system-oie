package org.example.hrsystem;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import org.example.hrsystem.Department.Department;
import org.example.hrsystem.Employee.Employee;
import org.example.hrsystem.Employee.dto.EmployeeRequestDTO;
import org.example.hrsystem.Employee.dto.EmployeeResponseDTO;
import org.example.hrsystem.Expertise.Expertise;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private ObjectMapper objectMapper;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private DepartmentRepository departmentRepository;

    private static final String EMPLOYEE_NAME = "maryiam";
    private static final String EMPLOYEE_ROOT_MANAGER_NAME = "ROOT_MANAGER";
    private static final String UNIQUE_DEPARTMENT_NAME = "UNIQUE_DEPARTMENT_NAME";
    private static final String UNIQUE_TEAM_NAME = "UNIQUE_TEAM_NAME";
    private static final Long INVALID_ID = -888L;
    //NON EXISTENCE NAME
    //Department cannot name be empty
    private static final String NON_EXISTENCE_NAME = "NON EXISTENCE NAME";
    private static final String EMPLOYEE_API = "/hr/api/employee";
    private static final BigDecimal GROSS_SALARY = new BigDecimal("5000.00");
    private static final LocalDate DEFAULT_BIRTH_DATE = LocalDate.of(2001, 8, 15);
    private static final LocalDate DEFAULT_GRADUATION_DATE = LocalDate.of(2025, 8, 15);
    private static final String ERROR_EMPLOYEE_NAME_EMPTY = "Employee name cannot be empty";
    private static final String ERROR_SALARY_POSITIVE = "Salary must be positive";
    private static final String ERROR_DEPARTMENT_NOT_EXIST = "Department Doesn't Exist";
    private static final String ERROR_DEPARTMENT_NAME_EMPTY = "Department name cannot be empty";
    private static final String ERROR_MANAGER_NAME_EMPTY = "Employee must have a manager";


    private static final String ERROR_MANAGER_NOT_EXIST = "Manager Doesn't Exist";
    private static final String ERROR_TEAM_NOT_EXIST = "Team Doesn't Exist";
    private static final String ERROR_EXPERT_NOT_EXIST = "Expert Doesn't Exist";
    private static final String ERROR_EMPLOYEE_NOT_EXIST = "Employee Doesn't Exist";
    @BeforeEach
    void setUp() {

    }

    @Test
    @DatabaseSetup(value = "/dataset/addNewEmployee_WithValidDepartment.xml", type = DatabaseOperation.CLEAN_INSERT)
    void addNewEmployee_WithAccurateData_ReturnsCreatedStatus() throws Exception {
        Employee manager = employeeRepository.findByName(EMPLOYEE_ROOT_MANAGER_NAME).get(0);
        //create new inputEmployee object
        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
                .name(EMPLOYEE_NAME)
                .gender(Gender.FEMALE)
                .birthDate(DEFAULT_BIRTH_DATE)
                .graduationDate(DEFAULT_GRADUATION_DATE)
                .departmentName(UNIQUE_DEPARTMENT_NAME)
                .grossSalary(GROSS_SALARY)
                .managerId(manager.getId())
                .build();
        //send post request with the object
        MvcResult result = mockMvc.perform(post(EMPLOYEE_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputEmployee)))
                //expect output is successful
                .andExpect(status().isCreated())
                .andReturn();
        MockHttpServletResponse response = result.getResponse();
        EmployeeResponseDTO responseToEmployeeEntity = objectMapper.readValue(response.getContentAsString(), EmployeeResponseDTO.class);
        assertThat(responseToEmployeeEntity.getId()).isNotNull();
        Optional<Employee> dbEmployee = employeeRepository.findById(responseToEmployeeEntity.getId());
        assertThat(dbEmployee).isPresent();
        Employee actualEmployee = dbEmployee.get();
        assertThat(actualEmployee.getName()).isEqualTo(inputEmployee.getName());
        assertThat(actualEmployee.getDepartment().getName()).isEqualTo(inputEmployee.getDepartmentName());
//        assertThat(actualEmployee.getTeam()).isEqualTo(inputEmployee.getTeam());

    }

    @Test
    @DatabaseSetup(value = "/dataset/addNewEmployee_WithValidDepartment.xml", type = DatabaseOperation.INSERT)
    void addNewEmployee_WithNullName_ReturnsBadRequestStatus() throws Exception {
        Employee manager = employeeRepository.findByName(EMPLOYEE_ROOT_MANAGER_NAME).get(0);
        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
                .name(null)
                .gender(Gender.FEMALE)
                .birthDate(DEFAULT_BIRTH_DATE)
                .graduationDate(DEFAULT_GRADUATION_DATE)
                .departmentName(UNIQUE_DEPARTMENT_NAME)
                .grossSalary(GROSS_SALARY)
                .managerId(manager.getId())
                .build();
        mockMvc.perform(post(EMPLOYEE_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputEmployee)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", containsInAnyOrder(ERROR_EMPLOYEE_NAME_EMPTY)));
        Optional<Department> department = departmentRepository.findByName(UNIQUE_DEPARTMENT_NAME);
        assertThat(department).isPresent();
        assertThat(employeeRepository.findByDepartmentName(UNIQUE_DEPARTMENT_NAME)).isEmpty();
    }

    @Test
    @DatabaseSetup(value = "/dataset/addNewEmployee_WithValidDepartment.xml", type = DatabaseOperation.INSERT)
    void addNewEmployee_WithNegativeSalary_ReturnsBadRequestStatus() throws Exception {
        Employee manager = employeeRepository.findByName(EMPLOYEE_ROOT_MANAGER_NAME).get(0);
        String uuidName = unique("unique-name");
        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
                .name(uuidName)
                .gender(Gender.FEMALE)
                .departmentName(UNIQUE_DEPARTMENT_NAME)
                .grossSalary(new BigDecimal("-123.45"))
                .managerId(manager.getId()).build();
        mockMvc.perform(post(EMPLOYEE_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputEmployee)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", containsInAnyOrder(ERROR_SALARY_POSITIVE)));
        assertThat(employeeRepository.findByName(uuidName)).isEmpty();
    }

    @Test
    @DatabaseSetup(value = "/dataset/addNewEmployee_WithValidDepartment.xml", type = DatabaseOperation.CLEAN_INSERT)
    void addNewEmployee_WithValidDepartment_ReturnsCreatedStatus() throws Exception {
        Employee manager = employeeRepository.findByName(EMPLOYEE_ROOT_MANAGER_NAME).get(0);

        //create new inputEmployee object
        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
                .name(EMPLOYEE_NAME)
                .gender(Gender.FEMALE)
                .departmentName(UNIQUE_DEPARTMENT_NAME)
                .grossSalary(GROSS_SALARY)
                .managerId(manager.getId())
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
    @DatabaseSetup(value = "/dataset/addNewEmployee_WithValidDepartment.xml", type = DatabaseOperation.CLEAN_INSERT)

    void addNewEmployee_WithNullDepartment_ReturnsBadRequestStatus() throws Exception {
        Employee manager = employeeRepository.findByName(EMPLOYEE_ROOT_MANAGER_NAME).get(0);

        String uuidName = unique("unique-name");
        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
                .name(uuidName)
                .gender(Gender.FEMALE)
                .birthDate(DEFAULT_BIRTH_DATE)
                .graduationDate(DEFAULT_GRADUATION_DATE)
                .departmentName(null)
                .grossSalary(GROSS_SALARY)
                .managerId(manager.getId())
                .build();
        mockMvc.perform(post(EMPLOYEE_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputEmployee)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", containsInAnyOrder( ERROR_DEPARTMENT_NAME_EMPTY )));

        assertThat(employeeRepository.findByName(uuidName)).isEmpty();

    }

    @Test
    @DatabaseSetup(value = "/dataset/addNewEmployee_WithValidDepartment.xml", type = DatabaseOperation.CLEAN_INSERT)
    void addNewEmployee_WithNonExistenceDepartmentName_ReturnsNotFoundStatus() throws Exception {
        Employee manager = employeeRepository.findByName(EMPLOYEE_ROOT_MANAGER_NAME).get(0);
        String uuidName = unique("unique-name");
        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
                .name(uuidName)
                .gender(Gender.FEMALE)
                .birthDate(DEFAULT_BIRTH_DATE)
                .graduationDate(DEFAULT_GRADUATION_DATE)
                .departmentName(NON_EXISTENCE_NAME)
                .grossSalary(GROSS_SALARY)
                .managerId(manager.getId())
                .build();
        mockMvc.perform(post(EMPLOYEE_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputEmployee)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ERROR_DEPARTMENT_NOT_EXIST));
        assertThat(employeeRepository.findByName(uuidName)).isEmpty();
    }

    @Test
    @DatabaseSetup(value = "/dataset/addNewEmployee_WithValidExpertise.xml", type = DatabaseOperation.CLEAN_INSERT)
    void addNewEmployee_WithValidExpertise_ReturnsCreatedStatus() throws Exception {
        Employee manager = employeeRepository.findByName(EMPLOYEE_ROOT_MANAGER_NAME).get(0);
        List<String> expertises = List.of("OOP");
        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
                .name(EMPLOYEE_NAME)
                .gender(Gender.FEMALE)
                .birthDate(DEFAULT_BIRTH_DATE)
                .graduationDate(DEFAULT_GRADUATION_DATE)
                .departmentName(UNIQUE_DEPARTMENT_NAME)
                .grossSalary(GROSS_SALARY)
                .expertise(expertises)
                .managerId(manager.getId())
                .build();
        MvcResult result = mockMvc.perform(post(EMPLOYEE_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputEmployee)))
                .andExpect(status().isCreated()).andReturn();
        MockHttpServletResponse response = result.getResponse();
        Employee responseToEmployeeEntity = objectMapper.readValue(response.getContentAsString(), Employee.class);
        assertThat(responseToEmployeeEntity.getId()).isNotNull();
        Optional<Employee> dbEmployee = employeeRepository.findById(responseToEmployeeEntity.getId());
        assertThat(dbEmployee).isPresent();
        Employee actualEmployee = dbEmployee.get();
        assertThat(actualEmployee.getExpertises()).hasSize(1);
        assertThat(actualEmployee.getExpertises().get(0).getName()).isEqualTo(expertises.get(0));
    }

    @Test
    @DatabaseSetup(value = "/dataset/addNewEmployee_WithValidExpertise.xml", type = DatabaseOperation.CLEAN_INSERT)
    void addNewEmployee_WithMultipleExpertises_ReturnsCreatedStatus() throws Exception {
        Employee manager = employeeRepository.findByName(EMPLOYEE_ROOT_MANAGER_NAME).get(0);

        List<String> expertises = List.of("OOP", "Java");
        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
                .name(EMPLOYEE_NAME)
                .gender(Gender.FEMALE)
                .birthDate(DEFAULT_BIRTH_DATE)
                .graduationDate(DEFAULT_GRADUATION_DATE)
                .departmentName(UNIQUE_DEPARTMENT_NAME)
                .grossSalary(GROSS_SALARY)
                .managerId(manager.getId())
                .expertise(expertises)
                .build();
        MvcResult result = mockMvc.perform(post(EMPLOYEE_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputEmployee)))
                .andDo(print())
                .andExpect(status().isCreated()).andReturn();
        MockHttpServletResponse response = result.getResponse();
        Employee responseToEmployeeEntity = objectMapper.readValue(response.getContentAsString(), Employee.class);
        assertThat(responseToEmployeeEntity.getId()).isNotNull();
        Optional<Employee> dbEmployee = employeeRepository.findById(responseToEmployeeEntity.getId());
        assertThat(dbEmployee).isPresent();
        List<Expertise> actualEmployeeExpertises = dbEmployee.get().getExpertises();
        List<String> actualEmployeeExpertisesNames = new ArrayList<>();
        for (Expertise actualEmployeeExpertise : actualEmployeeExpertises) {
            actualEmployeeExpertisesNames.add(actualEmployeeExpertise.getName());
        }
        assertThat(actualEmployeeExpertisesNames) .containsExactlyInAnyOrderElementsOf(expertises);
    }

        @Test
        @DatabaseSetup(value = "/dataset/addNewEmployee_WithValidExpertise.xml", type = DatabaseOperation.CLEAN_INSERT)
        public void addNewEmployee_WithExpertiseNotValid_ReturnsNotFoundStatus() throws Exception {
            Employee manager = employeeRepository.findByName(EMPLOYEE_ROOT_MANAGER_NAME).get(0);

            String uuidName = unique("unique-name");
        List<String> expertises = List.of("non existence expertise ");
        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
                .name(uuidName)
                .gender(Gender.FEMALE)
                .birthDate(DEFAULT_BIRTH_DATE)
                .graduationDate(DEFAULT_GRADUATION_DATE)
                .departmentName(UNIQUE_DEPARTMENT_NAME)
                .grossSalary(GROSS_SALARY)
                .expertise(expertises)
                .managerId(manager.getId())
                .build();
        mockMvc.perform(post(EMPLOYEE_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputEmployee)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ERROR_EXPERT_NOT_EXIST));
        assertThat(employeeRepository.findByName(uuidName)).isEmpty();
    }

    @Test
    @DatabaseSetup(value = "/dataset/addNewEmployee_WithManagerId.xml", type = DatabaseOperation.CLEAN_INSERT)
    public void addNewEmployee_WithManagerId_ReturnsCreatedStatus() throws Exception {
        Employee manager = employeeRepository.findByName(EMPLOYEE_ROOT_MANAGER_NAME).get(0);
        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
                .name(EMPLOYEE_NAME)
                .gender(Gender.FEMALE)
                .birthDate(DEFAULT_BIRTH_DATE)
                .graduationDate(DEFAULT_GRADUATION_DATE)
                .departmentName(UNIQUE_DEPARTMENT_NAME)
                .grossSalary(GROSS_SALARY)
                .managerId(manager.getId())
                .build();
        MvcResult result = mockMvc.perform(post(EMPLOYEE_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputEmployee)))
                .andExpect(status().isCreated()).andReturn();
        MockHttpServletResponse response = result.getResponse();
        EmployeeResponseDTO responseToEmployeeEntity = objectMapper.readValue(response.getContentAsString(), EmployeeResponseDTO.class);
        assertThat(responseToEmployeeEntity.getId()).isNotNull();
        Optional<Employee> dbEmployee = employeeRepository.findById(responseToEmployeeEntity.getId());
        assertThat(dbEmployee).isPresent();
        Employee actualEmployee = dbEmployee.get();
        assertThat(actualEmployee.getManager().getId()).isEqualTo(inputEmployee.getManagerId());
    }

    @Test
    @DatabaseSetup(value = "/dataset/addNewEmployee_WithManagerId.xml", type = DatabaseOperation.CLEAN_INSERT)
    public void addNewEmployee_WithInValidManagerId_ReturnsNotFoundStatus() throws Exception {
        String uuidName = unique("unique-name");
        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
                .name(uuidName)
                .gender(Gender.FEMALE)
                .birthDate(DEFAULT_BIRTH_DATE)
                .graduationDate(DEFAULT_GRADUATION_DATE)
                .departmentName(UNIQUE_DEPARTMENT_NAME)
                .grossSalary(GROSS_SALARY)
                .managerId(INVALID_ID)
                .build();
        mockMvc.perform(post(EMPLOYEE_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputEmployee)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ERROR_MANAGER_NOT_EXIST));
        assertThat(employeeRepository.findByName(uuidName)).isEmpty();
    }
    @Test
    @DatabaseSetup(value = "/dataset/addNewEmployee_WithManagerId.xml", type = DatabaseOperation.CLEAN_INSERT)
    public void addNewEmployee_WithNullManagerId_ReturnsBadRequestStatus() throws Exception {
        String uuidName = unique("unique-name");
        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
                .name(uuidName)
                .gender(Gender.FEMALE)
                .birthDate(DEFAULT_BIRTH_DATE)
                .graduationDate(DEFAULT_GRADUATION_DATE)
                .departmentName(UNIQUE_DEPARTMENT_NAME)
                .grossSalary(GROSS_SALARY)
                .managerId(null)
                .build();
        mockMvc.perform(post(EMPLOYEE_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputEmployee)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$", containsInAnyOrder( ERROR_MANAGER_NAME_EMPTY )));

        assertThat(employeeRepository.findByName(uuidName)).isEmpty();
    }
    @Test
    @DatabaseSetup(value = "/dataset/addNewEmployee_WithValidTeam.xml", type = DatabaseOperation.CLEAN_INSERT)
    void addNewEmployee_WithValidTeam_ReturnsCreatedStatus() throws Exception {
        Employee manager = employeeRepository.findByName(EMPLOYEE_ROOT_MANAGER_NAME).get(0);
        //create new inputEmployee object
        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
                .name(EMPLOYEE_NAME)
                .gender(Gender.FEMALE)
                .birthDate(DEFAULT_BIRTH_DATE)
                .graduationDate(DEFAULT_GRADUATION_DATE)
                .departmentName(UNIQUE_DEPARTMENT_NAME)
                .teamName(UNIQUE_TEAM_NAME)
                .grossSalary(GROSS_SALARY)
                .managerId(manager.getId())
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
        assertThat(actualEmployee.getTeam().getName()).isEqualTo(UNIQUE_TEAM_NAME);

    }

    @Test
    @DatabaseSetup(value = "/dataset/addNewEmployee_WithValidDepartment.xml", type = DatabaseOperation.CLEAN_INSERT)
    void addNewEmployee_WithNonExistenceTeamName_ReturnsNotFoundStatus() throws Exception {
        Employee manager = employeeRepository.findByName(EMPLOYEE_ROOT_MANAGER_NAME).get(0);
        String inValidIdTeamName = "NON EXISTENCE NAME ";
        String uuidName = unique("unique-name");
        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
                .name(uuidName)
                .gender(Gender.FEMALE)
                .birthDate(DEFAULT_BIRTH_DATE)
                .graduationDate(DEFAULT_GRADUATION_DATE)
                .departmentName(UNIQUE_DEPARTMENT_NAME)
                .teamName(inValidIdTeamName)
                .grossSalary(GROSS_SALARY)
                .managerId(manager.getId())
                .build();
        mockMvc.perform(post(EMPLOYEE_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputEmployee)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ERROR_TEAM_NOT_EXIST));
        assertThat(employeeRepository.findByName(uuidName)).isEmpty();

    }
    //given-arrange when-act then-asset
    @Test
    @DatabaseSetup(value = "/dataset/get_employee_info.xml", type = DatabaseOperation.CLEAN_INSERT)
    public void getEmployee_WithValidId_ReturnEmployeeInfo() throws Exception {
        //given
        Employee ExpectedEmployee=  employeeRepository.findByName(EMPLOYEE_ROOT_MANAGER_NAME).get(0);
        // act
        MvcResult result = mockMvc.perform(get(EMPLOYEE_API + "/" + ExpectedEmployee.getId())).andExpect(status().isOk()).andReturn();
        //assert
        MockHttpServletResponse response = result.getResponse();
        EmployeeResponseDTO responseToEmployeeEntity = objectMapper.readValue(response.getContentAsString(), EmployeeResponseDTO.class);
        assertThat(responseToEmployeeEntity.getName()).isEqualTo(ExpectedEmployee.getName());
        assertThat(responseToEmployeeEntity.getDepartmentName()).isEqualTo(ExpectedEmployee.getDepartment().getName());
        assertThat(responseToEmployeeEntity.getTeamName()).isEqualTo(ExpectedEmployee.getTeam().getName());

    }

    @Test
    @DatabaseSetup(value = "/dataset/get_employee_info.xml", type = DatabaseOperation.CLEAN_INSERT)
    public void getEmployee_WithInvalidId_ReturnNotFoundStatus() throws Exception {

        mockMvc.perform(get(EMPLOYEE_API + "/" + INVALID_ID))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ERROR_EMPLOYEE_NOT_EXIST))
                .andReturn();
    }

    @Test
    @DatabaseSetup(value = "/dataset/employee_with_expertises.xml", type = DatabaseOperation.CLEAN_INSERT)
    public void getEmployee_WithMultipleExpertises_ReturnsAllExpertises() throws Exception {

        Employee ExpectedEmployee=  employeeRepository.findByName(EMPLOYEE_ROOT_MANAGER_NAME).get(0);
        // when
        mockMvc.perform(get(EMPLOYEE_API + "/" + ExpectedEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expertises").isArray())
                .andExpect(jsonPath("$.expertises.length()").value(2))
                .andExpect(jsonPath("$.expertises[*].name", containsInAnyOrder("Java","OOP")));

    }
    private String unique(String name) {
        return name + "-" + UUID.randomUUID();
    }
}

//
//


