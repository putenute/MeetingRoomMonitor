package com.rewe.digital.calendar;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponseException;
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
import com.google.api.services.gmail.GmailScopes;
import com.rewe.digital.calendar.api.DataTransferObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
public class CalendarReader {

    private final NotificationService notificationService;

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
            @Value("${cal.serviceAccountEmail}") final String serviceAccountEmail,
            final NotificationService notificationService) {

        this.p12file = p12file;
        this.serviceAccountEmail = serviceAccountEmail;
        this.notificationService = notificationService;
        knownRooms.put("Room - RED", "rewe-digital.com_2d34333934343339393831@resource.calendar.google.com");
        knownRooms.put("Room - YELLOW", "rewe-digital.com_2d34343833323535383331@resource.calendar.google.com");
        knownRooms.put("Room - VIENNA", "rewe-digital.com_3532363232323630313836@resource.calendar.google.com");
        knownRooms.put("Room - PETROL", "rewe-digital.com_2d38393135363131393037@resource.calendar.google.com");
        knownRooms.put("Room - GREEN", "rewe-digital.com_323733373636393237@resource.calendar.google.com");
        knownRooms.put("Room - ORANGE", "rewe-digital.com_35393432353237302d313938@resource.calendar.google.com");
        knownRooms.put("Room - VENUS", "rewe-digital.com_33313835373234392d3337@resource.calendar.google.com");
        knownRooms.put("Room - SKY RED", "rewe-digital.com_2d353631343032313834@resource.calendar.google.com");
       // knownRooms.put("MeetingMonitorTestFoo", "rewe-digital.com_ucbb14vo8bef2m06p1bgf4pc5s@group.calendar.google.com"); // ONLY FOR TESTING PURPOSE!




        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (final GeneralSecurityException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        createCalendars();
    }

    public void createCalendars() {
        try {

            final Credential credential =
                    authorize(serviceAccountEmail, p12file);
            client = new com.google.api.services.calendar.Calendar.Builder(
                    httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();

            refreshMeetingsForAllCalendars();
        } catch (final Exception e) {
            System.out.print(e.getLocalizedMessage());
        }
    }

    public void refreshMeetingsForAllCalendars(){
        for (final String room : knownRooms.keySet()) {
            RoomCalendar cal = null;
            if (!calendarList.containsKey(room)) {
                cal = new RoomCalendar(knownRooms.get(room),
                        room);
            } else {
                cal = calendarList.get(room);
            }

            try {
                pullMeetings(cal);
                calendarList.put(room, cal);
            } catch (final GoogleJsonResponseException jsonEx) {
                System.out.println("GoogleJsonResponseException during API-Fetch of room "+ room + " with Code="+ jsonEx.getStatusCode() + " message="+jsonEx.getStatusMessage());
            } catch (final TokenResponseException oauthEx) {
                System.out.println("Oauth2-TokenException during API-Fetch of room " + room + " with Code=" +
                        oauthEx.getStatusCode() + " message=" + oauthEx.getStatusMessage());
            } catch (final Exception e) {
                System.out.println("Error during API-Fetch of room "+ room);
                e.printStackTrace();
            }

        }
    }

    private static Credential authorize(final String serviceAccountEmail, final String p12File) throws Exception {
        final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        final GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .setServiceAccountId(serviceAccountEmail)
                .setServiceAccountScopes(Arrays.asList(CalendarScopes.CALENDAR, GmailScopes
                        .GMAIL_SEND, GmailScopes.GMAIL_COMPOSE))
                .setServiceAccountPrivateKeyFromP12File(new File(p12File))
                .setServiceAccountUser("meetingroommonitor@appspot.gserviceaccount.com")
                .build();
        return credential;
    }

    public static void updateStatus(final RoomCalendar calendar) {
        if (calendar.getStatus()) {
            System.out.println("Cal "+calendar.getRoomName() + ": Status=Frei");
            calendarStatus.put(calendar.getRoomName(), true);
        } else {
            System.out.println("Cal "+calendar.getRoomName() + ": Status=Belegt");
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
                                final Meeting meeting = new Meeting(event.getId(), "Privat", "Privater Termin",
                                        new Date(event.getStart().getDateTime().getValue()),
                                        new Date(event.getEnd().getDateTime().getValue()));
                                calendar.addMeeting(meeting);
                            } else if (!CollectionUtils.isEmpty(event.getAttendees())) {
                                    for (final EventAttendee attendee : event.getAttendees()) {
                                        if (attendee.getResource() != null) {
                                            if (attendee.getResource() == true &&
                                                    attendee.getDisplayName().equals(calendar.getRoomName()) &&
                                                    !attendee.getResponseStatus().equals("declined")) {
                                                final Meeting meeting = new Meeting(event.getId(), organizer, event
                                                        .getSummary(),
                                                        new Date(event.getStart().getDateTime().getValue()),
                                                        new Date(event.getEnd().getDateTime().getValue()));
                                                calendar.addMeeting(meeting);
                                            }
                                        }
                                    }


                            } else {
                                final Meeting meeting = new Meeting(event.getId(), "", event.getSummary(),
                                        new Date(event.getStart().getDateTime().getValue()),
                                        new Date(event.getEnd().getDateTime().getValue()));
                                calendar.addMeeting(meeting);
                            }
                        }
                }
            }
        }

    }

    /**
     * Is used to vote a room clean or dirty
     */
    public void roomvote(final String roomId, final boolean isClean) {
        final RoomCalendar roomCalendar = calendarList.get(roomId) == null ? new RoomCalendar("", "") :
                calendarList.get(roomId);

        final Format formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        final String dateString = formatter.format(new Date());

        if (isClean) {
            roomCalendar.getRoomVotedClean().add(new Date());
            String tweet = dateString + ": :) Die Sauberkeit des Raums '" + roomId.toUpperCase() + "' wurde als " +
                    "vorbildlich " +
                    "bezeichnet!";
            if (tweet.length() > 140) {
                tweet = ":) Die Sauberkeit eines Raums wurde als vorbildlich bezeichnet!";
            }
            notificationService.postTweet(
                    tweet);
        } else {
            roomCalendar.getRoomVotedDirty().add(new Date());
            String tweet = dateString + ": :/ Die Sauberkeit des Raums '" + roomId.toUpperCase() +
                    " wurde als mangelhaft bewertet!";
            if (tweet.length() > 140) {
                tweet = ":/ Die Sauberkeit eines Raums wurde als mangelhaft bezeichnet!";
            }
            notificationService.postTweet(tweet);
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
            //Maybe the meeting is finished already?
            boolean meetingFinished = false;
            for (final Meeting meeting : calendar.getManuallyFinishedMeetings()) {
                if (meeting.getId().equals(actualMeeting.getId())) {
                    meetingFinished = true;
                    break;
                }
            }
            if (!meetingFinished) {
                dataTransferObject.setCurrentEventEndTime(actualMeeting.getEndTimePretty());
                dataTransferObject.setCurrentEventName(actualMeeting.getTitle());
                dataTransferObject.setCurrentEventOrganizer(actualMeeting.getOrganizer());
                dataTransferObject.setCurrentEventStartTime(actualMeeting.getStartTimePretty());
            } else {
                dataTransferObject.setCurrentEventName("JETZT KEIN TERMIN");
            }
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

        }else{
            dataTransferObject.setNextEventName( "KEIN WEITERER TERMIN");
        }

        // Next free meetings
        //Find next free room
        String nextFreeRoom = "";
        dataTransferObject.setNextFreeRoomFreeUntil("");
        dataTransferObject.setNextFreeRoomName("");



        //Find free Meetingroom in known rooms
        final Iterator iter = calendarList.entrySet().iterator();
        while (iter.hasNext()) {
            final Map.Entry pair = (Map.Entry) iter.next();
            final String currentRoomName = (String) pair.getKey();
            final RoomCalendar currentCal = (RoomCalendar) pair.getValue();
            if (currentCal.getStatus() == true ) {
                if (!(currentRoomName.equals(roomName))){
                    nextFreeRoom = (String) pair.getKey();
                    // TODO: Find out which room is nearest to roomName!

                    // Fill the fields, dude!
                    dataTransferObject.setNextFreeRoomFreeUntil(currentCal.getNextMeetingStartTime());
                    dataTransferObject.setNextFreeRoomName(currentRoomName);
                    System.out.println("Found next Free room next of room "+roomName+": "+currentRoomName+" until "+currentCal.getNextMeetingStartTime());
                    break;
                }
            }

            // Find free rooms in internal data muff, in case someone has cancelled a meetin manually
            for (final Meeting meeting : currentCal.getManuallyFinishedMeetings()) {
               if(currentCal.getMeetingAt(new Date()).getId().equals(meeting.getId())){
                    System.out.println("Found next Free room next of room "+roomName+": "+currentRoomName+" until "+currentCal.getNextMeetingStartTime());
                    dataTransferObject.setNextFreeRoomName(currentRoomName);
                    // How long is it free, the room?
                    dataTransferObject.setNextFreeRoomFreeUntil(currentCal.getNextMeetingStartTime(meeting.getEndTime()));
                    break;
                }
            }


        }

        if(nextFreeRoom.equals("")) {


        }

        // Set nexFreeRoomVariables
        final RoomCalendar nextFreeRoomCalendar = calendarList.get(nextFreeRoom);


        return dataTransferObject;
    }

    public void resetVote(final String roomId) {
        final RoomCalendar roomCalendar = calendarList.get(roomId);
        if (roomCalendar == null) {
            return;
        }
        roomCalendar.getRoomVotedClean().clear();
        roomCalendar.getRoomVotedDirty().clear();
    }

    public void endcurrrentEvent(final String roomId) {
        final RoomCalendar roomCalendar = calendarList.get(roomId);
        if (roomCalendar == null) {
            return;
        }
        final Meeting currentMeeting = roomCalendar.getMeetingAt(new Date());
        if (currentMeeting != null) {
            currentMeeting.setEndTime(new Date());
            if (roomCalendar.getManuallyFinishedMeetings().size() > 50) {
                roomCalendar.getManuallyFinishedMeetings().clear();
            }
            roomCalendar.getManuallyFinishedMeetings().add(currentMeeting);
        }
    }
}
