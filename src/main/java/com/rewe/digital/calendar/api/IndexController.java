package com.rewe.digital.calendar.api;

import com.rewe.digital.calendar.CalendarReader;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
public class IndexController {

    @Autowired
    CalendarReader calReader;



    @RequestMapping("/")
    public String index(final HttpServletResponse response) throws IOException {
        final List list = IOUtils.readLines(ClassLoader.getSystemResourceAsStream("public/index.htm"));

        return String.join("", list);
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

    @RequestMapping("/events/{roomId}")
    public DataTransferObject getEventsForRoom(@PathVariable("roomId") final String roomId) {
        calReader.refreshMeetingsForAllCalendars();
        return calReader.getMeetingRoomMonitorData(roomId);
    }

    @RequestMapping("/events/{roomId}/endevent")
    public void endEventsForRoom(@PathVariable("roomId") final String roomId) {
        calReader.refreshMeetingsForAllCalendars();
        calReader.endcurrrentEvent(roomId);
    }

    @RequestMapping("/vote/{roomId}/{action}")
    public void voteRoomClean(@PathVariable("roomId") final String roomId,
            @PathVariable("action") final String action) {
        if (action.equalsIgnoreCase("clean")) {
            calReader.roomvote(roomId, true);
        } else if (action.equalsIgnoreCase("dirty")) {
            calReader.roomvote(roomId, false);
        } else if (action.equalsIgnoreCase("reset")) {
            calReader.resetVote(roomId);
        }
    }
}