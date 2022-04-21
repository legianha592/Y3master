package com.y3technologies.masters.exception;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class RestErrorMessage {
    private HttpStatus status;
    private ArrayList<String> messages = new ArrayList<>();

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Singapore")
    private LocalDateTime timestamp;

    private RestErrorMessage() {
        timestamp = LocalDateTime.now();
    }

    public RestErrorMessage(String message, HttpStatus status) {
        this();
        this.status = status;
        messages.add(message);
    }

    public RestErrorMessage(ArrayList<String> messages, HttpStatus status) {
        this();
        this.status = status;
        this.messages = messages;
    }

    public void addErrorMessage(String message) {
        messages.add(message);
    }
}
