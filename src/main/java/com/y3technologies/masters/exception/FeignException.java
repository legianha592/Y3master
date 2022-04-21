package com.y3technologies.masters.exception;

import java.util.ArrayList;

import org.springframework.http.HttpStatus;

/**
 * @author beekhon.ong
 */
public class FeignException extends RuntimeException {

    private HttpStatus status;
    private ArrayList<String> messages;
    private String timestamp;

    public FeignException() {
        super();
    }

    public FeignException(ArrayList<String> messages, HttpStatus status, String timestamp) {
        super();
        this.messages = messages;
        this.status = status;
        this.timestamp = timestamp;
    }

    public ArrayList<String> getMessages() {
        return messages;
    }

    public HttpStatus getStatus(){
        return status;
    }

    public String getTimestamp(){
        return timestamp;
    }
}