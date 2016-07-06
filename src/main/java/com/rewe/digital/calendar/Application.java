package com.rewe.digital.calendar;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;

@SpringBootApplication
public class Application {

    private static CalendarReader reader;

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = "calendar-reader";
    private static HttpTransport httpTransport;
    private static com.google.api.services.calendar.Calendar client;
    private static final HashMap<String, RoomCalendar> calendarList = new HashMap<>();
    private static final HashMap<String, Boolean> calendarStatus = new HashMap<>();
    private static final String actualCalendar = "";
    private static final String nextFreeRoom = "";

    public static void main(final String[] args) {
        //This line starts the webserver
        SpringApplication.run(Application.class, args);
    }
}