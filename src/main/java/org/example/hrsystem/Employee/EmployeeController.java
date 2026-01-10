package org.example.hrsystem.Employee;

import jakarta.validation.Valid;
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
    public ResponseEntity<Employee> addEmployee(@RequestBody @Valid EmployeeRequestDTO employee) {
        Employee employee1 = employeeService.addEmployee(employee);
        return new ResponseEntity<>(employee1, HttpStatus.CREATED);
    }
@GetMapping("/{employeeId}")
    public ResponseEntity<Employee> getEmployeeInfo(
        @PathVariable Long employeeId
) {
    Employee employee= employeeService.getEmployeeInfo(employeeId);
    return new ResponseEntity<>(employee, HttpStatus.OK);
}

}
