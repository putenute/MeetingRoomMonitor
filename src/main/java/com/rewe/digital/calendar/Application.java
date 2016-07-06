package com.rewe.digital.calendar;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.Events;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
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

        //this.reader = new CalendarReader();

        /*final Options options = new Options();
        options.addOption("d", true, "webserver directory");
        options.addOption("c", true, "calendar name");
        final CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (final ParseException e) {
            e.printStackTrace();
        }
        final String calendarName = cmd.getOptionValue("c");
        final String directory = cmd.getOptionValue("d");
        if (directory == null || calendarName == null) {
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("ant", options);
            System.exit(-1);
        } else {
            final File webserverDirectory = new File(directory);
            if (!webserverDirectory.exists()) {
                System.err.println("webserver directory does not exist. ");
                System.exit(-1);
            }

        }

        extractFromJar("public/css/bootstrap.min.css", directory + "/css");
        extractFromJar("public/css/font-awesome.min.css", directory + "/css");
        extractFromJar("public/css/modern-business.css", directory + "/css");
        extractFromJar("public/css/custom_calendar_reader.css", directory + "/css");

        extractFromJar("calendar.p12", ".");
        final CalendarReader reader = new CalendarReader("calendar.properties");
        reader.setActualCalendar(calendarName);

        System.out.println("Running...");
        while (true) {

            Iterator iter = calendarList.entrySet().iterator();
            while (iter.hasNext()) {
                final Map.Entry pair = (Map.Entry) iter.next();
                pullMeetings((RoomCalendar) pair.getValue());
                updateStatus((RoomCalendar) pair.getValue());
            }

            nextFreeRoom = "";
            iter = calendarStatus.entrySet().iterator();
            while (iter.hasNext()) {
                final Map.Entry pair = (Map.Entry) iter.next();
                if (!(boolean) pair.getValue()) {
                    if (!(((String) pair.getKey()).equals(actualCalendar))) {
                        nextFreeRoom = (String) pair.getKey();
                        break;
                    }
                }
            }

            final InputStream templateInputStream =
                    CalendarReader.class.getClassLoader().getResourceAsStream("public/index.htm");
            final BufferedReader r =
                    new BufferedReader(new InputStreamReader(templateInputStream, StandardCharsets.UTF_8));

            String htmlText = "";
            String line = "";
            try {
                while ((line = r.readLine()) != null) {
                    htmlText += line;

                }
            } catch (final IOException e) {
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
            } catch (final Exception e) {
                //catch any exceptions here
            }
            try {
                templateInputStream.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }

            try {
                final long sleepTime = 10000; //ms
                System.out.println("Schlafe jetzt " + sleepTime / 1000 + " Sekunden");
                Thread.sleep(sleepTime);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }*/
    }

    public static void updateStatus(final RoomCalendar calendar) {
        if (calendar.getStatus()) {
            calendarStatus.put(calendar.getRoomName(), true);
        } else {
            calendarStatus.put(calendar.getRoomName(), false);
        }
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
                        if (now.get(java.util.Calendar.DAY_OF_YEAR) ==
                                timeToCheck.get(java.util.Calendar.DAY_OF_YEAR)) {
                            if (!event.getStatus().equals("cancelled")) {
                                String organizer = "-";
                                if (event.getOrganizer() != null) {
                                    if (event.getOrganizer().getDisplayName() == null) {
                                        organizer = event.getOrganizer().getEmail();
                                    } else {
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
                                            if (attendee.getResource() == true &&
                                                    attendee.getDisplayName().equals(calendar.getRoomName()) &&
                                                    !attendee.getResponseStatus().equals("declined")) {
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
        } catch (final IOException e) {
            e.printStackTrace();
        }
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
            final byte[] buffer = new byte[4096];
            resStreamOut = new FileOutputStream(new File(targetDirectory, new File(resourceName).getName()));
            while ((readBytes = stream.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
            resStreamOut.close();
            stream.close();
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

}