package com.rewe.digital.calendar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(final String[] args) {
        //This line starts the webserver
        SpringApplication.run(Application.class, args);
    }

}