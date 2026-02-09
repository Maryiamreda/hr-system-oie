package org.example.hrsystem.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
//  class to show the error message consistently
public class ErrorResponse {
    private String message;
}
