package org.example.hrsystem;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
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
    @Test
    void addNewEmployee_WithAccurateDate_StatusIsCreated() throws Exception {
        //create new employee object
        EmployeeRequestDTO employee=  generateEmployeeDTO();
        //send post request with the object
        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                //expect output is successful
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(employee.getName()))
                .andExpect(jsonPath("$.gender").value(employee.getName()))
                .andExpect(jsonPath("$.birthDate").value(employee.getDateOfBirth().toString()))
                .andExpect(jsonPath("$.gender").value(employee.getGender().toString()))
                .andExpect(jsonPath("$.graduationDate").value(employee.getGraduationDate().toString()))
                .andExpect(jsonPath("$.salary").value(employee.getNetSalary()));
    }
    private EmployeeRequestDTO generateEmployeeDTO() {
        EmployeeRequestDTO dto = new EmployeeRequestDTO();
        dto.setName("maryiam");
        dto.setGender(Gender.FEMALE);
        dto.setBirthDate(LocalDate.of(2001,8,15));
        dto.setGraduationDate(LocalDate.of(2025,8,15));
        dto.setTeam("1");
        dto.setDepatment("1");
        dto.setGrossSalary(500); //gross salary
        dto.setManager("1");
        return dto;
    }

}
