package com.rewe.digital.calendar;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.Events;
import com.rewe.digital.calendar.api.DataTransferObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class CalendarReader {

    public static final Map<String, String> knownRooms = new HashMap<>();
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = "calendar-reader";
    private static HttpTransport httpTransport;
    private static com.google.api.services.calendar.Calendar client;
    private static final HashMap<String, RoomCalendar> calendarList = new HashMap<>();
    private static final HashMap<String, Boolean> calendarStatus = new HashMap<>();
    private static final String actualCalendar = "";
    private static final String nextFreeRoom = "";

    private final String p12file;
    private final String serviceAccountEmail;

    @Inject
    public CalendarReader(@Value("${cal.p12file}") final String p12file,
            @Value("${cal.serviceAccountEmail}") final String serviceAccountEmail) {

        this.p12file = p12file;
        this.serviceAccountEmail = serviceAccountEmail;
        knownRooms.put("Room - RED", "rewe-digital.com_2d34333934343339393831@resource.calendar.google.com");
        knownRooms.put("Room - YELLOW", "rewe-digital.com_2d34343833323535383331@resource.calendar.google.com");
        knownRooms.put("Room - VIENNA", "rewe-digital.com_3532363232323630313836@resource.calendar.google.com");
        knownRooms.put("Room - PETROL", "rewe-digital.com_2d38393135363131393037@resource.calendar.google.com");
        knownRooms.put("Room - GREEN", "rewe-digital.com_323733373636393237@resource.calendar.google.com");
        knownRooms.put("Room - ORANGE", "rewe-digital.com_35393432353237302d313938@resource.calendar.google.com");
        knownRooms.put("Room - VENUS", "rewe-digital.com_33313835373234392d3337@resource.calendar.google.com");

        createCalendars();
    }

    public void createCalendars() {
        try {

            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            final Credential credential =
                    authorize(serviceAccountEmail, p12file);
            client = new com.google.api.services.calendar.Calendar.Builder(
                    httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();


            refreshMeetingsForAllCalendars();


        } catch (final IOException e) {
            e.printStackTrace();
        } catch (final GeneralSecurityException e) {
            e.printStackTrace();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshMeetingsForAllCalendars(){
        for (final String room : knownRooms.keySet()) {
            final RoomCalendar cal =
                    new RoomCalendar(knownRooms.get(room),
                            room);

            try {

                pullMeetings(cal);
                calendarList.put(room, cal);
            } catch (GoogleJsonResponseException jsonEx) {
                System.out.println("GoogleJsonResponseException during API-Fetch of room "+ room + " with Code="+ jsonEx.getStatusCode() + " message="+jsonEx.getStatusMessage());
            }catch (Exception e) {
                System.out.println("Error during API-Fetch of room "+ room);
                //e.printStackTrace();
            }

        }
    }

    private static Credential authorize(final String serviceAccountEmail, final String p12File) throws Exception {
        final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        final GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .setServiceAccountId(serviceAccountEmail)
                .setServiceAccountScopes(Collections.singleton(CalendarScopes.CALENDAR))
                .setServiceAccountPrivateKeyFromP12File(new File(p12File))
                .build();
        return credential;
    }

    public static void updateStatus(final RoomCalendar calendar) {
        if (calendar.getStatus()) {
            calendarStatus.put(calendar.getRoomName(), true);
        }
        else {
            calendarStatus.put(calendar.getRoomName(), false);
        }
    }

    private static void pullMeetings(final RoomCalendar calendar) throws IOException {

        final java.util.Calendar now = java.util.Calendar.getInstance();
        final Date today = new Date();
        today.setHours(0);
        final DateTime minTime = new DateTime(System.currentTimeMillis() - (120 * 60 * 1000));
        today.setHours(23);
        today.setMinutes(59);
        final DateTime maxTime = new DateTime(System.currentTimeMillis() + (12 * 60 * 60 * 1000));
        calendar.clearMeetings();
         final Calendar.Events.List list = client.events().list(calendar.getRoomId());
        list.setTimeMin(minTime);
        list.setTimeMax(maxTime);
        list.setMaxResults(10);
        list.setSingleEvents(true);
        final Events eventFeed = list.execute();
        for (final Event event : eventFeed.getItems()) {
            if (event.getStart() != null && event.getEnd() != null) {
                final java.util.Calendar timeToCheck = java.util.Calendar.getInstance();
                timeToCheck.setTimeInMillis(event.getStart().getDateTime().getValue());
                if (now.get(java.util.Calendar.YEAR) == timeToCheck.get(java.util.Calendar.YEAR)) {
                    if (now.get(java.util.Calendar.DAY_OF_YEAR) == timeToCheck.get(java.util.Calendar.DAY_OF_YEAR))
                        if (!event.getStatus().equals("cancelled")) {
                            String organizer = "-";
                            if (event.getOrganizer() != null) {
                                if (event.getOrganizer().getDisplayName() == null) {
                                    organizer = event.getOrganizer().getEmail();
                                }
                                else {
                                    organizer = event.getOrganizer().getDisplayName();
                                }
                            }
                            //CHECK: If visibility = private, then there will be no attendees!
                            if (event.getVisibility() != null && event.getVisibility().equals("private")) {
                                final Meeting meeting = new Meeting("Privat", "Privater Termin",
                                        new Date(event.getStart().getDateTime().getValue()),
                                        new Date(event.getEnd().getDateTime().getValue()));
                                calendar.addMeeting(meeting);
                            } else {
                                for (final EventAttendee attendee : event.getAttendees()) {
                                    if (attendee.getResource() != null) {
                                        if (attendee.getResource() == true && attendee.getDisplayName().equals(calendar.getRoomName()) && !attendee.getResponseStatus().equals("declined")) {
                                            final Meeting meeting = new Meeting(organizer, event.getSummary(),
                                                    new Date(event.getStart().getDateTime().getValue()),
                                                    new Date(event.getEnd().getDateTime().getValue()));
                                            calendar.addMeeting(meeting);
                                        }
                                    }
                                }
                            }
                        }
                }
            }
        }

    }

    public DataTransferObject getMeetingRoomMonitorData(final String roomName) {
        final DataTransferObject dataTransferObject = new DataTransferObject();


        // Get all meetings for this room
        final RoomCalendar calendar = calendarList.get(roomName);
        if(calendar == null) {
            dataTransferObject.setRoomName("ROOM NOT KNOWN!");
            return dataTransferObject;
        }
        dataTransferObject.setRoomName(roomName);



        // Current Meeting
        final Date now = new Date();
        final Meeting actualMeeting = calendar.getMeetingAt(now);
        if (actualMeeting != null) {
            dataTransferObject.setCurrentEventEndTime(actualMeeting.getEndTimePretty());
            dataTransferObject.setCurrentEventName( actualMeeting.getTitle());
            dataTransferObject.setCurrentEventOrganizer(actualMeeting.getOrganizer());
            dataTransferObject.setCurrentEventStartTime(actualMeeting.getStartTimePretty());
        } else {
            dataTransferObject.setCurrentEventName( "JETZT KEIN TERMIN");
        }




        // Next meeting(s)
        if (calendar.getMeetingsAfter(now).size() > 0) {
            final Meeting nextMeeting = calendar.getMeetingsAfter(now).get(0);
            if (nextMeeting != null) {
                dataTransferObject.setNextEventEndTime(nextMeeting.getEndTimePretty());
                dataTransferObject.setNextEventName(nextMeeting.getTitle());
                dataTransferObject.setNextEventOrganizer(nextMeeting.getOrganizer());
                dataTransferObject.setNextEventStartTime(nextMeeting.getStartTimePretty());
            }else {
                dataTransferObject.setNextEventName( "KEIN WEITERER TERMIN");
            }

        }


        // Next free meetings
        // next free meetings
        //if (nextFreeRoomCalendar != null) {
            //html = html.replace(NEXT_FREE_ROOM_NAME, nextFreeRoomCalendar.getRoomName());
           // html = html.replace(NEXT_FREE_ROOM_TIME, nextFreeRoomCalendar.getNextMeetingStartTime());
        //}
        dataTransferObject.setNextFreeRoomFreeUntil("23:11");
        dataTransferObject.setNextFreeRoomName("Room DeineMudda");

        return dataTransferObject;
    }
}
