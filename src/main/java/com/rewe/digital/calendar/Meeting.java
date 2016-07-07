package com.rewe.digital.calendar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * Created by sebastianglahn on 22.12.15.
 */

public class Meeting {

    private String id;
    private String organizer;
    private String startTimePretty;
    private String endTimePretty;
    private String title;
    private Date startTime;
    private Date endTime;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm");

    public Meeting(final String id, final String organizer, final String title, final Date startTime, final Date
            endTime) {
        this.id = Objects.requireNonNull(id);
        this.organizer = Objects.requireNonNull(organizer);
        this.title = title;
        this.startTime = Objects.requireNonNull(startTime);
        this.endTime = Objects.requireNonNull(endTime);
        this.startTimePretty = dateFormatter.format(startTime);
        this.endTimePretty = dateFormatter.format(endTime);
    }

    public String getId() {
        return id;
    }

    public String getEndTimePretty() {
        return endTimePretty;
    }

    public void setEndTimePretty(final String endTimePretty) {
        this.endTimePretty = endTimePretty;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getStartTimePretty() {
        return startTimePretty;
    }

    public void setStartTimePretty(final String startTimePretty) {
        this.startTimePretty = startTimePretty;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(final String organizer) {
        this.organizer = organizer;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(final Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(final Date endTime) {
        this.endTime = endTime;
    }

    public void endMeeting() {

    }
}
