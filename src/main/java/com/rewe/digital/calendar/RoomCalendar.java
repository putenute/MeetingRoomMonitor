package com.rewe.digital.calendar;

import java.util.*;

public class RoomCalendar {

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }


    public class MeetingComparator implements Comparator<Meeting> {
        @Override
        public int compare(Meeting o1, Meeting o2) {
            return o1.getStartTime().compareTo(o2.getStartTime());
        }
    }

    private ArrayList<Meeting> meetings = new ArrayList<>();
    private String roomName;
    private String roomId;

    public RoomCalendar(final String id, final String name) {
        this.roomId = Objects.requireNonNull(id);
        this.roomName = Objects.requireNonNull(name);
    }

    public void addMeeting(final Meeting meeting) {
        this.meetings.add(meeting);
        Collections.sort(meetings, new MeetingComparator());
    }

    public Meeting getMeetingAt(final Date time) {
        for(Meeting meeting : meetings) {
            if (meeting.getStartTime().before(time) && meeting.getEndTime().after(time))
               return meeting;
        }
        return null;
    }

    public void clearMeetings() {
        this.meetings = new ArrayList<>();
    }

    public String getNextMeetingStartTime() {
        String time = "Ganzer Tag";
        ArrayList<Meeting> nextMeetings = getMeetingsAfter(new Date());
        Collections.sort(meetings, new MeetingComparator());
        if (meetings.size() > 0) {
            time = meetings.get(0).getStartTimePretty();
        }
        return time;
    }

    public boolean getStatus() {
        Date now = new Date();
        return getMeetingAt(now) == null ? false : true;
    }

    public ArrayList<Meeting> getMeetings() {
        return meetings;
    }

    public ArrayList<Meeting> getMeetingsAfter(final Date time) {
        ArrayList<Meeting> nextMeetings = new ArrayList<>();
        for(Meeting meeting : meetings) {
            if (meeting.getStartTime().after(time))
                nextMeetings.add(meeting);
        }
        return nextMeetings;
    }
}
