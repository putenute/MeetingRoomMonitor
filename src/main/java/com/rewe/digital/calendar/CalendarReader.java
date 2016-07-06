package com.rewe.digital.calendar;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.Events;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

@Component
public class CalendarReader {
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = "calendar-reader";
    private static HttpTransport httpTransport;
    private static com.google.api.services.calendar.Calendar client;
    private static final HashMap<String, RoomCalendar> calendarList = new HashMap<>();
    private static final HashMap<String, Boolean> calendarStatus = new HashMap<>();
    private static final String actualCalendar = "";
    private static final String nextFreeRoom = "";


    @Inject
    public CalendarReader(@Value("${cal.p12file}") final String p12file, @Value("${cal.serviceAccountEmail}")
    final String serviceAccountEmail) {
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            final Credential credential =
                    authorize(serviceAccountEmail, p12file);
            client = new com.google.api.services.calendar.Calendar.Builder(
                    httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();

            RoomCalendar cal =
                        new RoomCalendar("rewe-digital.com_2d34333934343339393831@resource.calendar.google.com",
                                "Room RED");
                calendarList.put("Room RED", cal);
                calendarStatus.put("Room RED", true);

            pullMeetings(cal);

            cal = new RoomCalendar("rewe-digital.com_2d353631343032313834@resource.calendar.google.com",
                                "SKY RED");
                calendarList.put("SKY RED", cal);
                calendarStatus.put("SKY RED", true);



            cal =                    new RoomCalendar("rewe-digital.com_2d34343833323535383331@resource.calendar.google.com",
                                "Room YELLOW");
                calendarList.put("Room YELLOW", cal);
                calendarStatus.put("Room YELLOW", true);



            /* TODO: Service Account hat keinen Zugriff auf Räume !
            {
                RoomCalendar cal = new RoomCalendar("rewe-digital.com_2d34353638383831343730@resource.calendar.google.com", "Room GRAY");
                calendarList.put("Room GRAY", cal);
            }

            {
                RoomCalendar cal = new RoomCalendar("rewe-digital.com_2d38393135363131393037@resource.calendar.google.com", "Room PETROL");
                calendarList.put("Room PETROL", cal);
            }

            {
                RoomCalendar cal = new RoomCalendar("rewe-digital.com_3235373634393033383931@resource.calendar.google.com", "Arena Green");
                calendarList.put("Arena Green", cal);
            }
            {
                RoomCalendar cal = new RoomCalendar("rewe-digital.com_323733373636393237@resource.calendar.google.com", "Room GREEN");
                calendarList.put("Room GREEN", cal);
            }
            {
                RoomCalendar cal = new RoomCalendar("rewe-digital.com_35393432353237302d313938@resource.calendar.google.com", "Room ORANGE");
                calendarList.put("Room ORANGE", cal);
            }
            {
                RoomCalendar cal = new RoomCalendar("rewe-digital.com_3532363232323630313836@resource.calendar.google
                .com", "Room VIENNA");
                calendarList.put("Room VIENNA", cal);
            }
            */

        } catch (final IOException e) {
            e.printStackTrace();
        } catch (final GeneralSecurityException e) {
            e.printStackTrace();
        } catch (final Exception e) {
            e.printStackTrace();
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

    public static void pullMeetings(final RoomCalendar calendar) {
        final java.util.Calendar now = java.util.Calendar.getInstance();
        final Date today = new Date();
        today.setHours(0);
        final DateTime minTime = new DateTime(today);
        today.setHours(23);
        today.setMinutes(59);
        final DateTime maxTime = new DateTime(today);
        calendar.clearMeetings();
        try {
            final Events eventFeed =
                    client.events().list(calendar.getRoomId()).setTimeMin(minTime).setTimeMax(maxTime).execute();
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
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
