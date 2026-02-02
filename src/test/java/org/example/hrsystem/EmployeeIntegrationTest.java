package org.example.hrsystem;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.example.hrsystem.Department.Department;
import org.example.hrsystem.Employee.Employee;
import org.example.hrsystem.Employee.dto.EmployeeRequestDTO;
import org.example.hrsystem.Employee.dto.EmployeeResponseDTO;
import org.example.hrsystem.Employee.dto.EmployeeSalaryInfoDTO;
import org.example.hrsystem.Expertise.Expertise;
import org.example.hrsystem.enums.Gender;
import org.example.hrsystem.Employee.EmployeeRepository;
import org.example.hrsystem.Department.DepartmentRepository;


import org.example.hrsystem.utilities.SalaryCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

import static org.example.hrsystem.utilities.EmployeeMessageConstants.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;

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
    @Autowired
    private SalaryCalculator salaryCalculator;
    private static final String EMPLOYEE_NAME = "maryiam";
    private static final String EMPLOYEE_ROOT_MANAGER_NAME = "ROOT_MANAGER";

    //UNIQUE_EMPLOYEE_NAME_UPDATE
    private static final String UNIQUE_DEPARTMENT_NAME = "UNIQUE_DEPARTMENT_NAME";
    private static final String UNIQUE_EMPLOYEE_NAME_UPDATE = "UNIQUE_EMPLOYEE_NAME_UPDATE";
    private static final String UNIQUE_EMPLOYEE_NAME_DELETE = "UNIQUE_EMPLOYEE_NAME_DELETE";
    private static final String UNIQUE_MANAGER_NAME_DELETE = "MANAGER";

    private static final String UNIQUE_TEAM_NAME = "UNIQUE_TEAM_NAME";
    private static final Long NONVALID_ID = -888L;
    private static final String NONVALID_TEAM_NAME = "NONVALID_TEAM_NAME";

    //NON EXISTENCE NAME
    //Department cannot name be empty
    private static final String NON_EXISTENCE_NAME = "NON EXISTENCE NAME";
    private static final String EMPLOYEE_API = "/hr/api/employee";
    private static final BigDecimal GROSS_SALARY = new BigDecimal("5000.00");
    private static final LocalDate DEFAULT_BIRTH_DATE = LocalDate.of(2001, 8, 15);
    private static final LocalDate DEFAULT_GRADUATION_DATE = LocalDate.of(2025, 8, 15);
    private static final int DEFAULT_PAGE_NUMBER = 0;
    private static final int DEFAULT_PAGE_SIZE = 6;

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
    @DatabaseSetup(value = "/dataset/addNewEmployee_WithValidDepartment.xml")
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
    @DatabaseSetup(value = "/dataset/addNewEmployee_WithValidDepartment.xml")
    void addNewEmployee_WithNegativeSalary_ReturnsBadRequestStatus() throws Exception {
        Employee manager = employeeRepository.findByName(EMPLOYEE_ROOT_MANAGER_NAME).get(0);
        String uuidName = appendUUidToString("unique-name");
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

        String uuidName = appendUUidToString("unique-name");
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
                .andExpect(jsonPath("$", containsInAnyOrder(ERROR_DEPARTMENT_NAME_EMPTY)));

        assertThat(employeeRepository.findByName(uuidName)).isEmpty();

    }

    @Test
    @DatabaseSetup(value = "/dataset/addNewEmployee_WithValidDepartment.xml")
    void addNewEmployee_WithNonValidDepartmentName_ReturnsBadRequestStatus() throws Exception {
        Employee manager = employeeRepository.findByName(EMPLOYEE_ROOT_MANAGER_NAME).get(0);
        String uuidName = appendUUidToString("unique-name");
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
                .andExpect(status().isBadRequest())
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
    @DatabaseSetup(value = "/dataset/addNewEmployee_WithValidExpertise.xml")
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
        List<String> actualEmployeeExpertisesNames = actualEmployeeExpertises.stream().map(Expertise::getName).toList();
        assertThat(actualEmployeeExpertisesNames).containsAll(expertises);
    }

    @Test
    @DatabaseSetup(value = "/dataset/addNewEmployee_WithValidExpertise.xml")
    public void addNewEmployee_WithNonValidExpertise_ReturnsBadRequestStatus() throws Exception {
        Employee manager = employeeRepository.findByName(EMPLOYEE_ROOT_MANAGER_NAME).get(0);

        String uuidName = appendUUidToString("unique-name");
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
                .andExpect(status().isBadRequest())
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
    @DatabaseSetup(value = "/dataset/addNewEmployee_WithManagerId.xml")
    public void addNewEmployee_WithNonValidManagerId_ReturnsBadRequestStatus() throws Exception {
        String uuidName = appendUUidToString("unique-name");
        EmployeeRequestDTO inputEmployee = EmployeeRequestDTO.builder()
                .name(uuidName)
                .gender(Gender.FEMALE)
                .birthDate(DEFAULT_BIRTH_DATE)
                .graduationDate(DEFAULT_GRADUATION_DATE)
                .departmentName(UNIQUE_DEPARTMENT_NAME)
                .grossSalary(GROSS_SALARY)
                .managerId(NONVALID_ID)
                .build();
        mockMvc.perform(post(EMPLOYEE_API)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputEmployee)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ERROR_MANAGER_NOT_EXIST));
        assertThat(employeeRepository.findByName(uuidName)).isEmpty();
    }

    @Test
    @DatabaseSetup(value = "/dataset/addNewEmployee_WithManagerId.xml")
    public void addNewEmployee_WithNullManagerId_ReturnsBadRequestStatus() throws Exception {
        String uuidName = appendUUidToString("unique-name");
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
                .andExpect(jsonPath("$", containsInAnyOrder(ERROR_MANAGER_NAME_EMPTY)));

        assertThat(employeeRepository.findByName(uuidName)).isEmpty();
    }

    @Test
    @DatabaseSetup(value = "/dataset/addNewEmployee_WithValidTeam.xml")
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
    @DatabaseSetup(value = "/dataset/addNewEmployee_WithValidDepartment.xml")
    void addNewEmployee_WithNonValidTeamName_ReturnsBadRequestStatus() throws Exception {
        Employee manager = employeeRepository.findByName(EMPLOYEE_ROOT_MANAGER_NAME).get(0);
        String inValidIdTeamName = "NON EXISTENCE NAME ";
        String uuidName = appendUUidToString("unique-name");
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ERROR_TEAM_NOT_EXIST));
        assertThat(employeeRepository.findByName(uuidName)).isEmpty();

    }

    //given-arrange when-act then-asset
    @Test
    @DatabaseSetup(value = "/dataset/get_employee_info.xml")
    public void getEmployee_WithValidId_ReturnEmployeeInfo() throws Exception {
        //given
        Employee ExpectedEmployee = employeeRepository.findByName(EMPLOYEE_ROOT_MANAGER_NAME).get(0);
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
    @DatabaseSetup(value = "/dataset/get_employee_info.xml")
    public void getSalaryInfo_WithValidEmployeeIdAnd15PercentTaxRateAnd500FixedReduction_ReturnCorrectEmployeeSalary() throws Exception {
        //given
        Employee ExpectedEmployee = employeeRepository.findByName(EMPLOYEE_ROOT_MANAGER_NAME).get(0);
        //net Salary= gross Salary - (Gross Salary * 0.15) - 500
        BigDecimal netSalary = new BigDecimal("3750.00");
        // act
        MvcResult result = mockMvc.perform(get(EMPLOYEE_API + "/" + ExpectedEmployee.getId() + "/salary")).andExpect(status().isOk()).andReturn();
        //assert
        MockHttpServletResponse response = result.getResponse();
        EmployeeSalaryInfoDTO employeeSalaryInfoDTO = objectMapper.readValue(response.getContentAsString(), EmployeeSalaryInfoDTO.class);
        assertThat(employeeSalaryInfoDTO.getGrossSalary()).isEqualTo(ExpectedEmployee.getGrossSalary());
        assertThat(employeeSalaryInfoDTO.getNetSalary()).isEqualTo(netSalary);

    }

    @Test
    @DatabaseSetup(value = "/dataset/get_employee_info.xml")
    public void getSalaryInfo_WithNonValidEmployeeId_ReturnsNotFoundStatus() throws Exception {
        mockMvc.perform(get(EMPLOYEE_API + "/" + NONVALID_ID + "/salary"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ERROR_EMPLOYEE_NOT_EXIST))
                .andReturn();
    }

    @Test
    @DatabaseSetup(value = "/dataset/get_employee_info.xml")
    public void getEmployee_WithNonValidId_ReturnsBadRequestStatus() throws Exception {

        mockMvc.perform(get(EMPLOYEE_API + "/" + NONVALID_ID))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ERROR_EMPLOYEE_NOT_EXIST))
                .andReturn();
    }

    @Test
    @DatabaseSetup(value = "/dataset/employee_with_expertises.xml")
    public void getEmployee_WithMultipleExpertises_ReturnsAllExpertises() throws Exception {

        Employee ExpectedEmployee = employeeRepository.findByName(EMPLOYEE_ROOT_MANAGER_NAME).get(0);
        // when
        mockMvc.perform(get(EMPLOYEE_API + "/" + ExpectedEmployee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expertises").isArray())
                .andExpect(jsonPath("$.expertises.length()").value(2))
                .andExpect(jsonPath("$.expertises[*].name", containsInAnyOrder("Java", "OOP")));

    }

    @Test
    @DatabaseSetup(value = "/dataset/getEmployeesFromTeam.xml")
    public void getEmployeesFromTeam_WithValidTeamName_ReturnOkStatus() throws Exception {

        Pageable pageable = PageRequest.of(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE);

        Page<Employee> dBTeamEmployeeList = employeeRepository.findByTeamName(UNIQUE_TEAM_NAME, pageable);
        List<Long> dBTeamEmployeeListIds = dBTeamEmployeeList.stream().map(Employee::getId).toList();
        //UNIQUE_TEAM_NAME
        MvcResult result = mockMvc.perform(get(EMPLOYEE_API)
                        .param("teamName", UNIQUE_TEAM_NAME)
                        .param("page", String.valueOf(DEFAULT_PAGE_NUMBER))
                        .param("size", String.valueOf(DEFAULT_PAGE_SIZE))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(DEFAULT_PAGE_SIZE))
                .andExpect(jsonPath("$.numberOfElements").value(dBTeamEmployeeListIds.size()))
                .andExpect(jsonPath("$.number").value(DEFAULT_PAGE_NUMBER))
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        JSONArray contentListJson = JsonPath.read(response.getContentAsString(), "$.content");
        List<EmployeeResponseDTO> responseTeamEmployeeList = objectMapper.readValue(contentListJson.toJSONString(), new TypeReference<>() {});
        assertThat(responseTeamEmployeeList).isNotNull();
        List<Long> responseTeamEmployeeListIds = responseTeamEmployeeList.stream().map(EmployeeResponseDTO::getId).toList();
        assertThat(responseTeamEmployeeListIds.size()).isEqualTo(dBTeamEmployeeListIds.size());
        assertThat(dBTeamEmployeeListIds).containsAll(responseTeamEmployeeListIds);


    }

    @Test
    @DatabaseSetup(value = "/dataset/getEmployeesFromTeam.xml")
    void getEmployeesFromTeam_WithNonValidTeamName_ReturnsOkStatusWithEmptyContent() throws Exception {
        mockMvc.perform(get(EMPLOYEE_API).param("teamName", NONVALID_TEAM_NAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andReturn();
    }

    @Test
    @DatabaseSetup(value = "/dataset/employeeHierarchy.xml")
    public void getDirectSubordinates_WithValidManager_ReturnsOkStatusWithDirectSubordinates() throws Exception {
        Employee manager = employeeRepository.findByName(UNIQUE_MANAGER_NAME_DELETE).get(0);

        List<Employee> dBTeamEmployeeList = employeeRepository.findByManager(manager);
        List<Long> dBTeamEmployeeListIds = dBTeamEmployeeList.stream().map(Employee::getId).toList();

        MvcResult result = mockMvc.perform(
                        get(EMPLOYEE_API + "/manager/" + manager.getId() + "/subordinates")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        List<EmployeeResponseDTO> EmployeeResponseDTOList = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});
        List<Long> expectedIds = EmployeeResponseDTOList.stream()
                .map(EmployeeResponseDTO::getId)
                .toList();
        assertThat(expectedIds.size()).isEqualTo(dBTeamEmployeeListIds.size());
        assertThat(expectedIds).containsAll(dBTeamEmployeeListIds);

    }
    @Test
    public void getDirectSubordinates_WithNonExistentManager_ReturnsNotFoundStatus() throws Exception {
        mockMvc.perform(
                        get(EMPLOYEE_API + "/manager/"+NONVALID_ID+"/subordinates")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ERROR_MANAGER_NOT_EXIST));
    }

    @Test
    @DatabaseSetup(value = "/dataset/updateEmployee_WithValidData.xml")
    void updateEmployee_WithValidData_ReturnsOkStatus() throws Exception {
        Employee employeeToUpdate = employeeRepository.findByName(UNIQUE_EMPLOYEE_NAME_UPDATE).get(0);
        Employee manager = employeeRepository.findByName(EMPLOYEE_ROOT_MANAGER_NAME).get(0);

        String updatedName = appendUUidToString("updated-employee");
        BigDecimal updatedSalary = new BigDecimal("7000.00");
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("name", updatedName);
        bodyMap.put("grossSalary", updatedSalary);
        bodyMap.put("departmentName", UNIQUE_DEPARTMENT_NAME);
        bodyMap.put("teamName", UNIQUE_TEAM_NAME);
        bodyMap.put("managerId", manager.getId());

        mockMvc.perform(patch(EMPLOYEE_API + "/" + employeeToUpdate.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .contentType("application/merge-patch+json")
                        .content(objectMapper.writeValueAsString(bodyMap))
                )
                .andExpect(status().isOk())
                .andExpect(content().string(SUCCESS_EMPLOYEE_DATA_UPDATED));


        Optional<Employee> dbEmployee = employeeRepository.findById(employeeToUpdate.getId());
        assertThat(dbEmployee).isPresent();
        Employee actualEmployee = dbEmployee.get();
        assertThat(actualEmployee.getName()).isEqualTo(updatedName);
        assertThat(actualEmployee.getManager().getId()).isEqualTo(manager.getId());
        assertThat(actualEmployee.getManager().getName()).isEqualTo(EMPLOYEE_ROOT_MANAGER_NAME);
        assertThat(actualEmployee.getTeam().getName()).isEqualTo(UNIQUE_TEAM_NAME);
        assertThat(actualEmployee.getDepartment().getName()).isEqualTo(UNIQUE_DEPARTMENT_NAME);
        //assert that other data havent changed
        assertThat(actualEmployee.getGender()).isEqualTo(employeeToUpdate.getGender());
        assertThat(actualEmployee.getBirthDate()).isEqualTo(employeeToUpdate.getBirthDate());
        assertThat(actualEmployee.getGraduationDate()).isEqualTo(employeeToUpdate.getGraduationDate());


    }

    @Test
    @DatabaseSetup(value = "/dataset/updateEmployee_WithValidData.xml")
    public void updateEmployee_WithNonValidEmployeeId_ReturnsBadRequestStatus() throws Exception {

        String updatedName = appendUUidToString("updated-employee");
        EmployeeRequestDTO updateRequestData = EmployeeRequestDTO.builder()
                .name(updatedName)
                .build();

        mockMvc.perform(patch(EMPLOYEE_API + "/" + NONVALID_ID)
                        .contentType("application/merge-patch+json")
                        .content(objectMapper.writeValueAsString(updateRequestData)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ERROR_EMPLOYEE_NOT_EXIST));
        //MAKE SURE NOTHING IN THE DB GOT UPDATED WITH THE UNIQUE NAME
        assertThat(employeeRepository.findByName(updatedName)).isEmpty();
    }


    @Test
    @DatabaseSetup(value = "/dataset/updateEmployee_WithValidData.xml")
    public void updateEmployee_WithNonValidManagerId_ReturnsBadRequestStatus() throws Exception {
        Employee employeeToUpdate = employeeRepository.findByName(UNIQUE_EMPLOYEE_NAME_UPDATE).get(0);
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("managerId", NONVALID_ID);
        mockMvc.perform(patch(EMPLOYEE_API + "/" + employeeToUpdate.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .contentType("application/merge-patch+json")
                        .content(objectMapper.writeValueAsString(bodyMap)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ERROR_MANAGER_NOT_EXIST));
        Optional<Employee> dbEmployee = employeeRepository.findById(employeeToUpdate.getId());
        assertThat(dbEmployee).isPresent();
        Employee actualEmployee = dbEmployee.get();
        System.out.println();
        //MAKE SURE THE OG MANAGER HASN'T GOT UPDATED
        assertThat(actualEmployee.getManager().getId()).isEqualTo(employeeToUpdate.getManager().getId());
    }


    @Test
    @DatabaseSetup(value = "/dataset/updateEmployee_WithValidData.xml")
    void updateEmployee_WithNonValidTeamName_ReturnsBadRequestStatus() throws Exception {
        Employee employeeToUpdate = employeeRepository.findByName(UNIQUE_EMPLOYEE_NAME_UPDATE).get(0);
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("teamName", NON_EXISTENCE_NAME);

        mockMvc.perform(patch(EMPLOYEE_API + "/" + employeeToUpdate.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .contentType("application/merge-patch+json")

                        .content(objectMapper.writeValueAsString(bodyMap)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ERROR_TEAM_NOT_EXIST));
        Optional<Employee> dbEmployee = employeeRepository.findById(employeeToUpdate.getId());
        assertThat(dbEmployee).isPresent();
        Employee actualEmployee = dbEmployee.get();
        assertThat(actualEmployee.getTeam().getName()).isEqualTo(employeeToUpdate.getTeam().getName());

    }


    @Test
    @DatabaseSetup(value = "/dataset/updateEmployee_WithValidData.xml")
    void updateEmployee_WithNonValidDepartmentName_ReturnsBadRequestStatus() throws Exception {
        Employee employeeToUpdate = employeeRepository.findByName(UNIQUE_EMPLOYEE_NAME_UPDATE).get(0);
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("departmentName", NON_EXISTENCE_NAME);

        mockMvc.perform(patch(EMPLOYEE_API + "/" + employeeToUpdate.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .contentType("application/merge-patch+json")
                        .content(objectMapper.writeValueAsString(bodyMap)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ERROR_DEPARTMENT_NOT_EXIST));
        Optional<Employee> dbEmployee = employeeRepository.findById(employeeToUpdate.getId());
        assertThat(dbEmployee).isPresent();
        Employee actualEmployee = dbEmployee.get();
        assertThat(actualEmployee.getDepartment().getName()).isEqualTo(employeeToUpdate.getDepartment().getName());

    }

    @Test
    @DatabaseSetup(value = "/dataset/updateEmployee_WithValidData.xml")
    void updateEmployee_WithNewExpertise_ReturnsOkStatus() throws Exception {
        Employee employeeToUpdate = employeeRepository.findByName(UNIQUE_EMPLOYEE_NAME_UPDATE).get(0);
        List<String> expertises = List.of("OOP");
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("expertise", expertises);

        mockMvc.perform(patch(EMPLOYEE_API + "/" + employeeToUpdate.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .contentType("application/merge-patch+json")
                        .content(objectMapper.writeValueAsString(bodyMap)))
                .andDo(print())
                .andExpect(status().isOk());
        Optional<Employee> dbEmployee = employeeRepository.findById(employeeToUpdate.getId());
        assertThat(dbEmployee).isPresent();
        List<Expertise> actualEmployeeExpertises = dbEmployee.get().getExpertises();
        List<String> actualEmployeeExpertisesNames = new ArrayList<>();
        for (Expertise actualEmployeeExpertise : actualEmployeeExpertises) {
            actualEmployeeExpertisesNames.add(actualEmployeeExpertise.getName());
        }
        //this fail if there is another expertits in the list
        assertThat(actualEmployeeExpertisesNames).contains(expertises.get(0));
        //should assert that other expertises employee had still exists
        assertThat(actualEmployeeExpertises).containsAll(employeeToUpdate.getExpertises());

    }

    // test for changing valid fields to null
    @Test
    @DatabaseSetup(value = "/dataset/updateEmployee_WithValidData.xml")
    void updateEmployee_WithValidNullFields_ReturnOkStatus() throws Exception {
        Employee employeeToUpdate = employeeRepository.findByName(UNIQUE_EMPLOYEE_NAME_UPDATE).get(0);
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("gender", null);
        bodyMap.put("birthDate", null);
        bodyMap.put("graduationDate", null);

        mockMvc.perform(patch(EMPLOYEE_API + "/" + employeeToUpdate.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .contentType("application/merge-patch+json")
                        .content(objectMapper.writeValueAsString(bodyMap)))
                .andDo(print())
                .andExpect(status().isOk())

                .andExpect(content().string(SUCCESS_EMPLOYEE_DATA_UPDATED));
        Optional<Employee> dbEmployee = employeeRepository.findById(employeeToUpdate.getId());
        assertThat(dbEmployee).isPresent();
        Employee actualEmployee = dbEmployee.get();
        assertThat(actualEmployee.getGender()).isNull();
        assertThat(actualEmployee.getBirthDate()).isNull();
        assertThat(actualEmployee.getGraduationDate()).isNull();

    }

    // test for changing fields to null that must have value
    @Test
    @DatabaseSetup(value = "/dataset/updateEmployee_WithValidData.xml")
    void updateEmployee_WithNullDepartment_ReturnsBadRequestStatus() throws Exception {
        Employee employeeToUpdate = employeeRepository.findByName(UNIQUE_EMPLOYEE_NAME_UPDATE).get(0);
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("departmentName", null);


        mockMvc.perform(patch(EMPLOYEE_API + "/" + employeeToUpdate.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .contentType("application/merge-patch+json")
                        .content(objectMapper.writeValueAsString(bodyMap)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ERROR_DEPARTMENT_NAME_EMPTY));
        Optional<Employee> dbEmployee = employeeRepository.findById(employeeToUpdate.getId());
        assertThat(dbEmployee).isPresent();
        Employee actualEmployee = dbEmployee.get();
        assertThat(actualEmployee.getDepartment().getName()).isEqualTo(employeeToUpdate.getDepartment().getName());
    }

    @Test
    @DatabaseSetup(value = "/dataset/updateEmployee_WithValidData.xml")
    void updateEmployee_WithNullManager_ReturnsBadRequestStatus() throws Exception {
        Employee employeeToUpdate = employeeRepository.findByName(UNIQUE_EMPLOYEE_NAME_UPDATE).get(0);
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("managerId", null);

        mockMvc.perform(patch(EMPLOYEE_API + "/" + employeeToUpdate.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .contentType("application/merge-patch+json")
                        .content(objectMapper.writeValueAsString(bodyMap)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ERROR_MANAGER_NAME_EMPTY));

        Optional<Employee> dbEmployee = employeeRepository.findById(employeeToUpdate.getId());
        assertThat(dbEmployee).isPresent();
        Employee actualEmployee = dbEmployee.get();
        assertThat(actualEmployee.getManager().getId()).isEqualTo(employeeToUpdate.getManager().getId());
        assertThat(actualEmployee.getManager().getName()).isEqualTo(employeeToUpdate.getManager().getName());
    }

    @Test
    @DatabaseSetup(value = "/dataset/updateEmployee_WithValidData.xml")
    void updateEmployee_WithNullTeam_ReturnsBadRequestStatus() throws Exception {
        Employee employeeToUpdate = employeeRepository.findByName(UNIQUE_EMPLOYEE_NAME_UPDATE).get(0);
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("teamName", null);

        mockMvc.perform(patch(EMPLOYEE_API + "/" + employeeToUpdate.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .contentType("application/merge-patch+json")
                        .content(objectMapper.writeValueAsString(bodyMap)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ERROR_TEAM_NAME_EMPTY));

        Optional<Employee> dbEmployee = employeeRepository.findById(employeeToUpdate.getId());
        assertThat(dbEmployee).isPresent();
        Employee actualEmployee = dbEmployee.get();
        assertThat(actualEmployee.getManager().getId()).isEqualTo(employeeToUpdate.getManager().getId());
        assertThat(actualEmployee.getTeam().getName()).isEqualTo(employeeToUpdate.getTeam().getName());
    }

    @Test
    @DatabaseSetup(value = "/dataset/deleteEmployee.xml")
    void deleteNonManagerEmployee_WithValidId_ReturnOkStatus() throws Exception {
        Long employeeToDeleteId = employeeRepository.findByName(UNIQUE_EMPLOYEE_NAME_DELETE).get(0).getId();
        mockMvc.perform(delete(EMPLOYEE_API + "/" + employeeToDeleteId)
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(SUCCESS_EMPLOYEE_DELETED));
        Optional<Employee> dbEmployee = employeeRepository.findById(employeeToDeleteId);
        assertThat(dbEmployee).isEmpty();
    }

    @Test
    @DatabaseSetup(value = "/dataset/deleteEmployee.xml")
    void deleteEmployee_WithNoRootManager_ReturnConflictStatus() throws Exception {
        Employee managerToDelete = employeeRepository.findByName(EMPLOYEE_ROOT_MANAGER_NAME).get(0);
        mockMvc.perform(delete(EMPLOYEE_API + "/" + managerToDelete.getId()).
                        contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(ERROR_DELETING_EXECUTIVE_EMPLOYEE));
        Optional<Employee> dbManagerEmployee = employeeRepository.findById(managerToDelete.getId());
        assertThat(dbManagerEmployee).isPresent();
        //make sure no data got deleted or changed
        assertThat(dbManagerEmployee.get()).usingRecursiveComparison().ignoringFields("id").isEqualTo(managerToDelete);

    }

    @Test
    @DatabaseSetup(value = "/dataset/deleteEmployee.xml")
    void deleteEmployee_WithNonValidId_ReturnsBadRequestStatus() throws Exception {
        mockMvc.perform(delete(EMPLOYEE_API + "/" + NONVALID_ID).
                        contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ERROR_EMPLOYEE_NOT_EXIST));

    }

    @Test
    @DatabaseSetup(value = "/dataset/deleteEmployee.xml")
    public void deleteManagerEmployee_WithManagerHasSubordinates_ShouldMovesSubordinatesToUpperManager() throws Exception {
        Employee managerToDelete = employeeRepository.findByName(UNIQUE_MANAGER_NAME_DELETE).get(0);
        Employee upperManager = managerToDelete.getManager();
        List<Employee> subordinatesBeforeManagerDeletion = employeeRepository.findByManager(managerToDelete);
        List<Employee> upperManagerInitialSubordinates = employeeRepository.findByManager(upperManager);
        upperManagerInitialSubordinates.remove(managerToDelete);
        mockMvc.perform(delete(EMPLOYEE_API + "/" + managerToDelete.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(SUCCESS_EMPLOYEE_DELETED));
        List<Employee> upperManagerSubordinatesAfterDeletion = employeeRepository.findByManager(upperManager);
        List<Long> newSubordinatedIds = subordinatesBeforeManagerDeletion.stream().map(Employee::getId).toList();
        List<Long> oldSubordinatedIds = upperManagerInitialSubordinates.stream().map(Employee::getId).toList();
        List<Long> actualIds = upperManagerSubordinatesAfterDeletion.stream().map(Employee::getId).toList();
        //assert reassigning was successfully
        assertThat(actualIds).containsAll(newSubordinatedIds);
        //assert Initial subordinates still exist
        assertThat(actualIds).containsAll(oldSubordinatedIds);


    }

    private String appendUUidToString(String name) {
        return name + "-" + UUID.randomUUID();
    }
}




