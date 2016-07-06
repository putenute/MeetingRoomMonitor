package com.rewe.digita.calendar;

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
import org.apache.commons.cli.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CalendarReader {
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = "calendar-reader";
    private static HttpTransport httpTransport;
    private static com.google.api.services.calendar.Calendar client;
    private static HashMap<String, RoomCalendar> calendarList = new HashMap<>();
    private static HashMap<String, Boolean> calendarStatus = new HashMap<>();
    private static String actualCalendar = "";
    private static String nextFreeRoom = "";

    public CalendarReader(final String propertiesFile) {
        Properties prop = new Properties();
        try {
            prop.load(CalendarReader.class.getClassLoader().getResourceAsStream(propertiesFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            Credential credential = authorize(prop.getProperty("serviceAccountEmail"), prop.getProperty("p12file"));
            client = new com.google.api.services.calendar.Calendar.Builder(
                    httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
            {
                RoomCalendar cal = new RoomCalendar("rewe-digital.com_2d34333934343339393831@resource.calendar.google.com", "Room RED");
                calendarList.put("Room RED", cal);
                calendarStatus.put("Room RED", true);
            }
            {
                RoomCalendar cal = new RoomCalendar("rewe-digital.com_2d353631343032313834@resource.calendar.google.com", "SKY RED");
                calendarList.put("SKY RED", cal);
                calendarStatus.put("SKY RED", true);
            }

            {
                RoomCalendar cal = new RoomCalendar("rewe-digital.com_2d34343833323535383331@resource.calendar.google.com", "Room YELLOW");
                calendarList.put("Room YELLOW", cal);
                calendarStatus.put("Room YELLOW", true);
            }
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
                RoomCalendar cal = new RoomCalendar("rewe-digital.com_3532363232323630313836@resource.calendar.google.com", "Room VIENNA");
                calendarList.put("Room VIENNA", cal);
            }
            */



        } catch (IOException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RoomCalendar getCurrentCalendar() {
        return calendarList.get(actualCalendar);
    }

    public RoomCalendar getNextFreeRoomCalendar() {
        RoomCalendar cal = calendarList.get(nextFreeRoom);

        return calendarList.get(nextFreeRoom);
    }

    private static Credential authorize(final String serviceAccountEmail, final String p12File) throws Exception {
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport)
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

    public static void pullMeetings(final RoomCalendar calendar) {
        java.util.Calendar now = java.util.Calendar.getInstance();
        Date today = new Date();
        today.setHours(0);
        DateTime minTime = new DateTime(today);
        today.setHours(23);
        today.setMinutes(59);
        DateTime maxTime = new DateTime(today);
        calendar.clearMeetings();
        try {
            Events eventFeed = client.events().list(calendar.getRoomId()).setTimeMin(minTime).setTimeMax(maxTime).execute();
            for (Event event : eventFeed.getItems()) {
                if (event.getStart() != null && event.getEnd() != null) {
                    java.util.Calendar timeToCheck = java.util.Calendar.getInstance();
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
                                    Meeting meeting = new Meeting("Privat", "Privater Termin", new Date(event.getStart().getDateTime().getValue()), new Date(event.getEnd().getDateTime().getValue()));
                                    calendar.addMeeting(meeting);
                                } else {
                                    for (EventAttendee attendee : event.getAttendees()) {
                                        if (attendee.getResource() != null) {
                                            if (attendee.getResource() == true && attendee.getDisplayName().equals(calendar.getRoomName()) && !attendee.getResponseStatus().equals("declined")) {
                                                Meeting meeting = new Meeting(organizer, event.getSummary(), new Date(event.getStart().getDateTime().getValue()), new Date(event.getEnd().getDateTime().getValue()));
                                                calendar.addMeeting(meeting);
                                            }
                                        }
                                    }
                                }
                            }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getActualCalendar() {
        return actualCalendar;
    }

    public static void setActualCalendar(String actualCalendar) {
        CalendarReader.actualCalendar = actualCalendar;
    }


    public String transformHTML(String html, final RoomCalendar calendar, final RoomCalendar nextFreeRoomCalendar) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.YYYY HH:mm");
        Date now = new Date();
        String ROOM_STATE = "{room_state}";
        String ROOM_STATE_COLOR = "{room_state_color}";
        String ROOM_NAME = "{this_room_name}";
        String TIME_AND_DATE = "{time_and_date}";
        String NOW_START_TIME = "{now_start_time}";
        String NOW_END_TIME = "{now_end_time}";
        String NOW_SUMMARY = "{now_summary}";
        String NOW_ORGANISATOR = "{now_organisator}";
        String LATER_START_TIME = "{later_start_time}";
        String LATER_END_TIME = "{later_end_time}";
        String LATER_SUMMERY = "{later_summary}";
        String LATER_ORGANIZER = "{later_organisator}";
        String NEXT_FREE_ROOM_NAME = "{next_free_room_name}";
        String NEXT_FREE_ROOM_TIME = "{next_free_room_time}";

        // Header
        if (calendar.getStatus()) {
            html = html.replace(ROOM_STATE, "BELEGT");
            html = html.replace(ROOM_STATE_COLOR, "#FF4400");
        } else {
            html = html.replace(ROOM_STATE, "FREI");
            html = html.replace(ROOM_STATE_COLOR, "#44FF00");
        }
        html = html.replace(ROOM_NAME, calendar.getRoomName());
        html = html.replace(TIME_AND_DATE, dateFormatter.format(now));

        // Now
        Meeting actualMeeting = calendar.getMeetingAt(now);
        if (actualMeeting != null) {
            html = html.replace(NOW_START_TIME, actualMeeting.getStartTimePretty());
            html = html.replace(NOW_END_TIME, actualMeeting.getEndTimePretty());
            html = html.replace(NOW_SUMMARY, actualMeeting.getTitle());
            html = html.replace(NOW_ORGANISATOR, actualMeeting.getOrganizer());
        } else {
            html = html.replace(NOW_START_TIME, "-");
            html = html.replace(NOW_END_TIME, "-");
            html = html.replace(NOW_SUMMARY, "-");
            html = html.replace(NOW_ORGANISATOR, "-");
        }

        // Later
        if (calendar.getMeetingsAfter(now).size() > 0) {
            Meeting nextMeeting = calendar.getMeetingsAfter(now).get(0);
            if (nextMeeting != null) {
                html = html.replace(LATER_START_TIME, nextMeeting.getStartTimePretty());
                html = html.replace(LATER_END_TIME, nextMeeting.getEndTimePretty());
                html = html.replace(LATER_SUMMERY, nextMeeting.getTitle());
                html = html.replace(LATER_ORGANIZER, nextMeeting.getOrganizer());
            }
        } else {
            html = html.replace(LATER_START_TIME, "-");
            html = html.replace(LATER_END_TIME, "-");
            html = html.replace(LATER_SUMMERY, "-");
            html = html.replace(LATER_ORGANIZER, "-");
        }

        // next free meetings
        if (nextFreeRoomCalendar != null) {
            html = html.replace(NEXT_FREE_ROOM_NAME, nextFreeRoomCalendar.getRoomName());
            html = html.replace(NEXT_FREE_ROOM_TIME, nextFreeRoomCalendar.getNextMeetingStartTime());
        }
        return html;
    }


    public static void extractFromJar(final String resourceName, final String targetDirectory) {
        InputStream stream = null;
        OutputStream resStreamOut = null;
        try {
            stream = CalendarReader.class.getClassLoader().getResourceAsStream(resourceName);
            if (stream == null) {
                throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");
            }
            int readBytes;
            byte[] buffer = new byte[4096];
            resStreamOut = new FileOutputStream(new File(targetDirectory, new File(resourceName).getName()));
            while ((readBytes = stream.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
            resStreamOut.close();
            stream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("d", true, "webserver directory");
        options.addOption("c", true, "calendar name");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String calendarName = cmd.getOptionValue("c");
        String directory = cmd.getOptionValue("d");
        if (directory == null || calendarName == null) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("ant", options);
            System.exit(-1);
        } else {
            File webserverDirectory = new File(directory);
            if (!webserverDirectory.exists()) {
                System.err.println("webserver directory does not exist. ");
                System.exit(-1);
            }

        }
        extractFromJar("public/css/bootstrap.min.css", directory+"/css");
        extractFromJar("public/css/font-awesome.min.css", directory+"/css");
        extractFromJar("public/css/modern-business.css", directory+"/css");
        extractFromJar("public/css/custom_calendar_reader.css", directory+"/css");

        extractFromJar("calendar.p12", ".");
        CalendarReader reader = new CalendarReader("calendar.properties");
        reader.setActualCalendar(calendarName);

        System.out.println("Running...");
        while (true) {

            Iterator iter = calendarList.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry pair = (Map.Entry) iter.next();
                pullMeetings((RoomCalendar) pair.getValue());
                updateStatus((RoomCalendar) pair.getValue());
            }

            nextFreeRoom = "";
            iter = calendarStatus.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry pair = (Map.Entry) iter.next();
                if (!(boolean)pair.getValue()) {
                    if (!(((String)pair.getKey()).equals(actualCalendar))){
                        nextFreeRoom = (String) pair.getKey();
                        break;
                    }
                }
            }

            InputStream templateInputStream = CalendarReader.class.getClassLoader().getResourceAsStream("public/index.htm");
            BufferedReader r = new BufferedReader(new InputStreamReader(templateInputStream, StandardCharsets.UTF_8));

            String htmlText = "";
            String line = "";
            try {
                while ((line = r.readLine()) != null) {
                    htmlText += line;

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            htmlText = reader.transformHTML(htmlText, reader.getCurrentCalendar(), reader.getNextFreeRoomCalendar());

            FileWriter fWriter = null;
            BufferedWriter writer = null;
            try {
                fWriter = new FileWriter(new File(directory, "index.htm"));
                writer = new BufferedWriter(fWriter);
                writer.write(htmlText);

                writer.close();
            } catch (Exception e) {
                //catch any exceptions here
            }
            try {
                templateInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                long sleepTime = 10000; //ms
                System.out.println("Schlafe jetzt "+sleepTime/1000 +" Sekunden");
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
