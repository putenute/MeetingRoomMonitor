package com.rewe.digital.calendar.api;


public class DataTransferObject {

    private String roomName;

    private String currentEventName;
    private String currentEventOrganizer;
    private String currentEventStartTime;
    private String currentEventEndTime;

    private String nextEventName;
    private String nextEventOrganizer;
    private String nextEventStartTime;
    private String nextEventEndTime;

    private String nextFreeRoomName;
    private String nextFreeRoomFreeUntil;

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(final String roomName) {
        this.roomName = roomName;
    }

    public String getCurrentEventName() {
        return currentEventName;
    }

    public void setCurrentEventName(final String currentEventName) {
        this.currentEventName = currentEventName;
    }

    public String getCurrentEventOrganizer() {
        return currentEventOrganizer;
    }

    public void setCurrentEventOrganizer(final String currentEventOrganizer) {
        this.currentEventOrganizer = currentEventOrganizer;
    }

    public String getCurrentEventStartTime() {
        return currentEventStartTime;
    }

    public void setCurrentEventStartTime(final String currentEventStartTime) {
        this.currentEventStartTime = currentEventStartTime;
    }

    public String getCurrentEventEndTime() {
        return currentEventEndTime;
    }

    public void setCurrentEventEndTime(final String currentEventEndTime) {
        this.currentEventEndTime = currentEventEndTime;
    }

    public String getNextEventName() {
        return nextEventName;
    }

    public void setNextEventName(final String nextEventName) {
        this.nextEventName = nextEventName;
    }

    public String getNextEventOrganizer() {
        return nextEventOrganizer;
    }

    public void setNextEventOrganizer(final String nextEventOrganizer) {
        this.nextEventOrganizer = nextEventOrganizer;
    }

    public String getNextEventStartTime() {
        return nextEventStartTime;
    }

    public void setNextEventStartTime(final String nextEventStartTime) {
        this.nextEventStartTime = nextEventStartTime;
    }

    public String getNextEventEndTime() {
        return nextEventEndTime;
    }

    public void setNextEventEndTime(final String nextEventEndTime) {
        this.nextEventEndTime = nextEventEndTime;
    }

    public String getNextFreeRoomName() {
        return nextFreeRoomName;
    }

    public void setNextFreeRoomName(final String nextFreeRoomName) {
        this.nextFreeRoomName = nextFreeRoomName;
    }

    public String getNextFreeRoomFreeUntil() {
        return nextFreeRoomFreeUntil;
    }

    public void setNextFreeRoomFreeUntil(final String nextFreeRoomFreeUntil) {
        this.nextFreeRoomFreeUntil = nextFreeRoomFreeUntil;
    }
}
