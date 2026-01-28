package org.example.hrsystem.utilities;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class SalaryCalculator {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.15");
    private static final BigDecimal FIXED_DEDUCTION = new BigDecimal("500.00");

    public BigDecimal calculateNetSalary(BigDecimal grossSalary) {
        //gross Salary - (Gross Salary * 0.15) - 500
        if (grossSalary == null) return BigDecimal.ZERO;

        BigDecimal taxAmount = grossSalary.multiply(TAX_RATE);
        BigDecimal totalDeductions = taxAmount.add(FIXED_DEDUCTION);

        return grossSalary.subtract(totalDeductions)
                .setScale(2, RoundingMode.HALF_UP);
    }
}


