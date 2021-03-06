package com.rewe.digital.calendar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class RoomCalendar {

    private List<Meeting> meetings = new ArrayList<>();
    private String roomName;
    private String roomId;
    private final List<Date> roomVotedDirty = new ArrayList<>();
    private final List<Date> roomVotedClean = new ArrayList<>();
    private final List<Meeting> manuallyFinishedMeetings = new ArrayList<>();

    public RoomCalendar(final String id, final String name) {
        this.roomId = Objects.requireNonNull(id);
        this.roomName = Objects.requireNonNull(name);
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(final String roomName) {
        this.roomName = roomName;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(final String roomId) {
        this.roomId = roomId;
    }

    public List<Date> getRoomVotedClean() {
        return roomVotedClean;
    }

    public List<Date> getRoomVotedDirty() {
        return roomVotedDirty;
    }

    public class MeetingComparator implements Comparator<Meeting> {
        @Override
        public int compare(final Meeting o1, final Meeting o2) {
            return o1.getStartTime().compareTo(o2.getStartTime());
        }
    }


    public void addMeeting(final Meeting meeting) {
        this.meetings.add(meeting);
        Collections.sort(meetings, new MeetingComparator());
    }

    public Meeting getMeetingAt(final Date time) {
        for (final Meeting meeting : meetings) {
            if (meeting.getStartTime().before(time) && meeting.getEndTime().after(time)){
                System.out.println(time.toString()+": There is currently a meeting "+meeting.getTitle()+" in room"+roomName);
                return meeting;
            }

        }
        return null;
    }

    public Meeting getLastFinishedMeetingBefore(final Date time) {
        final Optional<Meeting> first = meetings
                .stream()
                .sorted((e1, e2) -> e1.getEndTime()
                        .compareTo(e2.getEndTime())).findFirst();

        for (final Meeting meeting : meetings) {
            if (meeting.getStartTime().before(time) && meeting.getEndTime().after(time)) {
                return meeting;
            }
        }
        return null;
    }

    public void clearMeetings() {
        this.meetings = new ArrayList<>();
    }

    public String getNextMeetingStartTime() {
        return getNextMeetingStartTime(new Date());
    }

    public String getNextMeetingStartTime(Date time) {
        if (getMeetingsAfter(time).size() > 0) {
            final Meeting nextMeeting = getMeetingsAfter(time).get(0);
            if (nextMeeting != null) {
                return nextMeeting.getStartTimePretty();
            }
        }
        return "Ganzer Tag";
    }

    public boolean getStatus() {
        final Date now = new Date();
        Meeting currentMeeting =  getMeetingAt(now);
        if(currentMeeting == null) {
            return true;
        }
        return false;
    }

    public List<Meeting> getMeetings() {
        return meetings;
    }

    public ArrayList<Meeting> getMeetingsAfter(final Date time) {
        final ArrayList<Meeting> nextMeetings = new ArrayList<>();
        for (final Meeting meeting : meetings) {
            if (meeting.getStartTime().after(time))
                nextMeetings.add(meeting);
        }
        return nextMeetings;
    }

    public List<Meeting> getManuallyFinishedMeetings() {
        return manuallyFinishedMeetings;
    }
}
