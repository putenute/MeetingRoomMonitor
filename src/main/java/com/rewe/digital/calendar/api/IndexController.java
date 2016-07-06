package com.rewe.digital.calendar.api;

import com.rewe.digital.calendar.CalendarReader;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

@RestController
public class IndexController {

    @Autowired
    CalendarReader calReader;



    @RequestMapping("/")
    public String index(final HttpServletResponse response) throws IOException {
        final ClassLoader classLoader = getClass().getClassLoader();
        final File file = new File(classLoader.getResource("public/index.htm").getFile());

        return FileUtils.readFileToString(file);
    }

    @RequestMapping("/events/sample")
    public DataTransferObject getSample() {
        final DataTransferObject dataTransferObject = new DataTransferObject();

        dataTransferObject.setCurrentEventEndTime("12:30");
        dataTransferObject.setCurrentEventName("Schnitzel braten");
        dataTransferObject.setCurrentEventOrganizer("Florian Wolk");
        dataTransferObject.setCurrentEventStartTime("10:45");
        dataTransferObject.setNextEventEndTime("14:00");
        dataTransferObject.setNextEventName("Bouletten formen");
        dataTransferObject.setNextEventOrganizer("Mickey Mouse");
        dataTransferObject.setNextEventStartTime("13:00");
        dataTransferObject.setNextFreeRoomFreeUntil("16:00");
        dataTransferObject.setNextFreeRoomName("Sky RED");
        dataTransferObject.setRoomName("Room -  RED");

        return dataTransferObject;


    }


    @RequestMapping("/events")
    public DataTransferObject getEvents() {

        return calReader.getMeetingRoomMonitorData();


    }


}