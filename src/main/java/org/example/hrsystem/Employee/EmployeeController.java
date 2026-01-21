package org.example.hrsystem.Employee;

import jakarta.validation.Valid;
import org.example.hrsystem.Employee.dto.EmployeeRequestDTO;
import org.example.hrsystem.Employee.dto.EmployeeResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/hr/api/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;
    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> addEmployee(@RequestBody @Valid EmployeeRequestDTO employee) {
        EmployeeResponseDTO employee1 = employeeService.addEmployee(employee);
        return new ResponseEntity<>(employee1, HttpStatus.CREATED);
    }
@GetMapping("/{employeeId}")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeInfo(
        @PathVariable Long employeeId
) {
    EmployeeResponseDTO employee= employeeService.getEmployeeInfo(employeeId);
    return new ResponseEntity<>(employee, HttpStatus.OK);
}
    @PutMapping("/{employeeId}")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(
            @PathVariable Long employeeId,
            @RequestBody EmployeeRequestDTO employeeRequest) {
        System.out.println("bohoooo");

        EmployeeResponseDTO updatedEmployee = employeeService.updateEmployee(employeeId, employeeRequest);
        return new ResponseEntity<>(updatedEmployee, HttpStatus.OK);
    }
}
