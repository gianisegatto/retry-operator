package com.example.controller;

import com.example.RetryOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RetryOperatorController {


    @Autowired
    private RetryOperator<String, String> retryOperator;

    @RequestMapping("/")
    public String index(@RequestParam(name = "message") String message) {

        String response = this.retryOperator.executeAndRetryIfFail(message1 -> doIt(message1), message1 -> doRecover(message1), message, "Some error happened");

        return response;
    }

    private String doIt(final String message) {
        if ("error".equalsIgnoreCase(message)) {
            throw new RuntimeException("Error");
        }
        System.out.println("Doing: " + message);
        return message;
    }

    private String doRecover(final String message) {
        System.out.println("Recovering: " + message);
        return "Recovered " + message;
    }
}
