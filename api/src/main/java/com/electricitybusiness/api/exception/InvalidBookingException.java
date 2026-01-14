package com.electricitybusiness.api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidBookingException extends RuntimeException {
    private final String field;

    public InvalidBookingException(String field, String message) {
        super(message);
        this.field = field;
    }
}
