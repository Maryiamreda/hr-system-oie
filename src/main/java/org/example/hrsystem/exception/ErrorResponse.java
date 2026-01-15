package org.example.hrsystem.exception;

import com.sun.jna.platform.unix.solaris.LibKstat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
//  class to show the error message consistently
public class ErrorResponse {
    private String message;
}
